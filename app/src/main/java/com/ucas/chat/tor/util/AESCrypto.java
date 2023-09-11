package com.ucas.chat.tor.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.ucas.chat.tor.message.Message;

import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apaches.commons.codec.digest.DigestUtils;


public class AESCrypto {
	
	public static byte[] digest_fast(byte[] msg){
        return DigestUtils.sha1(msg);
    }	
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] hex16_to_bytes(String hexString) {
        hexString = hexString.replaceAll(" ", "");
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // ��λһ�飬��ʾһ���ֽ�,��������ʾ��16�����ַ�������ԭ��һ���ֽ�
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }
    
    public static int bytes_to_int(byte[] bytes) {
        int value = 0;
        //�ɸ�λ����λ
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;//����λ��
        }
        return value;
    }
    
    public static byte hexToByte(String inHex){
        return (byte)Integer.parseInt(inHex,16);
    }
    public static byte[] hexToByteArray(String inHex){
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1){
            //����
            hexlen++;
            result = new byte[(hexlen/2)];
            inHex="0"+inHex;
        }else {
            //ż��
            result = new byte[(hexlen/2)];
        }
        int j=0;
        for (int i = 0; i < hexlen; i+=2){
            result[j]=hexToByte(inHex.substring(i,i+2));
            j++;
        }
        return result;
    }

    public static byte[] encrypt(byte[] key, byte[] raw) throws Exception {//https://www.cnblogs.com/lianghui66/archive/2013/03/07/2948494.html
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");// 定义加密算法，key是你的byte数组定义的密钥，SecretKeySpec是采用某种加密算法加密后的密钥
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance(Config.AES_ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(Message.subBytes(key, 0, 16));
        cipher.init(Cipher.ENCRYPT_MODE, seckey, iv);
        byte[] result = cipher.doFinal(raw);
//        Base64.Encoder encoder = Base64.getEncoder();
//        return encoder.encodeToString(result);
//        return new String(result);
        return result;
    }

    /**
     * ���ܷ�����ʹ��key�䵱����iv�����Ӽ����㷨��ǿ��
     *
     * @param key ��Կ
     * @param enc ����������
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] decrypt(byte[] key, byte[] enc) throws Exception {//解密
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance(Config.AES_ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(Message.subBytes(key, 0, 16));
        cipher.init(Cipher.DECRYPT_MODE, seckey, iv);

        Base64.Encoder encoder = Base64.getEncoder();
        String output = encoder.encodeToString(enc);

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] result = cipher.doFinal(decoder.decode(output));
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void main(String[] args) throws Exception {
        //�ͻ��˴���
//        String text = "hello i am java aes";
        char[] chars = new char[2047];        //1790
        Arrays.fill(chars,'2');
        String text0 = new String(chars);
        System.out.println("ԭ���ַ���"+text0);
        byte[] text = text0.getBytes();
        System.out.println("ԭ��:\n" + RSACrypto.bytesToHex(text));
        System.out.println("ԭ�ĳ���:" + text.length);
        //�������16λaes��Կ
        byte[] aesKey = SecureRandomUtil.getRandom(16).getBytes();
        byte[] encryptText = AESCrypto.encrypt(aesKey, text);
        System.out.println("���ܺ�:\n" + bytesToHex(encryptText));
        System.out.println("���ܺ󳤶�:\n" + encryptText.length);
        byte[] decryptText = AESCrypto.decrypt(aesKey, encryptText);
        System.out.println("���ܺ�:\n" + bytesToHex(decryptText));
        System.out.println("���ܺ�:\n" + new String(decryptText));
        System.out.println("���ܺ󳤶�:\n" + decryptText.length);
    }
	public static byte[] paddingToLength(byte[] msg, int target_len) {
//	        System.out.println("message.ExternalPayload.padding_to_length begin working...");
	        Random random = new Random();
	        StringBuffer sb = new StringBuffer();
//	        sb.append(new String(msg));
	        String str = "abcdefghijklmnopqrstuvwxyz0123456789";
	        if (target_len - msg.length > 0) {
	            for (int i = 0; i < (target_len - msg.length); i++) {
	                int number = random.nextInt(36);
	                sb.append(str.charAt(number));
	            }
	        }
	        return sb.toString().getBytes();
	    }	
}
