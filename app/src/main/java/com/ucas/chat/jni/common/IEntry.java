package com.ucas.chat.jni.common;

public interface IEntry {

    byte[] entry(String iv,String keyFileName,byte[] fileStream);

    int entry(String iv,String keyFileName,String filename,String entryFileName);


}
