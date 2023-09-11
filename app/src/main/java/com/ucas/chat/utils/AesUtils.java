package com.ucas.chat.utils;

import android.util.Base64;
import android.util.Log;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.db.MyInforTool;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



public class AesUtils {

    public static final String TAG = ConstantValue.TAG_CHAT + "AesUtils";

    /**16进制数*/
    private final static String HEX = "0123456789ABCDEF";
    /**密钥长度*/
    private static final int KEY_LENGTH = 16;
    /**默认填充位数*/
    private static final String DEFAULT_VALUE = "0";

    /**
     * 加密
     * @param key 密钥
     * @param src 加密文本
     * @return 加密后的文本
     * @throws Exception
     */
    public static String encrypt(String key, String src) throws Exception {
        // 对源数据进行Base64编码
        src = Base64.encodeToString(src.getBytes(), Base64.DEFAULT);
        // 补全KEY为16位
        byte[] rawKey = toMakeKey(key, KEY_LENGTH, DEFAULT_VALUE).getBytes();
        // 获取加密后的字节数组
        byte[] result = getBytes(rawKey, src.getBytes("utf-8"),"AES", Cipher.ENCRYPT_MODE);
        // 对加密后的字节数组进行Base64编码
        result = Base64.encode(result, Base64.DEFAULT);
        // 返回字符串
        return new String(result, Charset.defaultCharset());
    }

    /**
     * 解密
     * @param key 密钥
     * @param encrypted 待解密文本
     * @return 返回解密后的数据
     * @throws Exception
     */
    public static String decrypt(String key, String encrypted) throws Exception {
        // 补全KEY为16位
        byte[] rawKey = toMakeKey(key, KEY_LENGTH, DEFAULT_VALUE).getBytes();
        // 获取加密后的二进制字节数组
        byte[] enc = encrypted.getBytes(Charset.defaultCharset());
        // 对二进制数组进行Base64解码
        enc = Base64.decode(enc, Base64.DEFAULT);
        // 获取解密后的二进制字节数组
        byte[] result = getBytes(rawKey, enc,"AES", Cipher.DECRYPT_MODE);
        // 对解密后的二进制数组进行Base64解码
        result = Base64.decode(result, Base64.DEFAULT);
        // 返回字符串
        return new String(result, "utf-8");
    }

    /**
     * 加密
     * @param key 密钥
     * @param src 加密文本
     * @return 加密后的数据
     * @throws Exception
     */
    public static String encrypt2Java(String key, String src) throws Exception {
        // /src = Base64.encodeToString(src.getBytes(), Base64.DEFAULT);
        byte[] rawKey = toMakeKey(key, KEY_LENGTH, DEFAULT_VALUE).getBytes();// key.getBytes();
//    byte[] result = encrypt2Java(rawKey, src.getBytes("utf-8"));
        byte[] result = getBytes(rawKey, src.getBytes("utf-8"), "AES/CBC/PKCS5Padding", Cipher.ENCRYPT_MODE);
        // result = Base64.encode(result, Base64.DEFAULT);
        return toHex(result);
    }

    /**
     * 加密
     * @param key 密钥
     * @param src 加密文本
     * @return 加密后的数据
     * @throws Exception
     */
    public static String decrypt2Java(String key, String src) throws Exception {
        // /src = Base64.encodeToString(src.getBytes(), Base64.DEFAULT);
        byte[] rawKey = toMakeKey(key, KEY_LENGTH, DEFAULT_VALUE).getBytes();// key.getBytes();
        byte[] result = getBytes(rawKey, src.getBytes("utf-8"), "AES/CBC/PKCS5Padding", Cipher.DECRYPT_MODE);
        // result = Base64.encode(result, Base64.DEFAULT);
        return toHex(result);
    }

    /**
     * 密钥key ,默认补的数字，补全16位数，以保证安全补全至少16位长度,android和ios对接通过
     * @param key 密钥key
     * @param length 密钥应有的长度
     * @param text 默认补的文本
     * @return 密钥
     */
    private static String toMakeKey(String key, int length, String text) {
        // 获取密钥长度
        int strLen = key.length();
        // 判断长度是否小于应有的长度
        if (strLen < length) {
            // 补全位数
            StringBuilder builder = new StringBuilder();
            // 将key添加至builder中
            builder.append(key);
            // 遍历添加默认文本
            for (int i = 0; i < length - strLen; i++) {
                builder.append(text);
            }
            // 赋值
            key = builder.toString();
        }
        return key;
    }

    /**
     * 加解密过程
     * 1. 通过密钥得到一个密钥专用的对象SecretKeySpec
     * 2. Cipher 加密算法，加密模式和填充方式三部分或指定加密算 (可以只用写算法然后用默认的其他方式)Cipher.getInstance("AES");
     * @param key 二进制密钥数组
     * @param src 加解密的源二进制数据
     * @param mode 模式，加密为：Cipher.ENCRYPT_MODE;解密为：Cipher.DECRYPT_MODE
     * @return 加解密后的二进制数组
     * @throws NoSuchAlgorithmException 无效算法
     * @throws NoSuchPaddingException 无效填充
     * @throws InvalidKeyException 无效KEY
     * @throws InvalidAlgorithmParameterException 无效密钥
     * @throws IllegalBlockSizeException 非法块字节
     * @throws BadPaddingException 坏数据
     */
    private static byte[] getBytes(byte[] key, byte[] src,String transformation, int mode) throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        // 密钥规格
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        // 密钥实例
        Cipher cipher = Cipher.getInstance(transformation);
        // 初始化密钥模式
        cipher.init(mode, secretKeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        // 加密数据
        return cipher.doFinal(src);
    }

    /**获取16进制字符串*/
    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }
    /**将16进制字符串转换为未编码后的数据*/
    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    /**
     * 把16进制转化为字节数组
     * @param hexString 16进制字符串
     * @return 加密后的字节数组
     */
    private static byte[] toByte(String hexString) {
        // 获取源数据长度
        int len = hexString.length() / 2;
        // 创建字节数组
        byte[] result = new byte[len];
        // 遍历
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        // 返回二进制字节数组
        return result;
    }

    /**
     * 二进制转字符,转成了16进制
     * 0123456789abcdef
     * @param bytes 字节组数
     * @return 16进制编码的字符串
     */
    private static String toHex(byte[] bytes) {
        // 判断二进制数组长度是否小于0
        if (bytes.length <= 0) return "";
        // 创建字符串连接对象
        StringBuilder builder = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            // 拼接字符
            builder.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
        }
        // 返回字符串
        return builder.toString();
    }

    /**
     * 对文件进行AES加密
     * @param sourceFile 待加密文件
     * @param toFile 加密后的文件
     * @param dir 文件存储路径
     * @param key 密钥
     * @return 加密后的文件
     */
    public static File encryptFile(File sourceFile, String toFile, String dir, String key) {
        // 新建临时加密文件
        File encryptFile = null;
        // 输入流
        InputStream inputStream = null;
        // 输出流
        OutputStream outputStream = null;
        try {
            // 读取源文件，创建文件输入流
            inputStream = new FileInputStream(sourceFile);
            // 创建加密后的文件
            encryptFile = new File(dir + toFile);
            // 根据文件创建输出流
            outputStream = new FileOutputStream(encryptFile);
            // 初始化 Cipher
            Cipher cipher = initAESCipher(key, Cipher.ENCRYPT_MODE);
            // 以加密流写入文件
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
            // 创建缓存字节数组
            byte[] cache = new byte[1024];
            // 读取
            int len;
            // 读取加密并写入文件
            while ((len = cipherInputStream.read(cache)) != -1) {
                outputStream.write(cache, 0, len);
                outputStream.flush();
            }
            // 关闭加密输入流
            cipherInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(inputStream);
            closeStream(outputStream);
        }
        return encryptFile;
    }

    /**
     * AES方式解密文件
     * @param sourceFile 源文件
     * @param toFile 目标文件
     * @param dir 文件存储路径
     * @param key 密钥
     * @return
     */
    public static File decryptFile(File sourceFile, String toFile, String dir, String key) {
        // 解密文件
        File decryptFile = null;
        // 文件输入流
        InputStream inputStream = null;
        // 文件输出流
        OutputStream outputStream = null;
        try {
            // 创建解密文件
            decryptFile = new File(dir + toFile);
            // 初始化Cipher
            Cipher cipher = initAESCipher(key, Cipher.DECRYPT_MODE);
            // 根据源文件创建输入流
            inputStream = new FileInputStream(sourceFile);
            // 创建输出流
            outputStream = new FileOutputStream(decryptFile);
            // 获取解密输出流
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            // 创建缓冲字节数组
            byte[] buffer = new byte[1024];
            int len;
            // 读取解密并写入
            while ((len = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, len);
                cipherOutputStream.flush();
            }
            // 关闭流
            cipherOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(inputStream);
            closeStream(outputStream);
        }
        return decryptFile;
    }

    /**
     * 初始化 AES Cipher
     * @param key 密钥
     * @param cipherMode 加密模式
     * @return 密钥
     */
    private static Cipher initAESCipher(String key, int cipherMode) {
        Cipher cipher = null;
        try {
            // 将KEY进行修正
            byte[] rawKey = toMakeKey(key, KEY_LENGTH, DEFAULT_VALUE).getBytes();
            // 创建密钥规格
            SecretKeySpec secretKeySpec = new SecretKeySpec(rawKey, "AES");
            // 获取密钥
            cipher = Cipher.getInstance("AES");
            // 初始化
            cipher.init(cipherMode, secretKeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return cipher;
    }

    /**
     * 关闭流
     * @param closeable 实现Closeable接口
     */
    private static void closeStream(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * userinfor.txt
     * asdf
     */
    public static void test(){
        //String key = MyInforTool.getInstance().getKey();
        String key = "08c08c24f1f2a444";
        Log.e("AesUtils", " test:: key = " + key);
        String value = null;
        try {
            value = encrypt(key, "asdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AesUtils", " test:: userinfor.txt 加密" + value);
        try {
            Log.e("AesUtils", " test:: userinfor.txt 解密" + decrypt(key, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * contact.txt
     * ascem3j2ff7rrzcymrzx6y25mmt5jcjwmxznbnjs4bhkumgnoi2fr2ad.onion
     * 袁绍
     */
    public static void test1(){
        String key = "08c08c24f1f2a444";
        Log.e("AesUtils", " test:: key = " + key);
        String onion = null;
        try {
            onion = encrypt(key, "ascem3j2ff7rrzcymrzx6y25mmt5jcjwmxznbnjs4bhkumgnoi2fr2ad.onion");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AesUtils", " test1::contact.txt 加密onion：" + onion);
        try {
            Log.e("AesUtils", " test1:: contact.txt 解密onion：" + decrypt(key, onion));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String name = null;
        try {
            name = encrypt(key, "袁绍");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AesUtils", " test1:: contact.txt 加密name：" + name);
        try {
            Log.e("AesUtils", " test1:: contact.txt 解密name：" + decrypt(key, name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * hostname.txt
     * xqmierlcofd7e3twppdg2mep6ulboehpp5sehr5vbnlp7wwdspakh2yd.onion
     */
    public static void test2(){
        String key = "08c08c24f1f2a444";
        Log.e("AesUtils", " test2:: key = " + key);
        String value = null;
        try {
            value = encrypt(key, "xqmierlcofd7e3twppdg2mep6ulboehpp5sehr5vbnlp7wwdspakh2yd.onion");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AesUtils", " test2:: hostname.txt 加密 " + value);
        try {
            Log.e("AesUtils", " test2:: hostname.txt 解密 " + decrypt(key, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * server.txt
     * r4c5vjwreftmhmz3zde4p2l3xjevrcx7wtdtqzi6o7ooq6vfs6sysgad.onion
     * 3kxo6zl3crqalsliipvrt3x3detzdujaoqquusyfovus44fy6pyw35id.onion
     */
    public static void test3(){
        String key = "08c08c24f1f2a444";
        Log.e("AesUtils", " test3::key = " + key);
        String onion1 = "r4c5vjwreftmhmz3zde4p2l3xjevrcx7wtdtqzi6o7ooq6vfs6sysgad.onion";
        try {
            onion1 = encrypt(key, onion1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AesUtils", " test3:: server.text  onion1加密 " + onion1);
        try {
            Log.e("AesUtils", " test3:: server.text onion1解密 " + decrypt(key, onion1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String onion2 = "3kxo6zl3crqalsliipvrt3x3detzdujaoqquusyfovus44fy6pyw35id.onion";
        try {
            onion2 = encrypt(key, onion2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AesUtils", " test3:: server.text onion2加密 " + onion2);
        try {
            Log.e("AesUtils", " test3:: server.text onion2解密 " + decrypt(key, onion2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * client_private_key.txt
     *
     * MIICegIBADANBgkqhkiG9w0BAQEFAASCAmQwggJgAgEAAoGBAIGxw5yUhrGLSqCU
     * B2CGpELcQmU6NiHcwUqIjIPcMGvWHsS3qYXW2RetV+BtV+F2SFyDUQiiqrH6Z6F9
     * AUDGFauGmQL8E5iJcS5ThhHcMMLTItN6C/ceJwjfpRZPoxqXY7x2bKk8cTIr5mmC
     * UHvgtIr7QWA48MFGGc74OR4SBCLhAgMBAAECgYBG2S4LplNiP75XtCXHhCXNZHdc
     * xwz1OJvatHdWPP2ymviZ697Of0x+k7ISpojAYJYs+4tT1VC8AxeDbz7LxzLB9Cm3
     * S4Sv+z6D+kDQpL2/MCFBy2D44z98SSDS2LOf59uPfiOBaHrrEP+thaCfP410B1r2
     * 0p98XrHuPaj9laRtMQJFAMCXam6+9ElJEtAUTSpYNOvvA7FoPPzojcgMs1t16Jqq
     * raU4NyNkvz3fVv3BictslSVJICeXA6SYf6HemGqYPJ1j6LQFAj0ArGUQb0gWBq6g
     * 3mlZj7zkmz/SDwy/g7itr7bGdGbx+oj9JxuU95UZ/EKBNRoR/psWTbv1h332fcb8
     * 4OYtAkQXBAFcKsXW3Dy7UTHHlbL9Xxr54JMpRkFrXtkwXO7nTO5jiExOloOkSUSE
     * HtTw0pEgW5TUJl/xE6htF8TvODJGPOJaYQI9AKKLJ9PEw//I5yTNcAR2tZUrt4M5
     * IdmwZxMl4jgeWElAoHAfMXW+v6F5kxneWtkUoRLqBbaara4p0IS8gQJETLYoZkai
     * DS7r4wyG7EX9ZUdSCD1gtPwfXWwSwTHzz4tkK9k/c2I6me/TwgP+LAykTi+aUqP7
     * Dx2zI9vmmQ83i1Cbx40=
     */
    public static void test4(){

        String key = "08c08c24f1f2a444";
        Log.e("AesUtils", " test4:: key = " + key);
        String value = "MIICegIBADANBgkqhkiG9w0BAQEFAASCAmQwggJgAgEAAoGBAIGxw5yUhrGLSqCU\n" +
                       "B2CGpELcQmU6NiHcwUqIjIPcMGvWHsS3qYXW2RetV+BtV+F2SFyDUQiiqrH6Z6F9\n" +
                        "AUDGFauGmQL8E5iJcS5ThhHcMMLTItN6C/ceJwjfpRZPoxqXY7x2bKk8cTIr5mmC\n" +
                        "UHvgtIr7QWA48MFGGc74OR4SBCLhAgMBAAECgYBG2S4LplNiP75XtCXHhCXNZHdc\n" +
                        "xwz1OJvatHdWPP2ymviZ697Of0x+k7ISpojAYJYs+4tT1VC8AxeDbz7LxzLB9Cm3\n" +
                        "S4Sv+z6D+kDQpL2/MCFBy2D44z98SSDS2LOf59uPfiOBaHrrEP+thaCfP410B1r2\n" +
                        "0p98XrHuPaj9laRtMQJFAMCXam6+9ElJEtAUTSpYNOvvA7FoPPzojcgMs1t16Jqq\n" +
                        "raU4NyNkvz3fVv3BictslSVJICeXA6SYf6HemGqYPJ1j6LQFAj0ArGUQb0gWBq6g\n" +
                        "3mlZj7zkmz/SDwy/g7itr7bGdGbx+oj9JxuU95UZ/EKBNRoR/psWTbv1h332fcb8\n" +
                        "4OYtAkQXBAFcKsXW3Dy7UTHHlbL9Xxr54JMpRkFrXtkwXO7nTO5jiExOloOkSUSE\n" +
                        "HtTw0pEgW5TUJl/xE6htF8TvODJGPOJaYQI9AKKLJ9PEw//I5yTNcAR2tZUrt4M5\n" +
                        "IdmwZxMl4jgeWElAoHAfMXW+v6F5kxneWtkUoRLqBbaara4p0IS8gQJETLYoZkai\n" +
                        "DS7r4wyG7EX9ZUdSCD1gtPwfXWwSwTHzz4tkK9k/c2I6me/TwgP+LAykTi+aUqP7\n" +
                        "Dx2zI9vmmQ83i1Cbx40=";
        try {
            value = encrypt(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AesUtils", " test4:: client_private_key.txt 加密 " + value);
        try {
            Log.e("AesUtils", " test4:: client_private_key.txt 解密 " + decrypt(key, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * client_public_key.txt
     *
     * MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBscOclIaxi0qglAdghqRC3EJl
     * OjYh3MFKiIyD3DBr1h7Et6mF1tkXrVfgbVfhdkhcg1EIoqqx+mehfQFAxhWrhpkC
     * /BOYiXEuU4YR3DDC0yLTegv3HicI36UWT6Mal2O8dmypPHEyK+ZpglB74LSK+0Fg
     * OPDBRhnO+DkeEgQi4QIDAQAB
     */
    public static void test5(){

        String key = "08c08c24f1f2a444";
        Log.e("AesUtils", " test5:: key = " + key);
        String value = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBscOclIaxi0qglAdghqRC3EJl\n" +
                        "OjYh3MFKiIyD3DBr1h7Et6mF1tkXrVfgbVfhdkhcg1EIoqqx+mehfQFAxhWrhpkC\n" +
                        "/BOYiXEuU4YR3DDC0yLTegv3HicI36UWT6Mal2O8dmypPHEyK+ZpglB74LSK+0Fg\n" +
                        "OPDBRhnO+DkeEgQi4QIDAQAB";
        try {
            value = encrypt(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AesUtils", " test5:: client_public_key.txt 加密 " + value);
        try {
            Log.e("AesUtils", " test5:: client_public_key.txt 解密 " + decrypt(key, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
