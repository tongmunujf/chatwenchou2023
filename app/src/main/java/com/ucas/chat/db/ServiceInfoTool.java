package com.ucas.chat.db;


import android.content.ContentValues;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import com.ucas.chat.bean.ServiceInfoBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.tor.util.FileUtil;
import com.ucas.chat.utils.LogUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.ucas.chat.MyApplication.getContext;

public class ServiceInfoTool {
    public static final String TAG = ConstantValue.TAG_CHAT + "ServiceInfoTool";
    public static String table_name = "tablename";
    private static final ServiceInfoTool mInstance = new ServiceInfoTool();
    private ServiceInfoHelper mServerHelper;

    public static final int SERVICE_INFO_LENGTH = 2;
    public final static String[] SERVICE_INFO_NODE = new String[]{"r4c5vjwreftmhmz3zde4p2l3xjevrcx7wtdtqzi6o7ooq6vfs6sysgad.onion"};
    public final static String[] SERVICE_COMMUNICATION = new String[]{"3kxo6zl3crqalsliipvrt3x3detzdujaoqquusyfovus44fy6pyw35id.onion"};

    public static ServiceInfoTool getInstance() {return mInstance;}

    public static List<ServiceInfoBean> initSeviceInfo(){
        List<ServiceInfoBean> list = new ArrayList<>();

        for (int i = 0; i<SERVICE_INFO_LENGTH;i++){

            ServiceInfoBean bean = new ServiceInfoBean(SERVICE_INFO_NODE[i],SERVICE_COMMUNICATION[i]);
            list.add(bean);

        }
        return list;
    }

    public ServiceInfoBean readInfoFromFile(){

        //String[] splited = copy_file("server.txt").split("\r");
        String[] splited = FileUtil.readFileFromSdcardChatUser(FilePathUtils.SERVER_NAME).split("\r");
        SERVICE_INFO_NODE[0]=splited[0].trim();
        SERVICE_COMMUNICATION[0]=splited[1].trim();
        LogUtils.d(TAG," readInfoFromFile:: server.txt splited[0] = " + splited[0]);
        LogUtils.d(TAG," readInfoFromFile:: server.txt splited[1] = " + splited[1]);
        ServiceInfoBean bean = new ServiceInfoBean(splited[0].trim(),splited[1].trim());
        return bean;
    }

    public static String getServiceCommunication(){
        return SERVICE_COMMUNICATION[0];
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

//            while((length = is.read(buf))!=-1){
//                content += new String(buf, "utf-8");// TODO: 2021/7/13 utf-8格式
//            }

        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return content;
    }

}
