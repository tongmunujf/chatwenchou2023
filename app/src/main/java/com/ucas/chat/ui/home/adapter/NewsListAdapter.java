package com.ucas.chat.ui.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ucas.chat.R;
import com.ucas.chat.bean.MsgListBean;
import com.ucas.chat.bean.NewsBean;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.db.MailListSQLiteHelper;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.db.MyInforTool;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;
import com.ucas.chat.utils.TimeUtils;

import java.util.List;

import static com.ucas.chat.MyApplication.getContext;

public class NewsListAdapter extends BaseAdapter {
    private static String TAG = ConstantValue.TAG_CHAT + "NewsListAdapter";

    private Context mContext;
    private List<MsgListBean> newsList;

    public NewsListAdapter(Context mContext, List<MsgListBean> list) {
        this.mContext = mContext;
        this.newsList = list;
    }

    public void notifyData(List<MsgListBean> list){
        this.newsList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_news_list ,null);
            viewHolder = new ViewHolder();
            viewHolder.im_head = view.findViewById(R.id.im_head);
            viewHolder.tv_friend_name = view.findViewById(R.id.tv_friend_name);
            viewHolder.tv_news_content = view.findViewById(R.id.tv_news_content);
            viewHolder.tv_news_state = view.findViewById(R.id.tv_news_state);
            viewHolder.tv_time = view.findViewById(R.id.tv_time);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder=(ViewHolder)view.getTag();
        }
        MsgListBean newsBean = newsList.get(position);
        LogUtils.d(TAG, " getView:: newsBean = " + newsBean);

        MailListSQLiteHelper mailListSQLiteHelper = MailListSQLiteHelper.getInstance(getContext());
        List<ContactsBean> contactsBeans = mailListSQLiteHelper.queryAll();// TODO: 2022/3/23 获取全部的好友信息
        LogUtils.d(TAG, " getView:: contactsBeans = " + contactsBeans.toString());


        String friendNickname = "";

        for (int i=0;i<contactsBeans.size();i++){

            String transformFriendOrionId = AesTools.getDecryptContent(newsBean.getFriendOrionid(), AesTools.AesKeyTypeEnum.COMMON_KEY);
            String transformOrionId = AesTools.getDecryptContent(contactsBeans.get(i).getOrionId(), AesTools.AesKeyTypeEnum.COMMON_KEY);

            if (transformFriendOrionId == null || transformFriendOrionId.isEmpty()){
                transformFriendOrionId = newsBean.getFriendOrionid();
            }

            if(transformFriendOrionId.equals(transformOrionId)){
                friendNickname = contactsBeans.get(i).getNickName();
                break;
            }

        }

        friendNickname = AesTools.getDecryptContent(friendNickname, AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " getView:: 解析后friendNickname：" + friendNickname);

        viewHolder.tv_friend_name.setText(friendNickname);// TODO: 2022/3/23 改为以昵称的方式显示 ,

        LogUtils.d(TAG, " getView:: 加密message = " + newsBean.getTextContent());
        String message = null;

        message = AesTools.getDecryptContent(newsBean.getTextContent(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        LogUtils.d(TAG, " getView:: 解密message = " + message);
        if (message == null || message.isEmpty()){
            //对没有进行加密的聊天内容进行兼容
            message = newsBean.getTextContent();
        }
        viewHolder.tv_news_content.setText(message);

        return view;
    }

    static class ViewHolder{
       ImageView im_head;
       TextView tv_friend_name;
       TextView tv_news_state;
       TextView tv_news_content;
       TextView tv_time;
    }
}
