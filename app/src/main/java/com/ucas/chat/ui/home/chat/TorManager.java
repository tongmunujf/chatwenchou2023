package com.ucas.chat.ui.home.chat;

import android.content.Context;
import android.content.Intent;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.utils.LogUtil;
import com.ucas.chat.utils.LogUtils;

import org.torproject.android.service.OrbotService;


import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import static org.torproject.android.service.TorServiceConstants.ACTION_START;

public class TorManager {
    public static final String TAG = ConstantValue.TAG_CHAT + "TorManager";

    public static void startTor(Context context) {
        Intent intent = new Intent(context, OrbotService.class);//从当前的位置跳转到下一个activity中
        intent.setAction(ACTION_START);
        context.startService(intent);
        LogUtils.d(TAG, " startTor:: " );
    }

}
