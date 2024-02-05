package com.ucas.chat.ui.login;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apaches.commons.codec.DecoderException;
import org.apaches.commons.codec.binary.Hex;
import org.apaches.commons.codec.digest.DigestUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import com.ucas.chat.MyApplication;
import com.ucas.chat.R;
import com.ucas.chat.TorManager;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.bean.KeyInforBean;
import com.ucas.chat.bean.MyInforBean;
import com.ucas.chat.bean.NodeBean;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.db.AddressBookHelper;
import com.ucas.chat.db.ChatContract;
import com.ucas.chat.db.KeyHelper;
import com.ucas.chat.db.KeyHelperTool;
import com.ucas.chat.db.MailListSQLiteHelper;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.db.MyInforTool;
import com.ucas.chat.db.MySelfInfoHelper;
import com.ucas.chat.db.NodeHelper;
import com.ucas.chat.db.ServiceInfoHelper;
import com.ucas.chat.db.ServiceInfoTool;
import com.ucas.chat.eventbus.Event;
import com.ucas.chat.jni.JniEntryUtils;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.tor.util.FileUtil;
import com.ucas.chat.ui.ChangePasswordActivity;
import com.ucas.chat.ui.home.HomeActivity;
import com.ucas.chat.ui.register.RegisterActivity;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.FileUtils;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.PermissionUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;
import com.ucas.chat.utils.ToastUtils;
import java.util.ArrayList;
import java.util.List;

import static com.ucas.chat.MyApplication.getContext;
import static com.ucas.chat.TorManager.stopTor;
import static com.ucas.chat.utils.StringUtils.getProgress;
import static org.torproject.android.service.TorServiceConstants.LOCAL_EXTRA_LOG;

public class LoginActivity extends BaseActivity {
    public static final String TAG = ConstantValue.TAG_CHAT + "LoginActivity";
    private TextView mEdUserName;
    private EditText mEdPassWord;
    private Button mButtConfirm;
    private TextView mTvForgetPassword;
    private TextView mTvRegister;
    private static TextView mTvShowProgress;// TODO: 2021/7/21

    private MailListSQLiteHelper mHelper;
    private SQLiteDatabase myDb;
    private MailListUserNameTool mTool;
    private List<ContactsBean> mMailList;

    //added ###########
    private MySelfInfoHelper mySelfInfoHelper;
    private MyInforBean myInforBean;
    private AddressBookHelper addressBookHelper;

    //guard_node逻辑
    public static int num_restart_tor = 1;
    public static int GET_NODE_ERROR = 2;
    private NodeHelper nodeHelper;

    private String mName;
    private String mPassword;
    private ServiceInfoHelper mServiceHelper;
    private SQLiteDatabase mDatabase;
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
    };
    private static final int PERMISSION_REQUEST_CODE = 100001;

    static UserBean bean;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    String progress = (String) msg.obj;
                    if (mTvShowProgress != null)
                        mTvShowProgress.setText("loading...:" + msg.obj);
                    break;

            }
            ;

        }
    };// TODO: 2021/7/21 更新登录进度

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_login);

        TorManager.stopTor(getContext());

        KeyHelper keyHelper = KeyHelper.getInstance(getContext());
        ArrayList<KeyInforBean> keyList =keyHelper.queryAll();
        LogUtils.d(TAG, " keyList = " + keyList.toString());
        LogUtils.e(TAG," ******************************************************");
        KeyInforBean byKeyNameBean = keyHelper.queryByKeyName(KeyHelperTool.KEY_NAME_ARR[0]);
        LogUtils.d(TAG, " KEY_NAME_ARR[0] = " + KeyHelperTool.KEY_NAME_ARR[0]);
        LogUtils.d(TAG, " byKeyNameBean = " + byKeyNameBean.toString());
        LogUtils.e(TAG," ******************************************************");
        KeyInforBean byKeyNameBean1 = keyHelper.queryByKeyName(KeyHelperTool.KEY_NAME_ARR[1]);
        LogUtils.d(TAG, " KEY_NAME_ARR[1] = " + KeyHelperTool.KEY_NAME_ARR[1]);
        LogUtils.d(TAG, " byKeyNameBean1 = " + byKeyNameBean1.toString());
        LogUtils.e(TAG," ******************************************************");

        initPermission();
        mEdUserName = findViewById(R.id.ed_user_name);
        mEdPassWord = findViewById(R.id.ed_pass_word);
        mTvForgetPassword = findViewById(R.id.tv_forget_password);
        mButtConfirm = findViewById(R.id.butt_confirm);
        mTvRegister = findViewById(R.id.tv_register);
        mTvShowProgress = findViewById(R.id.tv_show_progress);// TODO: 2021/7/21
        mTool = MailListUserNameTool.getInstance();
        mMailList = mTool.initMailList();

        mEdPassWord.setText("123");

        //#######
        mySelfInfoHelper= MySelfInfoHelper.getInstance(getContext());
        myInforBean=mySelfInfoHelper.queryAll();

        LogUtils.d(TAG, " onCreate:: 转换前myInforBean = " + myInforBean.toString());

        String onionName = AesTools.getDecryptContent(myInforBean.getOnionName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " onCreate:: 转换后myInforBean——》onionName = " + onionName);
        Constant.MY_ONION_HOSTNAME = onionName;

        String privateKey = AesTools.getDecryptContent(myInforBean.getPrivateKey(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " onCreate:: 转换后myInforBean——》privateKey = " + privateKey);
        Constant.CLIENT_PRIVATE_KEY = privateKey;// TODO: 2021/7/17 动态加载

        String publicKey = AesTools.getDecryptContent(myInforBean.getPublicKey(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " onCreate:: 转换后myInforBean——》publicKey = " + publicKey);
        Constant.CLIENT_PUBLIC_KEY = publicKey;

        addressBookHelper= AddressBookHelper.getInstance(getContext());
        mServiceHelper = new ServiceInfoHelper(this);
        mServiceHelper.getWritableDatabase();

        int number=0;
        mDatabase = mServiceHelper.getReadableDatabase();
        Cursor c = mDatabase.rawQuery("select * from " + "tablename", null);
        number=c.getCount();
        if (number==0){
            ContentValues values = new ContentValues();
            values.put(ChatContract.ServerInfoEntry.NODE_SERVER_ID, ServiceInfoTool.SERVICE_INFO_NODE[0]);
            values.put(ChatContract.ServerInfoEntry.COMMUNICATION_SERVER_ID, ServiceInfoTool.SERVICE_COMMUNICATION[0]);
            mDatabase = mServiceHelper.getWritableDatabase();
            mDatabase.insert("tablename", null, values);
            Toast.makeText(this, "插入成功", Toast.LENGTH_SHORT).show();
        }

        mDatabase = mServiceHelper.getReadableDatabase();
        LogUtils.d("get:first:",mServiceHelper.getSecond());

        //MailList = [AddressBookBean{nickName='b', gender=1, headImagePath='Temporarily none', remoteOnionName='liqf2ad7xgi4ewixwvk6qxf5bsevaq7qojvfzu74ruwusvc4ullfonyd.onion', remotePublicKey='null', remaks='Temporarily none'}]
        LogUtils.d(TAG, " onCreate:: MailList = " + addressBookHelper.queryAll().toString());
        initClick();
        FileUtils.copy_file_from_sdcard(getContext());
        EventBus.getDefault().register(this);
        FileUtil.createReceiveFileFolder();

     //JniEntryUtils.setFileKeyLocation(1);
    }

    private void initClick() {
        mButtConfirm.setOnClickListener(this);
        mTvForgetPassword.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.tv_forget_password:
                Intent intentPassword = new Intent(LoginActivity.this, ChangePasswordActivity.class);
                startActivityForResult(intentPassword,ConstantValue.LOGIN_TO_CHANGE_PASSWORD);
                break;
            case R.id.tv_register:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.butt_confirm:
                login();
                break;
        }
    }

    private void login(){

        stopTor(getContext());// TODO: 2022/4/13 目前采用先断开的方法解决2次进入登录界面无法登录的问题

        mName = mEdUserName.getText().toString().trim();
        mPassword = mEdPassWord.getText().toString().trim();


        LogUtils.d(TAG, " login:: 输入paaswd = " + mPassword );
        LogUtils.d(TAG, " login:: 转化前数据库myAccount:  " + myInforBean.getAccount());
        LogUtils.d(TAG, " login:: 转化前数据库myPassWord: " + myInforBean.getPassword());

        String decryptPassword = AesTools.getDecryptContent(myInforBean.getPassword(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        mName = AesTools.getDecryptContent(myInforBean.getAccount(), AesTools.AesKeyTypeEnum.COMMON_KEY);

        LogUtils.d(TAG, " login:: 转化后decryptPassword = " + decryptPassword);
        LogUtils.d(TAG, " login:: 转化后decryptAccountName = " + mName);


        if(!mPassword.equals(decryptPassword)){
            ToastUtils.showMessage(getContext(), getString(R.string.tip_login_error));
            return;
        }

        nodeHelper = NodeHelper.getInstance(getContext());
        nodeHelper.getWritableDatabase();
//
        TorManager.startTor(LoginActivity.this);

        Message message = new Message();// TODO: 2021/7/21 加上登录进度  
        message.what = 0;//标记为0，
        message.obj = "0%";
        mHandler.handleMessage(message);

    }


    public static class ProgressReceiver extends BroadcastReceiver{// TODO: 2021/7/21 广播接收
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent){
                Log.d("ProgressReceiver ", " intent is null" );
                return;
            }

            String progress = intent.getStringExtra(LOCAL_EXTRA_LOG);
            if (TextUtils.isEmpty(progress)){
                Log.d("ProgressReceiver ", " progress is null" );
                return;
            }

            if (progress.contains("NOTICE: Bootstrapped")){
                progress = getProgress(progress);
                System.out.println(TAG+"  登录进度  "+progress);
                Message message = new Message();
                message.what = 0;//标记为0，

                message.obj = progress;
                mHandler.handleMessage(message);



            }


        }
    }

    private void saveLoginInfo(){
        LogUtils.d(TAG, " saveLoginInfo::");

        bean = new UserBean();
        LogUtils.d(TAG, " saveLoginInfo:: 转化前myInforBean = " + myInforBean.toString());
        String account = AesTools.getDecryptContent(myInforBean.getAccount(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " saveLoginInfo:: 转化后account: " + account);
        String nickName = AesTools.getDecryptContent(myInforBean.getNickName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " saveLoginInfo:: 转化后nickName: " + nickName);
        String passWord = AesTools.getDecryptContent(myInforBean.getPassword(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " saveLoginInfo:: 转化后passWord: " + passWord);
        String onionName = AesTools.getDecryptContent(myInforBean.getOnionName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " saveLoginInfo:: 转化后onionName: " + onionName);
        String privateKey = AesTools.getDecryptContent(myInforBean.getPrivateKey(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " saveLoginInfo:: 转化后privateKey: " + privateKey);
        String publicKey = AesTools.getDecryptContent(myInforBean.getPublicKey(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " saveLoginInfo:: 转化后publicKey: " + publicKey);


        bean.setImPhoto(0);
        bean.setUserName(account);
        bean.setUserId(nickName);
        bean.setPassword(passWord);
        bean.setOnionName(onionName);
        bean.setPrivateKey(privateKey);
        bean.setPublicKey(publicKey);
        bean.setOnlineStatus("1");// TODO: 2021/10/27
        SharedPreferencesUtil.setUserBeanSharedPreferences(getContext(), bean);
        LogUtils.d(TAG, " saveLoginInfo:: myInforBean转化后数据 写入BeanSharedPreferences");

        ContactsBean tmp =SharedPreferencesUtil.getContactBeanSharedPreferences(getContext());
        if(tmp!=null){
            tmp.setOnlineStatus("0");
            SharedPreferencesUtil.setContactBeanSharedPreferences(getContext(),tmp);
        }
   }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(Event messageEvent){
        String msg = messageEvent.getType();
        String message = messageEvent.getMessage();//收到的消息
        LogUtils.d(TAG, " onMoonEvent:: msg = " + msg);
        switch (msg){
            case Event.GET_NODE:
                if ((message.equals("错误")) && (GET_NODE_ERROR != 0)) {
                    GET_NODE_ERROR--;
                    //String hostname = read_file("hostname").trim();
                    String hostname = FileUtil.readFileFromSdcardChatUser(FilePathUtils.HOSTNAME).trim();
                    LogUtils.d(TAG, " onMoonEvent:: hostname.txt hostname: " + hostname);
//                    String hostnameWithoutOnion = hostname.replace(" ","").replace(".onion","");
                    hostname = AesTools.getDecryptContent(hostname, AesTools.AesKeyTypeEnum.COMMON_KEY);
                    LogUtils.d(TAG, " onMoonEvent:: hostname.txt 转化后hostname: " + hostname);
                    String from =DigestUtils.sha256Hex(hostname.trim());
                    LogUtils.d(TAG, " onMoonEvent:: 我的onion地址hostname: " +  hostname.trim());
                    LogUtils.d(TAG, " onMoonEvent:: from: " +  from);

//                    getNode getNode = new getNode(from,mServiceHelper.getFirst());
//                    getNode.start();
                } else if (!message.equals("错误")) {
                    useGuardNode(message);
                }
                break;
            case Event.TOR_CONNECTED:
                if (num_restart_tor == 1){//这个判断会影响从掩护界面跳转到HomeActivity界面的
                    System.out.println("网络开始");
                    com.ucas.chat.TorManager.interface_start_listen(getContext());
                }
                LogUtils.d(TAG, " onMoonEvent:: mName: " + mName);
                LogUtils.d(TAG, " onMoonEvent:: mPassword: " + mPassword);
                saveLoginInfo();
                mEdPassWord.setText("");
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.putExtra(ConstantValue.LOGIN_INTENT_HOME_NAME_VALUE, mName);
                startActivity(intent);

                finish();


                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (requestCode == 100 && (uri = data.getData()) != null) {
                getContentResolver().takePersistableUriPermission(uri, data.getFlags() & (
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startFor(Activity activity) {
        //sdcard/Android/data
        Uri uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
        DocumentFile documentFile = DocumentFile.fromTreeUri(activity, uri);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        assert documentFile != null;
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile.getUri());
        activity.startActivityForResult(intent, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

        /**
         * 检查，申请权限
         */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean has = PermissionUtils.checkPermissions(this, BASIC_PERMISSIONS);
            if (!has) {
                PermissionUtils.requestPermissions(this, PERMISSION_REQUEST_CODE,
                        BASIC_PERMISSIONS);
            }
        }
        //Build.VERSION_CODES.O ——》Build.VERSION_CODES.M
        startFor(this);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            startFor(this);
//        }
    }
    /**
     * 使用guard节点逻辑
     */
    private void useGuardNode(String message) {
        int length = message.length();
        int Length2 = message.replace("(", "").length();
        int cutLength = length - Length2;
        int fingerPrintNum = 1;
        int timeNum = 3;
        //flag=0时不需要重启tor,flag=1时需要重启tor
        int flag=0;
        NodeHelper nodeHelper = new NodeHelper(getContext());
        for (int i = 1; i <= cutLength; i++) {
            String fingerPrint = message.split(",")[fingerPrintNum].substring(2, 42);
            String time = message.split(",")[timeNum].replace(" ", "").replace(")", "").replace("]","");

            String guardNode = nodeHelper.getFirst();
            if((guardNode != null)&&(guardNode.equals(fingerPrint))){
                break;
            } else if ((guardNode != null) && (!guardNode.equals(fingerPrint)) && (i == 1)) {
                nodeHelper.deleteAll();
            }
            NodeBean nodeBean = new NodeBean(fingerPrint, time);
            nodeHelper.insertData(getContext(), nodeBean);
            System.out.println("fingerPrint：" + fingerPrint);
            System.out.println("time：" + time);
            fingerPrintNum += 4;
            timeNum += 4;
            flag=1;
        }
        System.out.println("查询数据库后获得的所有节点：" + nodeHelper.queryAll());
        if(flag==1){
//            stopTor(getContext());
//            startTor(getContext());
        }
    }
}
