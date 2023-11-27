package com.ucas.chat.jni.common;

import com.google.auto.service.AutoService;

import java.io.FileInputStream;
@AutoService(IKeyIndex.class)
public class KeyIndexImpl implements IKeyIndex {
    int index=0;
    @Override
    public int keyIndex(String filename) {
        String value=  getFileHeader(filename);//获取前两个字节
        return Integer.parseInt(value,16);
    }

    private String getFileHeader(String filePath) {
        String value = null;
        try (FileInputStream is = new FileInputStream(filePath)) {
            byte[] b = new byte[1];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {

        }
        return value;
    }
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }

        return builder.toString();
    }

}
