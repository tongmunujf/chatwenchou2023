package com.ucas.chat.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ucas.chat.R;
import com.ucas.chat.bean.ImageSize;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.ui.login.PhotoActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Target;

/**
 * @author huangrui
 * @date 2018/8/31 10:57
 * @desc
 */
public class BitmapUtil {
    public static final String TAG = ConstantValue.TAG_CHAT + "BitmapUtil";


     public static ImageSize getImageSize(Bitmap bitmap) {
        ImageSize imageSize = new ImageSize();
        if (null == bitmap || bitmap.isRecycled()) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteTmp = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteTmp, 0, byteTmp.length, bitmapOptions);
        int outWidth = bitmapOptions.outWidth;
        int outHeight = bitmapOptions.outHeight;
        int maxWidth = 400;
        int maxHeight = 400;
        int minWidth = 150;
        int minHeight = 150;
        if (outWidth / maxWidth > outHeight / maxHeight) {//
            if (outWidth >= maxWidth) {//
                imageSize.setWidth(maxWidth);
                imageSize.setHeight(outHeight * maxWidth / outWidth);
            } else {
                imageSize.setWidth(outWidth);
                imageSize.setHeight(outHeight);
            }
            if (outHeight < minHeight) {
                imageSize.setHeight(minHeight);
                int width = outWidth * minHeight / outHeight;
                if (width > maxWidth) {
                    imageSize.setWidth(maxWidth);
                } else {
                    imageSize.setWidth(width);
                }
            }
        } else {
            if (outHeight >= maxHeight) {
                imageSize.setHeight(maxHeight);
                imageSize.setWidth(outWidth * maxHeight / outHeight);
            } else {
                imageSize.setHeight(outHeight);
                imageSize.setWidth(outWidth);
            }
            if (outWidth < minWidth) {
                imageSize.setWidth(minWidth);
                int height = outHeight * minWidth / outWidth;
                if (height > maxHeight) {
                    imageSize.setHeight(maxHeight);
                } else {
                    imageSize.setHeight(height);
                }
            }
        }

        return imageSize;
    }


    /**
     * 文件路径是否存在
     * @param fileName
     * @return
     */
    public static String filePathIsExists(String fileName){
         String path = FilePathUtils.RECIEVE_FILE_PATH + fileName;
         File file = new File(path);
         if (file.exists()){
             Log.d(TAG, " filePathIsExists:: path = " + path);
             return path;
         }else {
             path = FilePathUtils.TARGET_FILE_PATH + fileName;
             file = new File(path);
         }
         if (file.exists()){
             Log.d(TAG, " filePathIsExists:: path = " + path);
             return path;
         }
        Log.d(TAG, " filePathIsExists:: 图片路径不存在");
         return null;
    }

    /**
     * 文件是否为图片
     * @param path
     * @return
     */
    public static boolean isPicFile(String path){
         boolean isPic = false;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        if (options.outWidth != -1 && options.outHeight != -1) {
            // This is an image file.
            isPic = true;
        }
        return isPic;
    }

    /**
     * 接收图片，进行预览
     * @param context
     * @param fileName
     */
    public static void picturePreview(Context context, String fileName){
        String path = FilePathUtils.RECIEVE_FILE_PATH + fileName;
        File file = new File(path);
        if (file.exists()){
            showPhoto(context, fileName);
            return;
        }else {
            path = FilePathUtils.TARGET_FILE_PATH + fileName;
        }
        file = new File(path);
        if (file.exists()){
           showPhoto(context, fileName);
        }else {
            ToastUtils.showMessage(context, context.getString(R.string.file_path_error));
        }
    }

    private static void showPhoto(Context context, String fileName){
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra("PIC_NAME", fileName);
        context.startActivity(intent);
    }


}
