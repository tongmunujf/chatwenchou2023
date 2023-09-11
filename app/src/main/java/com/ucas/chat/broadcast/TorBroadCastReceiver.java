package com.ucas.chat.broadcast;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.ucas.chat.bean.MsgListBean;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.bean.session1.MsgTypeStateNew;
import com.ucas.chat.db.ChatContract;
import com.ucas.chat.db.MailListSQLiteHelper;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.db.news.MsgListSQLiteHelper;
import com.ucas.chat.eventbus.Event;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.RandomUtil;
import com.ucas.chat.utils.SharedPreferencesUtil;
import com.ucas.chat.utils.TimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.torproject.android.service.OrbotServiceAction;

import static com.ucas.chat.MyApplication.getContext;

import java.util.List;

public class TorBroadCastReceiver extends BroadcastReceiver {
    private static final String TAG = ConstantValue.TAG_CHAT + "TorBroadCastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == intent){
            Log.d("TorBroadCastReceiver ", " intent is null" );
            return;
        }
        String Status = intent.getStringExtra(Constant.TOR_BROAD_CAST_INTENT_KEY);
        Log.d("TorBroadCastReceiver ", " Status : " + Status);
        if (TextUtils.isEmpty(Status)){
            Log.d("TorBroadCastReceiver ", " Status is null" );
            return;
        }
        switch (Status) {
            case (OrbotServiceAction.TOR_HAS_CONNECTED): //登陆的时候启动Tor,Tor启动100%会发送广播
                EventBus.getDefault().post(new Event(Event.TOR_CONNECTED, null, null));
                break;
            case (OrbotServiceAction.TOR_CONNECT_READY)://连接对方成功
                EventBus.getDefault().post(new Event(Event.TOR_CONNECT_READY, null, null));
                break;
            case (Constant.SEND_PROTOCOL_FAILURE): //连接对方失败
                EventBus.getDefault().post(new Event(Event.SEND_PROTOCOL_FAILURE, null, null));
                //发送协议失败
                Log.d("TorBroadcastReceiver ", " Status : " + Status);
                break;
            case ( Constant.START_COMMUNICATION_SUCCESS)://接收到此Status，才可以发送消息
                //协议部分完成可以开始通信了
                Log.d("TorBroadcastReceiver ", " Status : " + Status);
                EventBus.getDefault().post(new Event(Event.START_COMMUNICATION_SUCCESS, null, null));
                break;
            case (Constant.PEER_HAS_RECEIVED_MESSAGE): //对方已经接收到自己发送的消息后，回复的ack，即表示自己发送消息成功，
                String ack = intent.getStringExtra("result");
//                Log.d("TorBroadcastReceiver ", " Status : " + Status + " ack = " + AESCrypto.bytesToHex(ack.getBytes()));
                if (!TextUtils.isEmpty(ack)){
                    EventBus.getDefault().post(new Event(Event.PEER_HAS_RECEIVED_MESSAGE_SUCCESS, ack, null));
                }else {
                    EventBus.getDefault().post(new Event(Event.PEER_HAS_RECEIVED_MESSAGE_FAILURE, null, null));
                }
                break;
            case (Constant.HAS_RECEIVED_MESSAGE)://对方回复的消息
                String receiveMessage = intent.getStringExtra("Message");
                String peerHostname = intent.getStringExtra("PeerHostname");// TODO: 2021/8/24  改为peerHostname
                String messageID = intent.getStringExtra("MessageID");// TODO: 2021/8/24 增加消息id 
                
                Log.d("TorBroadcastReceiver ", " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                Log.d("TorBroadcastReceiver ", " Status : " + Status);
                Log.d("TorBroadcastReceiver ", " Message : " + receiveMessage);
//                Log.d("TorBroadcastReceiver ", " peerHostnameHash : " + AESCrypto.bytesToHex(peerHostnameHash.getBytes()));
                Log.d("TorBroadcastReceiver ", " peerHostname : " + peerHostname);
                Log.d("TorBroadcastReceiver ", " ***********************************");
                receiveMessage(receiveMessage, peerHostname,messageID);
                //接收到对方消息

                break;

        }
    }

    /**
     * 接收文本消息
     * @param mess
     */
    private void receiveMessage(String mess, String peerHostname,String messageID ){// TODO: 2021/8/24 动态获取消息id ，改为peerHostname
        LogUtils.d(TAG , " receiveMessage:: peerHostname: " + peerHostname);
        UserBean userBean = SharedPreferencesUtil.getUserBeanSharedPreferences(getContext());
        String selfUserId = userBean.getUserId();
        Log.d(TAG, " receiveMessage:: selfUserId = " + selfUserId);

        MailListSQLiteHelper mailListSQLiteHelper = new MailListSQLiteHelper(getContext());
        List<ContactsBean> contactsBeans = mailListSQLiteHelper.queryAll();// TODO: 2022/3/23 获取全部的好友信息
        LogUtils.d(TAG, " receiveMessage:: 全部好友信息 contactsBeans = " + contactsBeans.toString());

        ContactsBean contactsBean = mailListSQLiteHelper.getContactsBean(peerHostname);// TODO: 2022/3/22 从数据库获取
        LogUtils.d(TAG , " receiveMessage:: contactsBean: " + contactsBean.toString());
        String friendUserId = contactsBean.getUserName();// TODO: 2022/3/22 从数据库获取

        friendUserId = AesTools.getEncryptContent(friendUserId, AesTools.AesKeyTypeEnum.COMMON_KEY);

        Log.d(TAG, " receiveMessage:: 转化后friendUserId = " + friendUserId);

//        String messageID = RandomUtil.randomChar();// TODO: 2021/8/8 更新时间标记，用这个来唯一标记当前次的发送情况

        ContentValues values = new ContentValues();
        MsgListSQLiteHelper helper = MsgListSQLiteHelper.getInstance(getContext());
        helper.getWritableDatabase();
        values.put(ChatContract.MsgListEntry.SEND_TIME, TimeUtils.currentTimeMillis()+"");
        values.put(ChatContract.MsgListEntry.CHAT_TYPE, MsgTypeStateNew.text);
        values.put(ChatContract.MsgListEntry.TEXT_CONTENT, mess);
        values.put(ChatContract.MsgListEntry.FILE_PATH, "");
        values.put(ChatContract.MsgListEntry.FILE_SIZE, "");
        values.put(ChatContract.MsgListEntry.FILE_NAME, "");
        values.put(ChatContract.MsgListEntry.FROM, friendUserId);
        values.put(ChatContract.MsgListEntry.TO, selfUserId);
        values.put(ChatContract.MsgListEntry.IS_ACKED, 1);
        values.put(ChatContract.MsgListEntry.MESSAGE_ID, messageID);

        values.put(ChatContract.MsgListEntry.FRIEND_ORIONID, contactsBean.getOrionId());
        values.put(ChatContract.MsgListEntry.FRIEND_NICKNAME, contactsBean.getNickName());


        helper.insertData(getContext(),values);//存放消息
        Gson gson = new Gson();
        MsgListBean bean = new MsgListBean(mess, friendUserId, selfUserId, 1,messageID,contactsBean.getOrionId(),contactsBean.getNickName());// TODO: 2021/9/24 改 IS_ACKED为1
        String messJson = gson.toJson(bean);
        EventBus.getDefault().post(new Event(Event.HAS_RECEIVED_MESSAGE, messJson, messageID));// TODO: 2021/8/25 将peerHostname改为messageID //推送给p2pactivity
    }
}
