package com.ucas.chat.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ucas.chat.R;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.ui.home.HomeActivity;
import com.ucas.chat.ui.login.LoginActivity;
import com.ucas.chat.utils.SharedPreferencesUtil;
import com.ucas.chat.utils.ToastUtils;

import java.util.Random;

import static com.ucas.chat.MyApplication.getContext;

/**
 * 注册
 */
public class RegisterActivity extends BaseActivity {

    private ImageView mImBack;
    private TextView mEdUserName;
    private EditText mEdPassWord;
    private EditText mEdPassWordAgain;
    private Button mButtConfirm;
    private String name;
    private String password;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_register);
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

    private void register(){
        name = mEdUserName.getText().toString().trim();
        password = mEdPassWord.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Log.d(TAG, "login: user name is null");
            ToastUtils.showMessage(getContext(),getString(R.string.tip_uesr_name));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Log.d(TAG, "login: password is null");
            ToastUtils.showMessage(getContext(),getString(R.string.tip_pass_word));
            return;
        }

        if (!mEdPassWordAgain.getText().toString().trim().equals(password)) {
            Log.d(TAG, "login: password is null");
            ToastUtils.showMessage(getContext(),getString(R.string.tip_confirm_pass_word));
            return;
        }


//        Random rand = new Random();
//        int imIndex = rand.nextInt(UserBean.imHead.length);
//        UserBean bean = new UserBean();
//        bean.setImPhoto(imIndex);
//        bean.setUserName(name);
//        bean.setPassword(password);
 //       SharedPreferencesUtil.setUserBeanSharedPreferences(getContext(), bean);
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.im_back:
                finish();
                break;
            case R.id.butt_confirm:
                register();
                break;
        }
    }
}
