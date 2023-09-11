package com.ucas.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.ucas.chat.R;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.db.ChatContract;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.db.MailListSQLiteHelper;
import com.ucas.chat.utils.SharedPreferencesUtil;
import com.ucas.chat.utils.ToastUtils;

import static com.ucas.chat.MyApplication.getContext;

/**
 * 修改密码
 */
public class ChangePasswordActivity extends BaseActivity {

    private ImageView mImBack;
    private EditText mEdUserName;
    private EditText mEdPassWord;
    private EditText mEdPassWordAgain;
    private Button mButtConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_change_password);
        initView();
        initListener();

    }

    private void initView() {
        mImBack = findViewById(R.id.im_back);
        mEdUserName = findViewById(R.id.ed_user_name);
        mEdPassWord = findViewById(R.id.ed_pass_word);
        mEdPassWordAgain = findViewById(R.id.ed_pass_word_again);
        mButtConfirm = findViewById(R.id.butt_confirm);
    }

    private void initListener() {
        mImBack.setOnClickListener(this);
        mButtConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.im_back:
                finish();
                break;
            case R.id.butt_confirm:
               changePassword();
                break;
        }
    }

    private void changePassword() {
        String password = mEdPassWord.getText().toString().trim();
        String name = mEdUserName.getText().toString().trim();
        if (!MailListUserNameTool.isExistUserName(getContext(), name)){
            ToastUtils.showMessage(getContext(), getString(R.string.error_user_name));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Log.d(TAG, "login: password is null");
            ToastUtils.showMessage(getContext(),getString(R.string.tip_pass_word));
            return;
        }

        if (!MailListUserNameTool.isExistUserName(getContext(),name)){
            ToastUtils.showMessage(getContext(),getString(R.string.error_user_name));
            return;
        }

        if (!mEdPassWordAgain.getText().toString().trim().equals(password)) {
            Log.d(TAG, "login: password is null");
            ToastUtils.showMessage(getContext(),getString(R.string.tip_confirm_pass_word));
            return;
        }

        updatePassWord(name, password);
      //  updateSharedPreferences(name,password);
        Intent intent = getIntent();
        setResult(ConstantValue.LOGIN_TO_CHANGE_PASSWORD,intent);
        finish();

    }

    /**
     * 更新密码到数据库
     * @param name
     * @param passWord
     */
    private void updatePassWord(String name, String passWord){
        MailListSQLiteHelper helper = new MailListSQLiteHelper(getContext());
        helper.updatePassWord(MailListSQLiteHelper.table_name, name, passWord);
    }

    /**
     * 更新自己SharedPreferences的密码
     * @param name
     * @param passWord
     */
//    private void updateSharedPreferences(String name, String passWord){
//        int index = MailListUserNameTool.getIndex(getContext(), name);
//        String key = MailListUserNameTool.getShareKey(index);
//        SharedPreferencesUtil.setStringSharedPreferences(getContext(),ConstantValue.USER_PASSWORD_SHARE_NAME, key,passWord);
//    }
}
