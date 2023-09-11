package com.ucas.chat.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.ucas.chat.R;
import com.ucas.chat.TorManager;
import com.ucas.chat.base.BaseFragment;
import com.ucas.chat.bean.AddressBookBean;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.db.AddressBookHelper;
import com.ucas.chat.db.ChatContract;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.db.MailListSQLiteHelper;
import com.ucas.chat.db.MyInforTool;
import com.ucas.chat.eventbus.Event;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.ui.home.InterfaceOffline.checkPeerStatus;
import com.ucas.chat.ui.home.adapter.ContactListAdapter;
import com.ucas.chat.ui.view.decoration.DividerItemDecoration;
import com.ucas.chat.ui.view.dialog.InputDialog;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.AesUtils;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;
import com.ucas.chat.utils.ToastUtils;

import org.apaches.commons.codec.digest.DigestUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 通讯录
 */
public class ContactListFragment extends BaseFragment {

    private static String TAG = ConstantValue.TAG_CHAT + "ContactListFragment";
    private ImageView mImAdd;
    private RecyclerView mRv;
    private ContactListAdapter mAdapter;
    private LinearLayoutManager mManager;
    private List<ContactsBean> mDatas = new ArrayList<>();
    private UserBean mySelfBean=null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImAdd = view.findViewById(R.id.im_add);
        mImAdd.setOnClickListener(this);
        mRv = view.findViewById(R.id.rv);
        mRv.setLayoutManager(mManager = new LinearLayoutManager(getActivity()));
        mAdapter = new ContactListAdapter(getActivity(), mDatas);
        mRv.setAdapter(mAdapter);
        //如果add两个，那么按照先后顺序，依次渲染。
        mRv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        EventBus.getDefault().register(this);
    }

    private void initDatas() {
        LogUtils.d(TAG, " initDatas:: ");
        mySelfBean= SharedPreferencesUtil.getUserBeanSharedPreferences(getContext());
        mDatas = new ArrayList<>();
//        MailListSQLiteHelper helper = MailListSQLiteHelper.getInstance(getContext());
        UserBean self = SharedPreferencesUtil.getUserBeanSharedPreferences(getContext());

//        AddressBookHelper addressBookHelper=AddressBookHelper.getInstance(getContext());
//        for (AddressBookBean bean: addressBookHelper.queryAll()){

        MailListSQLiteHelper helper = MailListSQLiteHelper.getInstance(getContext());
        LogUtils.d(TAG, " initDatas:: MailListSQLiteHelper");
        for(ContactsBean bean:helper.queryAll()){// TODO: 2022/3/21 更改读这里我文件的
            LogUtils.d(TAG, " initDatas:: bean = " + bean.toString());
            LogUtils.d(TAG, " initDatas:: selfOnionName = " + self.getOnionName());
            LogUtils.d(TAG, " initDatas:: orionId = " + bean.getOrionId());

            if (!TextUtils.equals(self.getOnionName(), bean.getOrionId())) {// TODO: 2022/3/21 更改读这里我文件的
                ContactsBean temp = new ContactsBean();
                temp.setImageId("1");
                temp.setNickName(getTransformValue(bean.getNickName()));
                temp.setUserId(getTransformValue(bean.getNickName()));
                temp.setOrionId(bean.getOrionId());
                String to = DigestUtils.sha256Hex(bean.getOrionHashId().trim());
                temp.setOrionHashId(bean.getOrionHashId());
                //temp.setUserName(bean.getUserName());
                temp.setUserName(getTransformValue(bean.getUserName()));
                temp.setOrionId(getTransformValue(bean.getOrionId()));
                String from = DigestUtils.sha256Hex(mySelfBean.getOnionName().trim());

                mDatas.add(temp);
            }
        }
        LogUtils.d(TAG, " initDatas:: data = " + mDatas.toString());
        mAdapter.setDatas(mDatas);
        mAdapter.notifyDataSetChanged();
    }

    private String getTransformValue(String value){
        String result = AesTools.getDecryptContent(value, AesTools.AesKeyTypeEnum.COMMON_KEY);
        return result;
    }

    private void updateContactOnlineStatus(Event.PeerOnlineStatusMessage checkOnlineStatusMessage ){
        String onionHash = checkOnlineStatusMessage.getOnionHash();
        String onlineStatus = checkOnlineStatusMessage.getOnlineStatus();
        String updateTime = checkOnlineStatusMessage.getStatusUpdateTime();
        for(int i=0;i<this.mDatas.size();i++){
            ContactsBean bean =this.mDatas.get(i);
            if(bean.getOrionHashId().equals(onionHash)){
                bean.setOnlineStatus(onlineStatus);
                if(onlineStatus.equals("1")){
                    String orionId = bean.getOrionId();
                    TorManager.initMessageHandler(getContext(), orionId);
                }
                break;
            }
        }
    }
    @SuppressLint("LongLogTag")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(Event messageEvent){
        String type = messageEvent.getType();
        String message = messageEvent.getMessage();//收到的消息
        String peerHostname = messageEvent.getPeerHostname(); //对方orion地址
        LogUtils.d(TAG, " onMoonEvent:: type = " + type + " message" + message + " peerHostname = " + peerHostname);
        switch (type) {
            case Event.CHECK_PEER_ONLINE_STATUS:
                LogUtils.d(TAG, "ContactListFragment Event.CHECK_PEER_ONLINE_STATUS!!!"+messageEvent.toString());
                Gson gson = new Gson();
                Event.PeerOnlineStatusMessage checkOnlineStatusMessage = gson.fromJson(message, Event.PeerOnlineStatusMessage.class);
                updateContactOnlineStatus(checkOnlineStatusMessage);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.im_add:
                final InputDialog dialog = new InputDialog(getActivity());
                dialog.setOnClickBottomListener(new InputDialog.OnClickBottomListener() {

                    @Override
                    public void onPositiveClick(String remark) {
                        ToastUtils.showMessage(getContext(),getString(R.string.add_success));
                        dialog.dismiss();
                    }
                    @Override
                    public void onNegtiveClick() {
                        dialog.dismiss();
                    }
                }).show();
                break;
        }
    }
    @Override
    public void onStart(){
        super.onStart();
        initDatas();
    }

    @Override
    public void onResume() {
        super.onResume();
        initDatas();
        LogUtils.d(TAG, "onResume");
    }
}
