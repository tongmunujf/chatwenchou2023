package com.ucas.chat.ui.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.ucas.chat.R;
import com.ucas.chat.ui.camera.adapter.DataPictureActivity;
import com.ucas.chat.ui.camera.adapter.MyUtils;
import com.ucas.chat.ui.home.chat.P2PChatActivity;
import com.ucas.chat.utils.PictureFileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.ucas.chat.ui.home.chat.P2PChatActivity.REQUEST_CODE_IMAGE;

/*
* 参考：https://blog.csdn.net/qq_28193019/article/details/102719369
*拍照主界面
* */


public class MyCameraActivity extends AppCompatActivity {

    private static final String TAG = "Chat:MyCameraActivity";
    private static final String NEWCAMERADATA ="new_camera_data";//重新进行拍照的新照片
    private static final String OLDCAMERADATA ="old_camera_data";//选择已有的旧照片
    private ScreenListener screenListener;
    private final int REQUEST_CODE = 100;
    private int QUALITY = 60;

    /**
     * Camera类用于管理和操作camera资源，它提供了完整的相机底层接口，支持相机资源切换，
     * 可设置预览、拍摄尺寸，设定光圈、曝光、聚焦等相关参数，获取预览、拍摄帧数据等功能
     * 注：预览≠拍摄
     */
    private Camera camera;// 代表相机对象
    private Button takePhoto;//拍照
    private Button selectPicture;//已有图片
    private CameraPreview cameraPreview;// 相机预览组件

    private byte[] jpegData;

    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " onCreate::");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_my_camera);

        initViews();
        screenListener = new ScreenListener( MyCameraActivity.this ) ;
        screenListener.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
            }

            @Override
            public void onScreenOff() {
                finish();
            }

            @Override
            public void onUserPresent() {
            }
        });

        mHomeWatcher  = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener()
        {
            @Override
            public void onHomePressed()
            {
                finish();
            }
            @Override
            public void onHomeLongPressed()
            {
                finish();
            }
        });
        mHomeWatcher.startWatch();
    }

    private void initViews() {

        takePhoto = (Button) findViewById(R.id.take_photo);
        selectPicture = (Button)findViewById(R.id.select_picture);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.surface_view);
        cameraPreview = new CameraPreview(this);
        camera = cameraPreview.getCamera();
        frameLayout.addView(cameraPreview);


        cameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.autoFocus(null); // 设置自动对焦
            }

        });


        takePhoto.setOnClickListener(new View.OnClickListener() { // 点击拍照
            @Override
            public void onClick(View view) {
                camera = cameraPreview.getCamera();
                //得到照相机的参数
                Camera.Parameters parameters = camera.getParameters();
                //图片的格式
                parameters.setPictureFormat(ImageFormat.JPEG);
                //设置对焦模式，自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                //对焦成功后，自动拍照
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            // 这个是实现相机拍照的主要方法，包含了三个回调参数。
                            // shutter是快门按下时的回调，raw是获取拍照原始数据的回调(只有高级手机相机才支持获得原始格式的数据)
                            // 绝大多数手机是将raw格式数据转为jpeg格式输出保存到本地
                            camera.takePicture(null, raw, mPictureCallback);
//                            camera.stopPreview();
                        }
                    }
                });
            }
        });



        selectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PictureFileUtil.openGalleryPic(MyCameraActivity.this, REQUEST_CODE_IMAGE);//选择已有的照片

//            startActivity(new Intent(MyCameraActivity.this, DataPictureActivity.class));
                Intent intent = new Intent(MyCameraActivity.this, DataPictureActivity.class);
                intent.putExtra("flag", 3);
//                intent.putExtra("data", data);
                startActivityForResult(intent, REQUEST_CODE);


            }
        });


    }

    // 获得最原始的 raw 格式数据，只有具有导出raw格式图像功能的手机才会被调用，一般raw格式图像至少几十M大小
    private Camera.PictureCallback raw = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {
            Log.i(TAG, "raw：");
            if (data != null) {
                Log.d(TAG, "onPictureTaken: " + data.length / 1024 + "K");
            }
        }
    };


    // 创建jpeg图片回调数据对象，可在这里对图片数据byte[] data进行压缩等操作，以及在此决定是否将图片保存到本地
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "jpeg: " + data.length / 1024 + "K");
            int size = data.length / 1024; // xx K Bytes
            // 根据图片大小，选择压缩的质量因子
            if (size > 500) { // > 500 KB
                QUALITY = 20;
            } else if (size > 300) {
                QUALITY = 30;
            } else if (size > 200) {
                QUALITY = 40;
            } else if (size > 100) {
                QUALITY = 50;
            }

            // 对图片进行压缩后还是以byte[] 返回
            Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
            // 不管是竖拍还是横拍， width > height
            Log.d(TAG, "bitmap width*height: " + b.getWidth() + "*" + b.getHeight()); // 1920*1080
            ByteArrayOutputStream bos = null;
            bos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, QUALITY, bos);  // 将图片压缩到流中
            jpegData = bos.toByteArray();


            // 对图片进行旋转，后面的尺寸变正常了
            int orientation = MyUtils.getOrientation(jpegData);
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
            if (orientation != -1) {
                bitmap = MyUtils.rotateBitmap(bitmap, orientation);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                jpegData = baos.toByteArray();
            }



            // 预览拍照图像（压缩过后）
            showPreviewPic(jpegData);
            Log.d(TAG, "final jpeg: " + jpegData.length / 1024 + "K");  // 301k --> 67k
        }
    };


    // 注：intent传数据似乎不能超过 1M ，故若传输原始JPEG图像（现在一般几M）肯定不行，得先压缩后再传输
    private void showPreviewPic(byte[] data) {
        Intent intent = new Intent(MyCameraActivity.this, ShowMultiImageActivity.class);
        intent.putExtra("flag", 3);
        intent.putExtra("data", data);
        startActivityForResult(intent, REQUEST_CODE);
    }


    // 预览后决定 重拍 还是 完成
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int flag = data.getIntExtra("flag", -1);
                String path = data.getStringExtra(ShowMultiImageActivity.PIC_PATH);
                Log.d(TAG, " onActivityResult:: flag = " + flag);
                Log.d(TAG, " onActivityResult:: path = " + path);
                switch (flag) {
                    case 1:// TODO: 2022/3/16 重新拍
                        jpegData = null;
                        camera = cameraPreview.getCamera();
                        camera.startPreview();
                        break;
                    case 2:// TODO: 2022/3/16 发送拍好照的这张
                        Intent intent = new Intent();
                        intent.putExtra(ShowMultiImageActivity.PIC_PATH, path);
                        intent.putExtra(NEWCAMERADATA, jpegData);
                        System.out.println("MyCameraActivity最后的照片"+ Arrays.toString(jpegData));
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                    case 3: // TODO: 2022/3/16 选择的是图片显示列表中的

                        byte[] picByte = data.getByteArrayExtra("new_camera_data");//获取存储的照片
                        String newcamerapicturePath = data.getStringExtra("newcamerapicturePath");

                        Intent intentsaved = new Intent();
                        intentsaved.putExtra(NEWCAMERADATA, picByte);
                        intentsaved.putExtra("newcamerapicturePath", newcamerapicturePath);// TODO: 2022/3/16 把图片路径也传过去

                        System.out.println("MyCameraActivity最后的照片"+ Arrays.toString(picByte));
                        setResult(RESULT_OK, intentsaved);
                        finish();


                        break;
                }
            }
        }


        if (requestCode == REQUEST_CODE_IMAGE){//选择已有的照片的回调
            // 图片选择结果回调
//            List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
//            for (LocalMedia media : selectListPic) {
//                System.out.println("图片："+media.getPath());
//            }


            if (data!=null)// TODO: 2021/8/26 增加null判断，不然在不选择图片情况下会闪退 
                setResult(RESULT_OK,data);
            finish();


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        screenListener.unregisterListener();//注销广播接收器
        mHomeWatcher.stopWatch();

    }
}