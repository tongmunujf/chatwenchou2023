package com.ucas.chat.ui.home.chat;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.bm.library.PhotoView;
import com.ucas.chat.R;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.bean.session.AttachStatusEnum;
import com.ucas.chat.bean.session.AttachmentProgress;
import com.ucas.chat.bean.session.ImageAttachment;
import com.ucas.chat.bean.session.message.IMMessage;
import com.ucas.chat.utils.ConvertUtils;
import com.ucas.chat.utils.TextUtils;
import com.ucas.chat.utils.ToastUtils;

/**
 * Created by wudeng on 2017/10/20.
 */

public class ShowImageActivity extends BaseActivity {

    private static final String TAG = ShowImageActivity.class.getSimpleName();

    private PhotoView mIvImage;
    private TextView mTvShowBig;

    private IMMessage mMessage;
    private Observer<AttachmentProgress> mProgressObserver;

    private boolean downloading = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (downloading && msg.what == 0x300){
                String path = ((ImageAttachment) mMessage.getAttachment()).getPath();
                if (!TextUtils.isEmpty(path)){
                    downloading =  false;
                    mTvShowBig.setVisibility(View.GONE);
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if (bitmap != null) {
                        mIvImage.setImageBitmap(bitmap);
                    }
                }else {
                    mHandler.sendEmptyMessageAtTime(0x300,1000);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_show_image);
        mIvImage = findViewById(R.id.image_view);
        mTvShowBig = findViewById(R.id.tv_show_big);
        mTvShowBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBigImage();
            }
        });

        mProgressObserver = new Observer<AttachmentProgress>() {
            @Override
            public void onChanged(AttachmentProgress progress) {
                if (downloading) {
                    int present = (int) (progress.getTransferred() /
                            (progress.getTotal() * 1.0f) * 100.0f);
                    String text =  "下载中…… " + String.valueOf(present) + "%";
                    mTvShowBig.setText(text);
                    // mProgressObserver 并不会回调 100% 下载，所以使用检查路径是否存在来判断
                    if (present >= 60){
                        mHandler.sendEmptyMessageAtTime(0x300,1000);
                    }
                }

            }
        };

        initImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        NIMClient.getService(MsgServiceObserve.class)
//                .observeAttachmentProgress(mProgressObserver, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        NIMClient.getService(MsgServiceObserve.class)
//                .observeAttachmentProgress(mProgressObserver, false);
    }

    private void initImage() {
        mIvImage.enable();
        mIvImage.setMaxScale(4);

        mMessage = (IMMessage) getIntent().getSerializableExtra("IMMessage");
        mTvShowBig.setVisibility(View.GONE);

        if (mMessage == null) {
            ToastUtils.showMessage(this, "图片无法显示，请重试~");
            finish();
            return;
        }

        String path = ((ImageAttachment) mMessage.getAttachment()).getPath();
        String thumbPath = ((ImageAttachment) mMessage.getAttachment()).getThumbPath();
        long size = ((ImageAttachment) mMessage.getAttachment()).getSize();

        // 原图已经下载，显示原图
        if (!TextUtils.isEmpty(path)) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                mIvImage.setImageBitmap(bitmap);
            }else {
                ToastUtils.showMessage(this,"原图 下载/显示 失败，请重试~");
                finish();
            }
        } else {
            // 显示缩略图
            if (!TextUtils.isEmpty(thumbPath)) {
                Bitmap bitmap = BitmapFactory.decodeFile(thumbPath);
                if (bitmap != null) {
                    mIvImage.setImageBitmap(bitmap);
                }
            }
            String sizeString = ConvertUtils.getSizeString(size);
            String text = "查看原图 (" + sizeString + ")";
            mTvShowBig.setText(text);
            mTvShowBig.setVisibility(View.VISIBLE);
        }
    }

    public void showBigImage() {
        downloading = true;
        mTvShowBig.setText("下载中……");
//        NIMClient.getService(MsgService.class)
//                .downloadAttachment(mMessage, false);
        mTvShowBig.setClickable(false);
    }

}

