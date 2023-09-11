package com.ucas.chat.utils;

import static com.ucas.chat.MyApplication.getContext;

import android.util.Log;

import com.ucas.chat.bean.KeyInforBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.db.KeyHelper;
import com.ucas.chat.db.KeyHelperTool;

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


    /**
     * contact.txt
     * ascem3j2ff7rrzcymrzx6y25mmt5jcjwmxznbnjs4bhkumgnoi2fr2ad.onion
     * 袁绍
     */
    public static void test1(){

        String value = "ascem3j2ff7rrzcymrzx6y25mmt5jcjwmxznbnjs4bhkumgnoi2fr2ad.onion";
        String encryptContent = getEncryptContent(value, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test1:: contact.txt 加密onion：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test1:: contact.txt 解密onion：decryptContent = " + decryptContent);

        String name = "袁绍";
        String encryptName = getEncryptContent(name, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test1:: contact.txt 加密Name：encryptName = " + encryptName);
        String decryptName = getDecryptContent(encryptName, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test1:: contact.txt 解密Name：decryptName = " + decryptName);

    }

    /**
     * hostname.txt
     * xqmierlcofd7e3twppdg2mep6ulboehpp5sehr5vbnlp7wwdspakh2yd.onion
     */
    public static void test2(){
        String value = "xqmierlcofd7e3twppdg2mep6ulboehpp5sehr5vbnlp7wwdspakh2yd.onion";
        String encryptContent = getEncryptContent(value, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test2:: hostname.txt 加密onion：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test2:: hostname.txt 解密onion：decryptContent = " + decryptContent);
    }

    /**
     * server.txt
     * r4c5vjwreftmhmz3zde4p2l3xjevrcx7wtdtqzi6o7ooq6vfs6sysgad.onion
     * 3kxo6zl3crqalsliipvrt3x3detzdujaoqquusyfovus44fy6pyw35id.onion
     */
    public static void test3(){
        String onion1 = "r4c5vjwreftmhmz3zde4p2l3xjevrcx7wtdtqzi6o7ooq6vfs6sysgad.onion";
        String encryptOnion1 = getEncryptContent(onion1, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test3:: server.txt 加密onion1：encryptOnion1 = " + encryptOnion1);
        String decryptOnion1 = getDecryptContent(encryptOnion1, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test3:: server.txt 解密onion1：decryptOnion1 = " + decryptOnion1);

        String onion2 = "3kxo6zl3crqalsliipvrt3x3detzdujaoqquusyfovus44fy6pyw35id.onion";
        String encryptOnion2 = getEncryptContent(onion2, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test3:: server.txt 加密onion2：encryptOnion2 = " + encryptOnion2);
        String decryptOnion2 = getDecryptContent(encryptOnion2, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test3:: server.txt 解密onion2：decryptOnion2 = " + decryptOnion2);
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
        String encryptContent = getEncryptContent(value, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test4:: client_private_key.txt 加密privateKey：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test4:: client_private_key.txt 解密privateKey：decryptContent = " + decryptContent);
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
        String value = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBscOclIaxi0qglAdghqRC3EJl\n" +
                "OjYh3MFKiIyD3DBr1h7Et6mF1tkXrVfgbVfhdkhcg1EIoqqx+mehfQFAxhWrhpkC\n" +
                "/BOYiXEuU4YR3DDC0yLTegv3HicI36UWT6Mal2O8dmypPHEyK+ZpglB74LSK+0Fg\n" +
                "OPDBRhnO+DkeEgQi4QIDAQAB";
        String encryptContent = getEncryptContent(value, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test5:: client_public_key.txt 加密publicKey：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test5:: client_public_key.txt 解密publicKey：decryptContent = " + decryptContent);
    }

    /**
     * 510337f1ec5f1ab6a4024fd63529103b8209c2160b9caa28b7dba3fc20450e9e_public_key.txt
     *
     * MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3sgZd8Ef6zBQHkaf85R1fRstB
     * 6SPSBgZRqiI0tN+282cPX9YB1P3h2MsEb8P1erWS72zxZwV1ENcuR6+Mj284HjeO
     * 0Gchd+LnDZjDwkM8b9CH83iZrkAS1Cqsrr+nz5rHb9AITbgFTQkEh7WgRnUqAt8f
     * 36XbpNKLktJGnYJ9EwIDAQAB
     */
    public static void test6(){
        String value = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3sgZd8Ef6zBQHkaf85R1fRstB\n" +
                "6SPSBgZRqiI0tN+282cPX9YB1P3h2MsEb8P1erWS72zxZwV1ENcuR6+Mj284HjeO\n" +
                "0Gchd+LnDZjDwkM8b9CH83iZrkAS1Cqsrr+nz5rHb9AITbgFTQkEh7WgRnUqAt8f\n" +
                "36XbpNKLktJGnYJ9EwIDAQAB";
        String encryptContent = getEncryptContent(value, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " text6:: xxx_public_key.txt 加密xxxPublicKey：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " text6:: xxx_public_key.txt 解密xxxPublicKey：decryptContent = " + decryptContent);
    }


}
