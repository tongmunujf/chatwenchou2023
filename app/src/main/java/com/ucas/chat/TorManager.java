package com.ucas.chat;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.android_play.MainActivity;
import com.ucas.chat.bean.AddressBookBean;
import com.ucas.chat.bean.MyInforBean;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.db.AddressBookHelper;
import com.ucas.chat.db.MyInforTool;
import com.ucas.chat.db.MySelfInfoHelper;
import com.ucas.chat.db.ServiceInfoHelper;
import com.ucas.chat.jni.JniEntryUtils;
import com.ucas.chat.tor.message.Message;
import com.ucas.chat.tor.server.ServerMessageHandler;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.tor.util.MailItem;
import com.ucas.chat.tor.util.RecordXOR;
import com.ucas.chat.tor.util.XORutil;
import com.ucas.chat.ui.login.LoginActivity;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.AesUtils;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;

import org.torproject.android.service.OrbotService;
import org.torproject.android.service.TorServiceConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ucas.chat.MyApplication.getContext;
import static org.torproject.android.service.TorServiceConstants.ACTION_STOP_VPN;

public class TorManager {
    private static final String TAG = ConstantValue.TAG_CHAT + "TorManager";
//    private final static int PORT = 6677;
    //文本0 文件2
    private static int PURPOSE_TEXT = 0;
    private static int PURPOSE_FILE = 2;
    /**
     *
     * @param context
     */
    public static void startTor(Context context) {
        sendIntentToService(context, TorServiceConstants.ACTION_START);
    }

    public static void sendIntentToService(Context context, String action) {
        Intent intent = new Intent(context, OrbotService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    /**
     * 停止Tor
     * @param context
     */
    public static void stopTor(Context context) {
        Intent intent = new Intent(context, OrbotService.class);
        intent.setAction(ACTION_STOP_VPN);
        context.stopService(intent);
    }

    /**
     * 点击登陆开启OrbotService,启动Tor
     * Tor连接100%之后调用此方法
     * @param context
     */
    public static void interface_start_listen(Context context) {
        LogUtils.e(TAG, " 1、interface_start_listen:: ");
        ServerMessageHandler messageHandler =ServerMessageHandler.getInstance();

        ServiceInfoHelper mServiceHelper = new ServiceInfoHelper(context);
        String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.d(TAG, " interface_start_listen:: decOfflineServer = " + decOfflineServer);
        messageHandler.setChannelServer(decOfflineServer);
        MySelfInfoHelper h= MySelfInfoHelper.getInstance(context);
        MyInforBean my=h.queryAll();

        LogUtils.d(TAG, " interface_start_listen:: 转化前 MyInforBean = " + my.toString());

        LogUtils.d(TAG, " interface_start_listen:: *********************************************");
        String onionName = AesTools.getDecryptContent(my.getOnionName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " interface_start_listen:: 转化后 onionName = " + onionName);
        String nickName = AesTools.getDecryptContent(my.getNickName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " interface_start_listen:: 转化后 nickName = " + nickName);
        String account = AesTools.getDecryptContent(my.getAccount(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " interface_start_listen:: 转化后 account = " + account);
        String pw = AesTools.getDecryptContent(my.getPassword(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " interface_start_listen:: 转化后 password = " + pw);
        String privateKey = AesTools.getDecryptContent(my.getPrivateKey(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " interface_start_listen:: 转化后 privateKey = " + privateKey);
        String publicKey = AesTools.getDecryptContent(my.getPublicKey(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " interface_start_listen:: 转化后 publicKey = " + publicKey);


        UserBean userBean=new UserBean();
        userBean.setPublicKey(publicKey);
        userBean.setPrivateKey(privateKey);
        userBean.setOnionName(onionName);
        userBean.setUserName(nickName);
        userBean.setUserId(account);
        userBean.setImPhoto(0);
        userBean.setOnlineStatus("1");
        userBean.setPassword(pw);
        messageHandler.setMySelfBean(userBean);


        SharedPreferencesUtil.setUserBeanSharedPreferences(getContext(), userBean);
        LogUtils.d(TAG, " interface_start_listen:: 网络--写入BeanSharedPreferences  "+"TorManager-interface_start_listen()");
        LogUtils.d(TAG, " interface_start_listen:: 网络--写入BeanSharedPreferences  userBean: "+ userBean.toString());

//        messageHandler.setLocalPort(Constant.REMOTE_ONION_PORT);
//        messageHandler.setPrivateKey(my.getPrivateKey());

        AddressBookHelper helper= AddressBookHelper.getInstance(context);
        ArrayList<AddressBookBean> contacts= helper.queryAll();

        for(int i=0;i<contacts.size();i++){
            AddressBookBean bean = contacts.get(i);

            LogUtils.d(TAG, " interface_start_listen:: 转化前 remoteOnionName = " + bean.getRemoteOnionName() + " remotePublicKey = " + bean.getRemotePublicKey());

            String remoteOnionName = AesTools.getDecryptContent(bean.getRemoteOnionName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
            LogUtils.d(TAG, " interface_start_listen:: 转化后 remoteOnionName = " + remoteOnionName);
            String remotePublicKey = bean.getRemotePublicKey();
           // String remotePublicKey = AesTools.getDecryptContent(bean.getRemotePublicKey(), AesTools.AesKeyTypeEnum.COMMON_KEY);
            LogUtils.d(TAG, " interface_start_listen:: 转化前 remotePublicKey = " + remotePublicKey);


            MailItem item = new MailItem(remoteOnionName, Constant.REMOTE_ONION_PORT,
                   remotePublicKey);
            messageHandler.addContact(item);
        }

        messageHandler.init(context);


//        try {
//            InterfaceToAndroid.interface_start_listen("127.0.0.1",6677, context);
//        } catch (Exception e) {
//            LogUtils.e(TAG, e.toString());
//        }
    }

    /**
     * 和对方聊天之前要先调用此接口
     * @param friendOrionId
     * @param context
     */
    public static void interface_connect_peer(String friendOrionId, Context context ){
        LogUtils.e(TAG, " 2、interface_connect_peer: friendOrionId = " + friendOrionId);
//        try {
//            InterfaceToAndroid.interface_connect_peer(friendOrionId, PORT, context);
//        } catch (Exception e) {
//            LogUtils.e(TAG, e.toString());
//        }
    }

    public static void initMessageHandler(Context context, String orionId){
        connectFriend(orionId);
    }

    public static void connectFriend(String orionName){
        LogUtils.d(TAG, " connectFriend:: orionName = " + orionName);
        ServerMessageHandler messageHandler =ServerMessageHandler.getInstance();
        messageHandler.createConnectionAsyc(orionName, PURPOSE_TEXT);
//        messageHandler.createConnectionAsyc(orionName, PURPOSE_FILE);
    }


    public static void startHandShakeProcess(String onionName){
        ServerMessageHandler handler = ServerMessageHandler.getInstance();
//        onionName=Constant.REMOTE_ONION_NAME;
        handler.startHandShakeProcess(onionName, PURPOSE_TEXT);
//        handler.startHandShakeProcess(onionName, PURPOSE_FILE);
    }

    /**
     * 发消息给好友
     * @param textMessage
     * @param friendOrion
     * @param context
     */
    public static boolean interface_send_text(String textMessage, String friendOrion,String messageID,Context context)  {// TODO: 2021/8/24 增加消息id
        LogUtils.d(TAG, " 3、interface_send_text:: textMessage = " + textMessage + " friendOrion = " + friendOrion);

        byte[] afterByteArray = textMessage.getBytes(Charset.forName("ISO-8859-1"));
        Log.d(TAG, " interface_send_text:: textMessage转成byte[] afterByteArray = " +  Arrays.toString(afterByteArray));
        Log.d(TAG, " interface_send_text:: textMessage转成byte[] afterByteArray.length = " +  afterByteArray.length);

        int sendSize = 0;//文本转为byte;
        try {
            sendSize = textMessage.getBytes("utf-8").length;
            Log.d(TAG, " interface_send_text:: tsendSize = " +  sendSize);
            //boolean isSend = judgeEnoughXOR(context,sendSize);
//            boolean isExhausted = JniEntryUtils.keyFileIsExhausted();
//
//            if(isExhausted){
//                //Toast.makeText(context, getContext().getText(R.string.key_exhausted), Toast.LENGTH_LONG).show();
//                Log.d(TAG," interface_send_text:: key.bin用尽");
//                return false;// TODO: 2021/10/28 不够长度就不发送
//            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        ServerMessageHandler messageHandler =ServerMessageHandler.getInstance();
        messageHandler.handleTextMessageSend(textMessage,friendOrion, Constant.REMOTE_ONION_PORT,messageID);

        return true;
    }


    public static boolean handleFileMessageSend(String fileFullPath, String remoteOnion,String messageID, Context context)  {// TODO: 2021/8/25 增加消息id //在线发送的文件
        Log.d(TAG, " handleFileMessageSend:: fileFullPath = " + fileFullPath);
        Log.d(TAG, " handleFileMessageSend:: remoteOnion = " + remoteOnion);
        File file = new File(fileFullPath);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);

            int sendSize = fileInputStream.available();//文本转为byte;
            fileInputStream.close();
           // boolean isSend = judgeEnoughXOR(context,sendSize);
//            boolean isExhausted = JniEntryUtils.keyFileIsExhausted();
//
//            if(isExhausted)
//                return false;// TODO: 2021/10/28 不够长度就不发送

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        ServerMessageHandler handler = ServerMessageHandler.getInstance();
        handler.handleFileMessageSend(fileFullPath, remoteOnion, Constant.REMOTE_ONION_PORT,messageID);

        return true;
    }



    public static boolean handleByteMessageSend(String fileFullPath,byte[] bitmapBytes, String remoteOnion,String messageID, Context context ){// TODO: 2021/8/26 增加消息id。在线发送byte图片
        Log.d(TAG, " handleByteMessageSend:: 在线发送图片 fileFullPath = " + fileFullPath);
        Log.d(TAG, " handleByteMessageSend:: 在线发送图片 remoteOnion = " + remoteOnion);
        Log.d(TAG, " handleByteMessageSend:: 在线发送图片 messageID = " + messageID);
        int sendSize = bitmapBytes.length;//文本转为byte;
        Log.d(TAG, " handleByteMessageSend:: 在线发送图片 sendSize = " + sendSize);
       // boolean isSend = judgeEnoughXOR(context,sendSize);
//        boolean isExhausted = JniEntryUtils.keyFileIsExhausted();
//        if(isExhausted)
//            return false;// TODO: 2021/10/28 不够长度就不发送

        ServerMessageHandler handler = ServerMessageHandler.getInstance();
        handler.handleByteMessageSend( fileFullPath,bitmapBytes, remoteOnion, Constant.REMOTE_ONION_PORT,messageID);// TODO: 2021/8/26 增加消息id。

        return true;
    }

}
