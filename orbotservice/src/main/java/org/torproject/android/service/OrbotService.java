/* Copyright (c) 2009-2011, Nathan Freitas, Orbot / The Guardian Project - https://guardianproject.info/apps/orbot */
/* See LICENSE for licensing information */
/*
 * Code for iptables binary management taken from DroidWall GPLv3
 * Copyright (C) 2009-2010  Rodrigo Zechin Rosauro
 */

package org.torproject.android.service;
//280行

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jaredrummler.android.shell.CommandResult;

import net.freehaven.tor.control.TorControlCommands;
import net.freehaven.tor.control.TorControlConnection;

import org.apache.commons.io.FileUtils;
import org.torproject.android.service.nodedb.NodeHelper;
import org.torproject.android.service.util.CustomShell;
import org.torproject.android.service.util.CustomTorResourceInstaller;
import org.torproject.android.service.util.DummyActivity;
import org.torproject.android.service.util.Prefs;
import org.torproject.android.service.util.TorServiceUtils;
import org.torproject.android.service.util.Utils;
import org.torproject.android.service.vpn.OrbotVpnManager;
import org.torproject.android.service.vpn.VpnPrefs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

//import IPtProxy.IPtProxy;

public class OrbotService extends VpnService implements TorServiceConstants, OrbotConstants {

    public final static String BINARY_TOR_VERSION = org.torproject.android.binary.TorServiceConstants.BINARY_TOR_VERSION;
    static final int NOTIFY_ID = 1;
    private final static int CONTROL_SOCKET_TIMEOUT = 60000;
    private static final int ERROR_NOTIFY_ID = 3;
    private static final int HS_NOTIFY_ID = 4;
    private static final Uri V2_HS_CONTENT_URI = Uri.parse("content://org.torproject.android.service.ui.hiddenservices.providers/hs");
    private static final Uri V3_ONION_SERVICES_CONTENT_URI = Uri.parse("content://org.torproject.android.service.ui.v3onionservice/v3");
    private static final Uri COOKIE_CONTENT_URI = Uri.parse("content://org.torproject.android.service.ui.hiddenservices.providers.cookie/cookie");
    private static final Uri V3_CLIENT_AUTH_URI = Uri.parse("content://org.torproject.android.service.ui.v3onionservice.clientauth/v3auth");
    private final static String NOTIFICATION_CHANNEL_ID = "orbot_channel_1";
    private static final String[] LEGACY_V2_ONION_SERVICE_PROJECTION = new String[]{
            OnionService._ID,
            OnionService.NAME,
            OnionService.DOMAIN,
            OnionService.PORT,
            OnionService.AUTH_COOKIE,
            OnionService.AUTH_COOKIE_VALUE,
            OnionService.ONION_PORT,
            OnionService.ENABLED};
    private static final String[] V3_ONION_SERVICE_PROJECTION = new String[]{
            OnionService._ID,
            OnionService.NAME,
            OnionService.DOMAIN,
            OnionService.PORT,
            OnionService.ONION_PORT,
            OnionService.ENABLED,
    };
    private static final String[] LEGACY_COOKIE_PROJECTION = new String[]{
            ClientCookie._ID,
            ClientCookie.DOMAIN,
            ClientCookie.AUTH_COOKIE_VALUE,
            ClientCookie.ENABLED};
    private static final String[] V3_CLIENT_AUTH_PROJECTION = new String[]{
            V3ClientAuth._ID,
            V3ClientAuth.DOMAIN,
            V3ClientAuth.HASH,
            V3ClientAuth.ENABLED
    };
    public static int mPortSOCKS = -1;
    public static int mPortHTTP = -1;
    public static int mPortDns = TOR_DNS_PORT_DEFAULT;
    public static int mPortTrans = TOR_TRANSPROXY_PORT_DEFAULT;
    public static File appBinHome;
    public static File appCacheHome;
    public static File fileTor;
    public static File fileTorRc;
    private final ExecutorService mExecutor = Executors.newCachedThreadPool();
    boolean mIsLollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    TorEventHandler mEventHandler;
    OrbotVpnManager mVpnManager;
    Handler mHandler;
    //we should randomly sort alBridges so we don't have the same bridge order each time
    Random bridgeSelectRandom = new Random(System.nanoTime());
    ActionBroadcastReceiver mActionBroadcastReceiver;
    private String mCurrentStatus = STATUS_OFF;
    //TorControlConnection定义好的文件直接用就行
    private TorControlConnection conn = null;
    private int mLastProcessId = -1;
    private File fileControlPort, filePid;
    private NotificationManager mNotificationManager = null;
    private NotificationCompat.Builder mNotifyBuilder;
    private boolean mNotificationShowing = false;
    private File mHSBasePath;
    public static File mV3OnionBasePath;
    private File mV3AuthBasePath;
    private ArrayList<Bridge> alBridges = null;
    private static String v3Dirpath =  "/data/data/com.ucas.chat/files";

    //一次安装一台机器，端口不变onion就不变
    private static int localPort = 6677;


    public static String getOinonHostname() {
        Log.d("getOinonHostname", "begin work.....");
        String line = null;
        File fileOnionHostname = new File(v3Dirpath, "hidden_service_replace/hostname");
        File filePubliceKey = new File(v3Dirpath, "hidden_service_replace/hs_ed25519_public_key");
        File fileSercretKey = new File(v3Dirpath, "hidden_service_replace/hs_ed25519_secret_key");

        try {
            if (fileSercretKey.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileSercretKey));
                line = bufferedReader.readLine();
                Log.d("fileSercretKey", line);
                bufferedReader.close();
            }else{
                System.out.println("文件不存在");
            }
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (filePubliceKey.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(filePubliceKey));
                line = bufferedReader.readLine();
                Log.d("filePubliceKey", line);
                bufferedReader.close();
            }else{
                System.out.println("文件不存在");
            }
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (fileOnionHostname.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileOnionHostname));
                line = bufferedReader.readLine();
                Log.d("fileOnionHostname", line);
                bufferedReader.close();
            }else{
                System.out.println("文件不存在");
            }
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }


    /**
     * @param bridgeList bridges that were manually entered into Orbot settings
     * @return Array with each bridge as an element, no whitespace entries see issue #289...
     */
    private static String[] parseBridgesFromSettings(String bridgeList) {
        // this regex replaces lines that only contain whitespace with an empty String
        bridgeList = bridgeList.trim().replaceAll("(?m)^[ \t]*\r?\n", "");
        return bridgeList.split("\\n");
    }

//    private static boolean useIPtProxy() {
//        String bridgeList = Prefs.getBridgesList();
//        return bridgeList.contains("obfs3") || bridgeList.contains("obfs4") || bridgeList.contains("meek");
//    }

    public void debug(String msg) {
        Log.d(OrbotConstants.TAG, msg);

        if (Prefs.useDebugLogging()) {
            sendCallbackLogMessage(msg);
        }
    }

    public void logException(String msg, Exception e) {
        if (Prefs.useDebugLogging()) {
            Log.e(OrbotConstants.TAG, msg, e);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));

            sendCallbackLogMessage(msg + '\n' + new String(baos.toByteArray()));

        } else
            sendCallbackLogMessage(msg);

    }

    private void showConnectedToTorNetworkNotification() {
        showToolbarNotification(getString(R.string.status_activated), NOTIFY_ID, R.drawable.ic_stat_tor);
    }

    private boolean findExistingTorDaemon() {
        try {
            mLastProcessId = initControlConnection(1, true);

            if (mLastProcessId != -1 && conn != null) {
                sendCallbackLogMessage(getString(R.string.found_existing_tor_process));
                sendCallbackStatus(STATUS_ON);
                showConnectedToTorNetworkNotification();
                return true;
            }
        } catch (Exception e) {
            debug("Error finding existing tor daemon: " + e);
        }
        return false;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        logNotice("Low Memory Warning!");
    }

    private void clearNotifications() {
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();

        if (mEventHandler != null)
            mEventHandler.getNodes().clear();

        mNotificationShowing = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence name = getString(R.string.app_name); // The user-visible name of the channel.
        String description = getString(R.string.app_description); // The user-visible description of the channel.
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(false);
        mChannel.enableVibration(false);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    @SuppressLint({"NewApi", "RestrictedApi"})
    protected void showToolbarNotification(String notifyMsg, int notifyType, int icon) {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getPackageName());
        PendingIntent pendIntent = PendingIntent.getActivity(OrbotService.this, 0, intent, 0);

        if (mNotifyBuilder == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_stat_tor)
                    .setContentIntent(pendIntent)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setOngoing(Prefs.persistNotifications());
        }

        mNotifyBuilder.mActions.clear(); // clear out NEWNYM action
        if (conn != null) { // only add new identity action when there is a connection
            Intent intentRefresh = new Intent(CMD_NEWNYM);
            PendingIntent pendingIntentNewNym = PendingIntent.getBroadcast(this, 0, intentRefresh, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotifyBuilder.addAction(R.drawable.ic_refresh_white_24dp, getString(R.string.menu_new_identity), pendingIntentNewNym);
        }

        mNotifyBuilder.setContentText(notifyMsg)
                .setSmallIcon(icon)
                .setTicker(notifyType != NOTIFY_ID ? notifyMsg : null);

        if (!Prefs.persistNotifications())
            mNotifyBuilder.setPriority(Notification.PRIORITY_LOW);

        Notification notification = mNotifyBuilder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFY_ID, notification);
        } else if (Prefs.persistNotifications() && (!mNotificationShowing)) {
            startForeground(NOTIFY_ID, notification);
            logNotice("Set background service to FOREGROUND");
        } else {
            mNotificationManager.notify(NOTIFY_ID, notification);
        }

        mNotificationShowing = true;
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        showToolbarNotification("", NOTIFY_ID, R.drawable.ic_stat_tor);
        System.out.println("onStartCommand:"+intent);
        if (intent != null)
            exec(new IncomingIntentRouter(intent));
        else
            Log.d(OrbotConstants.TAG, "Got null onStartCommand() intent");

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(OrbotConstants.TAG, "task removed");
        Intent intent = new Intent(this, DummyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        try {
            //   unregisterReceiver(mNetworkStateReceiver);
            unregisterReceiver(mActionBroadcastReceiver);
        } catch (IllegalArgumentException iae) {
            //not registered yet
        }

        stopTorAsync();

        super.onDestroy();
    }

    private void stopTorAsync() {

        new Thread(() -> {
            Log.i("OrbotService", "stopTorAsync");
            try {
                sendCallbackStatus(STATUS_STOPPING);
                sendCallbackLogMessage(getString(R.string.status_shutting_down));

//                if (useIPtObfsMeekProxy())
//                    IPtProxy.stopObfs4Proxy();
//
//                if (useIPtSnowflakeProxy())
//                    IPtProxy.stopSnowflake();


                stopTorDaemon(true);

                //stop the foreground priority and make sure to remove the persistant notification
                stopForeground(true);

                sendCallbackLogMessage(getString(R.string.status_disabled));
            } catch (Exception e) {
                logNotice("An error occured stopping Tor: " + e.getMessage());
                sendCallbackLogMessage(getString(R.string.something_bad_happened));
            }
            clearNotifications();
            sendCallbackStatus(STATUS_OFF);
        }).start();
    }

    private static boolean useIPtObfsMeekProxy() {
        String bridgeList = Prefs.getBridgesList();
        return bridgeList.contains("obfs") || bridgeList.contains("meek");
    }

    private static boolean useIPtSnowflakeProxy() {
        String bridgeList = Prefs.getBridgesList();
        return bridgeList.contains("snowflake");
    }

//    private void startSnowflakeProxy() {
//        //this is using the current, default Tor snowflake infrastructure
//        IPtProxy.startSnowflake("stun:stun.l.google.com:19302", "https://snowflake-broker.azureedge.net/",
//                "ajax.aspnetcdn.com", null, true, false, true, 3);
//    }

    /**
     * if someone stops during startup, we may have to wait for the conn port to be setup, so we can properly shutdown tor
     */
    private void stopTorDaemon(boolean waitForConnection) throws Exception {

        int tryCount = 0;

        while (tryCount++ < 3) {
            if (conn != null) {
                logNotice("Using control port to shutdown Tor");

                try {
                    logNotice("sending HALT signal to Tor process");
                    conn.shutdownTor("SHUTDOWN");

                } catch (IOException e) {
                    Log.d(OrbotConstants.TAG, "error shutting down Tor via connection", e);
                }

                conn = null;
                break;
            }

            if (!waitForConnection)
                break;

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }
        }
    }

    private void requestTorRereadConfig() {
        try {
            if (conn != null)
                conn.signal("HUP");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void logNotice(String msg) {
        if (msg != null && msg.trim().length() > 0) {
            if (Prefs.useDebugLogging())
                Log.d(OrbotConstants.TAG, msg);

            sendCallbackLogMessage(msg);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG , " onCreate:: onCreate begin work....." );
        try {
            mHandler = new Handler();
            appBinHome = new File(v3Dirpath);;
            Log.d(TAG , " onCreate:: getFilesDir: " + getFilesDir());
            Log.d(TAG , " onCreate:: BinHome: " + appBinHome.getPath());//getDir(TorServiceConstants.DIRECTORY_TOR_BINARY, Application.MODE_PRIVATE);
            if (!appBinHome.exists())
                appBinHome.mkdirs();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appCacheHome = new File(getDataDir(), DIRECTORY_TOR_DATA);
            } else {
                appCacheHome = getDir(DIRECTORY_TOR_DATA, Application.MODE_PRIVATE);
            }

            if (!appCacheHome.exists())
                appCacheHome.mkdirs();

            Log.d(TAG, " onCreate:: listing files appCacheHome.getAbsolutePath(): " + appCacheHome.getAbsolutePath());
            Iterator<File> files = FileUtils.iterateFiles(appCacheHome, null, true);
            while (files.hasNext()) {
                File fileNext = files.next();
                Log.d(TAG, " onCreate:: "
                        + " AbsolutePath = " + fileNext.getAbsolutePath()
                        + " length = " + fileNext.length()
                        + " rw = " + fileNext.canRead() + "/" + fileNext.canWrite()
                        + " lastMod = " + new Date(fileNext.lastModified()).toLocaleString());
//                debug(fileNext.getAbsolutePath()
//                        + " length=" + fileNext.length()
//                        + " rw=" + fileNext.canRead() + "/" + fileNext.canWrite()
//                        + " lastMod=" + new Date(fileNext.lastModified()).toLocaleString()
//                );
            }

            fileTorRc = new File(appBinHome, TORRC_ASSET_KEY);
            Log.d(TAG,  " onCreate:: fileTorRc = " +  String.valueOf(fileTorRc));
            fileControlPort = new File(getFilesDir(), TOR_CONTROL_PORT_FILE);
            //Log.d("oncreat.fileControlPort", String.valueOf(fileControlPort));
            Log.d(TAG,  " onCreate:: fileControlPort = " +  String.valueOf(fileControlPort));
            filePid = new File(getFilesDir(), TOR_PID_FILE);
           // Log.d("oncreat.filePid", String.valueOf(filePid));
            Log.d(TAG,  " onCreate:: filePid = " +  String.valueOf(filePid));

            Log.d(TAG, " onCreate:: AbsolutePath: " + getFilesDir().getAbsolutePath());
            mHSBasePath = new File(getFilesDir().getAbsolutePath(), TorServiceConstants.HIDDEN_SERVICES_DIR);
            if (!mHSBasePath.isDirectory())
                mHSBasePath.mkdirs();

//            创建文件夹
            mV3OnionBasePath = new File(getFilesDir().getAbsolutePath(), TorServiceConstants.ONION_SERVICES_DIR);
            if (!mV3OnionBasePath.isDirectory())
                mV3OnionBasePath.mkdirs();

            mV3AuthBasePath = new File(getFilesDir().getAbsolutePath(), TorServiceConstants.V3_CLIENT_AUTH_DIR);
            if (!mV3AuthBasePath.isDirectory())
                mV3AuthBasePath.mkdirs();

            mEventHandler = new TorEventHandler(this);

            if (mNotificationManager == null) {
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }

            //    IntentFilter mNetworkStateFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            //  registerReceiver(mNetworkStateReceiver , mNetworkStateFilter);

            IntentFilter filter = new IntentFilter();
            filter.addAction(CMD_NEWNYM);
            filter.addAction(CMD_ACTIVE);
            mActionBroadcastReceiver = new ActionBroadcastReceiver();
            registerReceiver(mActionBroadcastReceiver, filter);


            if (Build.VERSION.SDK_INT >= 26)
                createNotificationChannel();

            torUpgradeAndConfig();

//            pluggableTransportInstall();

            new Thread(() -> {
                try {
                    findExistingTorDaemon();
                } catch (Exception e) {
                    Log.e(TAG, " onCreate:: error onBind", e);
                    logNotice(" onCreate:: error finding exiting process: " + e.toString());
                }

            }).start();

            try {
                mVpnManager = new OrbotVpnManager(this);
                Log.i(TAG, "onCreate:: mVpnManager begin work.....");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            //what error here
            Log.e(OrbotConstants.TAG, " onCreate:: Error installing Orbot binaries", e);
            logNotice("There was an error installing Orbot binaries");
        }

        Log.i(TAG, " onCreate:: end");
    }

    protected String getCurrentStatus() {
        return mCurrentStatus;
    }

//    private boolean pluggableTransportInstall() {
//
//        File fileCacheDir = new File(getCacheDir(), "pt");
//        if (!fileCacheDir.exists())
//            fileCacheDir.mkdir();
//        IPtProxy.setStateLocation(fileCacheDir.getAbsolutePath());
//        String fileTestState = IPtProxy.getStateLocation();
//        debug("IPtProxy state: " + fileTestState);
//
//        return false;
//    }

    private boolean torUpgradeAndConfig() throws IOException, TimeoutException {

        SharedPreferences prefs = Prefs.getSharedPrefs(getApplicationContext());
        String version = prefs.getString(PREF_BINARY_TOR_VERSION_INSTALLED, null);

        logNotice("checking binary version: " + version);

        CustomTorResourceInstaller installer = new CustomTorResourceInstaller(this, appBinHome);
        logNotice("upgrading binaries to latest version: " + BINARY_TOR_VERSION);

        fileTor = installer.installResources();
        if (fileTor != null && fileTor.canExecute()) {
            prefs.edit().putString(PREF_BINARY_TOR_VERSION_INSTALLED, BINARY_TOR_VERSION).apply();

            fileTorRc = new File(appBinHome, "torrc");//installer.getTorrcFile();
            return fileTorRc.exists();
        }

        return false;
    }

    private File updateTorrcCustomFile() throws IOException, TimeoutException {
        Log.d("updateTorrcCustomFile", "begin work......");

        SharedPreferences prefs = Prefs.getSharedPrefs(getApplicationContext());

        StringBuffer extraLines = new StringBuffer();

        extraLines.append("\n");
        extraLines.append("ControlPortWriteToFile").append(' ').append(fileControlPort.getCanonicalPath()).append('\n');

        extraLines.append("PidFile").append(' ').append(filePid.getCanonicalPath()).append('\n');

        extraLines.append("RunAsDaemon 0").append('\n');
        extraLines.append("SafeLogging 0").append('\n');
        extraLines.append("AvoidDiskWrites 0").append('\n');

        String socksPortPref = prefs.getString(OrbotConstants.PREF_SOCKS, (TorServiceConstants.SOCKS_PROXY_PORT_DEFAULT));

        if (socksPortPref.indexOf(':') != -1)
            socksPortPref = socksPortPref.split(":")[1];

        socksPortPref = checkPortOrAuto(socksPortPref);

        String httpPortPref = prefs.getString(OrbotConstants.PREF_HTTP, (TorServiceConstants.HTTP_PROXY_PORT_DEFAULT));

        if (httpPortPref.indexOf(':') != -1)
            httpPortPref = httpPortPref.split(":")[1];

        httpPortPref = checkPortOrAuto(httpPortPref);

        Log.d("updateTorrcCustomFile2", "begin work......");
        String isolate = "";
        if (prefs.getBoolean(OrbotConstants.PREF_ISOLATE_DEST, false)) {
            isolate += " IsolateDestAddr ";
        }

        Log.d("updateTorrcCustomFile3", "begin work......");
        String ipv6Pref = "";

        if (prefs.getBoolean(OrbotConstants.PREF_PREFER_IPV6, true)) {
            ipv6Pref += " IPv6Traffic PreferIPv6 ";
        }
        Log.d("updateTorrcCustomFile4", "begin work......");

        if (prefs.getBoolean(OrbotConstants.PREF_DISABLE_IPV4, false)) {
            ipv6Pref += " IPv6Traffic NoIPv4Traffic ";
        }
        Log.d("updateTorrcCustomFile5", "begin work......");

        extraLines.append("SOCKSPort ").append(socksPortPref).append(isolate).append(ipv6Pref).append('\n');
        extraLines.append("SafeSocks 0").append('\n');
        extraLines.append("TestSocks 0").append('\n');

        Log.d("updateTorrcCustomFile51", "begin work......");

        if (false){
            extraLines.append("SocksListenAddress 0.0.0.0").append('\n');
            Log.d("updateTorrcCustomFile52", "begin work......");
        }

        extraLines.append("HTTPTunnelPort ").append(httpPortPref).append('\n');
        Log.d("updateTorrcCustomFile53", "begin work......");

        if (false) {
            extraLines.append("ConnectionPadding 1").append('\n');
        }
        Log.d("updateTorrcCustomFile6", "begin work......");

        if (prefs.getBoolean(OrbotConstants.PREF_REDUCED_CONNECTION_PADDING, true)) {
            extraLines.append("ReducedConnectionPadding 1").append('\n');
        }
        Log.d("updateTorrcCustomFile7", "begin work......");

        if (prefs.getBoolean(OrbotConstants.PREF_CIRCUIT_PADDING, true)) {
            extraLines.append("CircuitPadding 1").append('\n');
        } else {
            extraLines.append("CircuitPadding 0").append('\n');
        }
        Log.d("updateTorrcCustomFile8", "begin work......");

        if (prefs.getBoolean(OrbotConstants.PREF_REDUCED_CIRCUIT_PADDING, true)) {
            extraLines.append("ReducedCircuitPadding 1").append('\n');
        }
        Log.d("updateTorrcCustomFile9", "begin work......");

        String transPort = prefs.getString("pref_transport", TorServiceConstants.TOR_TRANSPROXY_PORT_DEFAULT + "");
        String dnsPort = prefs.getString("pref_dnsport", TorServiceConstants.TOR_DNS_PORT_DEFAULT + "");

        extraLines.append("TransPort ").append(checkPortOrAuto(transPort)).append('\n');
        extraLines.append("DNSPort ").append(checkPortOrAuto(dnsPort)).append('\n');

        extraLines.append("VirtualAddrNetwork 10.192.0.0/10").append('\n');
        extraLines.append("AutomapHostsOnResolve 1").append('\n');

        extraLines.append("DormantClientTimeout 10 minutes").append('\n');
        // extraLines.append("DormantOnFirstStartup 0").append('\n');
        extraLines.append("DormantCanceledByStartup 1").append('\n');

        extraLines.append("DisableNetwork 0").append('\n');

        if (Prefs.useDebugLogging()) {
            extraLines.append("Log debug syslog").append('\n');
            extraLines.append("SafeLogging 0").append('\n');
        }
        extraLines.append("Log debug file /data/user/0/com.ucas.chat/tordata/info.log").append('\n');
        Log.d("updateTorrcCustomFile10", "begin work......");

        extraLines = processSettingsImpl(extraLines);
        Log.d("updateTorrcCustomFile11", "begin work......");

        if (extraLines == null)
            return null;
        Log.d("updateTorrcCustomFile12", "begin work......");

        extraLines.append('\n');
        extraLines.append(prefs.getString("pref_custom_torrc", "")).append('\n');
        Log.d("updateTorrcCustomFile13", "begin work......");

        logNotice("updating torrc custom configuration...");

        debug("torrc.custom=" + extraLines.toString());

        File fileTorRcCustom = new File(fileTorRc.getAbsolutePath() + ".custom");
        boolean success = updateTorConfigCustom(fileTorRcCustom, extraLines.toString());

        if (success && fileTorRcCustom.exists()) {
            logNotice("success.");
            return fileTorRcCustom;
        } else
            return null;

    }

    private String checkPortOrAuto(String portString) {
        if (!portString.equalsIgnoreCase("auto")) {
            boolean isPortUsed = true;
            int port = Integer.parseInt(portString);

            while (isPortUsed) {
                isPortUsed = TorServiceUtils.isPortOpen("127.0.0.1", port, 500);

                if (isPortUsed) //the specified port is not available, so let Tor find one instead
                    port++;
            }
            return port + "";
        }

        return portString;
    }

    public boolean updateTorConfigCustom(File fileTorRcCustom, String extraLines) throws IOException, TimeoutException {
        FileWriter fos = new FileWriter(fileTorRcCustom, false);
        PrintWriter ps = new PrintWriter(fos);
        ps.print(extraLines);
        ps.flush();
        ps.close();
        return true;
    }

    /**
     * Send Orbot's status in reply to an
     * {@link TorServiceConstants#ACTION_START} {@link Intent}, targeted only to
     * the app that sent the initial request. If the user has disabled auto-
     * starts, the reply {@code ACTION_START Intent} will include the extra
     * {@link TorServiceConstants#STATUS_STARTS_DISABLED}
     */
    private void replyWithStatus(Intent startRequest) {
        String packageName = startRequest.getStringExtra(EXTRA_PACKAGE_NAME);

        Intent reply = new Intent(ACTION_STATUS);
        reply.putExtra(EXTRA_STATUS, mCurrentStatus);
        reply.putExtra(EXTRA_SOCKS_PROXY, "socks://127.0.0.1:" + mPortSOCKS);
        reply.putExtra(EXTRA_SOCKS_PROXY_HOST, "127.0.0.1");
        reply.putExtra(EXTRA_SOCKS_PROXY_PORT, mPortSOCKS);
        reply.putExtra(EXTRA_HTTP_PROXY, "http://127.0.0.1:" + mPortHTTP);
        reply.putExtra(EXTRA_HTTP_PROXY_HOST, "127.0.0.1");
        reply.putExtra(EXTRA_HTTP_PROXY_PORT, mPortHTTP);

        if (packageName != null) {
            reply.setPackage(packageName);
            sendBroadcast(reply);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(reply);

        if (mPortSOCKS != -1 && mPortHTTP != -1)
            sendCallbackPorts(mPortSOCKS, mPortHTTP, mPortDns, mPortTrans);

    }

    /**
     * The entire process for starting tor and related services is run from this method.
     */
    private void startTor() {
        try {
            Log.d("startTor", "begin work......");

            // STATUS_STARTING is set in onCreate()
            //如果现在的状态是停止
            if (mCurrentStatus == STATUS_STOPPING) {
                // these states should probably be handled better
                sendCallbackLogMessage("Ignoring start request, currently " + mCurrentStatus);
                Log.d("starttor1", "begin work......");

                return;
            } else if (mCurrentStatus == STATUS_ON && (mLastProcessId != -1)) {
                showConnectedToTorNetworkNotification();
                sendCallbackLogMessage("Ignoring start request, already started.");
                Log.d("starttor2", "begin work......");

                // setTorNetworkEnabled (true);

                return;
            }

            sendCallbackStatus(STATUS_STARTING);
            Log.d("starttor3", "begin work......");

            try {
                if (conn != null) {
                    String torProcId = conn.getInfo("process/pid");
                    Log.d("starttor4", "begin work......");

                    if (!TextUtils.isEmpty(torProcId))
                        mLastProcessId = Integer.parseInt(torProcId);
                    Log.d("starttor5", "begin work......");

                } else {
                    if (fileControlPort != null && fileControlPort.exists())
                        findExistingTorDaemon();

                }
            } catch (Exception e) {
            }

            // make sure there are no stray daemons running
            //确保没有游离的守护进程在运行
            stopTorDaemon(false);
            Log.d("starttor6", "begin work......");

            SharedPreferences prefs = Prefs.getSharedPrefs(getApplicationContext());
            String version = prefs.getString(PREF_BINARY_TOR_VERSION_INSTALLED, null);
            logNotice("checking binary version: " + version);

            showToolbarNotification(getString(R.string.status_starting_up), NOTIFY_ID, R.drawable.ic_stat_tor);
            //sendCallbackLogMessage(getString(R.string.status_starting_up));
            //logNotice(getString(R.string.status_starting_up));

            ArrayList<String> customEnv = new ArrayList<>();
            Log.d("starttor7", "begin work......");
//            if (Prefs.bridgesEnabled())
//                if (Prefs.useVpn() && !mIsLollipop) {
//                    Log.d("starttor8", "begin work......");
//                    customEnv.add("TOR_PT_PROXY=socks5://" + OrbotVpnManager.sSocksProxyLocalhost + ":" + OrbotVpnManager.sSocksProxyServerPort);
//                }

            Log.d("starttor9", "begin work......");

            boolean success = runTorShellCmd();

            if (success) {
                try {
                    updateLegacyV2OnionNames();
                    Log.d("starttor9", "begin work......");

                } catch (SecurityException se) {
                    logNotice("unable to upload legacy v2 onion names");
                }
                try {
                    updateV3OnionNames();
                    Log.d("starttor10", "begin work......");
                } catch (SecurityException se) {
                    logNotice("unable to upload v3 onion names");
                }
            }

        } catch (Exception e) {
            logException("Unable to start Tor: " + e.toString(), e);
            stopTorAsync();
            showToolbarNotification(
                    getString(R.string.unable_to_start_tor) + ": " + e.getMessage(),
                    ERROR_NOTIFY_ID, R.drawable.ic_stat_notifyerr);
        }
    }


    private void updateV3OnionNames() throws SecurityException {
        Log.d("updateV3OnionNames", "begin work......");
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Cursor onionServices = contentResolver.query(V3_ONION_SERVICES_CONTENT_URI, null, null, null, null);
        if (onionServices != null) {
            try {
                while (onionServices.moveToNext()) {
                    String domain = onionServices.getString(onionServices.getColumnIndex(OnionService.DOMAIN));
                    Log.d("updateV3OnionNames", domain);

                    int localPort = onionServices.getInt(onionServices.getColumnIndex(OnionService.PORT));

                    if (domain == null || TextUtils.isEmpty(domain)) {
                        String v3OnionDirPath = new File(mV3OnionBasePath.getAbsolutePath(), "v3" + localPort).getCanonicalPath();
                        File hostname = new File(v3OnionDirPath, "hostname");
                        if (hostname.exists()) {
                            domain = Utils.readString(new FileInputStream(hostname)).trim();
                            ContentValues fields = new ContentValues();
                            fields.put(OnionService.DOMAIN, domain);
                            contentResolver.update(V3_ONION_SERVICES_CONTENT_URI, fields, "port=" + localPort, null);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            onionServices.close();
        }
    }

    private void updateLegacyV2OnionNames() throws SecurityException {
        // Tor is running, update new .onion names at db
        ContentResolver mCR = getApplicationContext().getContentResolver();
        Cursor hidden_services = mCR.query(V2_HS_CONTENT_URI, LEGACY_V2_ONION_SERVICE_PROJECTION, null, null, null);
        if (hidden_services != null) {
            try {
                while (hidden_services.moveToNext()) {
                    String HSDomain = hidden_services.getString(hidden_services.getColumnIndex(OnionService.DOMAIN));
                    Integer HSLocalPort = hidden_services.getInt(hidden_services.getColumnIndex(OnionService.PORT));
                    Integer HSAuthCookie = hidden_services.getInt(hidden_services.getColumnIndex(OnionService.AUTH_COOKIE));
                    String HSAuthCookieValue = hidden_services.getString(hidden_services.getColumnIndex(OnionService.AUTH_COOKIE_VALUE));

                    // Update only new domains or restored from backup with auth cookie
                    if ((HSDomain == null || HSDomain.length() < 1) || (HSAuthCookie == 1 && (HSAuthCookieValue == null || HSAuthCookieValue.length() < 1))) {
                        String hsDirPath = new File(mHSBasePath.getAbsolutePath(), "hs" + HSLocalPort).getCanonicalPath();
                        File file = new File(hsDirPath, "hostname");

                        if (file.exists()) {
                            ContentValues fields = new ContentValues();

                            try {
                                String onionHostname = Utils.readString(new FileInputStream(file)).trim();
                                if (HSAuthCookie == 1) {
                                    String[] aux = onionHostname.split(" ");
                                    onionHostname = aux[0];
                                    fields.put(OnionService.AUTH_COOKIE_VALUE, aux[1]);
                                }
                                fields.put(OnionService.DOMAIN, onionHostname);
                                mCR.update(V2_HS_CONTENT_URI, fields, "port=" + HSLocalPort, null);
                            } catch (FileNotFoundException e) {
                                logException("unable to read onion hostname file", e);
                                showToolbarNotification(getString(R.string.unable_to_read_hidden_service_name), HS_NOTIFY_ID, R.drawable.ic_stat_notifyerr);
                            }
                        } else {
                            showToolbarNotification(getString(R.string.unable_to_read_hidden_service_name), HS_NOTIFY_ID, R.drawable.ic_stat_notifyerr);

                        }
                    }
                }

            } catch (NumberFormatException e) {
                Log.e(OrbotConstants.TAG, "error parsing hsport", e);
            } catch (Exception e) {
                Log.e(OrbotConstants.TAG, "error starting share server", e);
            }

            hidden_services.close();
        }
    }

    private boolean runTorShellCmd() throws Exception {
        Log.d("runTorShellCmd", "begin work......");

        File fileTorrcCustom = updateTorrcCustomFile();

        //make sure Tor exists and we can execute it
        if (fileTor == null || (!fileTor.exists()) || (!fileTor.canExecute())){
            Log.d("runTorShellCmd", "fileTor wrong");
            return false;
        }

        if ((!fileTorRc.exists()) || (!fileTorRc.canRead())){
            Log.d("runTorShellCmd", "fileTorrc wrong");
            return false;
        }


        if ((!fileTorrcCustom.exists()) || (!fileTorrcCustom.canRead())){
            Log.d("runTorShellCmd", "fileTorrcCustom wrong");
            return false;
        }


        sendCallbackLogMessage(getString(R.string.status_starting_up));

        Log.d("orbotservice",CustomTorResourceInstaller.fileTor.getAbsolutePath());
        String torCmdString =
                fileTor.getAbsolutePath()
//                "/data/app/com.ucas.chat-kViRGwzkIa_0OWQYc_mlUw==/lib/arm/libtor.so"
                        + " DataDirectory " + appCacheHome.getAbsolutePath()
                        + " --defaults-torrc " + fileTorRc.getAbsolutePath()
                        + " -f " + fileTorrcCustom.getAbsolutePath();

        int exitCode = -1;

        try {
            exitCode = exec(torCmdString + " --verify-config", true);
        } catch (Exception e) {
            logNotice("Tor configuration did not verify: " + e.getMessage());
            return false;
        }

        if (exitCode == 0) {
            logNotice("Tor configuration VERIFIED.");
            try {
                exitCode = exec(torCmdString, false);
            } catch (Exception e) {
                logNotice("Tor was unable to start: "+torCmdString + e.getMessage());
                throw new Exception("Tor was unable to start: " + e.getMessage());
            }

            if (exitCode != 0) {
                logNotice("Tor did not start. Exit:" + exitCode);
                return false;
            }

            //now try to connect
            mLastProcessId = initControlConnection(10, false);

            if (mLastProcessId == -1) {
                logNotice(getString(R.string.couldn_t_start_tor_process_) + "; exit=" + exitCode);
                throw new Exception(getString(R.string.couldn_t_start_tor_process_) + "; exit=" + exitCode);
            } else {
                logNotice("Tor started; process id=" + mLastProcessId);
            }
        }

        return true;
    }

    protected void exec(Runnable runn) {
        mExecutor.execute(runn);
    }

    private int exec(String cmd, boolean wait) throws Exception {
        HashMap<String, String> mapEnv = new HashMap<>();
        mapEnv.put("HOME", appBinHome.getAbsolutePath());

        CommandResult result = CustomShell.run("sh", wait, mapEnv, cmd);
        debug("executing: " + cmd);
        debug("stdout: " + result.getStdout());
        debug("stderr: " + result.getStderr());

        return result.exitCode;
    }

    private int initControlConnection(int maxTries, boolean isReconnect) throws Exception {
        int controlPort = -1;
        int attempt = 0;

        logNotice(getString(R.string.waiting_for_control_port));

        while (conn == null && attempt++ < maxTries && (mCurrentStatus != STATUS_OFF)) {
            try {
                controlPort = getControlPort();
                if (controlPort != -1) {
                    logNotice(getString(R.string.connecting_to_control_port) + controlPort);
                    break;
                }

            } catch (Exception ce) {
                conn = null;
                //    logException( "Error connecting to Tor local control port: " + ce.getMessage(),ce);
            }

            try {
                //    logNotice("waiting...");
                Thread.sleep(2000);
            } catch (Exception e) {
            }
        }

        if (controlPort != -1) {
            Log.d("initControlport", String.valueOf(controlPort));
            Socket torConnSocket = new Socket(IP_LOCALHOST, controlPort);
            Log.d("torConnSocket", String.valueOf(torConnSocket));
            torConnSocket.setSoTimeout(CONTROL_SOCKET_TIMEOUT);

            conn = new TorControlConnection(torConnSocket);
            conn.launchThread(true);//is daemon
            logNotice("initControlConnection.conn:"+conn);
        }

        if (conn != null) {
            logNotice("SUCCESS connected to Tor control port.");

            File fileCookie = new File(appCacheHome, TOR_CONTROL_COOKIE);

            if (fileCookie.exists()) {
                logNotice("adding control port event handler");

                conn.setEventHandler(mEventHandler);

                logNotice("SUCCESS added control port event handler");
                byte[] cookie = new byte[(int) fileCookie.length()];
                DataInputStream fis = new DataInputStream(new FileInputStream(fileCookie));
                fis.read(cookie);
                fis.close();
                conn.authenticate(cookie);

                logNotice("SUCCESS - authenticated to control port.");

                //       conn.setEvents(Arrays.asList(new String[]{"DEBUG","STATUS_CLIENT","STATUS_GENERAL","BW"}));

                if (Prefs.useDebugLogging())
                    conn.setEvents(Arrays.asList("CIRC", "STREAM", "ORCONN", "BW", "INFO", "NOTICE", "WARN", "DEBUG", "ERR", "NEWDESC", "ADDRMAP"));
                else
                    conn.setEvents(Arrays.asList("CIRC", "STREAM", "ORCONN", "BW", "NOTICE", "ERR", "NEWDESC", "ADDRMAP"));

                //  sendCallbackLogMessage(getString(R.string.tor_process_starting) + ' ' + getString(R.string.tor_process_complete));

                String torProcId = conn.getInfo("process/pid");

                String confSocks = conn.getInfo("net/listeners/socks");
                StringTokenizer st = new StringTokenizer(confSocks, " ");

                confSocks = st.nextToken().split(":")[1];
                confSocks = confSocks.substring(0, confSocks.length() - 1);
                mPortSOCKS = Integer.parseInt(confSocks);

                String confHttp = conn.getInfo("net/listeners/httptunnel");
                st = new StringTokenizer(confHttp, " ");

                confHttp = st.nextToken().split(":")[1];
                confHttp = confHttp.substring(0, confHttp.length() - 1);
                mPortHTTP = Integer.parseInt(confHttp);

                String confDns = conn.getInfo("net/listeners/dns");
                st = new StringTokenizer(confDns, " ");
                if (st.hasMoreTokens()) {
                    confDns = st.nextToken().split(":")[1];
                    confDns = confDns.substring(0, confDns.length() - 1);
                    mPortDns = Integer.parseInt(confDns);
                    Prefs.getSharedPrefs(getApplicationContext()).edit().putInt(VpnPrefs.PREFS_DNS_PORT, mPortDns).apply();
                }

                String confTrans = conn.getInfo("net/listeners/trans");
                st = new StringTokenizer(confTrans, " ");
                if (st.hasMoreTokens()) {
                    confTrans = st.nextToken().split(":")[1];
                    confTrans = confTrans.substring(0, confTrans.length() - 1);
                    mPortTrans = Integer.parseInt(confTrans);
                }

                sendCallbackPorts(mPortSOCKS, mPortHTTP, mPortDns, mPortTrans);
                setTorNetworkEnabled(true);

                return Integer.parseInt(torProcId);

            } else {
                logNotice("Tor authentication cookie does not exist yet");
                conn = null;

            }
        }

        throw new Exception("Tor control port could not be found");
    }

    //从文件中读control port
    private int getControlPort() {
        int result = -1;

        try {
            if (fileControlPort.exists()) {
                debug("Reading control port config file: " + fileControlPort.getCanonicalPath());
                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileControlPort));
                String line = bufferedReader.readLine();

                if (line != null) {
                    //修改controlport,不好使
                    debug("line:"+line);
                    String lineReplae = line.replaceAll(line,"PORT=127.0.0.1:49065");
                    debug("lineReplae:"+lineReplae);

                    String[] lineParts = line.split(":");
                    debug("getControlPort.lineParts: " + Arrays.toString(lineParts));

                    result = Integer.parseInt(lineParts[1]);
                    debug("getControlPort.result: " + result);

                }


                bufferedReader.close();

                //store last valid control port
                SharedPreferences prefs = Prefs.getSharedPrefs(getApplicationContext());
                prefs.edit().putInt("controlport", result).apply();
            } else {
                debug("Control Port config file does not yet exist (waiting for tor): " + fileControlPort.getCanonicalPath());
            }
        } catch (FileNotFoundException e) {
            debug("unable to get control port; file not found");
        } catch (Exception e) {
            debug("unable to read control port config file");
        }

        return result;
    }

    public String getInfo(String key) {
        try {
            if (conn != null) {
                return conn.getInfo(key);
            }
        } catch (Exception ioe) {
            //    Log.e(TAG,"Unable to get Tor information",ioe);
            logNotice("Unable to get Tor information" + ioe.getMessage());
        }
        return null;
    }

    public void setTorNetworkEnabled(final boolean isEnabled) throws IOException {
        if (conn != null) { // it is possible to not have a connection yet, and someone might try to newnym
            new Thread() {
                public void run() {
                    try {
                        final String newValue = isEnabled ? "0" : "1";
                        conn.setConf("DisableNetwork", newValue);
                    } catch (Exception ioe) {
                        debug("error requesting newnym: " + ioe.getLocalizedMessage());
                    }
                }
            }.start();
        }
    }

    public void sendSignalActive() {
        if (conn != null && mCurrentStatus == STATUS_ON) {
            try {
                conn.signal("ACTIVE");
            } catch (IOException e) {
                debug("error send active: " + e.getLocalizedMessage());
            }
        }
    }

    public void newIdentity() {
        if (conn != null) { // it is possible to not have a connection yet, and someone might try to newnym
            new Thread() {
                public void run() {
                    try {
                        int iconId = R.drawable.ic_stat_tor;

                        if (conn != null && mCurrentStatus == STATUS_ON && Prefs.expandedNotifications())
                            showToolbarNotification(getString(R.string.newnym), NOTIFY_ID, iconId);

                        conn.signal(TorControlCommands.SIGNAL_NEWNYM);

                    } catch (Exception ioe) {
                        debug("error requesting newnym: " + ioe.getLocalizedMessage());
                    }
                }
            }.start();
        }
    }

    protected void sendCallbackBandwidth(long upload, long download, long written, long read) {
        Intent intent = new Intent(LOCAL_ACTION_BANDWIDTH);

        intent.putExtra("up", upload);
        intent.putExtra("down", download);
        intent.putExtra("written", written);
        intent.putExtra("read", read);
        intent.putExtra(EXTRA_STATUS, mCurrentStatus);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendCallbackLogMessage(final String logMessage) {
        Log.d(TAG, " sendCallbackLogMessage:: " + "begin work......");

        mHandler.post(() -> {

            if (logMessage.contains("100%")){
                Log.d(TAG," sendCallbackLogMessage:: " + " sendBroadcast 100% done");
                Intent intent = new Intent();
                intent.setAction(OrbotServiceAction.TOR_BROAD_CAST_ACTION);
                intent.setComponent(new ComponentName(OrbotServiceAction.PACKAGE,
                        OrbotServiceAction.TOR_BROAD_CAST_PATH));
                intent.putExtra(OrbotServiceAction.TOR_BROAD_CAST_INTENT_KEY, OrbotServiceAction.TOR_HAS_CONNECTED);
                sendBroadcast(intent);
            }

            Intent intent = new Intent(); // TODO: 2021/7/20 安卓8以后的静态广播都需要 intent.setComponent(new ComponentName())才能让接收器收到广播
            Log.d(TAG ," sendCallbackLogMessage:: logMessage: " + logMessage);
            intent.setAction(LOCAL_ACTION_LOG);
            intent.setComponent(new ComponentName(OrbotServiceAction.PACKAGE,
                    "com.ucas.chat.ui.login.LoginActivity$ProgressReceiver"));
            intent.putExtra(LOCAL_EXTRA_LOG, logMessage);
            intent.putExtra(EXTRA_STATUS, mCurrentStatus);
            sendBroadcast(intent);

            intent.setComponent(new ComponentName(OrbotServiceAction.PACKAGE,
                    "com.ucas.chat.ui.home.NewsFragment$ProgressReceiver"));
            sendBroadcast(intent);

//            LocalBroadcastManager.getInstance(OrbotService.this).sendBroadcast(intent);
        });

    }

    private void sendCallbackPorts(int socksPort, int httpPort, int dnsPort, int transPort) {
        Intent intent = new Intent(LOCAL_ACTION_PORTS); // You can also include some extra data.
        intent.putExtra(EXTRA_SOCKS_PROXY_PORT, socksPort);
        intent.putExtra(EXTRA_HTTP_PROXY_PORT, httpPort);
        intent.putExtra(EXTRA_DNS_PORT, dnsPort);
        intent.putExtra(EXTRA_TRANS_PORT, transPort);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (Prefs.useVpn())
            mVpnManager.handleIntent(new Builder(), intent);

    }

    protected void sendCallbackStatus(String currentStatus) {
        mCurrentStatus = currentStatus;
        Intent intent = getActionStatusIntent(currentStatus);
        sendBroadcastOnlyToOrbot(intent); // send for Orbot internals, using secure local broadcast
        sendBroadcast(intent); // send for any apps that are interested
    }

    /**
     * Send a secure broadcast only to Orbot itself
     *
     * @see {@link ContextWrapper#sendBroadcast(Intent)}
     * @see {@link LocalBroadcastManager}
     */
    private boolean sendBroadcastOnlyToOrbot(Intent intent) {
        return LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private Intent getActionStatusIntent(String currentStatus) {
        Intent intent = new Intent(ACTION_STATUS);
        intent.putExtra(EXTRA_STATUS, currentStatus);
        return intent;
    }

    /*
     *  Another way to do this would be to use the Observer pattern by defining the
     *  BroadcastReciever in the Android manifest.
     */

    /**
     * private final BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
     *
     * @Override public void onReceive(Context context, Intent intent) {
     * <p>
     * if (mCurrentStatus == STATUS_OFF)
     * return;
     * <p>
     * SharedPreferences prefs = Prefs.getSharedPrefs(getApplicationContext());
     * <p>
     * boolean doNetworKSleep = prefs.getBoolean(OrbotConstants.PREF_DISABLE_NETWORK, true);
     * <p>
     * final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
     * final NetworkInfo netInfo = cm.getActiveNetworkInfo();
     * <p>
     * boolean newConnectivityState = false;
     * int newNetType = -1;
     * <p>
     * if (netInfo!=null)
     * newNetType = netInfo.getType();
     * <p>
     * if(netInfo != null && netInfo.isConnected()) {
     * // WE ARE CONNECTED: DO SOMETHING
     * newConnectivityState = true;
     * }
     * else {
     * // WE ARE NOT: DO SOMETHING ELSE
     * newConnectivityState = false;
     * }
     * <p>
     * if (newConnectivityState != mConnectivity) {
     * mConnectivity = newConnectivityState;
     * <p>
     * //if (mConnectivity)
     * //  newIdentity();
     * }
     * <p>
     * }
     * };
     **/

    private StringBuffer processSettingsImpl(StringBuffer extraLines) throws IOException {
        Log.d("processSettingsImpl", "begin work......");

        logNotice(getString(R.string.updating_settings_in_tor_service));
        SharedPreferences prefs = Prefs.getSharedPrefs(getApplicationContext());

        boolean useBridges = false;
        Log.d("processSettingsImpl2", "begin work......");

        boolean becomeRelay = prefs.getBoolean(OrbotConstants.PREF_OR, false);
        boolean ReachableAddresses = prefs.getBoolean(OrbotConstants.PREF_REACHABLE_ADDRESSES, false);
        boolean enableStrictNodes = prefs.getBoolean("pref_strict_nodes", false);
        String entranceNodes = prefs.getString("pref_entrance_nodes", "");
        String exitNodes = prefs.getString("pref_exit_nodes", "");
        String excludeNodes = prefs.getString("pref_exclude_nodes", "");
        Log.d("processSettingsImp3", "begin work......");

//        extraLines.append("Socks5Proxy  47.243.253.113:21085").append('\n');//删除代理，用其他vpn来登陆就
       // extraLines.append("Socks5Proxy 119.13.77.50:29900").append('\n');//删除  代理，用其他vpn来登陆就
       // extraLines.append("Socks5Proxy 185.126.70.145:22009").append('\n');
       //extraLines.append("Socks5Proxy 185.126.70.145:22009").append('\n');
        extraLines.append("Socks5Proxy 195.133.10.141:22005").append('\n');

        if (!useBridges) {
            extraLines.append("UseBridges 0").append('\n');
            if (Prefs.useVpn()) { //set the proxy here if we aren't using a bridge
                if (!mIsLollipop) {
                    String proxyType = "socks5";
                    extraLines.append(proxyType + "Proxy" + ' ' + OrbotVpnManager.sSocksProxyLocalhost + ':' + OrbotVpnManager.sSocksProxyServerPort).append('\n');
                    Log.d("processSettingsImp4", "begin work......");

                }

            } else {
                String proxyType = prefs.getString("pref_proxy_type", null);
                if (proxyType != null && proxyType.length() > 0) {
                    String proxyHost = prefs.getString("pref_proxy_host", null);
                    String proxyPort = prefs.getString("pref_proxy_port", null);
                    String proxyUser = prefs.getString("pref_proxy_username", null);
                    String proxyPass = prefs.getString("pref_proxy_password", null);
                    Log.d("processSettingsImpl5", "begin work......");

                    if ((proxyHost != null && proxyHost.length() > 0) && (proxyPort != null && proxyPort.length() > 0)) {
                        extraLines.append(proxyType + "Proxy" + ' ' + proxyHost + ':' + proxyPort).append('\n');

                        if (proxyUser != null && proxyPass != null) {
                            if (proxyType.equalsIgnoreCase("socks5")) {
                                extraLines.append("Socks5ProxyUsername" + ' ' + proxyUser).append('\n');
                                extraLines.append("Socks5ProxyPassword" + ' ' + proxyPass).append('\n');
                            } else
                                extraLines.append(proxyType + "ProxyAuthenticator" + ' ' + proxyUser + ':' + proxyPort).append('\n');

                        } else if (proxyPass != null)
                            extraLines.append(proxyType + "ProxyAuthenticator" + ' ' + proxyUser + ':' + proxyPort).append('\n');
                    }
                }
            }
        } else {
            Log.d("processSettingsImp6", "begin work......");

            loadBridgeDefaults();
            extraLines.append("UseBridges 1").append('\n');
            //    extraLines.append("UpdateBridgesFromAuthority 1").append('\n');

            String bridgeList = Prefs.getBridgesList();

            String builtInBridgeType = null;
            Log.d("processSettingsImp7", "begin work......");

            //check if any PT bridges are needed
//            if (bridgeList.contains("obfs")) {
//                extraLines.append("ClientTransportPlugin obfs3 socks5 127.0.0.1:" + IPtProxy.Obfs3SocksPort).append('\n');
//                extraLines.append("ClientTransportPlugin obfs4 socks5 127.0.0.1:" + IPtProxy.Obfs4SocksPort).append('\n');
//
//                if (bridgeList.equals("obfs4"))
//                    builtInBridgeType = "obfs4";
//            }
//
//            if (bridgeList.equals("meek")) {
//                extraLines.append("ClientTransportPlugin meek_lite socks5 127.0.0.1:" + IPtProxy.MeekSocksPort).append('\n');
//                builtInBridgeType = "meek_lite";
//            }
//
//            if (bridgeList.equals("snowflake")) {
//                extraLines.append("ClientTransportPlugin snowflake socks5 127.0.0.1:" + IPtProxy.SnowflakeSocksPort).append('\n');
//                builtInBridgeType = "snowflake";
//            }

            if (!TextUtils.isEmpty(builtInBridgeType))
                getBridges(builtInBridgeType, extraLines);
            else {
                String[] bridgeListLines = parseBridgesFromSettings(bridgeList);
                int bridgeIdx = (int) Math.floor(Math.random() * ((double) bridgeListLines.length));
                String bridgeLine = bridgeListLines[bridgeIdx];
                extraLines.append("Bridge ");
                extraLines.append(bridgeLine);
                extraLines.append("\n");
            }
        }
        Log.d("processSettingsImp8", "begin work......");

        //only apply GeoIP if you need it
        File fileGeoIP = new File(appBinHome, GEOIP_ASSET_KEY);
        File fileGeoIP6 = new File(appBinHome, GEOIP6_ASSET_KEY);

        if (fileGeoIP.exists()) {
            extraLines.append("GeoIPFile" + ' ' + fileGeoIP.getCanonicalPath()).append('\n');
            extraLines.append("GeoIPv6File" + ' ' + fileGeoIP6.getCanonicalPath()).append('\n');
        }
        Log.d("processSettingsImp9", "begin work......");

        if (!TextUtils.isEmpty(entranceNodes))
            extraLines.append("EntryNodes" + ' ' + entranceNodes).append('\n');

        if (!TextUtils.isEmpty(exitNodes))
            extraLines.append("ExitNodes" + ' ' + exitNodes).append('\n');

        if (!TextUtils.isEmpty(excludeNodes))
            extraLines.append("ExcludeNodes" + ' ' + excludeNodes).append('\n');

        extraLines.append("StrictNodes" + ' ' + (enableStrictNodes ? "1" : "0")).append('\n');
//        String v3DirPath = new File(mV3OnionBasePath.getAbsolutePath(), "v3" + localPort).getCanonicalPath();

        extraLines.append("HiddenServiceDir ").append(v3Dirpath).append("/hidden_service_replace").append("\n");
        extraLines.append("HiddenServiceVersion 3").append("\n");
        extraLines.append("HiddenServicePort ").append(localPort).append(" 127.0.0.1:").append(localPort).append("\n");

        NodeHelper nodeHelper = new NodeHelper(getApplication());
        if(nodeHelper.getFirst()!=null){
            String guardNode = nodeHelper.queryAllGuardNode();
            extraLines.append("EntryNodes "+guardNode).append('\n');
        }

//        copy_hs_to_hsD();
        Log.d("processSettingsImp10", "begin work......");

        try {
            if (ReachableAddresses) {
                String ReachableAddressesPorts = prefs.getString(OrbotConstants.PREF_REACHABLE_ADDRESSES_PORTS, "*:80,*:443");
                extraLines.append("ReachableAddresses" + ' ' + ReachableAddressesPorts).append('\n');
            }

        } catch (Exception e) {
            showToolbarNotification(getString(R.string.your_reachableaddresses_settings_caused_an_exception_), ERROR_NOTIFY_ID, R.drawable.ic_stat_notifyerr);
            return null;
        }
        Log.d("processSettingsImp11", "begin work......");

        try {
            if (becomeRelay && (!useBridges) && (!ReachableAddresses)) {
                int ORPort = Integer.parseInt(prefs.getString(OrbotConstants.PREF_OR_PORT, "9001"));
                String nickname = prefs.getString(OrbotConstants.PREF_OR_NICKNAME, "Orbot");
                String dnsFile = writeDNSFile();

                extraLines.append("ServerDNSResolvConfFile" + ' ' + dnsFile).append('\n');
                extraLines.append("ORPort" + ' ' + ORPort).append('\n');
                extraLines.append("Nickname" + ' ' + nickname).append('\n');
                extraLines.append("ExitPolicy" + ' ' + "reject *:*").append('\n');

            }
        } catch (Exception e) {
            showToolbarNotification(getString(R.string.your_relay_settings_caused_an_exception_), ERROR_NOTIFY_ID, R.drawable.ic_stat_notifyerr);
            return null;
        }
        Log.d("processSettingsImp12", "begin work......");

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        addV3OnionServicesToTorrc(extraLines, contentResolver);
        addV3ClientAuthToTorrc(extraLines, contentResolver);
        addV2HiddenServicesToTorrc(extraLines, contentResolver);
        addV2ClientCookiesToTorrc(extraLines, contentResolver);
        Log.d("processSettingsImpl3", "begin work......");

        return extraLines;
    }

    private void addV3OnionServicesToTorrc(StringBuffer torrc, ContentResolver contentResolver) {
        Log.d("addV3OnionServices", "begin work......");

        try {
            Cursor onionServices = contentResolver.query(V3_ONION_SERVICES_CONTENT_URI, V3_ONION_SERVICE_PROJECTION, OnionService.ENABLED + "=1", null, null);
            Log.d("addV3OnionServices2", "begin work......");

            if (onionServices != null) {
                while (onionServices.moveToNext()) {

                    int localPort = onionServices.getInt(onionServices.getColumnIndex(OnionService.PORT));
                    int onionPort = onionServices.getInt(onionServices.getColumnIndex(OnionService.ONION_PORT));
//                    String v3DirPath = new File(mV3OnionBasePath.getAbsolutePath(), "v3" + localPort).getCanonicalPath();
//                    torrc.append("HiddenServiceDir ").append(v3DirPath).append("\n");
//                    torrc.append("HiddenServiceVersion 3").append("\n");
//                    torrc.append("HiddenServicePort ").append(onionPort).append(" 127.0.0.1:").append(localPort).append("\n");
                }
                onionServices.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    // todo needs modifications to set hidden service version back after doing v3 stuff...
    private void addV2HiddenServicesToTorrc(StringBuffer torrc, ContentResolver contentResolver) {
        try {
            Cursor hidden_services = contentResolver.query(V2_HS_CONTENT_URI, LEGACY_V2_ONION_SERVICE_PROJECTION, OnionService.ENABLED + "=1", null, null);
            if (hidden_services != null) {
                try {
                    while (hidden_services.moveToNext()) {
                        String HSname = hidden_services.getString(hidden_services.getColumnIndex(OnionService.NAME));
                        int HSLocalPort = hidden_services.getInt(hidden_services.getColumnIndex(OnionService.PORT));
                        int HSOnionPort = hidden_services.getInt(hidden_services.getColumnIndex(OnionService.ONION_PORT));
                        int HSAuthCookie = hidden_services.getInt(hidden_services.getColumnIndex(OnionService.AUTH_COOKIE));
                        String hsDirPath = new File(mHSBasePath.getAbsolutePath(), "hs" + HSLocalPort).getCanonicalPath();

                        debug("Adding hidden service on port: " + HSLocalPort);

                        torrc.append("HiddenServiceDir" + ' ' + hsDirPath).append('\n');
                        torrc.append("HiddenServicePort" + ' ' + HSOnionPort + " 127.0.0.1:" + HSLocalPort).append('\n');
                        torrc.append("HiddenServiceVersion 2").append('\n');

                        if (HSAuthCookie == 1)
                            torrc.append("HiddenServiceAuthorizeClient stealth " + HSname).append('\n');
                    }
                } catch (NumberFormatException e) {
                    Log.e(OrbotConstants.TAG, "error parsing hsport", e);
                } catch (Exception e) {
                    Log.e(OrbotConstants.TAG, "error starting share server", e);
                }

                hidden_services.close();
            }
        } catch (SecurityException se) {
        }
    }

    public static String buildV3ClientAuthFile(String domain, String keyHash) {
        return domain + ":descriptor:x25519:" + keyHash;
    }

    private void addV3ClientAuthToTorrc(StringBuffer torrc, ContentResolver contentResolver) {
        Cursor v3auths = contentResolver.query(V3_CLIENT_AUTH_URI, V3_CLIENT_AUTH_PROJECTION, V3ClientAuth.ENABLED + "=1", null, null);
        if (v3auths != null) {
            for (File file : mV3AuthBasePath.listFiles()) {
                if (!file.isDirectory())
                    file.delete(); // todo the adapter should maybe just write these files and not do this in service...
            }
            torrc.append("ClientOnionAuthDir " + mV3AuthBasePath.getAbsolutePath()).append('\n');
            try {
                int i = 0;
                while (v3auths.moveToNext()) {
                    String domain = v3auths.getString(v3auths.getColumnIndex(V3ClientAuth.DOMAIN));
                    String hash = v3auths.getString(v3auths.getColumnIndex(V3ClientAuth.HASH));
                    File authFile = new File(mV3AuthBasePath, (i++) + ".auth_private");
                    authFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(authFile);
                    fos.write(buildV3ClientAuthFile(domain, hash).getBytes());
                    fos.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "error adding v3 client auth...");
            }
        }
    }

    private void addV2ClientCookiesToTorrc(StringBuffer torrc, ContentResolver contentResolver) {
        try {
            Cursor client_cookies = contentResolver.query(COOKIE_CONTENT_URI, LEGACY_COOKIE_PROJECTION, ClientCookie.ENABLED + "=1", null, null);
            if (client_cookies != null) {
                try {
                    while (client_cookies.moveToNext()) {
                        String domain = client_cookies.getString(client_cookies.getColumnIndex(ClientCookie.DOMAIN));
                        String cookie = client_cookies.getString(client_cookies.getColumnIndex(ClientCookie.AUTH_COOKIE_VALUE));
                        torrc.append("HidServAuth" + ' ' + domain + ' ' + cookie).append('\n');
                    }
                } catch (Exception e) {
                    Log.e(OrbotConstants.TAG, "error starting share server", e);
                }
                client_cookies.close();
            }
        } catch (SecurityException se) {
        }
    }

    //using Google DNS for now as the public DNS server
    private String writeDNSFile() throws IOException {
        File file = new File(appBinHome, "resolv.conf");

        PrintWriter bw = new PrintWriter(new FileWriter(file));
        bw.println("nameserver 8.8.8.8");
        bw.println("nameserver 8.8.4.4");
        bw.close();

        return file.getCanonicalPath();
    }

    @SuppressLint("NewApi")
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        switch (level) {
            case TRIM_MEMORY_BACKGROUND:
                debug("trim memory requested: app in the background");
                break;

            case TRIM_MEMORY_COMPLETE:
                debug("trim memory requested: cleanup all memory");
                break;

            case TRIM_MEMORY_MODERATE:
                debug("trim memory requested: clean up some memory");
                break;

            case TRIM_MEMORY_RUNNING_CRITICAL:
                debug("trim memory requested: memory on device is very low and critical");
                break;

            case TRIM_MEMORY_RUNNING_LOW:
                debug("trim memory requested: memory on device is running low");
                break;

            case TRIM_MEMORY_RUNNING_MODERATE:
                debug("trim memory requested: memory on device is moderate");
                break;

            case TRIM_MEMORY_UI_HIDDEN:
                debug("trim memory requested: app is not showing UI anymore");
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return super.onBind(intent); // invoking super class will call onRevoke() when appropriate
    }

    // system calls this method when VPN disconnects (either by the user or another VPN app)
    @Override
    public void onRevoke() {
        Prefs.putUseVpn(false);
        mVpnManager.handleIntent(new Builder(), new Intent(ACTION_STOP_VPN));
        // tell UI, if it's open, to update immediately (don't wait for onResume() in Activity...)
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STOP_VPN));
    }

    private void setExitNode(String newExits) {
        SharedPreferences prefs = Prefs.getSharedPrefs(getApplicationContext());

        if (TextUtils.isEmpty(newExits)) {
            prefs.edit().remove("pref_exit_nodes").apply();

            if (conn != null) {
                try {
                    ArrayList<String> resetBuffer = new ArrayList<>();
                    resetBuffer.add("ExitNodes");
                    resetBuffer.add("StrictNodes");
                    conn.resetConf(resetBuffer);
                    conn.setConf("DisableNetwork", "1");
                    conn.setConf("DisableNetwork", "0");

                } catch (Exception ioe) {
                    Log.e(OrbotConstants.TAG, "Connection exception occured resetting exits", ioe);
                }
            }
        } else {
            prefs.edit().putString("pref_exit_nodes", newExits).apply();

            if (conn != null) {
                try {
                    File fileGeoIP = new File(appBinHome, GEOIP_ASSET_KEY);
                    File fileGeoIP6 = new File(appBinHome, GEOIP6_ASSET_KEY);

                    conn.setConf("GeoIPFile", fileGeoIP.getCanonicalPath());
                    conn.setConf("GeoIPv6File", fileGeoIP6.getCanonicalPath());

                    conn.setConf("ExitNodes", newExits);
                    conn.setConf("StrictNodes", "1");

                    conn.setConf("DisableNetwork", "1");
                    conn.setConf("DisableNetwork", "0");

                } catch (Exception ioe) {
                    Log.e(OrbotConstants.TAG, "Connection exception occured resetting exits", ioe);
                }
            }
        }

    }

    private void loadBridgeDefaults() {
        if (alBridges == null) {
            alBridges = new ArrayList<>();

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.bridges), "UTF-8"));
                String str;

                while ((str = in.readLine()) != null) {

                    StringTokenizer st = new StringTokenizer(str, " ");
                    Bridge b = new Bridge();
                    b.type = st.nextToken();

                    StringBuffer sbConfig = new StringBuffer();

                    while (st.hasMoreTokens())
                        sbConfig.append(st.nextToken()).append(' ');

                    b.config = sbConfig.toString().trim();
                    alBridges.add(b);
                }

                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getBridges(String type, StringBuffer extraLines) {

        Collections.shuffle(alBridges, bridgeSelectRandom);

        //let's just pull up to 2 bridges from the defaults at time
        int maxBridges = 2;
        int bridgeCount = 0;

        //now go through the list to find the bridges we want
        for (Bridge b : alBridges) {
            if (b.type.equals(type)) {
                extraLines.append("Bridge ");
                extraLines.append(b.type);
                extraLines.append(' ');
                extraLines.append(b.config);
                extraLines.append('\n');

                bridgeCount++;

                if (bridgeCount > maxBridges)
                    break;
            }
        }
    }

    public static final class OnionService implements BaseColumns {
        public static final String NAME = "name";
        public static final String PORT = "port";
        public static final String ONION_PORT = "onion_port";
        public static final String DOMAIN = "domain";
        public static final String AUTH_COOKIE = "auth_cookie";
        public static final String AUTH_COOKIE_VALUE = "auth_cookie_value";
        public static final String ENABLED = "enabled";
    }

    public static final class V3ClientAuth implements BaseColumns {
        public static final String DOMAIN = "domain";
        public static final String HASH = "hash";
        public static final String ENABLED = "enabled";
    }

    public static final class ClientCookie implements BaseColumns {
        public static final String DOMAIN = "domain";
        public static final String AUTH_COOKIE_VALUE = "auth_cookie_value";
        public static final String ENABLED = "enabled";
    }

    // for bridge loading from the assets default bridges.txt file
    static class Bridge {
        String type;
        String config;
    }

    private class IncomingIntentRouter implements Runnable {
        Intent mIntent;

        public IncomingIntentRouter(Intent intent) {
            mIntent = intent;
        }

        public void run() {
            String action = mIntent.getAction();
            Log.d("IncomingIntentRouter", "begin work......");

            if (!TextUtils.isEmpty(action)) {
                if (action.equals(ACTION_START) || action.equals(ACTION_START_ON_BOOT)) {

//                    if (useIPtObfsMeekProxy())
//                        IPtProxy.startObfs4Proxy("DEBUG", false, false);
//
//                    if (useIPtSnowflakeProxy())
//                        startSnowflakeProxy();


                    startTor();

                    replyWithStatus(mIntent);

                    if (Prefs.useVpn()) {
                        if (mVpnManager != null
                                && (!mVpnManager.isStarted())) {
                            //start VPN here
                            Intent vpnIntent = VpnService.prepare(OrbotService.this);
                            if (vpnIntent == null) //then we can run the VPN
                            {
                                mVpnManager.handleIntent(new Builder(), mIntent);

                            }
                        }

                        if (mPortSOCKS != -1 && mPortHTTP != -1)
                            sendCallbackPorts(mPortSOCKS, mPortHTTP, mPortDns, mPortTrans);
                    }

                } else if (action.equals(ACTION_START_VPN)) {
                    if (mVpnManager != null && (!mVpnManager.isStarted())) {
                        //start VPN here
                        Intent vpnIntent = VpnService.prepare(OrbotService.this);
                        if (vpnIntent == null) { //then we can run the VPN
                            mVpnManager.handleIntent(new Builder(), mIntent);
                        }
                    }

                    if (mPortSOCKS != -1 && mPortHTTP != -1)
                        sendCallbackPorts(mPortSOCKS, mPortHTTP, mPortDns, mPortTrans);


                } else if (action.equals(ACTION_STOP_VPN)) {
                    if (mVpnManager != null)
                        mVpnManager.handleIntent(new Builder(), mIntent);
                } else if (action.equals(ACTION_STATUS)) {
                    replyWithStatus(mIntent);
                } else if (action.equals(CMD_SIGNAL_HUP)) {
                    requestTorRereadConfig();
                } else if (action.equals(CMD_NEWNYM)) {
                    newIdentity();
                } else if (action.equals(CMD_ACTIVE)) {
                    sendSignalActive();
                } else if (action.equals(CMD_SET_EXIT)) {
                    setExitNode(mIntent.getStringExtra("exit"));
                } else {
                    Log.w(OrbotConstants.TAG, "unhandled OrbotService Intent: " + action);
                }
            }
        }
    }

    private class ActionBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CMD_NEWNYM: {
                    newIdentity();
                    break;
                }
                case CMD_ACTIVE: {
                    sendSignalActive();
                    break;
                }
            }
        }
    }

}
