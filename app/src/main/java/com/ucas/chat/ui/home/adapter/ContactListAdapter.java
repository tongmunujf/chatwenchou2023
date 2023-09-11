package com.ucas.chat.ui.home.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ucas.chat.R;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.ui.home.PersonalDetailsActivity;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.TextUtils;

import java.util.List;
import java.util.Random;

import static com.ucas.chat.bean.contact.ConstantValue.INTENT_CONTACTS_BEAN;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {
    private static String TAG = ConstantValue.TAG_CHAT + "ContactListAdapter";
    protected Context mContext;
    protected List<ContactsBean> mDatas;
    protected LayoutInflater mInflater;

    public ContactListAdapter(Context mContext, List<ContactsBean> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
        mInflater = LayoutInflater.from(mContext);
    }

    public List<ContactsBean> getDatas() {
        return mDatas;
    }

    public ContactListAdapter setDatas(List<ContactsBean> datas) {
        mDatas = datas;
        return this;
    }

    @NonNull
    @Override
    public ContactListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.adapter_contact_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactListAdapter.ViewHolder holder, final int position) {
        final ContactsBean bean = mDatas.get(position);
        MailListUserNameTool tool = MailListUserNameTool.getInstance();
//        int index = tool.getOneSelfImage(mContext, bean.getUserName());
        holder.headIcon.setImageResource(ConstantValue.imHeadIcon[1]);

        if(!bean.getNickName().equals(MailListUserNameTool.DEF_NICK_NAME)){
            holder.tvName.setText(bean.getNickName());
        }else {
            holder.tvName.setText(bean.getUserName());
        }

        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d(TAG, " onClick:: bean = " + bean.toString());
                Intent intent = new Intent(mContext, PersonalDetailsActivity.class);
                intent.putExtra(INTENT_CONTACTS_BEAN, bean);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView headIcon;
        View content;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            headIcon = (ImageView) itemView.findViewById(R.id.ivAvatar);
            content = itemView.findViewById(R.id.content);
        }
    }
}
