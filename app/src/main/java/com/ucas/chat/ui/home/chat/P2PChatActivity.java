package com.ucas.chat.ui.home.chat;

import static com.ucas.chat.MyApplication.getContext;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.android_play.MainActivity;
import com.google.gson.Gson;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.supets.pet.nativelib.Settings;
import com.ucas.chat.R;
import com.ucas.chat.TorManager;

import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.bean.MsgListBean;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.bean.session1.MsgTypeStateNew;
import com.ucas.chat.db.ChatContract;
import com.ucas.chat.db.ServiceInfoHelper;
import com.ucas.chat.db.news.MsgListSQLiteHelper;
import com.ucas.chat.eventbus.Event;
import com.ucas.chat.jni.ServiceLoaderImpl;
import com.ucas.chat.jni.common.IDecry;
import com.ucas.chat.jni.common.IEntry;
import com.ucas.chat.tor.server.ServerMessageHandler;
import com.ucas.chat.ui.camera.MyCameraActivity;
import com.ucas.chat.ui.camera.adapter.DataPictureActivity;
import com.ucas.chat.ui.home.InterfaceOffline.getOfflineFile;
import com.ucas.chat.ui.home.InterfaceOffline.getOfflineList;
import com.ucas.chat.ui.home.InterfaceOffline.getOfflinePic;
import com.ucas.chat.ui.home.InterfaceOffline.getOfflineText;
import com.ucas.chat.ui.home.InterfaceOffline.sendOfflineFile;
import com.ucas.chat.ui.home.InterfaceOffline.sendOfflinePic2;
import com.ucas.chat.ui.home.InterfaceOffline.sendOfflineText;
import com.ucas.chat.ui.home.InterfaceOffline.sendSentMessage;
import com.ucas.chat.ui.view.ChatUiHelper;
import com.ucas.chat.ui.view.RecordButton;
import com.ucas.chat.ui.view.SounchTouchView;
import com.ucas.chat.ui.view.StateButton;
import com.ucas.chat.ui.view.chat.AudioPlayHandler;
import com.ucas.chat.ui.view.voice.TimeDateUtils;
import com.ucas.chat.ui.view.voice.dialog.MYAudio;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.FileUtils;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.PictureFileUtil;
import com.ucas.chat.utils.RandomUtil;
import com.ucas.chat.utils.SharedPreferencesUtil;
import com.ucas.chat.utils.TextUtils;
import com.ucas.chat.utils.TimeUtils;
import com.ucas.chat.utils.ToastUtils;
import com.zlylib.fileselectorlib.FileSelector;
import com.zlylib.fileselectorlib.utils.Const;

import org.apaches.commons.codec.digest.DigestUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class P2PChatActivity extends BaseActivity implements RecordButton.OnRecordListener {

    private static String TAG = ConstantValue.TAG_CHAT + "P2PChatActivity";
    private static final String NEWCAMERADATA ="new_camera_data";//重新进行拍照的新照片
    private static final String OLDCAMERADATA ="old_camera_data";//选择已有的旧照片
    public static final int REQUEST_CODE_IMAGE = 0001;
    public static final int REQUEST_CODE_VEDIO = 0002;
    public static final int REQUEST_CODE_FILE = 0003;
    private final int REQUEST_CODE = 100;

    private ImageView mImBack;
    private TextView mTvNickName;
    private TextView mTvOnLineState;
    private ListView msg_listview;
    private LinearLayout mLlContent;
    private StateButton mBtnSend;//发送按钮
    private EditText mEtContent;
    private RelativeLayout mRlBottomLayout;
    private LinearLayout mLlAdd;//添加布局
    private LinearLayout mLlVoice;//录音布局
    private SounchTouchView mTransferAudio;//变音布局
    private ImageView mIvAdd;
    private ImageView mIvAudio;
    private RecordButton mRecordButton;
    private TextView mAudioTime;
    private View mLeftAudioAnim;
    private View mRightAudioAnim;

    private RelativeLayout mRlPhoto;
    private RelativeLayout mRlVideo;
    private RelativeLayout mRlCamera;

    private List<MsgListBean> mMsgList;
    private MessageListAdapter mAdapter;

    private ChatUiHelper mUiHelper;
    private MYAudio audio;
    private int voiceType = 0;//是否变声了

    private String mPlayId = "";
    private boolean isAudioPlay = false;
    private AudioPlayHandler mAudioPlayHandler;

    private ContactsBean mContactsBean;
    private String mFriendUserId;
    private String mSelfUserId;

    private UserBean mUserBean;
    private MsgListSQLiteHelper mHelper;

    private JSONObject dataJsonAll ;

    private String file_name = "name_file";
    private String pic_name = "name_pic";

    private SQLiteDatabase mDatabase;
    private ServiceInfoHelper mServiceHelper;
    private int  count = 0;
    private String filepath="/sdcard/Android/data/com.ucas.chat/files/";// TODO: 2021/8/23 安卓11不给用/mnt/sdcard/Android/data，提示没读取权限！改成这个可以了
    private String messageSet = "ran";

    private static Context mContext;

    private static final String jniIv = "++++";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, " onCreate::");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_p2p_chat);

        mContext= getApplicationContext();
        mImBack = findViewById(R.id.imBack);
        mTvNickName = findViewById(R.id.tv_nick_name);
        mTvOnLineState = findViewById(R.id.tv_if_on_line_state);
        msg_listview = findViewById(R.id.msg_listview);
        mLlContent = findViewById(R.id.llContent);
        mBtnSend = findViewById(R.id.btn_send);
        mEtContent = findViewById(R.id.et_chat_message);
        mRlBottomLayout = findViewById(R.id.bottom_layout);
        mLlAdd = findViewById(R.id.llAdd);
        mLlVoice = findViewById(R.id.llVoice);
        mTransferAudio = findViewById(R.id.transferAudioLayout);
        mIvAdd = findViewById(R.id.iv_more);
        mIvAudio = findViewById(R.id.iv_input_type);
        mRecordButton = findViewById(R.id.record);
        mAudioTime = findViewById(R.id.time);
        mLeftAudioAnim = findViewById(R.id.leftAnim);
        mRightAudioAnim = findViewById(R.id.rightAnim);

        mRlPhoto = findViewById(R.id.rlPhoto);
        mRlVideo = findViewById(R.id.rlVideo);
        mRlCamera = findViewById(R.id.rlCamera);

        initData();
        initContent();
        initListener();
        EventBus.getDefault().register(this);
    }

    private void initMessageHandler(){
        String orionId = mContactsBean.getOrionId();
        LogUtils.d(TAG, " initMessageHandler:: 转化后orionId = " + orionId);
        TorManager.initMessageHandler(this, orionId);//进行连接

    }

    private void initData() {

        Intent intent = getIntent();
        if (intent != null) {
            ContactsBean tmp2 = (ContactsBean) intent.getSerializableExtra(ConstantValue.INTENT_CONTACTS_BEAN);

            mContactsBean=tmp2;// TODO: 2022/3/24 直接改为这个了

            LogUtils.d(TAG, "initData::  mContactsBean = " + mContactsBean.toString() );
            String onlineStatus = mContactsBean.getOnlineStatus();
            if(onlineStatus.equals("1")){
                mTvNickName.setTextColor(getColor(R.color.blue4));
                mTvOnLineState.setText(R.string.on_line);
            }else{
                mTvNickName.setTextColor(getColor(R.color.green3));
                mTvOnLineState.setText(R.string.off_line);
            }

            mFriendUserId = mContactsBean.getNickName();
            String nickName = mContactsBean.getNickName();
            //mFriendUserId: 袁绍
            LogUtils.d(TAG, " initData:: 转化后mFriendUserId: " + mFriendUserId);
            //nickName: 袁绍
            LogUtils.d(TAG, " initData:: 转化后nickName: " + nickName);
            if (!nickName.equals("-1")){
                mTvNickName.setText(nickName);
            }else {
                mTvNickName.setText(mContactsBean.getUserName());
            }
            initMessageHandler();

        } else {
            ToastUtils.showMessage(this, "对象账号异常");
            finish();
            return;
        }
        mUserBean = SharedPreferencesUtil.getUserBeanSharedPreferences(getContext());
        mSelfUserId = mUserBean.getUserId();
        //UserBean{userId='asdf', userName='asdf', password='asdf', imPhoto=0}
        LogUtils.d(TAG, " initData:: 转化后mUserBean = " + mUserBean);
        mHelper = MsgListSQLiteHelper.getInstance(getContext());
        //SelfUserId = asdf  FriendUserId = 袁绍
        LogUtils.d(TAG, " initData:: 转换后SelfUserId = " + mSelfUserId + "  FriendUserId = " + mFriendUserId);
        mHelper = MsgListSQLiteHelper.getInstance(getContext());

        UserBean bean= SharedPreferencesUtil.getUserBeanSharedPreferences(P2PChatActivity.this);

        //userOnionName = xqmierlcofd7e3twppdg2mep6ulboehpp5sehr5vbnlp7wwdspakh2yd.onion  onionName = xqmierlcofd7e3twppdg2mep6ulboehpp5sehr5vbnlp7wwdspakh2yd.onion
        LogUtils.d(TAG, " initData:: 转换后userOnionName = " + mUserBean.getOnionName() + "  转化后onionName = " + bean.getOnionName());
        LogUtils.d(TAG, " initData:: UserBean = " + bean.toString());

        String hostname = bean.getOnionName();
        LogUtils.d(TAG, " initData:: 转化后hostname = " + hostname);
        String from =DigestUtils.sha256Hex(hostname.trim());      //################get offline message process


        //2021714 更新
        mServiceHelper = new ServiceInfoHelper(this);
        mDatabase = mServiceHelper.getReadableDatabase();
        LogUtils.d(TAG, " initData:: codeOfflineServer: " + mServiceHelper.getSecond());

        String secondStr = AesTools.getDecryptContent(mServiceHelper.getSecond(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " initData:: decOfflineServer: " + secondStr);

        getOfflineList getOfflineList =new getOfflineList(from, secondStr);
        getOfflineList.start();
        getOfflineText getOfflineText = new getOfflineText(from, secondStr);//获取离线消息
        getOfflineText.start();

    }

    private void initListener() {
        mBtnSend.setOnClickListener(this);
        mImBack.setOnClickListener(this);
        mRlPhoto.setOnClickListener(this);
        mRlVideo.setOnClickListener(this);

        mRlCamera.setOnClickListener(this);

        findViewById(R.id.rlFile).setOnClickListener(this);
        mRecordButton.setOnRecordListener(this);

        mTransferAudio.setListener(new SounchTouchView.OnSoundTouchListener() {// TODO: 2022/3/29 变声的接口
            @Override
            public void onConfirm(int type, int length) {
                voiceType = type;
                audio = new MYAudio(voiceType == 0 ? Settings.recordingOriginPath : Settings.recordingVoicePath, length);
               // sendMessage(mChatHandler.createAudioMessage(audio.audio_url, audio.length));

                Log.i("变声",audio.audio_url);

                String messageID = RandomUtil.randomChar();// TODO: 2021/8/8 更新时间标记，用这个来唯一标记当前次的发送情况

                String onlineStatus=mContactsBean.getOnlineStatus();


                if(onlineStatus.equals("1")){//在线发送
                    new Thread(){

                        @Override
                        public void run() {
                            boolean success =TorManager.handleFileMessageSend(audio.audio_url,mContactsBean.getOrionId(),messageID,getApplication());// TODO: 2021/8/25 增加消息id

                            if (!success){// TODO: 2021/10/29  //发送不够异或材料

                                Message message =Message.obtain();
                                message.what = 0;
                                handler.handleMessage(message);

                            }

                        }
                    }.start(); // TODO: 2021/9/1 采用线程的方式解决拆解加密带来的时间消耗导致界面卡屏问题

                }else{
                    String from = DigestUtils.sha256Hex(mUserBean.getOnionName());
                    String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());

                    String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
                    Log.d(TAG, " initListener:: decOfflineServer = " + decOfflineServer);

                    sendOfflineFile sendOfflineFile = new sendOfflineFile(from,to,"file",audio.audio_url,decOfflineServer,messageID);//mp3、wav可以发
                    sendOfflineFile.start();

                }

                sendFile(audio.audio_url,messageID);



            }

            @Override
            public void onCancel(int type) {
                mUiHelper.hideTransferAudioLayout();
                mUiHelper.hideBottomLayout(false);
            }
        });

    }

    private void initContent() {

        mMsgList = mHelper.queryFriendChatRecord(mUserBean.getUserId(), mContactsBean.getOrionId());// TODO: 2022/3/23 mContactsBean.getUserId() 改为getOrionId，因为getOrionId是唯一的 //从数据库读取历史聊天

        Log.d(TAG,  " initContent:: mMsgList = " + mMsgList.toString());
        mAdapter = new MessageListAdapter(getContext(), mMsgList, mContactsBean);//先将历史记录显示
        msg_listview.setAdapter(mAdapter);
        initChatUi();
        msg_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MsgListBean msgListBean = mMsgList.get(position);
                Log.i(TAG, " initContent:: msgListBeangetMsgType"+msgListBean.getMsgType()+"");

                switch (msgListBean.getMsgType()) {
                    case MsgTypeStateNew.file:

                        String last_third = msgListBean.getFilePath().substring(msgListBean.getFilePath().length()-3,msgListBean.getFilePath().length());
                        LogUtils.d("last_third", last_third);
                        if (last_third.equals("png")||last_third.equals("peg")||last_third.equals("jpj")||last_third.equals("ico")||last_third.equals("jpg")) {

                            Intent intent = new Intent(P2PChatActivity.this,PreviewPicturesActivity.class);
                            intent.putExtra("picturepath",msgListBean.getFilePath());
                            startActivity(intent);

                        }else if(last_third.equals("mp3")||last_third.equals("wav")){

                            MYAudio myAudio = new MYAudio(msgListBean.getFilePath(),msgListBean.getFilePath().length());
                            myAudio.startSound();

                        }


                        break;

//                    case image:
//                        showAttachOnActivity(P2PChatActivity.this, ShowImageActivity.class, message);
//                        break;
//                    case audio:
//                        playAudio(holder, message);
//                        break;
//                    case video:
//                        showAttachOnActivity(P2PChatActivity.this, ShowVideoActivity.class, message);
//                        break;
                }
            }
        });
    }

    private void initChatUi() {
        //mBtnAudio
        mUiHelper= ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mLlContent)
                 .bindttToSendButton(mBtnSend)
                 .bindEditText(mEtContent)
                 .bindBottomLayout(mRlBottomLayout)
                 .bindAddLayout(mLlAdd)
                 .bindToAddButton(mIvAdd)
                 .bindAudioLayout(mLlVoice)
                 .bindAudioIv(mIvAudio)
                 .bindTransferAudioLayout(mTransferAudio);
        //底部布局弹出,聊天列表上滑
        msg_listview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    msg_listview.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.getCount() > 0) {
                                //msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
                            }
                        }
                    });
                }
            }
        });
       // 点击空白区域关闭键盘
        msg_listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUiHelper.hideBottomLayout(false);
                mUiHelper.hideSoftInput();
                return false;
            }
        });
    }


    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case 0:
//                    Toast.makeText(mContext, R.string.lack_encyptal_file,Toast.LENGTH_SHORT).show();//提示异或密钥不足
                    Looper.prepare();
                    Toast.makeText(P2PChatActivity.this, R.string.lack_encyptal_file, Toast.LENGTH_SHORT).show();
                    Looper.loop();


                    System.out.println(R.string.lack_encyptal_file);

                    break;

            }

        }
    };

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.imBack:
                finish();
                break;
            case R.id.btn_send://发送文本
                String textMessage = mEtContent.getText().toString();
                messageSet = textMessage;
//                UserBean bean= SharedPreferencesUtil.getUserBeanSharedPreferences(P2PChatActivity.this);
//                String from = DigestUtils.sha256Hex(mUserBean.getOnionName()); //M
//                String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());
                String onlineStatus=mContactsBean.getOnlineStatus();

                String messageID = RandomUtil.randomChar(); // TODO: 2021/8/8 更新时间标记，用这个来唯一标记当前次的发送情况

                if(onlineStatus.equals("1")){
                    sendTextMessage(0, messageID );
//                    String remoteOnion = Constant.REMOTE_ONION_NAME;
                    new Thread(){// TODO: 2021/9/23 采用分片小xor后耗时操作，增加线程

                        @Override
                        public void run() {

                            Log.d(TAG, " onClick:: btn_send 发送在线文本消息 jni加密前 textMessage = " + textMessage);
                            boolean success = TorManager.interface_send_text(textMessage, mContactsBean.getOrionId(),messageID,getContext());// TODO: 2021/8/24 增加消息id

                            if (!success){// TODO: 2021/10/29  //发送不够异或材料

                                Message message =Message.obtain();
                                message.what = 0;
                                handler.handleMessage(message);

                            }


                        }
                    }.start();

                }else{
                    sendTextMessage(1, messageID );

                    LogUtils.d(TAG, " onClick:: send_offline_text messageID = " + messageID);

                    String hostname = mUserBean.getOnionName();
                    LogUtils.d(TAG, " onClick:: send_offline_text hostname = " + hostname.trim());
//
                    String from =DigestUtils.sha256Hex(hostname.trim());
                    LogUtils.d(TAG, " onClick:: send_offline_text from = " + from);

                    LogUtils.d(TAG, " onClick:: send_offline_text to orionId = " + mContactsBean.getOrionId());
                    String to =DigestUtils.sha256Hex(mContactsBean.getOrionId());
                    LogUtils.d(TAG, " onClick:: send_offline_text to = " + to);


                    LogUtils.d(TAG, " onClick:: send_offline_text from = " + mUserBean.getOnionName() + " to =" + mContactsBean.getOrionId() );
                    LogUtils.d(TAG, " onClick:: send_offline_text textMessage = " + textMessage);

                    String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
                    Log.d(TAG, " onClick:: send_offline_text decOfflineServer = " + decOfflineServer);

                    sendOfflineText sendOfflineText = new sendOfflineText(to, from, textMessage, decOfflineServer, messageID);
                    sendOfflineText.start();
                }

                break;
            case R.id.rlPhoto://选择图片
//                PictureFileUtil.openGalleryPic(P2PChatActivity.this, REQUEST_CODE_IMAGE);//手机内置相册
//                Intent intent = new Intent(P2PChatActivity.this, MyCameraActivity.class);// TODO: 2021/8/26 进入拍照主界面
//                startActivityForResult(intent,REQUEST_CODE_IMAGE);

                Intent intent = new Intent(P2PChatActivity.this, DataPictureActivity.class);
                intent.putExtra("flag", 3);
//                intent.putExtra("data", data);
                startActivityForResult(intent, REQUEST_CODE);// TODO: 2022/3/28 修改成 只选择图片

                break;


            case R.id.rlCamera:// TODO: 2022/3/28  修改成拍照
//                PictureFileUtil.openGalleryPic(P2PChatActivity.this, REQUEST_CODE_IMAGE);//手机内置相册
                Intent intent2 = new Intent(P2PChatActivity.this, MyCameraActivity.class);// TODO: 2021/8/26 进入拍照主界面
                startActivityForResult(intent2,REQUEST_CODE_IMAGE);
                break;


            case R.id.rlVideo:
                PictureFileUtil.openGalleryAudio(P2PChatActivity.this, REQUEST_CODE_VEDIO);
                break;
            case R.id.rlFile:
                openFileSelector(3);
                break;
            case R.id.record:

                break;
        }
    }

    @SuppressLint("LongLogTag")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(Event messageEvent) throws JSONException, InterruptedException {
        String type = messageEvent.getType();
        String message = messageEvent.getMessage();//收到的消息
        String peerHostname = messageEvent.getPeerHostname(); //对在线消息来说，这里得到的是消息id，对离线消息来说，这里是消息id+速度
        LogUtils.d(TAG, " onMoonEvent:: type = " + type + " message = " + message + " peerHostname = " + peerHostname);
        switch (type){
            case Event.SEND_OFFLINE_FILE://本机发送离线文件
                sendOffLineFile(message, peerHostname, type);
                break;
            case Event.SEND_OFFLINE_TEXT://本机发送离线文本
                 sendOffLineText(message, peerHostname);
            case Event.SEND_OFFLINE_PIC://本机发送离线图片
                sendOffLinePic(peerHostname);
                break;
            case Event.GET_OFFLINE_LIST:
                getOffLineList(message);
                break;
            case Event.START_COMMUNICATION_SUCCESS://接收到此Status，才可以以在线方式发送消息
                startCommunicationSuccess();
                break;
            case Event.SEND_PROTOCOL_FAILURE:
                sendProtocolFailure();
                break;
            case Event.PEER_HAS_RECEIVED_MESSAGE_SUCCESS:
                LogUtils.d(TAG," onMoonEvent:: PEER_HAS_RECEIVED_MESSAGE_SUCCESS 更新发送文本的状态");
                String ack = messageEvent.getMessage();
                ackMatch(ack);//更新发送文本的状态
                break;
            case Event.PEER_HAS_RECEIVED_MESSAGE_FAILURE:
                LogUtils.d(TAG," onMoonEvent:: PEER_HAS_RECEIVED_MESSAGE_FAILURE 对方接受到消息失败");
                ToastUtils.showMessage(getContext(), "对方接受到消息失败");
                LogUtils.d(TAG, " onMoonEvent:: 对方接受到消息失败" );
                break;
            case Event.GET_OFFLINE_TEXT://出错//本机收到离线文本
                getOffLineText(message);
                break;
             case Event.GET_OFFLINE_FILE://本机收到离线文件
                 getOffLineFile(message, peerHostname);
                break;
            case Event.GET_OFFLINE_PIC://本机收到离线图片
                getOffLinePic(peerHostname);
                break;
            case Event.HAS_RECEIVED_MESSAGE://对方发来的文本消息
                hasReceivedMessage(message);
                break;
             case Event.RECIEVE_ONLINE_FILE://收到文件的第一步，准备好环境，但未正式接收
                 receiveOffLineFile(message, peerHostname);
                break;
            case Event.FILE_MESSAGE:// TODO: 2021/8/13  接收文件的每一个分片，问题在于怎么和上面的messageID7或下面的messageID8绑定(已解决)，然后用来更新文件进度等
                fileMessage(message, peerHostname);
                break;
            case Event.CREATE_CONNECTION_SUCCESS:
                createConnectionSuccess(message, peerHostname);
                break;
        }
    }

    public void sendOffLineFile(String message, String peerHostname, String type){
        Log.d(TAG, " sendOffLineFile message = " + message );
        Log.d(TAG, " sendOffLineFile peerHostname = " + peerHostname );
        Log.d(TAG, " sendOffLineFile type = " + type );
        String messageID = peerHostname.split(",")[0];//该文件的唯一标记
        String speedOffline = peerHostname.split(",")[1];

        if (message.equals("错误")&&count!=2){
            String from = DigestUtils.sha256Hex(mUserBean.getOnionName()); //M
            String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());

            String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
            Log.d(TAG, " sendOffLineFile:: decOfflineServer = " + decOfflineServer);

            sendOfflineFile sendOfflineFile = new sendOfflineFile(from,to,type,filepath,decOfflineServer,messageID);// TODO: 2021/8/7 为什么加ran.txt 删除了？
            sendOfflineFile.start();// TODO: 2021/8/7  //有时候对方会收到2个重复文件，会不会是这里重复发的问题
            count++;
        }else{
            System.out.println(" sendOffLineFile:: 我该显示100%了,"+count);
            //为了获取离线文件名和文件传输速率对peerHostname做了修改

            System.out.println(" sendOffLineFile:: messageID: " + messageID);
            System.out.println(" sendOffLineFile:: speedOffline: " + speedOffline);
            MsgListBean fileBean1 = null;
            int updatePostion = -1;
            System.out.println(" sendOffLineFile:: mMsgList!!!!!!" + mMsgList.toString());
            for (int i = 0; i < mMsgList.size(); i++) {
                fileBean1 = mMsgList.get(i);
                if ((fileBean1.getMessageID() != null) && (fileBean1.getMessageID().equals(messageID)) && (fileBean1.getFileProgress() != 100)) {
                    fileBean1.setFileProgress(100);
                    fileBean1.setSpeed(speedOffline);
                    updatePostion = i;
                    break;
                }
            }
            System.out.println(" sendOffLineFile:: fileBean1:"+fileBean1);
            //文件进度和传输速度
            MyAsyncTask asyncTask1 = new MyAsyncTask(mAdapter.getViewList(), updatePostion, true);
            asyncTask1.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, fileBean1);
            mHelper.updateFileProgress2(messageID, 100);// TODO: 2021/8/10 替换filename为messageID 更新数据库
        }
    }

    public void sendOffLineText(String message, String peerHostname){
        Log.d(TAG, " sendOffLineText:: message = " + message);
        Log.d(TAG, " sendOffLineText:: peerHostname = " + peerHostname);
        String messageID4 = peerHostname;//该消息的标签
        if (message.equals("错误")&&count!=2){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String textMessage = mEtContent.getText().toString();
            String from = DigestUtils.sha256Hex(mUserBean.getOnionName()); //M
//                    String remoteOnion = Constant.REMOTE_ONION_NAME;
            String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());
            LogUtils.d(TAG, " sendOffLineText:: from = " + mUserBean.getOnionName() + " to =" + mContactsBean.getOrionId() + " text = " + textMessage);

            String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
            Log.d(TAG, " sendOffLineText:: decOfflineServer = " + decOfflineServer);
            sendOfflineText sendOfflineText = new sendOfflineText(from,to,messageSet, decOfflineServer,messageID4);
            sendOfflineText.start();// TODO: 2021/8/10 这里可能导致发2次消息
            count++;
        }else{
//                    System.out.println("我该变颜色了");
            ackMatchOffline("1",messageID4);
        }
    }

    public void sendOffLinePic(String peerHostname){
        System.out.println(" sendOffLinePic:: 我该显示100%了,"+count);
        //为了获取离线文件名和文件传输速率对peerHostname做了修改
        String messageID2 = peerHostname.split(",")[0];// TODO: 2021/8/9 得到该发送文件的标记
        String speedOffline2 = peerHostname.split(",")[1];
        System.out.println(" sendOffLinePic:: messageID: " + messageID2);
        System.out.println(" sendOffLinePic:: speedOffline: " + speedOffline2);
        MsgListBean fileBean1 = null;
        int updatePostion = -1;
        System.out.println(" sendOffLinePic:: mMsgList!!!!!!" + mMsgList.toString());
        for (int i = 0; i < mMsgList.size(); i++) {
            fileBean1 = mMsgList.get(i);
            if ((fileBean1.getMessageID() != null) && (fileBean1.getMessageID().equals(messageID2)) && (fileBean1.getFileProgress() != 100)) {
                fileBean1.setFileProgress(100);
                fileBean1.setSpeed(speedOffline2);
                updatePostion = i;
                break;
            }
        }
        System.out.println(" sendOffLinePic:: fileBean1:"+fileBean1);
        //文件进度和传输速度
        MyAsyncTask asyncTask1 = new MyAsyncTask(mAdapter.getViewList(), updatePostion, true);
        asyncTask1.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, fileBean1);

        String offlineMessageID = peerHostname.split(",")[0];//该文件的唯一标记

        mHelper.updateFileProgress2(offlineMessageID, 100);// TODO: 2021/8/10 替换filename为messageID 更新数据库
    }

    public void getOffLineList(String message) throws JSONException {
        if(message==null)
            return;
        LogUtils.d(TAG + " getOffLineList:: ","bus");
        LogUtils.d(TAG, " getOffLineList:: message = " + message);
        dataJsonAll = new JSONObject(message);
        LogUtils.d(TAG," onMoonEvent:: GET_OFFLINE_LIST dataJsonMessage = " + dataJsonAll);
        JSONObject json = dataJsonAll.getJSONObject("file");
        LogUtils.d(TAG," getOffLineList:: dataJsonFile = " + json);
        JSONArray dataFileAll = json.getJSONArray("messages");
        LogUtils.d(TAG," getOffLineList:: dataJsonFileMessage = " + dataFileAll);
        for (int i =0;i<dataFileAll.length();i++){
            JSONObject infoFile = dataFileAll.getJSONObject(i);
            String Id = infoFile.getString("id");

            String name = infoFile.getString("abs");
            file_name = name;
            UserBean bean= SharedPreferencesUtil.getUserBeanSharedPreferences(P2PChatActivity.this);
//                        String from = DigestUtils.sha256Hex(MailListUserNameTool.getOrionId(P2PChatActivity.this,bean.getUserName()));
            String from = DigestUtils.sha256Hex(mUserBean.getOnionName()); //M
            LogUtils.d(TAG,"  getOffLineList:: 发文件的from?????" + from);

            String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
            Log.d(TAG, "  getOffLineList:: decOfflineServer = " + decOfflineServer);

            getOfflineFile getOfflineFile = new getOfflineFile(from,Id,"/sdcard/Android/data/com.ucas.chat/files/" + infoFile.getString("abs"),decOfflineServer,name);// TODO: 2021/8/23 安卓11不给用/mnt/sdcard/Android/data，提示没读取权限！改成这个可以了
            getOfflineFile.start();
            String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());

            sendSentMessage sendSentMessage = new sendSentMessage(to, from, Id,name,decOfflineServer);
            sendSentMessage.start();
        }
        dataJsonAll = new JSONObject(message);
        LogUtils.d(TAG," getOffLineList:: dataJsonAllPic: " + dataJsonAll);
        JSONObject jsonPic = dataJsonAll.getJSONObject("pic");
        LogUtils.d(TAG," getOffLineList:: jsonPic: " + jsonPic);
        JSONArray dataPicAll = jsonPic.getJSONArray("messages");
        LogUtils.d(TAG," getOffLineList:: dataPicAll: " + dataPicAll);
        for (int i = 0; i<dataPicAll.length();i++){
            JSONObject infoPic = dataPicAll.getJSONObject(i);
            String Id = infoPic.getString("id");
            String name = infoPic.getString("abs");
            pic_name = name;

            UserBean bean= SharedPreferencesUtil.getUserBeanSharedPreferences(P2PChatActivity.this);
            String from = DigestUtils.sha256Hex(mUserBean.getOnionName()); //M
            String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());

            String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
            Log.d(TAG, " getOffLineList:: decOfflineServer = " + decOfflineServer);

            getOfflinePic getOfflinePic = new getOfflinePic(from,Id,"/sdcard/Android/data/com.ucas.chat/files/"+name,decOfflineServer,name);// TODO: 2021/8/23 安卓11不给用/mnt/sdcard/Android/data/XOR，提示没读取权限！改成这个可以了
            getOfflinePic.start();
            LogUtils.d(TAG," getOffLineList:: from:!!!!!!!!!!!!!!!!!!" + from);
            LogUtils.d(TAG," getOffLineList:: to:!!!!!!!!!!!!!" + to);
            sendSentMessage sendSentMessage = new sendSentMessage(to, from, Id,name,decOfflineServer);
            sendSentMessage.start();
        }
        LogUtils.d(TAG, " getOffLineList:: test!!!DataJsonAll: " + dataJsonAll );
    }

    public void startCommunicationSuccess(){
        LogUtils.d(TAG," startCommunicationSuccess:: 接收到此Status，才可以以在线方式发送消息");
        mTvNickName.setTextColor(getColor(R.color.blue4));
        mContactsBean.setOnlineStatus("1");//连接状态置1
        SharedPreferencesUtil.setContactBeanSharedPreferences(getContext(), mContactsBean);
        mTvOnLineState.setText(R.string.on_line);
        mAdapter.refreshAllHeadPicture();// TODO: 2021/8/27  //更新全部左边消息头像为在线头像
    }

    public void sendProtocolFailure(){
        LogUtils.d(TAG," sendProtocolFailure::对方离线");
        mTvNickName.setTextColor(getColor(R.color.gray12));
        mContactsBean.setOnlineStatus("0");
        SharedPreferencesUtil.setContactBeanSharedPreferences(getContext(), mContactsBean);
        mTvOnLineState.setText(R.string.off_line);
        ToastUtils.showMessage(getContext(), getString(R.string.protocol_failure));

        mAdapter.refreshAllHeadPicture();// TODO: 2021/8/27  //更新全部左边消息头像为离线头像

    }

    public void getOffLineText(String message) throws JSONException {
        if(message==null)
            return;

        JSONObject dataJson = new JSONObject(message);//https://www.dazhuanlan.com/szm897394125/topics/1335731
        LogUtils.d(TAG, " getOffLineText:: dataJson : " + dataJson );
        JSONArray data = dataJson.getJSONArray("messages");
        LogUtils.d(TAG, " getOffLineText:: data : " + data );
        for (int i =0;i<data.length();i++){
            JSONObject info = data.getJSONObject(i);
            String message_content = info.getString("message_content");
            String time = info.getString("time");
            String messageID3 = info.getString("message_id");//该消息的唯一标签
            ContentValues values = new ContentValues();
            values.put(ChatContract.MsgListEntry.SEND_TIME, time+"");
            values.put(ChatContract.MsgListEntry.CHAT_TYPE, MsgTypeStateNew.text);
            values.put(ChatContract.MsgListEntry.TEXT_CONTENT, message_content);
            values.put(ChatContract.MsgListEntry.FROM, mContactsBean.getUserId());
            values.put(ChatContract.MsgListEntry.TO, mUserBean.getUserId());
            values.put(ChatContract.MsgListEntry.IS_ACKED, 1);
            values.put(ChatContract.MsgListEntry.MESSAGE_ID, messageID3);//改消息标记
            String from = DigestUtils.sha256Hex(mUserBean.getOnionName()); //M
            String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());

            String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
            Log.d(TAG, " getOffLineText:: decOfflineServer = " + decOfflineServer);

            sendSentMessage sendSentMessage = new sendSentMessage(to, from, messageID3,message_content,decOfflineServer);
            sendSentMessage.start();
            mHelper.insertData(getContext(),values);
            MsgListBean bean1 = new MsgListBean(message_content,mContactsBean.getUserId() ,mUserBean.getUserId() , 1,messageID3,mContactsBean.getOrionId(),mContactsBean.getNickName());
            mMsgList.add(bean1);
            mAdapter.notifyDataSetChanged();
            msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
            LogUtils.d("接收","消息");
            Log.d(TAG, " getOffLineText:: 接收 消息" );
        }
        notifyAdapter();
    }

    public void getOffLineFile(String message, String peerHostname){
        String messageID5 = RandomUtil.randomChar();// TODO: 2021/8/8 更新文件标记，用这个来唯一标记当前次的发送情况
        Log.d(TAG, " getOffLineFile:: " );
        ContentValues values = new ContentValues();
        values.put(ChatContract.MsgListEntry.SEND_TIME, TimeUtils.currentTimeMillis()+"");
        values.put(ChatContract.MsgListEntry.CHAT_TYPE, MsgTypeStateNew.file);
        values.put(ChatContract.MsgListEntry.TEXT_CONTENT, getString(R.string.content_file));
        String pathFile = "/sdcard/Android/data/com.ucas.chat/files/" + peerHostname;// TODO: 2021/8/23 安卓11不给用/mnt/sdcard/Android/data/XOR，提示没读取权限！改成这个可以了
        values.put(ChatContract.MsgListEntry.FILE_PATH, pathFile);
        values.put(ChatContract.MsgListEntry.FILE_NAME, message);
        values.put(ChatContract.MsgListEntry.FILE_SIZE,  FileUtils.getFileSize(pathFile));
        values.put(ChatContract.MsgListEntry.FROM, mContactsBean.getUserId());
        values.put(ChatContract.MsgListEntry.TO, mUserBean.getUserId());
        values.put(ChatContract.MsgListEntry.IS_ACKED, 1);
        values.put(ChatContract.MsgListEntry.MESSAGE_ID, messageID5);// TODO: 2021/8/10 这里后期处理，从服务器发来的解析出来
        mHelper.insertData(getContext(),values);

        Log.d(TAG, " getOffLineFile:: path = " + pathFile );
        Log.d(TAG, " getOffLineFile:: name = " + message );
        Log.d(TAG, " getOffLineFile:: fileSize = " +  FileUtils.getFileSize(pathFile) );
        Log.d(TAG, " getOffLineFile:: from = " + mContactsBean.getUserId() );
        Log.d(TAG, " getOffLineFile:: to = " + mUserBean.getUserId() );
        Log.d(TAG, " getOffLineFile:: message_id = " + messageID5 );
        MsgListBean bean = new MsgListBean(pathFile, FileUtils.getFileName(pathFile), FileUtils.getFileSize(pathFile),0,
                "0",mContactsBean.getUserId(), mUserBean.getUserId() ,1,messageID5,mContactsBean.getOrionId(),mContactsBean.getNickName());
        mMsgList.add(bean);
        mAdapter.notifyDataSetChanged();
        msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
    }

    public void getOffLinePic(String peerHostname){
        String messageID6 = RandomUtil.randomChar();// TODO: 2021/8/8 更新文件标记，用这个来唯一标记当前次的发送情况
        Log.d(TAG, " getOffLinePic:: ");
        ContentValues values1 = new ContentValues();
        values1.put(ChatContract.MsgListEntry.SEND_TIME, TimeUtils.currentTimeMillis()+"");
        values1.put(ChatContract.MsgListEntry.CHAT_TYPE, MsgTypeStateNew.image);
        values1.put(ChatContract.MsgListEntry.TEXT_CONTENT, getString(R.string.content_im));
        String path_pic = "/sdcard/Android/data/com.ucas.chat/files/" + peerHostname;//真实存储在手机里的图片// TODO: 2021/8/23 安卓11不给用/mnt/sdcard/Android/data/XOR，提示没读取权限！改成这个可以了
        values1.put(ChatContract.MsgListEntry.FILE_PATH, path_pic);
        values1.put(ChatContract.MsgListEntry.FILE_NAME, peerHostname);
        values1.put(ChatContract.MsgListEntry.FILE_SIZE, FileUtils.getFileSize(path_pic));
        values1.put(ChatContract.MsgListEntry.FROM, mContactsBean.getUserId());
        values1.put(ChatContract.MsgListEntry.TO, mUserBean.getUserId());
        values1.put(ChatContract.MsgListEntry.IS_ACKED, 1);
        values1.put(ChatContract.MsgListEntry.MESSAGE_ID, messageID6);// TODO: 2021/8/10 后期从服务器返回的消息中解析出
        mHelper.insertData(getContext(),values1);

        Log.d(TAG, " getOffLinePic:: path = " + path_pic );
        Log.d(TAG, " getOffLinePic:: name = " + peerHostname );
        Log.d(TAG, " getOffLinePic:: fileSize = " +  FileUtils.getFileSize(path_pic) );
        Log.d(TAG, " getOffLinePic:: from = " + mContactsBean.getUserId() );
        Log.d(TAG, " getOffLinePic:: to = " + mUserBean.getUserId() );
        Log.d(TAG, " getOffLinePic:: message_id = " + messageID6 );

        MsgListBean bean1 = new MsgListBean(path_pic, FileUtils.getFileName(path_pic), FileUtils.getFileSize(path_pic),0,
                "0",mContactsBean.getUserId(), mUserBean.getUserId() ,1,messageID6,mContactsBean.getOrionId(),mContactsBean.getNickName());
        mMsgList.add(bean1);
        mAdapter.notifyDataSetChanged();
        msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
    }

    public void hasReceivedMessage(String message){
        Log.d(TAG, " hasReceivedMessage:: 对方发来的文本消息 message = " + message);
        Gson gson = new Gson();
        MsgListBean msgListBean = gson.fromJson(message, MsgListBean.class);
        Log.d(TAG, " hasReceivedMessage:: 收消息msgListBean = " + msgListBean.toString());
        String onlineStauts =mContactsBean.getOnlineStatus();
        Log.d(TAG, " hasReceivedMessage:: onlineStauts = " +onlineStauts);
        if(onlineStauts.equals("0")){
            mTvNickName.setTextColor(getColor(R.color.blue4));
            mContactsBean.setOnlineStatus("1");
            SharedPreferencesUtil.setContactBeanSharedPreferences(getContext(), mContactsBean);
            mTvOnLineState.setText(R.string.on_line);
            mAdapter.refreshAllHeadPicture();// TODO: 2021/8/27  //更新全部左边消息头像为在线头像
        }
        mMsgList.add(msgListBean);
        mAdapter.notifyDataSetChanged();
        msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
    }

    public void receiveOffLineFile(String message, String peerHostname){
        Gson gson = new Gson();
        Log.d(TAG, " receiveOffLineFile:: 收到文件的第一步，准备好环境，但未正式接收");
        String onlineStauts =mContactsBean.getOnlineStatus();
        Log.d(TAG, " receiveOffLineFile:: onlineStauts = " + onlineStauts);
        if(onlineStauts.equals("0")){//再次检测更新连接状态
            mTvNickName.setTextColor(getColor(R.color.blue4));
            mContactsBean.setOnlineStatus("1");
            SharedPreferencesUtil.setContactBeanSharedPreferences(getContext(), mContactsBean);
            mTvOnLineState.setText(R.string.on_line);

            mAdapter.refreshAllHeadPicture();// TODO: 2021/8/27  //更新全部左边消息头像为在线头像
        }

        String messageID7 = peerHostname;// TODO: 2021/8/8 更新文件标记，用这个来唯一标记这个文件
        Event.FileMetaMessage fileMetaMessage = gson.fromJson(message, Event.FileMetaMessage.class);
        String fileName =  fileMetaMessage.getFileName();//文件名
        long fileSize = fileMetaMessage.getTotalSize();
        ContentValues values = new ContentValues();
        String filePath ="/sdcard/Android/data/com.ucas.chat/files/"+fileName;// TODO: 2021/8/23 安卓11不给用/mnt/sdcard/Android/data，提示没读取权限！改成这个可以了
        values.put(ChatContract.MsgListEntry.SEND_TIME, TimeUtils.currentTimeMillis()+"");
        values.put(ChatContract.MsgListEntry.CHAT_TYPE, MsgTypeStateNew.file);
        values.put(ChatContract.MsgListEntry.TEXT_CONTENT, getString(R.string.content_file));
        values.put(ChatContract.MsgListEntry.FILE_PATH, filePath);
        values.put(ChatContract.MsgListEntry.FILE_NAME, fileName);
        values.put(ChatContract.MsgListEntry.FILE_SIZE, fileSize);
        values.put(ChatContract.MsgListEntry.FROM, mContactsBean.getUserId());
        values.put(ChatContract.MsgListEntry.TO, mUserBean.getUserId());
        values.put(ChatContract.MsgListEntry.IS_ACKED, 1);
        values.put(ChatContract.MsgListEntry.MESSAGE_ID, messageID7);// TODO: 2021/8/10 后期从服务器返回的消息中解析出

        values.put(ChatContract.MsgListEntry.FRIEND_ORIONID, mContactsBean.getOrionId());
        values.put(ChatContract.MsgListEntry.FRIEND_NICKNAME, mContactsBean.getNickName());// TODO: 2022/3/28 null 的原因

        Log.d(TAG, " receiveOffLineFile::  path = " + filePath );
        Log.d(TAG, " receiveOffLineFile::  name = " + fileName );
        Log.d(TAG, " receiveOffLineFile::  fileSize = " +  fileSize);
        Log.d(TAG, " receiveOffLineFile::  from = " + mContactsBean.getUserId() );
        Log.d(TAG, " receiveOffLineFile::  to = " + mUserBean.getUserId() );
        Log.d(TAG, " receiveOffLineFile::  message_id = " + messageID7 );

        mHelper.insertData(getContext(),values);

        MsgListBean bean2 = new MsgListBean(filePath, fileName, (int)fileSize,0,
                "0",mContactsBean.getUserId(), mUserBean.getUserId() ,1,messageID7,mContactsBean.getOrionId(),mContactsBean.getNickName());
        mMsgList.add(bean2);
        mAdapter.notifyDataSetChanged();
        msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
    }

    public void fileMessage(String message, String peerHostname){
        Log.d(TAG, " fileMessage:: 接收文件的每一个分片" );
        Gson gson = new Gson();
        String onlineStauts =mContactsBean.getOnlineStatus();
        Log.d(TAG, " fileMessage:: onlineStauts = " + onlineStauts );
        if(onlineStauts.equals("0")){
            mTvNickName.setTextColor(getColor(R.color.blue4));
            mContactsBean.setOnlineStatus("1");
            SharedPreferencesUtil.setContactBeanSharedPreferences(getContext(), mContactsBean);
            mTvOnLineState.setText(R.string.on_line);

            mAdapter.refreshAllHeadPicture();// TODO: 2021/8/27  //更新全部左边消息头像为在线头像
        }

        String messageID8 = peerHostname;// TODO: 2021/8/8 更新文件标记，用这个来唯一标记这个文件

        Event.FileMessage fileMessage = gson.fromJson(message, Event.FileMessage.class);
        String name =  fileMessage.getFileName();
        String percent = fileMessage.getFilePercent();
        String speed = fileMessage.getFileSpeed();
        if (percent.contains(".")){
            percent = percent.substring(0,percent.indexOf("."));
        }
        Log.d(TAG, " fileMessage:: name = " + name + " percent = " + percent + " speed = " + speed );
        MsgListBean fileBean = null;
//                mHelper.updateFileProgress(name,Integer.parseInt(percent));//根据文件名字更新文件进度 有bug，会导致文件名一样的全部变
        mHelper.updateFileProgress2(messageID8,Integer.parseInt(percent));// TODO: 2021/8/25 改为以消息id为更新数据库依据  是updateFileProgress2！

        int updatePostion = -1;
        for (int i=0; i<mMsgList.size(); i++){
            fileBean = mMsgList.get(i);// TODO: 2021/8/13 这里直接给fileBean赋值，万一没有合适的，有Bug? // TODO: 2021/8/10 这里后期得改为以messageID来查
            // TODO: 2021/8/25 增加消息id 为判断条件  去掉fileBean.getFileProgress() != 100 ，因为最后当为100时，这里一个也找不到，会导致下面执行if(fileBean==null)。 增加因为MsgTypeStateNew.image判断发照片的类型也复用了这里的代码
            if (fileBean.getMsgType() == MsgTypeStateNew.file ||fileBean.getMsgType() == MsgTypeStateNew.image ) {
                if (fileBean.getFileName().equals(name) && fileBean.getMessageID().equals(messageID8)) {
                    fileBean.setFileProgress(Integer.parseInt(percent));// TODO: 2021/8/26 拍照图片也用这个
                    fileBean.setSpeed(speed);
                    updatePostion = i;
                    Log.d(TAG, " fileMessage:: updatePostion = " + updatePostion);
//                            mAdapter.notifyDataSetChanged();// TODO: 2021/9/27 另一种更新界面的方法,但是全局刷新比较耗时

                    if(fileBean.getMsgType()==MsgTypeStateNew.image && percent.equals("100") ){
                        FileUtils.delectPicture(fileBean.getFilePath());// TODO: 2022/3/17 删除发送成功后的该照片
                    }
                    break;
                }else
                    fileBean = null;//修复bug，最后都没有找到合适的，要变为null！
            }else
                fileBean = null;//修复bug，最后都没有找到合适的，要变为null！
        }
        boolean isSend;

        if(fileBean==null){//没有找到的情况下。估计是Event.RECIEVE_ONLINE_FILE没有处理
            Log.d(TAG, " fileMessage:: fileBean == null");
            isSend=false;
            String filePath ="/sdcard/Android/data/com.ucas.chat/files/"+name;// TODO: 2021/8/23 安卓11不给用/mnt/sdcard/Android/data，提示没读取权限！改成这个可以了
            fileBean= new MsgListBean(filePath, name, (int)0,0,
                    "0",mContactsBean.getUserId(), mUserBean.getUserId() ,1,messageID8,mContactsBean.getOrionId(),mContactsBean.getNickName());
            Log.d(TAG, " fileMessage:: fileBean = " + fileBean);
            mMsgList.add(fileBean);
            mAdapter.notifyDataSetChanged();
            msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
        }else if (fileBean.getFrom().equals(mUserBean.getUserId())){
            Log.d(TAG, " fileMessage:: isSend = true fileBean = " + fileBean);
            isSend = true;
        }else {
            isSend = false;
            Log.d(TAG, " fileMessage:: isSend = false fileBean = " + fileBean);
        }
        //文件进度和传输速度
        MyAsyncTask asyncTask = new MyAsyncTask(mAdapter.getViewList(), updatePostion, isSend);
        asyncTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, fileBean);// TODO: 2021/9/27
    }

    public void createConnectionSuccess(String message, String peerHostname){

        Log.d(TAG,  " createConnectionSuccess:: message = " + message);
        if (message.equals("success")){//连接好友服务器tor成功？
            Log.d(TAG,  " createConnectionSuccess:: mContactsBean = " + mContactsBean.toString());
            String orionId = mContactsBean.getOrionId();
            TorManager.startHandShakeProcess(orionId);//再与朋友进行握手连接
            //临时添加
            //mTvOnLineState.setText(R.string.on_line);
        }else {
            //###########
            mTvNickName.setTextColor(getColor(R.color.gray12));
            mContactsBean.setOnlineStatus("0");
            SharedPreferencesUtil.setContactBeanSharedPreferences(getContext(), mContactsBean);
            mTvOnLineState.setText(R.string.off_line);
            ServerMessageHandler handler = ServerMessageHandler.getInstance();
            handler.setMySelfBean(mUserBean);
            Log.d(TAG,  " createConnectionSuccess:: 转化前OfflineServer: " + mServiceHelper.getSecond());
            Log.d(TAG,  " createConnectionSuccess:: peerHostname: " + peerHostname);
            String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
            Log.d(TAG,  " createConnectionSuccess:: decOfflineServer: " + decOfflineServer);
            handler.processOfflineMessage(peerHostname,decOfflineServer, "messageID");// TODO: 2021/8/10 留后面改

            mAdapter.refreshAllHeadPicture();// TODO: 2021/8/27  //更新全部左边消息头像为离线头像
        }
    }

    @SuppressLint("LongLogTag")
    private void ackMatch(String ack){
        for (int i=0; i<mMsgList.size(); i++){//遍历消息列表
            MsgListBean bean = mMsgList.get(i);
//            String content = bean.getTextContent();
            String messageID = bean.getMessageID();// TODO: 2021/8/24 这里应该改为以消息id验证。因为 message内容是不唯一

            if(!TextUtils.isEmpty(messageID)) {
//            byte[] hashByte = AESCrypto.digest_fast(content.getBytes());
//            String hashAck = new String(hashByte);
//            Log.d(TAG, " index = " + i + " hashAck = " + AESCrypto.bytesToHex(hashByte) + " friendAck = " + AESCrypto.bytesToHex(ack.getBytes()));
            if (messageID.equals(ack)){
                bean.setIsAcked(1);//变色的依据
                mHelper.updateIsAck2(messageID, bean.getIsAcked());// TODO: 2021/8/24 以消息id更新
            }
        }}
       mAdapter.notifyDataSetChanged();
        msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
    }

    /**
     * 离线消息变色
     *
     * @param ack
     */
    @SuppressLint("LongLogTag")
    private void ackMatchOffline(String ack,String messageID) {
        for (int i = 0; i < mMsgList.size(); i++) {
            MsgListBean bean = mMsgList.get(i);
            if (bean.getMessageID().equals(messageID)){
                String content = bean.getTextContent();
                bean.setIsAcked(1);
//                mHelper.updateIsAck(content, bean.getIsAcked());
                mHelper.updateIsAck2(messageID, bean.getIsAcked());// TODO: 2021/8/10 根据messageID标签更新ack 
                break;
            }
          
        }
        mAdapter.notifyDataSetChanged();
        msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
    }

    /**
     * 发送文本消息 ，保存到数据库中，并显示到界面
     */
    private void sendTextMessage(int sendmodel,String messageID ) {
        String textMessage = mEtContent.getText().toString();
        mEtContent.getText().clear();
        LogUtils.d(TAG, " sendTextMessage:: senModel = " + sendmodel);
        LogUtils.d(TAG, " sendTextMessage:: messageID = " + messageID);

        LogUtils.d(TAG, " sendTextMessage:: from userId = " + mUserBean.getUserId());
        LogUtils.d(TAG, " sendTextMessage:: to userId = " + mContactsBean.getUserId());
        LogUtils.d(TAG, " sendTextMessage:: orionId = " + mContactsBean.getOrionId());
        LogUtils.d(TAG, " sendTextMessage:: nickName = " + mContactsBean.getNickName());


        String fromUserId = AesTools.getEncryptContent(mUserBean.getUserId(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        String toUserId = AesTools.getEncryptContent(mContactsBean.getUserId(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        String orionId = AesTools.getEncryptContent(mContactsBean.getOrionId(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        String nickName = AesTools.getEncryptContent(mContactsBean.getNickName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
       // String message = AesTools.getEncryptContent(textMessage, AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        LogUtils.d(TAG, " sendTextMessage:: 加密后 from userId = " + fromUserId);
        LogUtils.d(TAG, " sendTextMessage:: 加密后 to userId = " + toUserId);
        LogUtils.d(TAG, " sendTextMessage:: 加密后 orionId = " + orionId);
        LogUtils.d(TAG, " sendTextMessage:: 加密后 nickName = " + nickName);
        LogUtils.d(TAG, " sendTextMessage:: 未加密后 message = " + textMessage);

        ContentValues values = new ContentValues();
        values.put(ChatContract.MsgListEntry.SEND_TIME, TimeUtils.currentTimeMillis()+"");
        values.put(ChatContract.MsgListEntry.CHAT_TYPE, MsgTypeStateNew.text);
        values.put(ChatContract.MsgListEntry.TEXT_CONTENT, textMessage);
        values.put(ChatContract.MsgListEntry.FROM, fromUserId);
        values.put(ChatContract.MsgListEntry.TO, toUserId);
        values.put(ChatContract.MsgListEntry.IS_ACKED, 0);
        values.put(ChatContract.MsgListEntry.MESSAGE_ID, messageID);

        values.put(ChatContract.MsgListEntry.FRIEND_ORIONID, orionId);
        values.put(ChatContract.MsgListEntry.FRIEND_NICKNAME, nickName);


        mHelper.insertData(getContext(),values);

        //MsgListBean bean = new MsgListBean(textMessage, mUserBean.getUserId(), mContactsBean.getUserId(), 0,messageID,mContactsBean.getOrionId(),mContactsBean.getNickName());
        MsgListBean bean = new MsgListBean(textMessage, fromUserId, toUserId, 0, messageID, orionId, nickName);
        LogUtils.d(TAG, " sendTextMessage:: 加密消息MsgListBean = " +bean.toString() );
        mMsgList.add(bean);
        mAdapter.notifyDataSetChanged();
        msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);

    }


    /**
     * 发送文件消息
     */
    private void sendFile(String filePath,String messageID){
        ContentValues values = new ContentValues();
        values.put(ChatContract.MsgListEntry.SEND_TIME, TimeUtils.currentTimeMillis()+"");
        values.put(ChatContract.MsgListEntry.CHAT_TYPE, MsgTypeStateNew.file);//写死为文件类型
        values.put(ChatContract.MsgListEntry.TEXT_CONTENT, getString(R.string.content_file));
        values.put(ChatContract.MsgListEntry.FILE_PATH, filePath);
        values.put(ChatContract.MsgListEntry.FILE_NAME, FileUtils.getFileName(filePath));
        values.put(ChatContract.MsgListEntry.FILE_SIZE, FileUtils.getFileSize(filePath));
        values.put(ChatContract.MsgListEntry.FROM, mUserBean.getUserId());
        values.put(ChatContract.MsgListEntry.TO, mContactsBean.getUserId());
        values.put(ChatContract.MsgListEntry.IS_ACKED, 1);
        values.put(ChatContract.MsgListEntry.MESSAGE_ID, messageID);

        values.put(ChatContract.MsgListEntry.FRIEND_ORIONID, mContactsBean.getOrionId());
        values.put(ChatContract.MsgListEntry.FRIEND_NICKNAME, mContactsBean.getNickName());
        LogUtils.d(TAG, " sendFile:: orionId: " + mContactsBean.getOrionId());
        LogUtils.d(TAG, " sendFile:: nickName: " + mContactsBean.getNickName());

        mHelper.insertData(getContext(),values);

        String fromId = AesTools.getEncryptContent(mUserBean.getUserId(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " sendFile:: fromId: " + fromId);
        MsgListBean bean = new MsgListBean(filePath, FileUtils.getFileName(filePath), FileUtils.getFileSize(filePath),0,
                "0",fromId, mContactsBean.getUserId(),1,messageID,mContactsBean.getOrionId(),mContactsBean.getNickName());
        mMsgList.add(bean);
        mAdapter.notifyDataSetChanged();
        msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
       // notifyAdapter();
    }

    /**
     * 发送图片消息
     */
    private void sendPicture(Bitmap bitmap,String messageID,String picturePath){// TODO: 2022/3/17 增加照片的路径，用于发送完成后的删除 
        ContentValues values = new ContentValues();
        values.put(ChatContract.MsgListEntry.SEND_TIME, TimeUtils.currentTimeMillis()+"");
        values.put(ChatContract.MsgListEntry.CHAT_TYPE, MsgTypeStateNew.image);//写死为图片类型
        values.put(ChatContract.MsgListEntry.TEXT_CONTENT, getString(R.string.content_file));
//        values.put(ChatContract.MsgListEntry.FILE_PATH, filePath);
        values.put(ChatContract.MsgListEntry.FILE_NAME, picturePath);// TODO: 2021/8/26 增加 //文件名
        values.put(ChatContract.MsgListEntry.FILE_SIZE, bitmap.getByteCount());
        values.put(ChatContract.MsgListEntry.FROM, mUserBean.getUserId());
        values.put(ChatContract.MsgListEntry.TO, mContactsBean.getUserId());
        values.put(ChatContract.MsgListEntry.IS_ACKED, 1);
        values.put(ChatContract.MsgListEntry.MESSAGE_ID, messageID);

        values.put(ChatContract.MsgListEntry.FRIEND_ORIONID, mContactsBean.getOrionId());
        values.put(ChatContract.MsgListEntry.FRIEND_NICKNAME, mContactsBean.getNickName());


        mHelper.insertData(getContext(),values);//存到数据库

        MsgListBean bean = new MsgListBean(bitmap,bitmap.getByteCount(),0,"0", mUserBean.getUserId(), mContactsBean.getUserId(), 0,messageID,picturePath,mContactsBean.getOrionId(),mContactsBean.getNickName());
        System.out.println("发图片mMsgList"+mAdapter.getCount());
        mMsgList.add(bean);
        System.out.println("发图片mMsgList"+mAdapter.getCount());
        mAdapter.notifyDataSetChanged();
        msg_listview.smoothScrollToPosition(mAdapter.getCount() - 1);
        // notifyAdapter();
    }

    @SuppressLint("LongLogTag")
    private void notifyAdapter(){
//        Log.d(TAG,  " mUserBean:: " + mUserBean.toString());
//        Log.d(TAG,  " mContactsBean:: " + mContactsBean.toString());
//        List<MsgListBean> allList = mHelper.queryFriendChatRecord( mUserBean.getUserId(), mContactsBean.getUserId());
//        Log.d(TAG,  " allList= " + allList.toString());
//        mMsgList.add(allList.get(allList.size()-1));
//        mAdapter.notifyItemInserted(mMsgList.size());
//        mLayoutManager.scrollToPosition(mMsgList.size());
//        Log.d(TAG,  " notifyAdapter:: " + mMsgList.toString());
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);//注意data会不会为空
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    if (data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION) != null) {
                        ArrayList<String> fileList = data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION);
                        Log.d(TAG, " OrionName" + mContactsBean.getOrionId()+" selected file size " +fileList.size());
                        for (int i = 0; i < fileList.size(); i++) {//一个个文件发送
                           // sendMessage(mChatHandler.createFileMessage(essFileList.get(i)));
                            Log.d(TAG, " filePath" + fileList.get(i));
                            filepath = fileList.get(i);
                            Log.d("在这里得到文件的路径~",   fileList.get(i));

                            String messageID = RandomUtil.randomChar();// TODO: 2021/8/8 更新时间标记，用这个来唯一标记当前次的发送情况

                            String onlineStatus=mContactsBean.getOnlineStatus();


                            if(onlineStatus.equals("1")){//在线发送
                                final int  fileIndex = i;
                                new Thread(){

                                    @Override
                                    public void run() {
                                        boolean success =TorManager.handleFileMessageSend(fileList.get(fileIndex),mContactsBean.getOrionId(),messageID,getApplication());// TODO: 2021/8/25 增加消息id

                                        if (!success){// TODO: 2021/10/29  //发送不够异或材料

                                            Message message =Message.obtain();
                                            message.what = 0;
                                            handler.handleMessage(message);

                                        }


                                    }
                                }.start(); // TODO: 2021/9/1 采用线程的方式解决拆解加密带来的时间消耗导致界面卡屏问题

                            }else{
                                String from = DigestUtils.sha256Hex(mUserBean.getOnionName());
                                String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());
                                String last_third = fileList.get(i).substring(fileList.get(i).length()-3,fileList.get(i).length());
                                LogUtils.d(" onActivityResult:: REQUEST_CODE_FILE last_third = ", last_third);
                                String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
                                Log.d(TAG, " onActivityResult:: REQUEST_CODE_FILE decOfflineServer = " + decOfflineServer);
                                if (!last_third.equals("png")&&!last_third.equals("peg")&&!last_third.equals("jpj")&&!last_third.equals("ico")){
                                    sendOfflineFile sendOfflineFile = new sendOfflineFile(from,to,"file",fileList.get(i),decOfflineServer,messageID);//jpg可以发
                                    sendOfflineFile.start();
                                }else{
                                    sendOfflineFile sendOfflineFile = new sendOfflineFile(from,to,"pic",fileList.get(i),decOfflineServer,messageID);
                                    sendOfflineFile.start();
                                }

                            }

                            sendFile(fileList.get(i),messageID);
                        }
                    }
                    break;
                case REQUEST_CODE_IMAGE:
                case REQUEST_CODE://相机拍照发送图片
                    Log.d(TAG, " onActivityResult:: REQUEST_CODE 相机拍照 在线发送图片" );
                    String messageID = null;// TODO: 2021/8/8 用这个来唯一标记当前次的发送情况

                    // 从内置相机相册中选择结果回调
                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);

                    for (LocalMedia media : selectListPic) {
                        messageID = RandomUtil.randomChar();// TODO: 2021/8/8 更新时间标记，用这个来唯一标记当前次的发送情况

                        Log.d(TAG, " onActivityResult:: REQUEST_CODE 在线发送图片messageID = " + messageID);
                        Log.d(TAG, " onActivityResult:: REQUEST_CODE 在线发送图片path = " + media.getPath());

                        String cameraFilePath = media.getPath();

                        File file =new File(cameraFilePath);

                        try {
                            FileInputStream fileInputStream = new FileInputStream(file);
                            int fileLength = fileInputStream.available();
                            byte[] bitmapBytes =new byte[fileLength];
                            fileInputStream.read(bitmapBytes);
                            fileInputStream.close();

                            Log.d(TAG, " onActivityResult:: REQUEST_CODE 在线发送图片byte = " + Arrays.toString(bitmapBytes));

                            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);//转为BitMAP类型的图片
                            sendPicture(bitmap,messageID,cameraFilePath);//在界面显示发送情况

                            String onlineStatus=mContactsBean.getOnlineStatus();//状态
                            if(onlineStatus.equals("1")){//在线发送
                                Log.d(TAG, " onActivityResult:: REQUEST_CODE 在线发送图片length = " + bitmapBytes.length);

                                final String  messageIDfinal=  messageID;

                                new Thread(){// TODO: 2021/9/23 采用分片小xor后耗时操作，增加线程

                                    @Override
                                    public void run() {
                                        boolean success = TorManager.handleByteMessageSend(messageIDfinal+".jpg",bitmapBytes,mContactsBean.getOrionId(),messageIDfinal,getApplication());// TODO: 2021/8/26 增加消息id。

                                        if (!success){// TODO: 2021/10/29  //发送不够异或材料

                                            Message message =Message.obtain();
                                            message.what = 0;
                                            handler.handleMessage(message);

                                        }

                                    }
                                }.start();

//                                TorManager.interface_send_text(Arrays.toString(bitmapBytes).substring(0,400), mContactsBean.getOrionId(), getContext(),messageID);
                            }else {
                                System.out.println("onActivityResult:: 离线发图片");
                                String from = DigestUtils.sha256Hex(mUserBean.getOnionName());
                                String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());

                                String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
                                Log.d(TAG, " onActivityResult:: onActivityResult 离线发图片 decOfflineServer = " + decOfflineServer);

                                sendOfflinePic2 sendOfflinePic = new sendOfflinePic2(from,to,"file",bitmapBytes,decOfflineServer,messageID);
                                sendOfflinePic.start();

                            }


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }

                    byte[] picByte = data.getByteArrayExtra(NEWCAMERADATA);//获取新拍照的照片

                    String picturePath = data.getStringExtra("newcamerapicturePath");// TODO: 2022/3/17 照片的路径

                    messageID = RandomUtil.randomChar();// TODO: 2021/8/8 更新时间标记，用这个来唯一标记当前次的发送情况

                    if(picturePath==null){
                        picturePath=  messageID+".jpg";
                    }


                    if (picByte!=null){


                   //     System.out.println("P2PChatActivity最后的照片"+ Arrays.toString(picByte));
                        Bitmap bitmap = BitmapFactory.decodeByteArray(picByte, 0, picByte.length);//转为BitMAP类型的图片
                        sendPicture(bitmap,messageID,picturePath);


                        String onlineStatus=mContactsBean.getOnlineStatus();//状态
                        if(onlineStatus.equals("1")){//在线发送
                            System.out.println("在线发图片");

                            final String  messageIDfinal=  picturePath;

                            String finalMessageID = messageID;
                            new Thread(){// TODO: 2021/9/23 采用分片小xor后耗时操作，增加线程

                                @Override
                                public void run() {
//                                    TorManager.handleFileMessageSend(fileList.get(i),mContactsBean.getOrionId());
                                    boolean success = TorManager.handleByteMessageSend(messageIDfinal,picByte,mContactsBean.getOrionId(), finalMessageID,getApplication());// TODO: 2021/8/26 增加消息id。
//                                    TorManager.interface_send_text(Arrays.toString(picByte).substring(0,50), mContactsBean.getOrionId(), getContext());
                                    if (!success){// TODO: 2021/10/29  //发送不够异或材料

                                        Message message =Message.obtain();
                                        message.what = 0;
                                        handler.handleMessage(message);

                                    }


                                }
                            }.start();

                        }else {
                            System.out.println("离线发图片");
                            String from = DigestUtils.sha256Hex(mUserBean.getOnionName());
                            String to = DigestUtils.sha256Hex(mContactsBean.getOrionId());

                            String decOfflineServer = AesTools.getDecryptContent(mServiceHelper.getSecond(),AesTools.AesKeyTypeEnum.COMMON_KEY);
                            Log.d(TAG, " onActivityResult:: 发送离线图片 decOfflineServer = " + decOfflineServer);

                            sendOfflinePic2 sendOfflinePic = new sendOfflinePic2(from,to,"pic",picByte,decOfflineServer,messageID);
                            sendOfflinePic.start();


                            FileUtils.delectPicture(picturePath);// TODO: 2022/3/17 删除发送成功后的该照片




//                            sendOfflineText sendOfflineText = new sendOfflineText(to,from,Arrays.toString(picByte),mServiceHelper.getSecond());
//                            sendOfflineText.start();

                        }



                    }


                    break;
                case REQUEST_CODE_VEDIO:
                    // 视频选择结果回调
                    List<LocalMedia> selectListVideo = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListVideo) {
                       // sendMessage(mChatHandler.createVideoMessage(media.getPath()));
                    }
                    break;
            }
        }

    }

    /**
     * 选择文件,需要先申请文件存储权限
     * int maxCount 选择的数量
     */
    private void openFileSelector(int maxCount) {
        FileSelector.from(this)
                // .onlyShowFolder()  //只显示文件夹
                //.onlySelectFolder()  //只能选择文件夹
                // .isSingle() // 只能选择一个
                .setMaxCount(maxCount) //设置最大选择数
                .setFileTypes("png", "jpg", "doc", "docx", "apk", "mp3", "gif", "txt", "mp4", "zip","exe","jpeg","rar","zip","pdf") //设置文件类型
                .setSortType(FileSelector.BY_NAME_ASC) //设置名字排序
                //.setSortType(FileSelector.BY_TIME_ASC) //设置时间排序
                //.setSortType(FileSelector.BY_SIZE_DESC) //设置大小排序
                //.setSortType(FileSelector.BY_EXTENSION_DESC) //设置类型排序
                .requestCode(REQUEST_CODE_FILE) //设置返回码
                .start();
    }

    private void hideAnim() {
        mAudioTime.setText("按住录音");
        mLeftAudioAnim.setVisibility(View.GONE);
        mRightAudioAnim.setVisibility(View.GONE);
        Animatable anim = (Animatable) mLeftAudioAnim.getBackground();
        anim.stop();
        Animatable ranim = (Animatable) mRightAudioAnim.getBackground();
        ranim.stop();
    }

    private void showAnim() {
        mLeftAudioAnim.setVisibility(View.VISIBLE);
        mRightAudioAnim.setVisibility(View.VISIBLE);
        Animatable anim = (Animatable) mLeftAudioAnim.getBackground();
        anim.start();
        Animatable ranim = (Animatable) mRightAudioAnim.getBackground();
        ranim.start();
        mAudioTime.setText("0:00");
    }

    @Override
    public void recordStart() {
        showAnim();
    }

    @Override
    public void recordTime(long time) {
        if (mLeftAudioAnim.getVisibility() == View.GONE) {
            mAudioTime.setText("按住录音");
        } else {
            mAudioTime.setText(TimeDateUtils.formatRecordTime((int) time));
        }
    }

    @Override
    public void recordFail() {
        hideAnim();
        ToastUtils.showMessage(getContext(),getString(R.string.audio_fail));
    }

    @Override
    public void recordSuccess(String path, int length) {
        hideAnim();
        ToastUtils.showMessage(getContext(), path);
        mUiHelper.showTransferAudioLayout();
        mTransferAudio.setAudioLength(length);
    }

    @Override
    public void cancelRecord() {
        hideAnim();
        ToastUtils.showMessage(getContext(),getString(R.string.audio_cancel));
    }

    @Override
    public void recordLengthShort() {
        hideAnim();
        ToastUtils.showMessage(getContext(),getString(R.string.audio_short));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
