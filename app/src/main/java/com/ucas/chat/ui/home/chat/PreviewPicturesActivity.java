package com.ucas.chat.ui.home.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ucas.chat.R;

public class PreviewPicturesActivity extends AppCompatActivity {// TODO: 2022/3/25  预览图片

    ImageView imShow;//预览图片
    LinearLayout linearLayout;// TODO: 2022/3/31 内有按钮
    Button cancelButton,okButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_preview_pictures);

        imShow = findViewById(R.id.im_show);
        linearLayout = findViewById(R.id.line1);
        cancelButton = findViewById(R.id.bt_cancel);
        okButton = findViewById(R.id.bt_ok);

        cancelButton.setOnClickListener(onClickListener);
        okButton.setOnClickListener(onClickListener);

        Intent intent = getIntent();

        String picturepath = intent.getStringExtra("picturepath");
        byte[] bitmapData = intent.getByteArrayExtra("bitmapdata");

        if(picturepath!=null){// TODO: 2022/3/31 跳转、处理不一样

            Log.i("\"picturepath\"",picturepath);
            Bitmap bitmap = BitmapFactory.decodeFile(picturepath);
            imShow.setImageBitmap(bitmap);
        }else if(bitmapData.length>0){

            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);//转为BitMAP类型的图片
            imShow.setImageBitmap(bitmap);

            linearLayout.setVisibility(View.VISIBLE);


        }


    }




    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.bt_cancel:
                    finish();
                    break;
                case R.id.bt_ok:
                    setResult(RESULT_OK);
                    finish();
                    break;

            }



        }
    };








}