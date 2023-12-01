package com.ucas.chat.jni.common;

import android.util.Log;

import com.google.auto.service.AutoService;
import com.ucas.chat.jni.ServiceLoaderImpl;

@AutoService(IEntry.class)
public class EntryImpl implements IEntry {

    @Override
    public byte[] entry(String iv,String keyFileName ,byte[] decode) {
        byte[] decodeEntry=ServiceLoaderImpl.padding(decode);
        byte[] ivEntry=ServiceLoaderImpl.padding(iv);
        byte[] key = "qazwsxedcrfvtgbyhnujmikol1234567".getBytes();
      /*  int index=ServiceLoaderImpl.load(IKeyIndex.class).keyIndex(keyFileName);
        String key = ServiceLoaderImpl.getFileKeyLocation(keyFileName,index);*/

        byte[] en= ServiceLoaderImpl.encrypt_lokicc_byte(decodeEntry,key,ivEntry,new String(decode).length());
        return en;
    }

    @Override
    public int entry(String iv,String keyFileName, String filename, String targetFileName) {

        //byte[] key = "qazwsxedcrfvtgbyhnujmikol1234567".getBytes();
        int index=ServiceLoaderImpl.load(IKeyIndex.class).keyIndex(keyFileName);
        String key = ServiceLoaderImpl.getFileKeyLocation(keyFileName,index);
        int load = ServiceLoaderImpl.encrypt_lokicc_file(filename, targetFileName, key.getBytes(),iv.getBytes());

        return load;
    }


}
