package com.ucas.chat.ui.view.voice.dialog;



import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class MYAudio  implements Serializable {
    public String audio_url;//语音路径
    public int length;//语音时长(单位s)

    public MYAudio(String path, int length) {
        this.audio_url=path;
        this.length=length;
    }

    public int getSoundsLength() {
        return length;
    }

    public String getSounds() {
        return audio_url;
    }

    public boolean isNetUrl() {
        return audio_url!=null&&audio_url.toLowerCase().startsWith("http");
    }


    public void startSound() {// TODO: 2022/3/29 播放声音

        MediaPlayer mediaPlayer;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setLooping(false);
            mediaPlayer.setDataSource(audio_url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();//释放
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
//                    Log.d(TAG, "Play local sound onError: " + i + ", " + i1);
                    return true;
                }
            });
        } catch (Exception e) {
//            Log.e(TAG, "playAudioFile: ", e);
        }

    }




}
