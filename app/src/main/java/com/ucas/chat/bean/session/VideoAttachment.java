package com.ucas.chat.bean.session;

import com.ucas.chat.bean.session.message.FileAttachment;

/**
 * 视频消息
 */
public class VideoAttachment extends FileAttachment {

    private int width;
    private int height;
    private long duration;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
