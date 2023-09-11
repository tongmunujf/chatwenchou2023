package com.ucas.chat.ui.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ucas.chat.R;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.tor.util.RecordXOR;
import com.ucas.chat.ui.camera.adapter.MyUtils;
import com.ucas.chat.ui.camera.adapter.RecycleScaleAdapter;

import org.apaches.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ShowMultiImageActivity extends AppCompatActivity {
    private static final String TAG = "ShowMultiImageActivity";

    RecyclerView recyclerView;
    ArrayList<byte[]> byteList;
    RecycleScaleAdapter adapter;

    private Button takeAgain, finish, saveButton;
    private LinearLayout linearLayout;  // takeAgain,finish的父控件



    private static String generateFileName() {
        return UUID.randomUUID().toString();
    }

    public static byte[] byteArrayXOR(byte[] a, byte[] b) {
        byte[] c = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = (byte) (a[i] ^ b[i]);//按位异或
        }

//		System.out.println("a piece "+AESCrypto.bytesToHex(a));
//		System.out.println("b piece "+AESCrypto.bytesToHex(b));
//		System.out.println("c piece "+AESCrypto.bytesToHex(c));

        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_show_multi_image);
        takeAgain = (Button) findViewById(R.id.take_photo_again);
        finish = (Button) findViewById(R.id.finish);
        saveButton = findViewById(R.id.save);

        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);

        initByteList();

        adapter = new RecycleScaleAdapter(this, byteList);
        recyclerView.setAdapter(adapter);
    }

    // 获取传过来的图像数据，对byteList初始化
    private void initByteList() {
        Intent intent = getIntent();
        int flag = intent.getIntExtra("flag", -1);
        if (flag == -1) {
            Toast.makeText(this, "获取数据失败！", Toast.LENGTH_SHORT).show();
            finish();
        } else if (flag == 3) {// 预览照片
            byteList = new ArrayList<>();
            byte[] data = intent.getByteArrayExtra("data");
            Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.d(TAG, "bitmap width*height: " + b.getWidth() + "*" + b.getHeight()); // 1920*1080




            byteList.add(data);
            doExtraThing();
        }
    }

    private void doExtraThing() {
        linearLayout.setVisibility(View.VISIBLE);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("flag", 2);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        takeAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureAgain();
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        byte[] data = byteList.get(byteList.size() - 1);

                        data = xorBitmap(data);// TODO: 2021/10/27 对图片进行xor异或

//                        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length); // 对图片进行压缩后还是以byte[] 返回


                        boolean savesuccess = saveBitmap(ShowMultiImageActivity.this, data);

                        Message msg2 = new Message();

                        if (savesuccess) {
                            msg2.what = 1;
                            handler.sendMessage(msg2);
                        } else{

                            msg2.what = 2;
                            handler.sendMessage(msg2);
                        }


                    }
                }).start();


            }
        });


    }


    Handler handler = new Handler(){

        @Override
        public void handleMessage( Message msg) {

            switch (msg.what){
                case 1:
                    Toast.makeText(ShowMultiImageActivity.this, "Saved successfully", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    Toast.makeText(ShowMultiImageActivity.this, "Saved failed！", Toast.LENGTH_LONG).show();
            }
        }
    };

    public static boolean saveBitmap(Context context, byte[] data ) {// TODO: 2021/10/27 保存图片，加上图片id


        String savePath;
        File filePic;

        String IN_PATH = "/pictures/";
        savePath = context.getApplicationContext().getFilesDir().getAbsolutePath() + IN_PATH;//data/data/com.ucas.chat/files/pictures

        try {
            filePic = new File(savePath + generateFileName() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
//            FileOutputStream fos = new FileOutputStream(filePic);
//
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
//
//
//            fos.write(byteArrayOutputStream.toByteArray());
//
//
//            fos.flush();
//            fos.close();





            RandomAccessFile raf = null;
            raf = new RandomAccessFile(filePic, "rw");
            raf.write(data, 0, data.length);
            raf.close();

            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }


        return false;
//        Log.i(TAG, "carsonho：filePic.getAbsolutePath()：%s", filePic.getAbsolutePath());

//        return filePic.getAbsolutePath();
    }



    public static byte[] xorBitmap(byte[] bitmapBytes) {// TODO: 2021/10/27 对图片进行xor异或
        try {
            File xorf = new File("/sdcard/Android/data/com.ucas.chat/files/XOR");

            RandomAccessFile xorfile = new RandomAccessFile(xorf, "r");

            long offSet = 0L;


//                long begin = offSet;
//				file.seek(begin);//将文件游标移动到文件的begin位置,
//				xorfile.seek(begin);
//                byte[] b = new byte[this.pieceSize];
            byte[] x = new byte[bitmapBytes.length];
//				file.read(b);
//                b = Arrays.copyOfRange(bitmapBytes,i*this.pieceSize,i*this.pieceSize+this.pieceSize);

//				System.out.println("the "+i+" piece");
            xorfile.read(x);


            byte[] c = byteArrayXOR(bitmapBytes, x);

            xorfile.close();

            return c;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("cut file error " + e.getMessage());
        }


        return bitmapBytes;
    }

    private void takePictureAgain() {
        Intent intent = new Intent();
        intent.putExtra("flag", 1);// TODO: 2022/3/16 重新拍 
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (linearLayout != null) {
            if (linearLayout.getVisibility() == View.VISIBLE) {
                takePictureAgain();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }
}
