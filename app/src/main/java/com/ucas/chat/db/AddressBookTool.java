package com.ucas.chat.db;

import android.content.res.AssetManager;
import android.util.Log;

import com.ucas.chat.MyApplication;
import com.ucas.chat.bean.AddressBookBean;
import com.ucas.chat.bean.MyInforBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.tor.util.FileUtil;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.AesUtils;
import com.ucas.chat.utils.FileUtils;
import com.ucas.chat.utils.LogUtils;

import org.apaches.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.zlylib.upperdialog.utils.ResUtils.getResources;

public class AddressBookTool {
    public static final String TAG = ConstantValue.TAG_CHAT + "AddressBookTool";
    private String nickName="";
    private int gender = 1;
    private String headImagePath="Temporarily none";
    private String  remoteOnionName="";
    private String remotePublicKey="";
    private String remaks="Temporarily none";


    private static final AddressBookTool mInstance = new AddressBookTool();
    private  AddressBookTool(){}
    public static AddressBookTool getInstance(){
        return mInstance;
    }


    public List<AddressBookBean> insertAddressBookBean(){
        List<AddressBookBean> list = new ArrayList<>();
        //判断有几个好友就生成几个AddressBookBean
       // String[] splited = copy_file("contact.txt").split("\r");
        String[] splited = FileUtil.readFileFromSdcardChatUser(FilePathUtils.CONTACT_NAME).split("\r");
        for(int i = 0;i<splited.length;i++){
            String[] str = splited[i].split(" ");
            remoteOnionName = str[0];
            nickName = str[1];
            LogUtils.d(TAG, " insertAddressBookBean:: 转化前remoteOnionName = " + remoteOnionName);
            LogUtils.d(TAG," insertAddressBookBean:: 转化前nickName = " + nickName);

            String decryptRemoteOnionName = AesTools.getDecryptContent(remoteOnionName, AesTools.AesKeyTypeEnum.COMMON_KEY);
            String decryptNickName = AesTools.getDecryptContent(nickName, AesTools.AesKeyTypeEnum.COMMON_KEY);
            LogUtils.d(TAG, " insertAddressBookBean:: 转化后decryptRemoteOnionName = " + decryptRemoteOnionName);
            LogUtils.d(TAG," insertAddressBookBean:: 转化后decryptNickName = " + decryptNickName);

            String filename = DigestUtils.sha256Hex(decryptRemoteOnionName)+"_public_key";
            LogUtils.d(TAG," insertAddressBookBean:: filename = " + filename);

           // remotePublicKey = FileUtils.copy_file(filename);
            remotePublicKey = FileUtil.readFileFromSdcardChatUser(filename);
            LogUtils.d(TAG, " insertAddressBookBean:: xxx_public_key.txt remotePublicKey: " + remotePublicKey);

//            String SStr = copy_file(filename);
//            String SStr1 = SStr.replaceAll("-----BEGIN PUBLIC KEY-----\r\n","");
//            remotePublicKey = SStr1.replaceAll("-----END PUBLIC KEY-----\r\n","");
//            LogUtils.d(TAG," insertAddressBookBean:: remotePublicKey = " + remotePublicKey);

//

            AddressBookBean bean = new AddressBookBean(nickName,gender,headImagePath,remoteOnionName,remotePublicKey.trim(),
                    remaks);
            list.add(bean);
        }
        return list;
    }

    public static String copy_file(String fileName){
        String content="";

        FileInputStream fileInputStream =null;
        AssetManager assetManager = MyApplication.getContext().getResources().getAssets();
        try {
            InputStream is = assetManager.open(fileName);
            int length = is.available();// TODO: 2021/7/14 直接读取文件内容大小
            byte[] buf =new byte[length];// TODO: 2021/7/14 改为文件大小 // TODO: 2021/7/13 改为1024
            is.read(buf);
            content = new String(buf,"utf-8");// TODO: 2021/7/13 utf-8格式
            is.close();

//            while((length = is.read(buf))!=-1){
//                content += new String(buf,"utf-8");// TODO: 2021/7/13 utf-8格式
//            }
            System.out.println("读取到的内容是："+ content);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
