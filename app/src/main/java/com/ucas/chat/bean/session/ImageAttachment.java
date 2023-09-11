package com.ucas.chat.bean.session;

import com.ucas.chat.bean.session.message.FileAttachment;

public class ImageAttachment extends FileAttachment {
    private int width;
    private int height;

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
}
