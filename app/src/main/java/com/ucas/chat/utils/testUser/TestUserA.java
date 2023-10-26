package com.ucas.chat.utils.testUser;

import static com.ucas.chat.utils.AesTools.getDecryptContent;
import static com.ucas.chat.utils.AesTools.getEncryptContent;

import android.util.Log;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.utils.AesTools;

public class TestUserA {
    public static final String TAG = ConstantValue.TAG_CHAT + "TestUserA";

    //离线服务器
    public static void serverSecond(){
        String value = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        String encryptContent = getEncryptContent(value, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " serverSecond:: contact.txt 加密onion：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " serverSecond:: contact.txt 解密onion：decryptContent = " + decryptContent);
    }

    /**
     * userinfor.txt
     * testA
     */
    public static void test(){
        String value = "testA";
        String encryptContent = getEncryptContent(value, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test:: userinfor.txt 加密testA：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test:: userinfor.txt 解密testA：decryptContent = " + decryptContent);
    }

    /**
     * contact.txt
     * liqf2ad7xgi4ewixwvk6qxf5bsevaq7qojvfzu74ruwusvc4ullfonyd.onion
     * b
     */
    public static void test1(){

        String value = "liqf2ad7xgi4ewixwvk6qxf5bsevaq7qojvfzu74ruwusvc4ullfonyd.onion";
        String encryptContent = getEncryptContent(value, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test1:: contact.txt 加密onion：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test1:: contact.txt 解密onion：decryptContent = " + decryptContent);

        String name = "b";
        String encryptName = getEncryptContent(name, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test1:: contact.txt 加密Name：encryptName = " + encryptName);
        String decryptName = getDecryptContent(encryptName, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test1:: contact.txt 解密Name：decryptName = " + decryptName);
    }

    /**
     * hostname.txt
     * ksrpouwqvjbasdsrksvmvsapnbgoso57infox5l2s6kiewfaofihkjid.onion
     */
    public static void test2(){
        String value = "ksrpouwqvjbasdsrksvmvsapnbgoso57infox5l2s6kiewfaofihkjid.onion";
        String encryptContent = getEncryptContent(value, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test2:: hostname.txt 加密onion：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test2:: hostname.txt 解密onion：decryptContent = " + decryptContent);
    }

    /**
     * client_private_key.txt
     * MIICegIBADANBgkqhkiG9w0BAQEFAASCAmQwggJgAgEAAoGBAIoIK+iWU96AWqvP
     * O7dTWXoKlLP0Y335dHtgqGihgQemrUOwVmCmPFUBP4lpAfhqWErKe4RyEI18jKbw
     * WTUcp3Nc7zfJd7rKj5M9wOtYt0GJIkqMZ6gj1EXNCJCItroy+y4O1Tc5iMg/uQ0J
     * l5Y+tx/0iLpsx+DCV7cYWVY2zkH3AgMBAAECgYAPr0QGBub61oz3DvJTL5ZwRrmF
     * BVU04F67Ek2wrgFydB8mHOiDzP/4DM9CdvsxViw4O2/zqd51pVx6L79nAhxZ7yES
     * MEhA0o14vLcgRz+j0vL3f8uObvm6Jj7GzrytBonyOg2FWT2oj50aANrr3h+okNYQ
     * gHdSFM330xERv0eA0QJFAPjVLGqdbJPDd2ID+ftdFlfkeJT8RG0ycLETRrQvCTht
     * 14T4LOOto/GDkbkGqRjFBWFBnn9/0exsVFPrWY9OVNmOj9WfAj0AjgH7erN5rgOK
     * zGej1lIiwSiMVh2ZJyZp4uT0KeCANWI3h2mFHTyQB5O6Y18W8seW7B+NqqJFQpOt
     * ZESpAkUA489GutUlcqKhybqaQucWbaFeRAgxHUfwpC1hrdTiPCdUCQitEEUIszQh
     * VaQja+n04vWQhmofZrRHAA2YEPtF82fxh0ECPE8fw6NmhRH0X03tIVi1gy/lC+yf
     * qUorGSyXLYR31nfoNdB5dvYHXKkcIupjfUHg+7KtEh5VNAoYhrAroQJESw/fXaI+
     * cxjYR1gI5E5TwfZF7V+Ozbqmtw2PFreQ48hE/t0jf2e2lOFAeZ/hWzGkIXYr4iYh
     * qzEaos0XNvU7H4ztucQ=
     */
    public static void test4(){

        String value = "MIICegIBADANBgkqhkiG9w0BAQEFAASCAmQwggJgAgEAAoGBAIoIK+iWU96AWqvP\n" +
                "O7dTWXoKlLP0Y335dHtgqGihgQemrUOwVmCmPFUBP4lpAfhqWErKe4RyEI18jKbw\n" +
                "WTUcp3Nc7zfJd7rKj5M9wOtYt0GJIkqMZ6gj1EXNCJCItroy+y4O1Tc5iMg/uQ0J\n" +
                "l5Y+tx/0iLpsx+DCV7cYWVY2zkH3AgMBAAECgYAPr0QGBub61oz3DvJTL5ZwRrmF\n" +
                "BVU04F67Ek2wrgFydB8mHOiDzP/4DM9CdvsxViw4O2/zqd51pVx6L79nAhxZ7yES\n" +
                "MEhA0o14vLcgRz+j0vL3f8uObvm6Jj7GzrytBonyOg2FWT2oj50aANrr3h+okNYQ\n" +
                "gHdSFM330xERv0eA0QJFAPjVLGqdbJPDd2ID+ftdFlfkeJT8RG0ycLETRrQvCTht\n" +
                "14T4LOOto/GDkbkGqRjFBWFBnn9/0exsVFPrWY9OVNmOj9WfAj0AjgH7erN5rgOK\n" +
                "zGej1lIiwSiMVh2ZJyZp4uT0KeCANWI3h2mFHTyQB5O6Y18W8seW7B+NqqJFQpOt\n" +
                "ZESpAkUA489GutUlcqKhybqaQucWbaFeRAgxHUfwpC1hrdTiPCdUCQitEEUIszQh\n" +
                "VaQja+n04vWQhmofZrRHAA2YEPtF82fxh0ECPE8fw6NmhRH0X03tIVi1gy/lC+yf\n" +
                "qUorGSyXLYR31nfoNdB5dvYHXKkcIupjfUHg+7KtEh5VNAoYhrAroQJESw/fXaI+\n" +
                "cxjYR1gI5E5TwfZF7V+Ozbqmtw2PFreQ48hE/t0jf2e2lOFAeZ/hWzGkIXYr4iYh\n" +
                "qzEaos0XNvU7H4ztucQ=";
        String encryptContent = getEncryptContent(value, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test4:: client_private_key.txt 加密privateKey：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test4:: client_private_key.txt 解密privateKey：decryptContent = " + decryptContent);
    }

    /**
     * client_public_key.txt
     * MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCKCCvollPegFqrzzu3U1l6CpSz
     * 9GN9+XR7YKhooYEHpq1DsFZgpjxVAT+JaQH4alhKynuEchCNfIym8Fk1HKdzXO83
     * yXe6yo+TPcDrWLdBiSJKjGeoI9RFzQiQiLa6MvsuDtU3OYjIP7kNCZeWPrcf9Ii6
     * bMfgwle3GFlWNs5B9wIDAQAB
     */
    public static void test5(){
        String value = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCKCCvollPegFqrzzu3U1l6CpSz\n" +
                "9GN9+XR7YKhooYEHpq1DsFZgpjxVAT+JaQH4alhKynuEchCNfIym8Fk1HKdzXO83\n" +
                "yXe6yo+TPcDrWLdBiSJKjGeoI9RFzQiQiLa6MvsuDtU3OYjIP7kNCZeWPrcf9Ii6\n" +
                "bMfgwle3GFlWNs5B9wIDAQAB";
        String encryptContent = getEncryptContent(value, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test5:: client_public_key.txt 加密publicKey：encryptContent = " + encryptContent);
        String decryptContent = getDecryptContent(encryptContent, AesTools.AesKeyTypeEnum.COMMON_KEY);
        Log.e(TAG, " test5:: client_public_key.txt 解密publicKey：decryptContent = " + decryptContent);
    }

    public static void test6(){

    }
}
