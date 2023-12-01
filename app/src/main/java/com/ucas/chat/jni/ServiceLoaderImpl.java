package com.ucas.chat.jni;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ServiceLoader;

public class ServiceLoaderImpl {
    static {
        System.loadLibrary("native-lib");
    }

    public static <S> S load(Class<S> service) {
        try {
            return ServiceLoader.load(service).iterator().next();
        } catch (Exception e) {
            return null;
        }
    }


    public static String getFileKeyLocation(String filePath, int postions) {
        String value = null;
        try {
            FileInputStream is = new FileInputStream(filePath);
            if(postions<0 || postions>255){
                return "密钥用尽";
            }

            if(postions==0){
                is.skip(1);
            }else{
                is.skip((postions-1)*32+1);
            }

            byte[] b = new byte[32];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return value;

    }

    public static void setFileKeyLocation(String filename,int location) {
        try {
            File file = new File(filename);
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(0);
            if(location<0 || location>255){
                return;
            }
            raf.write(location);
            raf.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static  String bytesToHexString(byte[] src) {
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


    public static byte[]  padding(String iv){
        return lenpadding(iv.getBytes());
    }

    public static byte[]  padding(byte[] decode){
        return lenpadding(decode);
    }

    public static byte[] lenpadding(byte[] decode){
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
        return decodeEntry;
    }




    public static native byte[] encrypt_lokicc_byte(byte[] inputStream, byte[] key, byte[] iv,int entryLen);
    public static native byte[] decry_lokiccc_byte(byte[] inputStream,byte[] key,byte[] iv);
    public static native int   decry_lokicc_file(String infileName,String outfileName,byte[] key,byte[] iv);
    public static native int   encrypt_lokicc_file(String infileName,String outfileName,byte[] key,byte[] iv);


}
