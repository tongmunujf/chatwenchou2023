package com.ucas.chat.utils;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.FileUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class AriaDownLoadUtils {
    String TAG = "AriaDownLoadUtils";
    private Context mContext;
    private String mUrl;
    private String mFileName;
    private long mTaskId = -1;

    public AriaDownLoadUtils(Context context) {
        Aria.download(this).register();
        mContext = context;
    }

    @Download.onWait
    public void onWait(DownloadTask task) {
        Log.d(TAG, "wait ==> " + task.getDownloadEntity().getFileName());
    }

    @Download.onPre
    protected void onPre(DownloadTask task) {
        Log.d(TAG, "onPre");
    }

    @Download.onTaskStart
    public void taskStart(DownloadTask task) {
        Log.d(TAG, "onStart");
        EventBus.getDefault().post(new EventBusEvent("taskStart"));
    }

    @Download.onTaskRunning
    protected void running(DownloadTask task) {
        Log.d(TAG, "running task.getPercent()= "+task.getPercent());// 获取百分比进度
        Log.d(TAG, "running task.getPercent()= "+task.getConvertSpeed());// 获取速度
        // eventBus --->  把消息发送出去  activity 接收 更新下载进度
        String percentAndSpeed = task.getPercent() + "," + task.getConvertSpeed();
        EventBus.getDefault().post(new EventBusEvent("running",percentAndSpeed));
    }

    @Download.onTaskResume
    public void taskResume(DownloadTask task) {
        Log.d(TAG, "resume");
    }

    @Download.onTaskStop
    public void taskStop(DownloadTask task) {
        Log.d(TAG, "stop");
        EventBus.getDefault().post(new EventBusEvent("taskStop"));
    }

    @Download.onTaskCancel
    public void taskCancel(DownloadTask task) {
        Log.d(TAG, "cancel");
        EventBus.getDefault().post(new EventBusEvent("taskCancel"));
    }

    @Download.onTaskFail
    public void taskFail(DownloadTask task) {
        Log.d(TAG, "fail");
        EventBus.getDefault().post(new EventBusEvent("taskFail"));
    }

    @Download.onTaskComplete
    public void taskComplete(DownloadTask task) {
        Log.d(TAG, "path ==> " + task.getDownloadEntity().getServerFileName());
        // eventBus --->  把消息发送出去  activity 接收
        EventBus.getDefault().post(new EventBusEvent("taskComplete"));
    }


    public void start(String url,String fileName) {
        mUrl = url;
//        String path = Environment.getExternalStorageDirectory().getPath() + "/chat";
//        if (new File(path).exists()){
//
//        }else {
//
//        }
        FileUtil.createDir(Environment.getExternalStorageDirectory().getPath() + "/chat");
        mTaskId = Aria.download(this)
                .load(url)
                .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/chat/"+fileName)
                .resetState()
                .create();

    }

    public void reStart() {
        Aria.download(this).load(mTaskId).resume();
    }

    public void stop() {
        Aria.download(this).load(mTaskId).stop();
    }

    public void cancel() {
        Aria.download(this).load(mTaskId).cancel();
    }

    public void unRegister() {
        Aria.download(this).unRegister();
    }
}

