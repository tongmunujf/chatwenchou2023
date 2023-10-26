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
        ServerMessageHandler messageHandler =ServerMessageHandler.getInstance();
//        messageHandler.init(context);
//        orionId=Constant.REMOTE_ONION_NAME;
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
        //InterfaceToAndroid.interface_send_text(textMessage, selfOrion , context);
//        TextMessageHandler textMessageHandler = TextMessageHandler.getInstance();\

        int sendSize = 0;//文本转为byte;
        try {
            sendSize = textMessage.getBytes("utf-8").length;

            boolean isSend = judgeEnoughXOR(context,sendSize);
            if(!isSend)
                return false;// TODO: 2021/10/28 不够长度就不发送

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        ServerMessageHandler messageHandler =ServerMessageHandler.getInstance();
        messageHandler.handleTextMessageSend(textMessage,friendOrion, Constant.REMOTE_ONION_PORT,messageID);

        return true;
    }


    public static boolean handleFileMessageSend(String fileFullPath, String remoteOnion,String messageID, Context context)  {// TODO: 2021/8/25 增加消息id //在线发送的文件

        File file = new File(fileFullPath);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);

            int sendSize = fileInputStream.available();//文本转为byte;
            fileInputStream.close();
            boolean isSend = judgeEnoughXOR(context,sendSize);
            if(!isSend)
                return false;// TODO: 2021/10/28 不够长度就不发送

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

        int sendSize = bitmapBytes.length;//文本转为byte;
        boolean isSend = judgeEnoughXOR(context,sendSize);
        if(!isSend)
            return false;// TODO: 2021/10/28 不够长度就不发送

        ServerMessageHandler handler = ServerMessageHandler.getInstance();
        handler.handleByteMessageSend( fileFullPath,bitmapBytes, remoteOnion, Constant.REMOTE_ONION_PORT,messageID);// TODO: 2021/8/26 增加消息id。

        return true;
    }



    public static boolean judgeEnoughXOR(Context context, int sendSize){// TODO: 2021/10/28 判断发送的长度是否在剩余的xor文件 长度内，够了才发送。不够不发送，并且提示长度不足

        RecordXOR recordXOR = SharedPreferencesUtil.getCommonRecordXOR(context);
        int endXORFileName = recordXOR.getEndFileName();//获取结束指针文件名
        int endXORIndex = recordXOR.getEndFileIndex();//获取结束指针文件指针

        File allXORFolder = new File(XORutil.XOR_PATH);//文件夹，内包含多个拆分的XOR文件
        File[] allXORFiles = allXORFolder.listFiles();//多个拆分的XOR文件

        List< File> allXORFileList = com.ucas.chat.tor.message.Message.sortFile(Arrays.asList(allXORFiles));//文件的顺序有问题的，要按数字大小排
        allXORFileList = Message.delectAllUsedFiles(allXORFileList,endXORFileName);// TODO: 2021/10/4 得到以 endXORFileName开始的allXORFileList

        if (allXORFileList.size()<=0){
//            Toast.makeText(context, R.string.lack_encyptal_file,Toast.LENGTH_SHORT).show();
//            System.out.println(R.string.lack_encyptal_file);
            return false;
        }

        //有Bug,可能有人删除这个文件了

        sendSize = sendSize-endXORIndex;

        if (sendSize<=0){

            return true;//说明可以用这个文件进行xor异或
        }

        int needXORfilesNumber =  sendSize/8388608;

        if (allXORFileList.size()-1>=needXORfilesNumber){

            return true;//够用
        }else {

//            Toast.makeText(context, R.string.lack_encyptal_file,Toast.LENGTH_SHORT).show();
            System.out.println(R.string.lack_encyptal_file);
            return false;


        }




    }





}
