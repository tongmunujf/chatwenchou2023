package com.ucas.chat.ui.download;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.cloud.progressbar.CircleProgressBar;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.ucas.chat.R;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.utils.AriaDownLoadUtils;
import com.ucas.chat.utils.EventBusEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;

/**
 * create by an_huang
 * on 2021/4/20
 */
public class DownloadActivity extends BaseActivity {
    private AriaDownLoadUtils downLoadUtils;
    private CircleProgressBar progressBar;
    private TextView tvFileName;
    private Button btnDownLoad;
    private String fileName,downloadUrl;

    private String fileSavePath = Environment.getExternalStorageDirectory().getPath() + "/chat";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_download);
        fileName = getIntent().getStringExtra("fileName");
        EventBus.getDefault().register(this);
        downLoadUtils = new AriaDownLoadUtils(this);
        initView();
    }

    private void initView() {
        findViewById(R.id.imBack).setOnClickListener(this);
        progressBar = findViewById(R.id.circle_progress);
        btnDownLoad = findViewById(R.id.commit);
        btnDownLoad.setOnClickListener(this);
        tvFileName = findViewById(R.id.fileName);
        tvFileName.setText(TextUtils.isEmpty(fileName)?"":fileName);

        String path = fileSavePath+"/"+fileName;
        File file = new File(path);
        if (file.exists()){
            btnDownLoad.setText("打开文件");
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusEvent event) {
        if (event != null) {
            switch (event.msg){
                case "taskStart":
                    btnDownLoad.setText("下载中...");
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                    progressBar.setText(0+"%");
                    break;
                case "running":
                    String downloadStatus = (String) event.data;
                    String[] strings = new String[2];
                    strings = downloadStatus.split(",");
                    progressBar.setProgress(Integer.valueOf(strings[0]));
                    progressBar.setText(strings[0]+"%");
                    break;
                case "taskComplete":
                    progressBar.setProgress(100);
                    progressBar.setText(100+"%");
//                    if ()
                    progressBar.setVisibility(View.GONE);
                    btnDownLoad.setText("下载完成");
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.imBack:
                finish();
                break;
            case R.id.commit:
                if ("开始下载".equals(btnDownLoad.getText().toString())){
                    downloadUrl = "https://img.jingmaiwang.com/download/jmw_142_v1.3.9.apk";
                    downLoadUtils.start(downloadUrl,fileName);
                }else if ("下载中...".equals(btnDownLoad.getText().toString())){
                    downLoadUtils.stop();
                    btnDownLoad.setText("继续下载");
                }else if ("继续下载".equals(btnDownLoad.getText().toString())){
                    downLoadUtils.reStart();
                    btnDownLoad.setText("下载中...");
                }else if("打开文件".equals(btnDownLoad.getText().toString())){
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("style", "1");
                    params.put("local", "true");
                    String openFilePath = fileSavePath+"/"+fileName;
                    QbSdk.openFileReader(DownloadActivity.this, openFilePath, params, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e("TAG","s="+s);
                        }
                    });
                    openFilePath = "";
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downLoadUtils.unRegister();
        EventBus.getDefault().unregister(this);
    }
}
