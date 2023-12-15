package com.ucas.chat.jni.common;

import android.util.Log;

import com.google.auto.service.AutoService;
import com.ucas.chat.jni.ServiceLoaderImpl;


@AutoService(IDecry.class)
public class DecryImpl implements IDecry {
    @Override
    public byte[] decry(String iv,String keyFileName, byte[] decodeEntry) {

       // byte[] key = "qazwsxedcrfvtgbyhnujmikol1234567".getBytes();

       int index=ServiceLoaderImpl.load(IKeyIndex.class).keyIndex(keyFileName);
        String key = ServiceLoaderImpl.getFileKeyLocation(keyFileName,index);

        byte[] ivDecry=ServiceLoaderImpl.padding(iv);
        byte[] en= ServiceLoaderImpl.decry_lokiccc_byte(decodeEntry, key.getBytes(), ivDecry);

        byte[] de=new byte[ServiceLoaderImpl.byteToInteger(en)];

        System.arraycopy(en,4,de,0,de.length);
        return de;
    }

    @Override
    public int decry(String iv, String keyFileName,String filename, String targetFileName) {
        byte[] key = "qazwsxedcrfvtgbyhnujmikol1234567".getBytes();

//        int index=ServiceLoaderImpl.load(IKeyIndex.class).keyIndex(keyFileName);
//        String key = ServiceLoaderImpl.getFileKeyLocation(keyFileName,index);
        Log.e("kang","解密以后的key="+key);

        int load=  ServiceLoaderImpl.decry_lokicc_file(filename,targetFileName,key,iv.getBytes());

        return load;
    }
}
