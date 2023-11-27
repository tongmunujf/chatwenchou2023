package com.ucas.chat.jni.common;

public interface IDecry {

    boolean samePhone=false;
    byte[] decry(String iv,String keyFileName,byte[] fileStream);

    int decry(String iv,String keyFileName,String filename,String targetFileName);
}
