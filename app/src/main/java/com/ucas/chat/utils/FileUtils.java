package com.ucas.chat.utils;

import static com.ucas.chat.MyApplication.getContext;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.tor.util.FilePathUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文件工具类
 * Created by wudeng on 2017/7/24.
 */

public class FileUtils {
    private static String TAG = ConstantValue.TAG_CHAT + "FileUtils";
    /**
     * 递归创建文件夹，从最上层文件夹开始，只要不存在就会新建
     * @param dirPath 文件夹完整路径
     */
    public static void mkDir(String dirPath) {
        String[] dirArray = dirPath.split("/");
        String pathTemp = "";
        for (int i = 1; i < dirArray.length; i++) {
            pathTemp = pathTemp + "/" + dirArray[i];
            File newF = new File(dirArray[0] + pathTemp);
            if (!newF.exists()) {
                newF.mkdir();
            }
        }
    }

    /**
     * 文件转 byte 数组
     * @param file 待转换文件
     * @return byte[]
     * @throws IOException 转换出错
     */
    public static byte[] file2byte(File file) throws IOException {
        byte[] bytes = null;
        if (file != null) {
            InputStream is = new FileInputStream(file);
            int length = (int) file.length();
            if (length >= Integer.MAX_VALUE) {
                Log.e("FileUtils","this file is max ");
                is.close();
                return null;
            }
            bytes = new byte[length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            is.close();
            if (offset < bytes.length) {
                Log.e("FileUtils","file length is error");
                return null;
            }
        }
        return bytes;
    }

    /**
     * 从Uri获取媒体文件，如图片或音频文件
     * @param context 上下文
     * @param uri 从 content provider 或者 onActivityResult 返回的 Uri
     * @return Uri 的媒体文件
     */
    public static File getFileFromMediaUri(Context context, Uri uri) {
        if (uri.getScheme().compareTo("content") == 0) {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(uri, null, null, null, null);// 根据Uri从数据库中找
            if (cursor != null) {
                cursor.moveToFirst();
                String filePath = cursor.getString(cursor.getColumnIndex("_data"));// 获取图片路径
                cursor.close();
                if (filePath != null) {
                    return new File(filePath);
                }
            }
        } else if (uri.getScheme().compareTo("file") == 0) {
            return new File(uri.toString().replace("file://", ""));
        }
        return null;
    }

    /**
     * 解压文件或文件夹到指定路径
     *
     * @param zipFilePath  待解压文件路径
     * @param outPath      输出根目录路径
     * @throws Exception 解压出错
     */
    public static void unZipFolder(String zipFilePath, String outPath) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry zipEntry;
        String szName;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPath + File.separator + szName);
                folder.mkdirs();
            } else {
                File file = new File(outPath + File.separator + szName);
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inZip.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }

    /**
     * 读取assets中的文件
     * @param fileName
     * @param context
     * @return
     * String result = getJson("province.txt");
     * //将读出的字符串转换成JSONobject
     *  JSONObject jsonObject = new JSONObject(str);
     *  //获取JSONObject中的数组数据
     *  jsonArray = jsonObject.getJSONArray(arrName);
     *  Type listType = new TypeToken<List<SearchCompanyResultBean>>() {
     *         }.getType();
     *   //这里的json是字符串类型 = jsonArray.toString();
     * List<SearchCompanyResultBean> list = new Gson().fromJson(json, listType );
     */
    public static String getAssetsFileToJson(String fileName,Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void copy_file_from_sdcard(Context context){
        Log.d(" copy_file_from_sdcard", " begin work.....");
        String v3Dirpath =  FilePathUtils.v3Dirpath;
        String sdcard_chat_user_hostname = FilePathUtils.USER_INFO_FILE + "/" + FilePathUtils.HOSTNAME;
        String sdcard_chat_user_ed25519 = FilePathUtils.USER_INFO_FILE + "/" + FilePathUtils.hs_ed25519_secret_key;
        Log.d(TAG, " copy_file_from_sdcard:: sdcard_chat_user_hostname = " + sdcard_chat_user_hostname);
        Log.d(TAG, " copy_file_from_sdcard:: sdcard_chat_user_ed25519 = " + sdcard_chat_user_ed25519);
        File file = new File(v3Dirpath);
        if(!file.exists()){
            file.mkdirs();
        }
        AssetManager assetManager = context.getAssets();
        BufferedInputStream bis=null;
        BufferedOutputStream bos=null;
        try {
            bis = new BufferedInputStream(new FileInputStream(sdcard_chat_user_hostname));
            bos = new BufferedOutputStream(new FileOutputStream(v3Dirpath+"/hostname"));

            byte[] buffer = new byte[1024];
            int readLen=0;
            while((readLen = bis.read(buffer))!=-1){
                bos.write(buffer,0,readLen);
            }
            if(bis!=null){
                bis.close();
            }
            if(bos!=null){
                bos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //bis = new BufferedInputStream(assetManager.open("hs_ed25519_secret_key"));
            bis = new BufferedInputStream(new FileInputStream(sdcard_chat_user_ed25519));
            bos = new BufferedOutputStream(new FileOutputStream(v3Dirpath+"/" + FilePathUtils.hs_ed25519_secret_key));

            byte[] buffer = new byte[1024];
            int readLen=0;
            while((readLen = bis.read(buffer))!=-1){
                bos.write(buffer,0,readLen);
            }
            if(bis!=null){
                bis.close();
            }
            if(bos!=null){
                bos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getFileName(String path){
        File file = new File(path);
        return file.getName();
    }

    /**
     * 获取文件后缀的方法
     *
     * @param path 要获取文件后缀的文件
     * @return 文件后缀
     */
    public static String getFileExtension(String path) {
        String extension = "";
        File file = new File(path);
        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                extension = name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) {
            extension = "";
        }
        return extension;
    }

    public static int getFileSize(String path){
        File f= new File(path);
        if (f.exists() && f.isFile()){

            return (int) (f.length()/1024);
        }else {
            return 0;
        }
    }

    public static void delectPicture(String picturePath){
        File file = new File(picturePath);
        file.delete();
    }


    public static String copy_file(String fileName){
        String content="";
        AssetManager assetManager = getContext().getResources().getAssets();
        try {
            InputStream is = assetManager.open(fileName);
            int length = is.available();// TODO: 2021/7/14 直接读取文件内容大小
            byte[] buf =new byte[length];// TODO: 2021/7/14 改为文件大小 // TODO: 2021/7/13 改为1024
            is.read(buf);
            content = new String(buf,"utf-8");// TODO: 2021/7/13 utf-8格式
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }



}
