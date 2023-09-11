package com.ucas.chat.ui.camera.adapter;

import android.graphics.Bitmap;

/**
 * @auther :haoyunlai
 * date         :2022/3/16 16:46
 * e-mail       :2931945387@qq.com
 * usefulness   :照片的参数
 */
public class PictureParam {

    String picturePath;// TODO: 2022/3/16 照片的路径 
    Bitmap bitmap;// TODO: 2022/3/16 照片具体的资源 

    public PictureParam(String picturePath, Bitmap bitmap) {
        this.picturePath = picturePath;
        this.bitmap = bitmap;
    }


    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    @Override
    public String toString() {
        return "PictureParam{" +
                "picturePath='" + picturePath + '\'' +
                ", bitmap=" + bitmap +
                '}';
    }
}