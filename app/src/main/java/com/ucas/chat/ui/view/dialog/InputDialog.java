package com.ucas.chat.ui.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ucas.chat.R;
import com.ucas.chat.utils.Constant;
import com.ucas.chat.utils.TextUtils;
import com.ucas.chat.utils.ToastUtils;

public class InputDialog extends Dialog {

    private EditText ed_input;
    /**
     * 确认和取消按钮
     */
    private Button negtiveBn ,positiveBn;

    private Context context;

    public InputDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_dialog_layout);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        positiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remark = ed_input.getText().toString().trim();
                if (TextUtils.isEmpty(remark)){
                    ToastUtils.showMessage(context, context.getString(R.string.input_remark));
                    return;
                }
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onPositiveClick(remark);
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        negtiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onNegtiveClick();
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        negtiveBn = (Button) findViewById(R.id.negtive);
        positiveBn = (Button) findViewById(R.id.positive);
        ed_input = findViewById(R.id.ed_input);
    }

    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;
    public InputDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }
    public interface OnClickBottomListener{
        /**
         * 点击确定按钮事件
         */
        public void onPositiveClick(String remark);
        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }


}
