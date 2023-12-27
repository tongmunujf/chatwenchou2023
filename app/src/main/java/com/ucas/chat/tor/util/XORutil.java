package com.ucas.chat.tor.util;

import android.content.Context;
import android.util.Log;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.jni.JniEntryUtils;
import com.ucas.chat.jni.ServiceLoaderImpl;
import com.ucas.chat.jni.common.IKeyIndex;
import com.ucas.chat.tor.message.Message;
import com.ucas.chat.ui.login.LoginActivity;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @auther :haoyunlai
 * date         :2021/10/3 16:21
 * e-mail       :2931945387@qq.com
 * usefulness   :处理有关xor文件的，包括找位置，删除xor文件某片段等
 */
public class XORutil {
    private static final String TAG = ConstantValue.TAG_CHAT + "XORutil";

    public static RecordXOR getStartXOR(Context context){// TODO: 2021/10/3  双方发信息前，确认能从哪个大家都可以进行异或开始的位置。

        RecordXOR recordStartXOR = SharedPreferencesUtil.getCommonRecordXOR(context);//此方法从全局指针文件中用

        if (recordStartXOR!=null)
            return recordStartXOR;
        System.out.println("recordStartXOR不存在");

        recordStartXOR = new RecordXOR();// TODO: 2021/11/24 要实例化 //保存开始要用的xor文件信息。

            recordStartXOR.setStartFileName(1);
            recordStartXOR.setStartFileIndex(1);

        return recordStartXOR;
    }




    public static byte[] xorFile2Byte(int fileName ,int fileIndex ){//要使用xor开始或结束的文件的信息转化为byte数组，即合并文件名和位置
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

        JniEntryUtils.setFileKeyLocation(fileIndex);

        Log.d(TAG, " 测试 xorFile2Byte:: keyIndex = " + fileIndex);
        data.putInt(fileIndex);//最大是999999999
        data.position(0);
        data.get(byteFileIndex);
        System.out.println(Arrays.toString(byteFileIndex));

        byte[] bytes = Message.byteMerger(byteFileName,byteFileIndex);

        return bytes;
    }


    public static int commonStartXORFileName(int friendStartXORFileName,int myStartXORFileName ){// TODO: 2021/10/4  获取共同能一起用的文件


        if (friendStartXORFileName>=myStartXORFileName){//因为文件的编号从小到大，所以以后面大的为准

            return friendStartXORFileName;
        }else
            return myStartXORFileName;

    }


    public static int compareKeyIndex(int friendStartXORFileName ,int friendStartXORIndex,int myStartXORFileName,int myStartXORIndex ){
        Log.d(TAG, " 测试 compareKeyIndex:: friendStartXORIndex = " + friendStartXORIndex + " myStartXORIndex = " + myStartXORIndex);
        int keyIndex = myStartXORIndex;
        if (friendStartXORIndex > myStartXORIndex){
            keyIndex = friendStartXORIndex;
        }
        Log.d(TAG, " 测试 compareKeyIndex:: keyIndex = " + keyIndex);
        JniEntryUtils.setFileKeyLocation(keyIndex);
        return keyIndex;
    }


    public static void addRecordXOR(CopyOnWriteArrayList<RecordXOR> recordXORs, RecordXOR recordXOR) {// TODO: 2021/10/6 将recordXOR加入到recordXORs中，若存在就不加入，一切以recordXORs中的为准

        String targetMessageID = recordXOR.getMessageID();//以targetMessageID为依据，遍历

        Iterator<RecordXOR> iterator = recordXORs.iterator();
        while (iterator.hasNext()){
            RecordXOR recordXOR1 = iterator.next();
            String messageID = recordXOR1.getMessageID();
            if (messageID.equals(targetMessageID)){
                return;//不用加入了
            }
        }
        recordXORs.add(recordXOR);
    }


    public static RecordXOR getRecordXOR(CopyOnWriteArrayList<RecordXOR> recordXORs, String targetMessageID) {
        Iterator<RecordXOR> iterator = recordXORs.iterator();
        while (iterator.hasNext()){
            RecordXOR recordXOR1 = iterator.next();
            String messageID = recordXOR1.getMessageID();
            if (messageID.equals(targetMessageID)){
                return recordXOR1;//不用加入了
            }
        }
        return null;
    }

    public static RecordXOR changecommonRecordXOR(RecordXOR commonRecordXOR,RecordXOR recordXOR){// TODO: 2021/10/24 比较尾指针，以靠最右的为准

        int endCommonFileName = commonRecordXOR.getEndFileName();
        int endCommonFileIndex = commonRecordXOR.getEndFileIndex();

        int endFileName = recordXOR.getEndFileName();
        int endFileIndex = recordXOR.getEndFileIndex();

        if (endCommonFileName<endFileName){
            commonRecordXOR = recordXOR;
        }else if (endCommonFileName==endFileName){
            if (endCommonFileIndex>=endFileIndex){//因为长度指针是相对文件尾的长度
                commonRecordXOR = recordXOR;
            }
        }
        return commonRecordXOR;
    }
}