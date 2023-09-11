package com.ucas.chat.utils;




import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

public class EncryptionUtils {
    private final static String TAG = "EncryptionUtils";

    public final static String SECRET = "e569805e331ca79e9e00dcf53e753cb7";

    /*
     * MD5加密，32位
     */
    public static String getMD5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

//    public static String createSign(Map<String, String> params){
//        StringBuilder sb = new StringBuilder();
//        // 将参数以参数名的字典升序排序
//        Map<String, String> sortParams = new TreeMap<String, String>(params);
//        // 遍历排序的字典,并拼接"key=value"格式
//        for (Map.Entry<String, String> entry : sortParams.entrySet()) {
//            String key = entry.getKey();
//            String value =  entry.getValue();
//            if (!value.isEmpty()){
//                value.trim();
//            }
//            sb.append("&").append(key).append("=").append(value.isEmpty()== true ? value : URLEncoder.encode(value));
//        }
//        String mapString = sb.toString().replaceFirst("&","");
//        String stringSecret = mapString + "&secret=" +SECRET;
//        String stringBase64 = Base64.encode(stringSecret);
//        String signMD5 = getMD5(stringBase64);
//
//        LogUtils.d(TAG, "createSign: mapString : " + mapString);
//        LogUtils.d(TAG, "createSign: stringSecret : " + stringSecret);
//        LogUtils.d(TAG, "createSign: stringBase64 : " + stringBase64);
//        LogUtils.d(TAG, "createSign: signMD5 : " + signMD5);
//        return signMD5;
//    }

}
