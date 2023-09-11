package com.ucas.chat.db;

import static com.ucas.chat.MyApplication.getContext;

import android.content.res.AssetManager;

import com.ucas.chat.bean.AddressBookBean;
import com.ucas.chat.bean.KeyInforBean;
import com.ucas.chat.bean.MyInforBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.utils.LogUtils;

import org.apaches.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class KeyHelperTool {
    public static final String TAG = ConstantValue.TAG_CHAT + "KeyHelperTool";

    public static  String[] KEY_NAME_ARR = {"MessageKey","CommonKey"};

    private static final KeyHelperTool keyHelperTool = new KeyHelperTool();
    private  KeyHelperTool(){}
    public static KeyHelperTool getInstance(){
        return keyHelperTool;
    }

    public List<KeyInforBean> insertKeyInforBean(){
        List<KeyInforBean> beanList = getKeyValue();
        LogUtils.d(TAG, " insertKeyInforBean:: beanList：" + beanList.toString());
        return beanList;
    }

    private List<KeyInforBean> getKeyValue(){
        List<KeyInforBean> list = new ArrayList<>();
        String[] splited = copy_file("key.txt").split("\r");
        for(int i = 0; i<splited.length; i++){
            String[] str = splited[i].split(" ");
            String keyName = str[0].trim();
            String keyValue = str[1].trim();
            String lastKey = keyValue.substring(0, 16);
            LogUtils.d(TAG, " getKeyValue:: key.txt keyName：" + keyName + " keyValue" + keyValue);
            KeyInforBean bean = new KeyInforBean(keyName, lastKey);
            LogUtils.d(TAG, " getKeyValue:: list add bean：" + bean.toString());
            list.add(bean);
        }
        return list;
    }

    public String copy_file(String fileName){
        String content="";
        FileInputStream fileInputStream =null;
        AssetManager assetManager = getContext().getResources().getAssets();
        try {
            InputStream is = assetManager.open(fileName);
            int length = is.available();// TODO: 2021/7/14 直接读取文件内容大小
            byte[] buf =new byte[length];// TODO: 2021/7/14 改为文件大小 // TODO: 2021/7/13 改为1024
            is.read(buf);
            content = new String(buf,"utf-8");// TODO: 2021/7/13 utf-8格式
            is.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
