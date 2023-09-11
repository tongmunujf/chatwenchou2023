package com.ucas.chat.ui.home;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.android_play.MainActivity;
import com.ucas.chat.R;
import com.ucas.chat.base.BaseFragment;
import com.ucas.chat.bean.AddressBookBean;
import com.ucas.chat.bean.MsgListBean;
import com.ucas.chat.bean.NewsBean;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.db.AddressBookHelper;
import com.ucas.chat.db.ChatContract;
import com.ucas.chat.db.MailListSQLiteHelper;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.db.MyInforTool;
import com.ucas.chat.db.news.MsgListSQLiteHelper;
import com.ucas.chat.progressdisplay.DialogAdapter;
import com.ucas.chat.progressdisplay.ProgressNode;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.ui.home.adapter.NewsListAdapter;
import com.ucas.chat.ui.home.chat.P2PChatActivity;
import com.ucas.chat.ui.view.dialog.CommonDialog;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.AesUtils;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.ucas.chat.MyApplication.getContext;
import static com.ucas.chat.TorManager.stopTor;
import static com.ucas.chat.bean.contact.ConstantValue.INTENT_CONTACTS_BEAN;
import static com.ucas.chat.utils.StringUtils.getProgress;
import static org.torproject.android.service.OrbotServiceAction.STATUSCHANGE_ACTION;
import static org.torproject.android.service.OrbotServiceAction.STATUSCHANGE_MESSAGE;
import static org.torproject.android.service.TorServiceConstants.LOCAL_ACTION_LOG;
import static org.torproject.android.service.TorServiceConstants.LOCAL_EXTRA_LOG;

/**
 * 消息
 */
public class NewsFragment extends BaseFragment {
    private static String TAG = ConstantValue.TAG_CHAT + "NewsFragment";
    private ImageView mImHead;
    private TextView mTvUserName;
    private static TextView mTvIfOnLine;
    private ListView mLvNewsList;
    private NewsListAdapter mAdapter;
    private List<MsgListBean> mLastNewsList;
    private String userName;
    private MsgListSQLiteHelper mHelper;
    private ContactsBean mContactsBean;

    private static Context context;

    public static ProgressBar reconnectProgressBar;// TODO: 2022/4/12 新增加的离线出现转圈圈

    public static Dialog dialog =null;
    public static DialogAdapter dialogAdapter;
    public static List<ProgressNode> progressNodes = new ArrayList<>();
    TextView tvCancelProgressbar;// TODO: 2022/4/12 用来取消progressbar

    static RecyclerView recyclerViewDialog;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImHead = view.findViewById(R.id.im_head);
        mTvUserName = view.findViewById(R.id.tv_user_name);
        mTvIfOnLine = view.findViewById(R.id.tv_if_on_line);

        mLvNewsList  = view.findViewById(R.id.lv_news_list);

        reconnectProgressBar = view.findViewById(R.id.reconnect_progressbar);
        reconnectProgressBar.setOnClickListener(showProgressBarClickListener);

        context  =getContext();
        mHelper = new MsgListSQLiteHelper(getContext());
        mLastNewsList = new ArrayList<>();

        mLastNewsList = mHelper.queryLast(getContext());// TODO: 2022/3/23 修改，获取不同人的最新一条的信息
        
        
//        newsList.clear();
        LogUtils.d(TAG+ " onViewCreated:: mLastNewsList ——》", mLastNewsList.toString());
        mAdapter = new NewsListAdapter(getActivity(), mLastNewsList);

        mLvNewsList.setAdapter(mAdapter);

        Intent intent = getActivity().getIntent();
        userName = intent.getStringExtra(ConstantValue.LOGIN_INTENT_HOME_NAME_VALUE);
        MailListUserNameTool tool = MailListUserNameTool.getInstance();
        int index = tool.getOneSelfImage(getContext(), userName);
        LogUtils.d(TAG, " onViewCreated:: userName = " + userName);
        mTvUserName.setText(userName);
        mImHead.setImageResource(ConstantValue.imHeadIcon[0]);
        initListener();
        mHelper = MsgListSQLiteHelper.getInstance(getContext());


        setAlarmDialog();// TODO: 2022/4/12  弹出框实例化
        

    }

    private void initData() {
        LogUtils.d(TAG, " initDatas::");
        MailListSQLiteHelper helper = MailListSQLiteHelper.getInstance(getContext());
        UserBean self = SharedPreferencesUtil.getUserBeanSharedPreferences(getContext());

        new Thread(new Runnable() {
            @Override
            public void run() {

                UserBean userself =null;
                Message message = null;
                while (true ){

                    try {
                        Thread.sleep(1000);

                        if(getContext()!=null) {
                            userself = SharedPreferencesUtil.getUserBeanSharedPreferences(getContext());
//                            System.out.println("网络NewsFragment存储的"+SharedPreferencesUtil.getUserBeanSharedPreferences(getContext()).getOnlineStatus());
                            String onlineStatus = userself.getOnlineStatus();
                            LogUtils.d(TAG, " initData:: onlineStatus = " + onlineStatus);
                            message = Message.obtain();
                            message.what = 2; //消息的标识
                            message.obj = onlineStatus; // 消息的存放
                            handler.sendMessage(message);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        List<ContactsBean> friends = new ArrayList<>();

        AddressBookHelper addressBookHelper=AddressBookHelper.getInstance(getContext());

        boolean mailListDataboolean = SharedPreferencesUtil.getMailListDataboolean(getContext());// TODO: 2022/3/21 查看有没有已经保存了好友的信息
        LogUtils.d(TAG, " initDatas:: mailListDataboolean = " +mailListDataboolean);

        for (AddressBookBean bean: addressBookHelper.queryAll()){
            LogUtils.d(TAG, " initDatas:: bean = " + bean.toString());
            LogUtils.d(TAG, " initDatas:: self.getOnionName() = " + self.getOnionName());
            LogUtils.d(TAG, " initDatas:: getRemoteOnionName = " + bean.getRemoteOnionName());

            String nickName = AesTools.getDecryptContent(bean.getNickName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
            LogUtils.d(TAG, " initDatas:: 转化后nickName = " + nickName);
            String remoteOnionName = AesTools.getDecryptContent(bean.getRemoteOnionName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
            LogUtils.d(TAG, " initDatas:: 转化后remoteOnionName = " + remoteOnionName);

            if (!TextUtils.equals(self.getOnionName(), remoteOnionName)) {
                LogUtils.d(TAG, " initDatas:: self.getOnionName() 不等于remoteOnionName");
                ContactsBean temp = new ContactsBean();
                temp.setImageId("1");
                temp.setNickName(nickName);
                temp.setOrionId(remoteOnionName);
                temp.setOrionHashId(new String(AESCrypto.digest_fast(remoteOnionName.getBytes())));
                temp.setUserName(nickName);
                friends.add(temp);
                LogUtils.d(TAG, " initDatas:: friendsList = " + friends.toString());

               // if(!mailListDataboolean) {

                List<ContactsBean> mailList = helper.queryAll();
                LogUtils.d(TAG, " initDatas:: mailList.size = " + mailList.size());
                if (mailList.size() == 0){
                    ContentValues values = new ContentValues();// TODO: 2022/3/21 将朋友信息写入数据库 MailList.db
                    LogUtils.d(TAG, " initDatas:: ContentValues = " + values.toString());
                    values.put(ChatContract.MailListEntry.USER_ID, bean.getNickName());// TODO: 2022/3/25 修复null
                    values.put(ChatContract.MailListEntry.USER_NAME, bean.getNickName());
                    values.put(ChatContract.MailListEntry.NICK_NAME, bean.getNickName());
                    values.put(ChatContract.MailListEntry.ORION_ID, bean.getRemoteOnionName());
                    values.put(ChatContract.MailListEntry.ORION_HASH_ID, new String(AESCrypto.digest_fast(bean.getRemoteOnionName().getBytes())));
                    values.put(ChatContract.MailListEntry.IMAGE_ID, "1");
                    helper.insertData(getContext(), values);
                }
               // }

            }
        }


        SharedPreferencesUtil.saveMailListData(getContext());// TODO: 2022/3/21 标记为保存了 朋友信息
        for (ContactsBean bean : helper.queryAll()) {
            LogUtils.d(TAG, " initDatas:: ContactsBean = " + bean.toString());
            LogUtils.d(TAG, " initDatas:: self.getUserId() = " + self.getUserId());
            if (!TextUtils.equals(self.getUserId(), bean.getUserId())) {
                friends.add(bean);
            }
        }

        mAdapter.notifyDataSetChanged();

        ContactsBean tmp =SharedPreferencesUtil.getContactBeanSharedPreferences(getContext());
        if(tmp==null) {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mContactsBean = (ContactsBean) intent.getSerializableExtra(ConstantValue.INTENT_CONTACTS_BEAN);
            }
        }else{
            mContactsBean=tmp;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void initListener() {
        mLvNewsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MailListSQLiteHelper mailListSQLiteHelper = MailListSQLiteHelper.getInstance(context);
                List<ContactsBean> contactsBeans = mailListSQLiteHelper.queryAll();// TODO: 2022/3/23 获取全部的好友信息

                MsgListBean msgListBean = mLastNewsList.get(position);
                LogUtils.d(TAG, " initListener:: NewsList ——》onItemClick 转化前contactsBeans = " + contactsBeans.toString());
                //MsgListBean{sendTime='1693384864599', msgType=1, textContent='123456', filePath='null', fileName='null', fileSize=0, speed='null', percentage=0, from='asdf', to='袁绍', isAcked=1, fileProgress=0, isState=0, messageID=48562}
                LogUtils.d(TAG, " initListener:: NewsList ——》onItemClick msgListBean = " + msgListBean.toString());

                int contactsBeanId = 0;

                String friendOrionId = msgListBean.getFriendOrionid();
                LogUtils.d(TAG, " initListener:: 转化前friendOrionId = " + msgListBean.getFriendOrionid());
                String transformFriendOrionId = AesTools.getDecryptContent(friendOrionId, AesTools.AesKeyTypeEnum.COMMON_KEY);
                LogUtils.d(TAG, " initListener:: 转化后friendOrionId = " + transformFriendOrionId);

                for(int i=0;i<contactsBeans.size();i++){
                    String onionId = contactsBeans.get(i).getOrionId();
                    String transformOrionId = AesTools.getDecryptContent(onionId, AesTools.AesKeyTypeEnum.COMMON_KEY);
                    LogUtils.d(TAG, " initListener:: 转化前onionId = " + onionId);
                    LogUtils.d(TAG, " initListener:: 转化后onionId = " + transformOrionId);

                    if(transformOrionId.equals(transformFriendOrionId)){
                        contactsBeanId = i;
                        break;
                    }
                }
                LogUtils.d(TAG, " initListener:: contactsBeanId = " +  contactsBeans.get(contactsBeanId));

                ContactsBean contactsBean = contactsBeans.get(contactsBeanId);
                LogUtils.d(TAG, " initListener:: 转化前userId = " +  contactsBean.getUserId());
                LogUtils.d(TAG, " initListener:: 转化前userName = " +  contactsBean.getUserName());
                LogUtils.d(TAG, " initListener:: 转化前nickName = " +  contactsBean.getNickName());
                LogUtils.d(TAG, " initListener:: 转化前orionId = " +  contactsBean.getOrionId());

                String userId = AesTools.getDecryptContent(contactsBean.getUserId(), AesTools.AesKeyTypeEnum.COMMON_KEY);
                LogUtils.d(TAG, " initListener:: 转化后userId = " +  userId);
                String userName = AesTools.getDecryptContent(contactsBean.getUserName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
                LogUtils.d(TAG, " initListener:: 转化后userName = " + userName);
                String nickName = AesTools.getDecryptContent(contactsBean.getNickName(), AesTools.AesKeyTypeEnum.COMMON_KEY);
                LogUtils.d(TAG, " initListener:: 转化后nickName = " +  nickName);
                String orionId = AesTools.getDecryptContent(contactsBean.getOrionId(), AesTools.AesKeyTypeEnum.COMMON_KEY);
                LogUtils.d(TAG, " initListener:: 转化后orionId = " +  orionId);
                contactsBean.setUserId(userId);
                contactsBean.setUserName(userName);
                contactsBean.setNickName(nickName);
                contactsBean.setOrionId(orionId);

                Intent intent = new Intent(getActivity(), P2PChatActivity.class);
                intent.putExtra(INTENT_CONTACTS_BEAN, contactsBean);
                //intent.putExtra(INTENT_CONTACTS_BEAN, contactsBeans.get(contactsBeanId));
                startActivity(intent);
            }
        });
        mLvNewsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final CommonDialog dialog = new CommonDialog(getActivity());
                dialog.setMessage(getContext().getString(R.string.delete_talk))
                        //.setImageResId(UserBean.imHead[newsList.get(position).getFriendHeadNum()])
                        .setTitle(getString(R.string.delete_talk))
                        .setSingle(false).setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        mLastNewsList.remove(position);
                        mAdapter.notifyDataSetChanged();
                        dialog.dismiss();

                        MsgListSQLiteHelper.getInstance(getContext()).cleanUpData();// TODO: 2021/10/27 删除历史消息

                        Message msg2 = new Message();
                        msg2.what = 1;
                        handler.handleMessage(msg2);

                    }

                    @Override
                    public void onNegtiveClick() {
                        dialog.dismiss();
                    }
                }).show();
                return true;
            }
        });

    }

    static Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Toast.makeText(context,"history message deleted successfully",Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    String onlineStatus = (String) msg.obj;

                    if(onlineStatus.equals("1")){
                        mTvIfOnLine.setText(R.string.on_line);
                        reconnectProgressBar.setVisibility(View.GONE);
                        if(dialog!=null)
                            dialog.dismiss();//取消显示

                    }else {
                        mTvIfOnLine.setText(R.string.off_line);
                        reconnectProgressBar.setVisibility(View.VISIBLE);
//                        if(dialog!=null)
//                            dialog.show();//显示
                    }


//                    Toast.makeText(getContext(),"删除历史消息成功",Toast.LENGTH_SHORT).show();

                    break;
                case 3:

//                    UserBean userself = SharedPreferencesUtil.getUserBeanSharedPreferences(context);
//                    userself.setOnlineStatus("1");
//                    SharedPreferencesUtil.setUserBeanSharedPreferences(context,userself);
//                    System.out.println("主页的登录进度"+userself);
//                    Toast.makeText(context,"删除历史消息成功",Toast.LENGTH_SHORT).show();
//                    System.out.println("sbjjjjjjjjjjjjjjjj"+userself);

                    break;
            }

        }
    };





    public static class ProgressReceiver extends BroadcastReceiver {// TODO: 2021/7/21 广播接收
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG, " ProgressReceiver intent = " + intent.getAction());
            if (null == intent){
                Log.d("ProgressReceiver ", " intent is null" );
                return;
            }


            if(intent.getAction().equals(LOCAL_ACTION_LOG)){

                String progress = intent.getStringExtra(LOCAL_EXTRA_LOG);
                if (TextUtils.isEmpty(progress)){
                    Log.d("ProgressReceiver ", " progress is null" );
                    return;
                }

                if (progress.contains("NOTICE: Bootstrapped")){
                    progress = getProgress(progress);
                    System.out.println(TAG+"  主页的登录进度  "+progress);

                    String newprogress = progress.trim();

                    if(newprogress.equals("100%")) {
                        System.out.println(TAG+"  主页的登录进度够了！  ");
//                    Message message = Message.obtain();
//                    message.what = 3;//标记为0，
////                    message.obj = progress;
//                    handler.handleMessage(message);
//
//                    UserBean userself = SharedPreferencesUtil.getUserBeanSharedPreferences(context);
//                    userself.setOnlineStatus("1");
//                    SharedPreferencesUtil.setUserBeanSharedPreferences(context,userself);
//                    System.out.println("网络--写入BeanSharedPreferences： "+"NewsFragment-ProgressReceiver()");
                        System.out.println("网络主页的登录进度100%");


                    }

                }
            }else if(intent.getAction().equals(STATUSCHANGE_ACTION)){

                String message = intent.getStringExtra(STATUSCHANGE_MESSAGE);
                Log.d("ProgressReceiver ", " message: " + message );
                if (TextUtils.isEmpty(message)){
                    Log.d("ProgressReceiver ", " progress is null" );
                    return;
                }

                System.out.println(TAG+"  网络状态广播：  "+message);

//                if(dialog.isShowing()) {
                    if (progressNodes.size() > 0) {

                        ProgressNode lastProgressNode = progressNodes.get(progressNodes.size() - 1);//上一个
                        lastProgressNode.setLoadStatus("1");
                    }

                    ProgressNode progressNode = new ProgressNode(message, "0");
                    progressNodes.add(progressNode);//将连接过程显示在弹出框中

                if(dialogAdapter!=null&&recyclerViewDialog !=null){//有的手机快很多，这里要判断空

                    dialogAdapter.notifyDataSetChanged();
                    recyclerViewDialog.scrollToPosition(progressNodes.size() - 1);

                }

//                }


            }




        }
    }



    View.OnClickListener showProgressBarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.show();//让该出现时再出现
        }
    };


    private void setAlarmDialog() {// TODO: 2022/4/12  弹出框


        dialog  = new Dialog(getContext());
        dialog.setContentView(R.layout.view_dialog_custom);
        dialog.setTitle("蓝牙装置");
        dialog.setCancelable(false);
        dialog.getWindow().setGravity(Gravity. BOTTOM);


        recyclerViewDialog = (RecyclerView)dialog.findViewById(R.id.rv_alarm);
        recyclerViewDialog.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDialog.scrollToPosition(progressNodes.size()-1);

        dialogAdapter = new DialogAdapter(getContext(), progressNodes,new DialogAdapter.Listener() {
            @Override
            public void onItemClick(BluetoothDevice dev) {


            }
        });
        recyclerViewDialog.setAdapter(dialogAdapter);

        tvCancelProgressbar = (TextView) dialog.findViewById(R.id.tv_cancel_progressbar);
        tvCancelProgressbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//                progressNodes = new ArrayList<>();//还原
//                dialogAdapter.notifyDataSetChanged();

            }
        });//取消弹出框



        dialog.create();
//        dialog.show();//让该出现时再出现


//        int gg = 16;//测试
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//
//                for(int i=0;i<gg;i++){
//
//                    ProgressNode progressNode = new ProgressNode(i+"","0");
//                    progressNodes.add(progressNode);
//
//                    getActivity().runOnUiThread(new Runnable() {
//                        public void run() {
//                            dialogAdapter.notifyDataSetChanged();
//                            recyclerViewDialog.scrollToPosition(progressNodes.size()-1);
//                        }
//                    });
//
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//
//            }
//        }).start();
//
//
//
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//                    Thread.sleep(20000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//
//                for(int i=0;i<gg;i++){
//
//                    progressNodes.get(i).setLoadStatus("-1");
//
//                    final int finalI = i;
//                    getActivity().runOnUiThread(new Runnable() {
//                        public void run() {
//                            dialogAdapter.notifyItemChanged(finalI);
//                            recyclerViewDialog.scrollToPosition(progressNodes.size()-1);
//                        }
//                    });
//
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//
//                }
//                dialog.cancel();
//            }
//        }).start();






    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if(dialog!=null){
            dialog.cancel();
            dialog=null;
        }
//        stopTor(getContext());
    }
}
