package com.ucas.chat.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ucas.chat.R;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.db.ChatContract;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.db.MailListSQLiteHelper;
import com.ucas.chat.db.news.MsgListSQLiteHelper;
import com.ucas.chat.tor.server.ServerMessageHandler;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.ui.home.chat.P2PChatActivity;
import com.ucas.chat.ui.view.dialog.InputDialog;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;

import static com.ucas.chat.MyApplication.getContext;
import static com.ucas.chat.bean.contact.ConstantValue.INTENT_CONTACTS_BEAN;

public class PersonalDetailsActivity extends BaseActivity {
    private static String TAG = ConstantValue.TAG_CHAT + "PersonalDetailsActivity";
    private ImageView mImBack;
    private ImageView im_head;
    private TextView tv_name;
    private TextView tv_remark_name;
    private TextView remarks;
    private RelativeLayout rel_change_remark;
//    private Switch mSwitch;
    private TextView tv_send_message;
    private ContactsBean mContactsBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activtiy_personal_details);
        initData();
        initView();
        initListener();

    }
    private void initData() {// TODO: 2022/1/25 修改跳转异常 ，要动态的！
        Intent intent = getIntent();
        if (intent != null) {
            mContactsBean = (ContactsBean) intent.getSerializableExtra(ConstantValue.INTENT_CONTACTS_BEAN);
            LogUtils.d(TAG, " initData:: mContactsBean = " + mContactsBean.toString());
        }
    }

    private void initView() {
        mImBack = findViewById(R.id.im_back);
        im_head =  findViewById(R.id.im_head);
        tv_name = findViewById(R.id.tv_name);
        remarks = findViewById(R.id.remarks);
        tv_remark_name = findViewById(R.id.tv_remark_name);
//        mSwitch = findViewById(R.id.s_v);
        rel_change_remark = findViewById(R.id.rel_change_remark);
        tv_send_message = findViewById(R.id.tv_send_message);
        tv_name.setText(mContactsBean.getUserName());
        if (!mContactsBean.getNickName().equals(MailListUserNameTool.DEF_NICK_NAME)){
            tv_remark_name.setText(mContactsBean.getNickName());
            remarks.setText(mContactsBean.getNickName());
        }else {
            tv_remark_name.setText("");
            remarks.setText("");
        }

        MailListUserNameTool tool = MailListUserNameTool.getInstance();
//        int index = tool.getOneSelfImage(getContext(), mContactsBean.getUserName());
        im_head.setImageResource(ConstantValue.imHeadIcon[1]);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initListener() {
        mImBack.setOnClickListener(this);
        rel_change_remark.setOnClickListener(this);
        tv_send_message.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.im_back:
                finish();
                break;
            case R.id.rel_change_remark:
                final InputDialog dialog = new InputDialog(PersonalDetailsActivity.this);
                dialog.setOnClickBottomListener(new InputDialog.OnClickBottomListener() {

                    @Override
                    public void onPositiveClick(String remark) {
                        remarks.setText(remark);
                        tv_remark_name.setText(remark);
                        String userName = mContactsBean.getUserName();

                        mContactsBean.setNickName(remark);

                        updateNickName(userName, remark);
                       // updateSharedPreferences(userName, remark);
                        dialog.dismiss();
                    }
                    @Override
                    public void onNegtiveClick() {
                        dialog.dismiss();
                    }
                }).show();
                break;
            case R.id.tv_send_message:
                LogUtils.d(TAG, " initData:: 发送消息mContactsBean = " + mContactsBean.toString());
                Intent intent = new Intent(PersonalDetailsActivity.this, P2PChatActivity.class);
                intent.putExtra(INTENT_CONTACTS_BEAN, mContactsBean);
                startActivity(intent);
                finish();
                SharedPreferencesUtil.setContactBeanSharedPreferences(getContext(), mContactsBean);
                break;
        }
    }

    /**
     * 更新备注到数据库
     */
    private void updateNickName(String name, String nickName){
        MailListSQLiteHelper helper = new MailListSQLiteHelper(getContext());
        helper.updateNickName(MailListSQLiteHelper.table_name, name, nickName);
    }

    public void cleanChatData(View view) {
        MsgListSQLiteHelper.getInstance(getContext()).cleanUpData();// TODO: 2021/10/27 删除历史消息

        Toast.makeText(this,"history message deleted successfully",Toast.LENGTH_SHORT).show();
    }

}
