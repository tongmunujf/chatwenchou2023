package com.ucas.chat.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ucas.chat.R;
import com.ucas.chat.bean.MyInforBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.db.MySelfInfoHelper;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.FileUtils;
import com.ucas.chat.utils.ToastUtils;

public class PassWordDialog extends Dialog{
    private static String TAG = ConstantValue.TAG_CHAT + "PassWordDialog";
    private EditText mEditText;
    private TextView mTvCancel;
    private TextView mTvSure;
    private MyInforBean myInforBean;
    private MySelfInfoHelper mySelfInfoHelper;
    private String mPassword;
    private Context mContext;

    private String mFromFilePath;
    private String mToFilePath;

    private String mFileName;

    public PassWordDialog(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public PassWordDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initView(context);
    }

    protected PassWordDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView(context);
    }

    public void setFileName(String fileName){
        this.mFileName = fileName;
        this.mFromFilePath = FilePathUtils.RECIEVE_FILE_PATH + fileName;
        this.mToFilePath = FilePathUtils.TARGET_FILE_PATH + fileName;
        Log.d(TAG, " setFileName:: fileName = " +  this.mFileName);
        Log.d(TAG, " setFileName:: mFromFilePath = " + this.mFromFilePath);
    }

    private void initView(Context context) {
        mContext = context;
        setContentView(R.layout.dialog_pass_word);
        mEditText = findViewById(R.id.editText);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvSure = findViewById(R.id.tv_sure);
        this.setCanceledOnTouchOutside(true);// 点击空白处关闭弹窗
        initListener();
        getPassword();
    }

    private void initListener() {

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mTvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputPassword = mEditText.getText().toString().trim();
                Log.d(TAG, " onClick:: inputPassword = " + inputPassword);
                if (mPassword.equals(inputPassword)){
                    FileUtils.copyFile(mContext, mFromFilePath ,mToFilePath);
                    dismiss();
                    showCountDialog(mContext);
                }else {
                    ToastUtils.showMessage(mContext,mContext.getString(R.string.pw_error));
                }
            }
        });
    }

    private void showCountDialog(Context context){
        CountDownDialog countDownDialog = new CountDownDialog(context);
        countDownDialog.setFileName(mFileName);
        countDownDialog.show();
    }

    private void getPassword(){
        mySelfInfoHelper= MySelfInfoHelper.getInstance(getContext());
        myInforBean=mySelfInfoHelper.queryAll();
        mPassword = AesTools.getDecryptContent(myInforBean.getPassword(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.d(TAG, " getPassword:: mPassword = " +mPassword);
    }

    @Override
    public void show() {
        super.show();
    }
}
