package com.ucas.chat.jni;

import android.util.Log;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.jni.common.IDecry;
import com.ucas.chat.jni.common.IEntry;
import com.ucas.chat.jni.common.IKeyIndex;
import com.ucas.chat.tor.message.Message;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.tor.util.FilePathUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class JniEntryUtils {
    private static String TAG = ConstantValue.TAG_CHAT + "JniEntryUtils";
    public static int getKeyIndex() {
        int index = ServiceLoaderImpl.load(IKeyIndex.class).keyIndex(FilePathUtils.SECRET_KEY_FILE);
        return index;
    }

    public static void setFileKeyLocation(int index){
        Log.d(TAG, " 测试 setFileKeyLocation:: index = " + index);
        ServiceLoaderImpl.setFileKeyLocation(FilePathUtils.SECRET_KEY_FILE, index);
    }

    public static boolean keyFileIsExhausted(){
        boolean isExhausted = false;
        int index = getKeyIndex();
        if (index > 255){
            isExhausted = true;
        }
        Log.d(TAG, " keyFileIsExhausted:: isExhausted = " + isExhausted);
        return isExhausted;
    }

    public static byte[] getKeyIndexByte(){
        int fileName = 1;
        ByteBuffer data = ByteBuffer.allocate(40);//https://blog.csdn.net/mrliuzhao/article/details/89453082
        byte[] byteFileName = new byte[Constant.BYTE_STARTXORFILENAME_LENGTH];//文件名。开始和结束的文件名长度一样，下同理
        byte[] byteFileIndex = new byte[Constant.BYTE_STARTXORINDEX_LENGTH];//位置

        data.order(ByteOrder.BIG_ENDIAN);//字节序(Byte Order)之大端

        data.putInt(fileName);
        data.position(2);//因为只要2个字节

        data.get(byteFileName);
        System.out.println(Arrays.toString(byteFileName));


        data.flip();//不仅将position复位为0，同时也将limit的位置放置在了position之前所在的位置上
        data.clear();

        ServiceLoaderImpl.setFileKeyLocation(FilePathUtils.SECRET_KEY_FILE,0);
        int fileIndex = JniEntryUtils.getKeyIndex();
        fileIndex++;

        Log.d(TAG, " getKeyIndexByte:: keyIndex = " + fileIndex);
        data.putInt(fileIndex);//最大是999999999
        data.position(0);
        data.get(byteFileIndex);
        System.out.println(Arrays.toString(byteFileIndex));

        byte[] bytes = Message.byteMerger(byteFileName,byteFileIndex);

        return bytes;
    }

    public static byte[] entry(byte[] sourceFileByte){
        byte[] byteArray = ServiceLoaderImpl.load(IEntry.class).entry("++++", FilePathUtils.SECRET_KEY_FILE, sourceFileByte);
        Log.d(TAG, " entry:: byteArray.size = " + byteArray.length);
        Log.d(TAG, " entry:: 加密后字节16进制："+ AESCrypto.bytesToHex(byteArray));
        return byteArray;
    }

    public static byte[] decry(byte[] ciphertextByte){
        Log.d(TAG, " decry:: byteArray.size = " + ciphertextByte.length);
        Log.d(TAG, " decry:: 解密前字节16进制："+ AESCrypto.bytesToHex(ciphertextByte));
        byte[] byteArray = ServiceLoaderImpl.load(IDecry.class).decry("++++", FilePathUtils.SECRET_KEY_FILE, ciphertextByte);
        return byteArray;
    }
}
