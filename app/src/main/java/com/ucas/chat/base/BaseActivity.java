package com.ucas.chat.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ucas.chat.bean.session.message.IMMessage;


public class BaseActivity extends AppCompatActivity implements View.OnClickListener{

    protected static String TAG ;
    public TextView mTvMachineStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  ActivityUtils.getInstance().removeActivity(this);
    }


    @Override
    public void onClick(View v) {

    }

    protected void showAttachOnActivity(Activity mActivity, Class<?> activity, IMMessage message) {
        Intent intent = new Intent(mActivity, activity);
        intent.putExtra("IMMessage", message);
        startActivity(intent);
    }
}

