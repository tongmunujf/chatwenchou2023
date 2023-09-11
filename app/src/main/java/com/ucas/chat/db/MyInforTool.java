package com.ucas.chat.db;


import android.content.res.AssetManager;
import android.util.Log;

import com.ucas.chat.bean.GuardNodeBean;
import com.ucas.chat.bean.MyInforBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.MyApplication;
import com.ucas.chat.utils.AesUtils;
import com.ucas.chat.utils.FileUtils;
import com.ucas.chat.utils.LogUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.ucas.chat.MyApplication.getContext;
import static com.zlylib.upperdialog.utils.ResUtils.getResources;

import org.apaches.commons.codec.digest.DigestUtils;


public class MyInforTool {

    private  String ACCOUNT="";
    private  String PASSWORD="";
    private  String NICK_NAME="";
    private  int GENDER= 1;
    private  String HEADIMAGEPATH="";
    private  String ONIONNAME="";
    private  String RSA_PRIVATE_KEY="";
    private  String RSA_PUBLIC_KEY="";

    public static final String TAG = ConstantValue.TAG_CHAT + "MyInforTool";
    private static final MyInforTool mInstance = new MyInforTool();
    private  MyInforTool(){}
    public static MyInforTool getInstance(){
        return mInstance;
    }


    public MyInforBean insertMyInforBean(){
        String[] splited = FileUtils.copy_file("userinfo.txt").split(" ");
        ACCOUNT = splited[0];
        PASSWORD = splited[1];
        NICK_NAME = splited[2];
        LogUtils.d(TAG, " insertMyInforBean:: userinfo.txt ACCOUNT：" + ACCOUNT + " PASSWORD：" + PASSWORD + " NICK_NAME：" + NICK_NAME);

        HEADIMAGEPATH = "Temporarily none";

        ONIONNAME = FileUtils.copy_file("hostname").trim();
        LogUtils.d(TAG, " insertMyInforBean:: hostname.txt ONIONNAME：" + ONIONNAME);

        String Str = FileUtils.copy_file("client_private_key");
        String Str1 = Str.replaceAll("-----BEGIN PRIVATE KEY-----\r\n","");
        RSA_PRIVATE_KEY = Str1.replaceAll("-----END PRIVATE KEY-----\r\n","").trim();
        LogUtils.d(TAG, " insertMyInforBean:: client_private_key.txt Str1：" + Str1);
        LogUtils.d(TAG, " insertMyInforBean:: client_private_key.txt RSA_PRIVATE_KEY：" + RSA_PRIVATE_KEY);

        String Stri = FileUtils.copy_file("client_public_key");
        String Stri1 = Stri.replaceAll("-----BEGIN PUBLIC KEY-----\r\n","");
        RSA_PUBLIC_KEY = Stri1.replaceAll("-----END PUBLIC KEY-----\r\n","").trim();
        LogUtils.d(TAG, " insertMyInforBean:: client_public_key.txt Stri1：" + Stri1);
        LogUtils.d(TAG, " insertMyInforBean:: client_private_key.txt RSA_PUBLIC_KEY：" + RSA_PUBLIC_KEY);

        MyInforBean bean = new MyInforBean(ACCOUNT,PASSWORD,NICK_NAME,GENDER,HEADIMAGEPATH,
                ONIONNAME,RSA_PUBLIC_KEY,RSA_PRIVATE_KEY);// TODO: 2021/7/16  RSA_PRIVATE_KEY,RSA_PUBLIC_KEY改为RSA_PUBLIC_KEY,RSA_PRIVATE_KEY
        return bean;
    }

    /**
     * 解密数据
     * @param value
     * @return
     */
    public String getDecryptValue(String value){
        String[] splited = FileUtils.copy_file("userinfo.txt").split(" ");
        String key = "08c08c24f1f2a444";
       // String key = DigestUtils.sha256Hex(splited[1]).substring(0, 16);
        LogUtils.d(TAG, " getDecryptValue:: key = " + key);
        String decryptValue = null;
        try {
            decryptValue = AesUtils.decrypt(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtils.d(TAG, " getDecryptValue:: decryptValue = " + decryptValue);
        return decryptValue;
    }

    /**
     * 加密数据
     * @param value
     * @return
     */
    public String getEncryptValue(String value){
        String key = "08c08c24f1f2a444";
        String result = null;
        Log.e("AesUtils", " getEncryptValue:: key = " + key);
        try {
            result = AesUtils.encrypt(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AesUtils", " getEncryptValue:: " + value + " 加密后——》" + result);
        return result;
    }
}
