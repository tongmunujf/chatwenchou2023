package com.ucas.chat.bean.session.message;

import org.json.JSONObject;

public class AudioAttachment extends FileAttachment{

    private long duration;

    public AudioAttachment() {

    }

    public AudioAttachment(String attach) {
        super(attach);
    }

    /**
     * 获取音频的播放时长
     * @return 播放时长，单位:ms
     */
    public long getDuration() {
        return duration;
    }

    /**
     * 设置音频的播放时长
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

}
