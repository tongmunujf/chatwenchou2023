package com.ucas.chat.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.io.File;

public class CountDownDialog extends Dialog{
    private static String TAG = ConstantValue.TAG_CHAT + "CountDownDialog";
    private Context mContext;

    private String mFromFilePath;
    private String mToFilePath;

    private final static long TOTAL_TIME = 4000;
    private final static long ONECE_TIME = 1000;

    public CountDownDialog(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public CountDownDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initView(context);
    }

    protected CountDownDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView(context);
    }

    public void setFileName(String fileName){
        this.mFromFilePath = FilePathUtils.RECIEVE_FILE_PATH + fileName;
        this.mToFilePath = FilePathUtils.TARGET_FILE_PATH + fileName;
        Log.d(TAG, " setFileName:: fileName = " + fileName);
        Log.d(TAG, " setFileName:: mFromFilePath = " + this.mFromFilePath);
    }

    private void initView(Context context) {
        mContext = context;
        setContentView(R.layout.dialog_count_down);
        this.setCanceledOnTouchOutside(false);// 点击空白处关闭弹窗
        countDownTimer.start();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private void cancelCountDown(){
        countDownTimer.cancel();
    }

    private CountDownTimer countDownTimer = new CountDownTimer(TOTAL_TIME, ONECE_TIME) {
        @Override
        public void onTick(long millisUntilFinished) {
//            String value = String.valueOf((int) (millisUntilFinished / 1000));
//            mTvValue.setText(value);
        }

        @Override
        public void onFinish() {
           // mTvValue.setText(getResources().getString(R.string.done));
            cancelCountDown();
            dismiss();
            deleteFile(mFromFilePath, mToFilePath);
        }
    };

    public void deleteFile(String mFromFilePath, String mToFilePath){
        File toFilePath = new File(mToFilePath);
        File fromFilePath = new File(mFromFilePath);
        if (toFilePath.exists() && fromFilePath.exists()){
            FileUtils.deleteFile(mFromFilePath);
        }else {
           Log.d(TAG, " 文件还没有进行备份成功");
        }
    }
}
