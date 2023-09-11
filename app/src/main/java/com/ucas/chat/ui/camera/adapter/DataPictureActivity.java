package com.ucas.chat.ui.camera.adapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.android_play.MainActivity;

import com.ucas.chat.R;
import com.ucas.chat.ui.camera.ShowMultiImageActivity;
import com.ucas.chat.ui.home.chat.PreviewPicturesActivity;
import com.ucas.chat.utils.RandomUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//图片显示列表
public class DataPictureActivity extends AppCompatActivity implements PictureListAdapter.DetailClickListener,PictureListAdapter.ItemClickListener {


    private RecyclerView recyclerView ;

    private PictureListAdapter pictureListAdapter;

    List<PictureParam> pictureList;// TODO: 2022/3/16 改成PictureParam类型

    String IN_PATH = "/pictures/";
    String savePath =null;//保存的图片路径

    int positionClick = -1;//点击浏览的图片位置

    private final int REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_data_picture);

        recyclerView = findViewById(R.id.recycleview);

//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DataPictureActivity.this);//线性布局
//        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());



        pictureListAdapter = new PictureListAdapter(this);
        recyclerView.setAdapter(pictureListAdapter);


        savePath = getApplicationContext().getFilesDir().getAbsolutePath() + IN_PATH;//data/data/com.ucas.chat/files/pictures


        File allXORFolder = new File(savePath);//文件夹，内包含多个拆分的XOR文件

        if (!allXORFolder.exists()) {
            return ;
        }
        if (!allXORFolder.isDirectory()) {
            return ;
        }

        File[] allXORFiles = allXORFolder.listFiles();//多个文件

        pictureList  = new ArrayList<>();//先用列表存储每一个V

        for (File file :allXORFiles){

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                int len = fileInputStream.available();
                byte[] xordata = new byte[len];
                fileInputStream.read(xordata);

                byte[] data = ShowMultiImageActivity.xorBitmap(xordata);// TODO: 2021/10/27 对图片进行xor异或,还原图片
//                byte[] data =xordata;

                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//转为BitMAP类型的图片

                PictureParam pictureParam = new PictureParam(file.getPath(),bitmap);// TODO: 2022/3/16

                pictureList.add(pictureParam);



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        if (pictureList.size()>0)
            pictureListAdapter.add(pictureList);




    }


    @Override
    public void onDetailClick(Bitmap picture) {

    }

    @Override
    public void onItemClick(PictureParam picture,int position) {

        positionClick = position;


        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Bitmap bitmap = picture.getBitmap();// TODO: 2022/3/16 获取图片资源
        String picturePath = picture.getPicturePath();// TODO: 2022/3/16 图片的路径

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        Intent intentPreview = new Intent(DataPictureActivity.this, PreviewPicturesActivity.class);
        intentPreview.putExtra("bitmapdata",data);
        startActivityForResult(intentPreview,REQUEST_CODE);// TODO: 2022/3/30 跳转去大图预览






//        Intent intent = new Intent();
//        intent.putExtra("flag", 3);
//        intent.putExtra("new_camera_data", data);
//        intent.putExtra("newcamerapicturePath", picturePath);// TODO: 2022/3/16 把图片路径也传过去
//
//
//        setResult(RESULT_OK, intent);
//
//
//
//
//
//        finish();


//
//        String filePath = savePath +picture;
//
//        File file = new File(filePath);
//
//        try {
//            FileInputStream fileInputStream  = new FileInputStream(file);
//
//            int filesize = fileInputStream.available();
//            byte[] data = new byte[filesize];
//
//            fileInputStream.read(data);
//            fileInputStream.close();
//
//
//            data = ShowMultiImageActivity.xorBitmap(data);// TODO: 2021/10/27 对图片进行xor异或
//
//
//
//
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                PictureParam pictureParam = pictureList.get(positionClick);
                Bitmap bitmap = pictureParam.getBitmap();
                String picturePath = pictureParam.getPicturePath();


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] dataBitmap = baos.toByteArray();


                Intent intent = new Intent();
                intent.putExtra("flag", 3);
                intent.putExtra("new_camera_data", dataBitmap);
                intent.putExtra("newcamerapicturePath", RandomUtil.randomChar()+".jpg");// TODO: 2022/3/16 把图片路径也传过去

                setResult(RESULT_OK, intent);

                finish();




            }
        }

    }

}