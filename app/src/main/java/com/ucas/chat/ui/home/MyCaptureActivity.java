package com.ucas.chat.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.ucas.chat.R;
import com.ucas.chat.utils.PermissionUtils;


public class MyCaptureActivity extends Activity {

    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int PERMISSION_REQUEST_CODE = 100001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_login);
        initPermission();
        //startActivityForResult(new Intent(this, CaptureActivity.class),0);
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean has = PermissionUtils.checkPermissions(this, BASIC_PERMISSIONS);
            if (!has) {
                PermissionUtils.requestPermissions(this, PERMISSION_REQUEST_CODE,
                        BASIC_PERMISSIONS);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                //String result = data.getStringExtra(KEY_RESULT);
            }
        }
    }
}
