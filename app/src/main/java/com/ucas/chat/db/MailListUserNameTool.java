package com.ucas.chat.db;

import android.content.Context;
import android.util.Log;

import com.ucas.chat.MyApplication;
import com.ucas.chat.bean.AddressBookBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.tor.util.FileUtil;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.AesUtils;
import com.ucas.chat.utils.LogUtils;

import org.apaches.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;


public class MailListUserNameTool {

    public static final String TAG = ConstantValue.TAG_CHAT + "MailListUserNameTool";

    private static final MailListUserNameTool mInstance = new MailListUserNameTool();
    private MailListUserNameTool() {
    }

    public static MailListUserNameTool getInstance() {
        return mInstance;
    }
    public static final int MAIL_LIST_LENGTH = 2;

    public final static String DEF_NICK_NAME = "-1";
    private final static String[] IMAGE_ID_ARR = new String[]{"0", "1"};
    private final static String[] USER_NAME_ARR = new String[]{"Mark", "Tom"};
    private final static String[] ORION_ARR = new String[]{
            "xlv76wpa7xzzpe73k2vr27x53m27s23t62ukhrizsdvbkeilxpnd6eyd.onion",
            "vih7k7l363b5jbpkzxercrmfye6p4wdd264g7b4bfmhezg4a34c4abqd.onion",};

    public static List<ContactsBean> initMailList(){

        AddressBookHelper helper = AddressBookHelper.getInstance( MyApplication.getContext());

        List<ContactsBean> list = new ArrayList<>();
        List<String> orionIsList = geOrionHashId();
        for (int i=0; i<MAIL_LIST_LENGTH; i++){
            int userId = 1000+i;
            ContactsBean bean = new ContactsBean(userId+"",USER_NAME_ARR[i], ConstantValue.DEF_PW,
                    ConstantValue.DEF_NICK_NAME, ORION_ARR[i], orionIsList.get(i), IMAGE_ID_ARR[i]);
            list.add(bean);
        }

        return list;
    }

    private static List<String> geOrionHashId(){
        List<String> orionHashIdList = new ArrayList<>();
        for (int i=0; i<ORION_ARR.length; i++){
            String orionId = ORION_ARR[i];
            byte[] orionHashIdByte = AESCrypto.digest_fast(orionId.getBytes());
            String orionHashId = new String(orionHashIdByte);
            orionHashIdList.add(orionHashId);
//            Log.d(TAG," orionHashId = " + AESCrypto.bytesToHex(orionHashId.getBytes()) );
        }
        return orionHashIdList;
    }

    /**
     * 判断修改密码的用户名是否存在
     * @param name
     * @return
     */
    public static boolean isExistUserName(Context context, String name){
       // List<ContactsBean> userNameList = SharedPreferencesUtil.getListSharedPreferences(context,ConstantValue.USER_NAME_SHARE_KEY);
        List<ContactsBean> userNameList = initMailList();
        for (int i=0; i<userNameList.size(); i++){
            ContactsBean bean = userNameList.get(i);
            if (name.equals(bean.getUserName())){
                return true;
            }
        }
        return false;
    }

    /**
     * 根据名字得到对于userId
     * @param context
     * @param name
     * @return
     */
    public static String getUserId(Context context, String name){
      //  List<ContactsBean> userNameList = SharedPreferencesUtil.getListSharedPreferences(context,ConstantValue.USER_NAME_SHARE_KEY);
        List<ContactsBean> userNameList = initMailList();
        for (int i=0; i<userNameList.size(); i++){
            ContactsBean bean = userNameList.get(i);
            if (name.equals(bean.getUserName())){
                return bean.getUserId();
            }
        }
        return null;
    }


    public static int getOneSelfImage(Context context, String name){
        MailListSQLiteHelper helper = MailListSQLiteHelper.getInstance(context);
        return 1;
    }

}
