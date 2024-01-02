package com.ucas.chat.utils;


import com.ucas.chat.bean.contact.ConstantValue;

import org.apaches.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AesUtils {

    public static final String TAG = ConstantValue.TAG_CHAT + "AesUtils";


    public static String encrypt(String key, String str) throws Exception {
        String ivParameter = key.substring(0, 16);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] raw = key.getBytes();  // 密钥转成byte
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes()); //偏移量转成byte
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encrypted); //base64编码
        } catch (Exception ex) {
            return null;
        }
    }

    public static String decrypt(String key, String str) throws Exception {
        String ivParameter = key.substring(0, 16);
        try {
            byte[] raw = key.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes()); //偏移量
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.decodeBase64(str);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original, "utf-8");
        } catch (Exception ex) {
            return null;
        }
    }

}
