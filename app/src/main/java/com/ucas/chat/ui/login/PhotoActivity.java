package com.ucas.chat.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.ucas.chat.R;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.ui.view.TouchImageView;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.ToastUtils;

import java.io.File;

public class PhotoActivity extends BaseActivity {
    public static final String TAG = ConstantValue.TAG_CHAT + "PhotoActivity";

    private String pic_name;

    private ImageView back;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo);
        back = findViewById(R.id.back);
        TouchImageView mTouchImageView = findViewById(R.id.touchImageView);
        Intent intent = getIntent();
        if(intent != null){
            pic_name = intent.getStringExtra("PIC_NAME");
            Log.d(TAG, " onCreate:: pic_name + " + pic_name);
        }

        String picPath = FilePathUtils.RECIEVE_FILE_PATH + pic_name;
        File file = new File(picPath);
        if (!file.exists()){
            picPath =  FilePathUtils.TARGET_FILE_PATH + pic_name;
        }

        file = new File(picPath);

        if (file.exists()){
            Bitmap bm = BitmapFactory.decodeFile(picPath);
            //Bitmap bm = BitmapFactory.decodeFile("/sdcard/Chat/PIC1.jpg");
            Drawable drawable = new BitmapDrawable(getResources(), bm);
            mTouchImageView.setImageDrawable(drawable);
            mTouchImageView.setMaxZoom(4f);
        }else {
            ToastUtils.showMessage(this,getString(R.string.file_path_error));
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
