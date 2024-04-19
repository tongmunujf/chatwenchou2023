package com.ucas.chat.utils;

import static com.ucas.chat.MyApplication.getContext;

import android.util.Log;

import com.ucas.chat.bean.KeyInforBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.db.KeyHelper;
import com.ucas.chat.db.KeyHelperTool;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AesTools {
    public static final String TAG = ConstantValue.TAG_CHAT + "AesTools";

    public enum AesKeyTypeEnum {
       MESSAGE_TYPE, COMMON_KEY;
    }

   private static String getAesKey(AesKeyTypeEnum keyType){
       KeyInforBean byKeyNameBean = null;
       switch (keyType){
           case MESSAGE_TYPE:
               byKeyNameBean = KeyHelper.getInstance(getContext()).queryByKeyName(KeyHelperTool.KEY_NAME_ARR[0]);
               break;
           case COMMON_KEY:
               byKeyNameBean = KeyHelper.getInstance(getContext()).queryByKeyName(KeyHelperTool.KEY_NAME_ARR[1]);
               break;
           default:
               LogUtils.d(TAG, " keyType错误");
               break;
       }
       String key = byKeyNameBean.getKeyValue();
       return key;
   }

    /**
     * 解密数据
     * @param content
     * @return
     */
    public static String getDecryptContent(String content, AesKeyTypeEnum keyType){
        //String key = "08c08c24f1f2a444";
        String decryptContent = null;
        String key = getAesKey(keyType);
        //852099ded7ee76aa
        LogUtils.d(TAG, " getDecryptContent:: content = " + content );
        LogUtils.d(TAG, " getDecryptContent:: keyType = " + keyType + " key = " + key);
        try {
            decryptContent = AesUtils.decrypt(key, content.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtils.d(TAG, " getDecryptContent:: decryptValue = " + decryptContent);
        return decryptContent;
    }

    /**
     * 加密数据
     * @param content
     * @return
     */
    public static String getEncryptContent(String content, AesKeyTypeEnum keyType){
        String encryptContent = null;
        String key = getAesKey(keyType);
        LogUtils.d(TAG, " getEncryptContent:: content = " + content );
        LogUtils.d(TAG, " getEncryptContent:: keyType = " + keyType + " key = " + key);
        try {
            encryptContent = AesUtils.encrypt(key, content.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, " getEncryptContent:: " + content + " 加密后——》" + encryptContent);
        return encryptContent;
    }


    /**
     * 对byte[]加密
     */
    public static byte[] getEncryptBytes(byte[] data, AesKeyTypeEnum keyType) {
        String key = getAesKey(keyType);
        Log.d(TAG, " encryptBytes:: key = " + key);
        byte[] bytesKey = key.getBytes();
        Log.d(TAG, " encryptBytes:: bytesKey = " + bytesKey.toString());
        if (key.equals(bytesKey.toString())){
            Log.d(TAG, " encryptBytes:: key相等");
        }
        //SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytesKey, "AES");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, " encryptBytes:: NoSuchAlgorithmException e = " + e.toString());
        } catch (NoSuchPaddingException e) {
            Log.d(TAG, " encryptBytes:: NoSuchPaddingException e = " + e.toString());
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        } catch (InvalidKeyException e) {
            Log.d(TAG, " encryptBytes:: InvalidKeyException e = " + e.toString());
        }
        byte[] result = new byte[0];
        try {
            result = cipher.doFinal(data);
        } catch (BadPaddingException e) {
            Log.d(TAG, " encryptBytes:: BadPaddingException e = " + e.toString());
        } catch (IllegalBlockSizeException e) {
            Log.d(TAG, " encryptBytes:: IllegalBlockSizeException e = " + e.toString());
        }
        return result;
    }

    /**
     * byte[]数组解密
     */
    public static byte[] getDecryptBytes(byte[] encryptedData, AesKeyTypeEnum keyType){
        String key = getAesKey(keyType);
        Log.d(TAG, " decryptBytes:: key = " + key);
        byte[] bytesKey = key.getBytes();
        Log.d(TAG, " decryptBytes:: bytesKey = " + bytesKey.toString());
        if (key.equals(bytesKey.toString())){
            Log.d(TAG, " decryptBytes:: key相等");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytesKey, "AES");
        //SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, " decryptBytes:: NoSuchAlgorithmException e = " + e.toString());
        } catch (NoSuchPaddingException e) {
            Log.d(TAG, " decryptBytes:: NoSuchPaddingException e = " + e.toString());
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        } catch (InvalidKeyException e) {
            Log.d(TAG, " decryptBytes:: InvalidKeyException e = " + e.toString());
        }
        byte[] result = new byte[0];
        try {
            result = cipher.doFinal(encryptedData);
        } catch (BadPaddingException e) {
            Log.d(TAG, " decryptBytes:: BadPaddingException e = " + e.toString());
        } catch (IllegalBlockSizeException e) {
            Log.d(TAG, " decryptBytes:: IllegalBlockSizeException e = " + e.toString());
        }
        return result;
    }

    /**
     * userinfor.txt
     * asdf
     */
    public static void test(){
        String value = "asdf";
        String encryptContent = getEncryptContent(value, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test:: userinfor.txt 加密asdf：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test:: userinfor.txt 解密asdf：decryptContent = " + decryptContent);
    }

}
