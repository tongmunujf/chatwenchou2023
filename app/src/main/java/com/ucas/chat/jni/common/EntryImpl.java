package com.ucas.chat.jni.common;

import android.util.Log;

import com.google.auto.service.AutoService;
import com.ucas.chat.jni.ServiceLoaderImpl;

@AutoService(IEntry.class)
public class EntryImpl implements IEntry {

    @Override
    public byte[] entry(String iv,String keyFileName ,byte[] decode) {

        int len=decode.length;
        int blocktail=len%16;
        int blocknum=len/16;
        byte paddingByte;
        byte[] decodeEntry = decode;
        if(decode.length<16){
            decodeEntry=new byte[16];
            System.arraycopy(decode, 0, decodeEntry, 0, decode.length);
            int aa=  Integer.parseInt(Integer.toHexString(16-blocktail), 16);
            paddingByte= (byte) aa;
            for(int i=decode.length;i<16;i++){
                decodeEntry[i]=paddingByte;
            }
        }else{
            if(blocktail!=0){
                decodeEntry=new byte[16*blocknum+16];
                System.arraycopy(decode, 0, decodeEntry, 0, decode.length);
                int aa=  Integer.parseInt(Integer.toHexString(16-blocktail), 16);
                paddingByte= (byte) aa;
                for(int i=decode.length;i<decodeEntry.length;i++){
                    decodeEntry[i]=paddingByte;
                }
            }
        }

        byte[] key = "qazwsxedcrfvtgbyhnujmikol1234567".getBytes();
//        int index=ServiceLoaderImpl.load(IKeyIndex.class).keyIndex(keyFileName);
//        String key = ServiceLoaderImpl.getFileKeyLocation(keyFileName,index);

        byte[] en= ServiceLoaderImpl.encrypt_lokicc_byte(decodeEntry,key,iv.getBytes());



        return en;
    }

    @Override
    public int entry(String iv,String keyFileName, String filename, String targetFileName) {

        //byte[] key = "qazwsxedcrfvtgbyhnujmikol1234567".getBytes();

        int index=ServiceLoaderImpl.load(IKeyIndex.class).keyIndex(keyFileName);
        String key = ServiceLoaderImpl.getFileKeyLocation(keyFileName,index);
        Log.e("kang","加密以后的key="+key);

        int load = ServiceLoaderImpl.encrypt_lokicc_file(filename, targetFileName, key.getBytes(),iv.getBytes());

        return load;
    }


}
