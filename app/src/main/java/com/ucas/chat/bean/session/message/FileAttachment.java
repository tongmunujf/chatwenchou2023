package com.ucas.chat.bean.session.message;

import android.text.TextUtils;

import com.ucas.chat.utils.MD5;
import com.ucas.chat.utils.StringUtils;
import com.ucas.chat.utils.storage.StorageType;
import com.ucas.chat.utils.storage.StorageUtil;

import org.json.JSONObject;

import java.io.File;
import java.security.NoSuchAlgorithmException;

/**
 * 带有文件的附件类型的基类
 * 描述文件的相关信息
 */
public class FileAttachment implements MsgAttachment {

    /**
     * 文件路径
     */
    protected String path;
    /**
     * 缩略图路径
     */
    protected String thumbPath;
    /**
     * 文件大小
     */
    protected long size;

    /**
     * 文件内容的MD5
     */
    protected String md5;

    /**
     * 文件下载地址
     */
    protected String url;

    /**
     * 文件显示名
     */
    protected String displayName;

    /**
     * 文件后缀名
     */
    protected String extension;

    public FileAttachment() {

    }

    public FileAttachment(String attach) {
        fromJson(attach);
    }

    /**
     * 获取文件本地路径，若文件不存在，返回null
     * @return 文件路径
     */
    public String getPath() {
        String path = getPathForSave();
        return new File(path).exists() ? path : null;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    /**
     * 获取用于保存该文件的位置
     * @return 文件路径
     */
    public String getPathForSave() {
        if (!TextUtils.isEmpty(path)) {
            return path;
        } else {
            return StorageUtil.getWritePath(getFileName(), storageType());
        }
    }

    /**
     * 获取缩略图文件的本地路径，若文件不存在，返回null
     * @return 文件路径
     */
    public String getThumbPath() {
        String path = getThumbPathForSave();
        return new File(path).exists() ? path : null;
    }

    /**
     * 获取用于保存缩略图文件的位置
     * @return 文件路径
     */
    public String getThumbPathForSave() {
        return StorageUtil.getWritePath(getFileName(), StorageType.TYPE_THUMB_IMAGE);
    }

    /**
     * 设置文件路径
     * @param path 文件路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取文件大小，单位为byte
     * @return 文件大小
     */
    public long getSize() {
        return size;
    }

    /**
     * 设置文件大小，单位为byte
     * @param size 文件大小
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * 获取文件内容MD5
     * @return 内容MD5
     */
    public String getMd5() {
        return md5;
    }

    /**
     * 设置文件内容MD5
     * @param md5 内容MD5
     */
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    /**
     * 获取文件在服务器上的下载url。若文件还未上传，返回null
     * @return 文件url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置文件在服务器上的下载url
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取文件后缀名
     * @return 后缀名
     */
    public String getExtension() {
        return extension;
    }

    /**
     * 设置文件后缀名
     * @param extension 后缀名
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * 获取文件名。
     * @return
     */
    public String getFileName() {
        if (!TextUtils.isEmpty(path)) {
            return StringUtils.nameOfPath(path);
        } else {
            if (TextUtils.isEmpty(md5)) {
                return MD5.getStringMD5(url);
            } else {
                return md5;
            }
        }
    }

    /**
     * 获取文件的显示名。可以和文件名不同，仅用于界面展示
     * @return 文件显示名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置文件显示名
     * @param displayName 文件显示名
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    protected StorageType storageType() {
        return StorageType.TYPE_FILE;
    }

    protected void save(JSONObject json) {

    }
    protected void load(JSONObject json) {

    }

    private static final String KEY_PATH = "path";
    private static final String KEY_NAME = "name";
    private static final String KEY_SIZE = "size";
    private static final String KEY_MD5 = "md5";
    private static final String KEY_URL = "url";
    private static final String KEY_EXT = "ext";

    public String toJson(boolean send) {
        JSONObject object = new JSONObject();
        try {
            if (!send && !TextUtils.isEmpty(path)) {
                object.put(KEY_PATH, path);
            }

            if (!TextUtils.isEmpty(md5)) {
                object.put(KEY_MD5, md5);
            }

            if (!TextUtils.isEmpty(displayName)) {
                object.put(KEY_NAME, displayName);
            }

            object.put(KEY_URL, url);
            object.put(KEY_SIZE, size);



            if (!TextUtils.isEmpty(extension)) {
                object.put(KEY_EXT, extension);
            }

            save(object);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    private void fromJson(String attach) {
//        JSONObject json = JSONHelper.parse(attach);
//        path = JSONHelper.getString(json, KEY_PATH);
//        md5 = JSONHelper.getString(json, KEY_MD5);
//        url = JSONHelper.getString(json, KEY_URL);
//        displayName = JSONHelper.getString(json, KEY_NAME);
//        size = JSONHelper.getLong(json, KEY_SIZE);
//        extension = JSONHelper.getString(json, KEY_EXT);

     //   load(json);
    }
}
