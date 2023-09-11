package com.ucas.chat.tor.util;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class RSACrypto {
    /**
     * ���ܷ���
     *
     * @param publicKey ��Կ
     * @param raw       ����������
     * @return ���ܺ������
     * @throws Exception
     */
    public static byte[] encrypt(String publicKey, byte[] raw) throws Exception {
        Key key = getPublicKey(publicKey);
        Cipher cipher = Cipher.getInstance(Config.RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        byte[] b1 = cipher.doFinal(raw);
//        return Base64.encodeBase64(b1);
        return b1;
    }

    /**
     * ���ܷ���
     *
     * @param privateKey ˽Կ
     * @param enc        ����������
     * @return ���ܺ������
     * @throws Exception
     */
    public static byte[] decrypt(String privateKey, byte[] enc) throws Exception {
        byte[] output = Base64.encodeBase64(enc);
        Key key = getPrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance(Config.RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        return cipher.doFinal(Base64.decodeBase64(output));
    }

    /**
     * ��ȡ��Կ
     *
     * @param key ��Կ�ַ���������base64���룩
     * @return ��Կ
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(key.getBytes()));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * ��ȡ˽Կ
     *
     * @param key ��Կ�ַ���������base64���룩
     * @return ˽Կ
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key.getBytes()));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * ǩ��
     *
     * @param privateKey ˽Կ
     * @param content    Ҫ����ǩ��������
     * @return ǩ��
     */
    public static byte[] sign(String privateKey, byte[] content) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey.getBytes()));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(priKey);
            signature.update(content);
            byte[] signed = signature.sign();
//            return new String(Base64.encodeBase64(signed));
            return signed;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ��ǩ
     *
     * @param publicKey ��Կ
     * @param content   Ҫ��ǩ������
     * @param sign      ǩ��
     * @return ��ǩ���
     */
    public static boolean checkSign(String publicKey, byte[] content, byte[] sign) {
        try {
            String output = new String(Base64.encodeBase64(sign));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode2(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initVerify(pubKey);
            signature.update(content);
            return signature.verify(Base64.decode2(output));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    public static void main(String[] args) throws Exception {
        //�ͻ��˴���
//        String text = "hello i am java hhhhhh";
        char[] chars = new char[1192];
        Arrays.fill(chars,'1');
        String text = new String(chars);
        //ʹ�÷���˹�Կ����
//        byte[] encryptText = test1.encrypt(Config.SERVER_PUBLIC_KEY, text.getBytes());
//        String output1 = bytesToHex(encryptText);
//        System.out.println("���ܺ�:\n" + output1);
//        System.out.println("���ܺ󳤶�:" + encryptText.length);
        //ʹ�ÿͻ���˽Կǩ��
        byte[] signature = RSACrypto.sign(Constant.CLIENT_PRIVATE_KEY, text.getBytes());
        System.out.println("ǩ��:\n" + bytesToHex(signature));
        System.out.println("ǩ���󳤶�:" + signature.length);


        //����˴���
        //ʹ�ÿͻ��˹�Կ��ǩ
        boolean result = RSACrypto.checkSign(Constant.CLIENT_PUBLIC_KEY, text.getBytes(), signature);
        System.out.println("��ǩ:\n" + result);
        //ʹ�÷����˽Կ����
//        byte[] decryptText = test1.decrypt(Config.SERVER_PRIVATE_KEY, encryptText);
////        System.out.println("���ܺ�:\n" + new String(decryptText));
//        System.out.println("���ܺ�:\n" + bytesToHex(decryptText));
    }

}
