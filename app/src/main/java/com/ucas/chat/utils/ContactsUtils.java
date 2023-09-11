package com.ucas.chat.utils;

import com.ucas.chat.bean.litepal.ContactsBean;

import org.litepal.crud.DataSupport;

import java.util.List;

public class ContactsUtils {

//    public static void buildMailListTable(Context context, String userId){
//        MySQLiteHelper helper = new MySQLiteHelper(context, ChatContract.TABLE_NAME_MAIL_LIST + userId);
//        setMailListTable(context, helper);
//    }

    /**
     * 初始化通讯录数据
     * @param context
     * @param helper
     */
//    public static void setMailListTable(Context context, MySQLiteHelper helper){
//        List<ContactsBean> userNameList = SharedPreferencesUtil.getListSharedPreferences(context, ConstantValue.USER_NAME_SHARE_KEY);
//        for (int i=0; i<userNameList.size(); i++){
//            ContactsBean bean = userNameList.get(i);
//            helper.savePerson(bean);
//        }
//        List<ContactsBean> list = helper.getPeople();
//        LogUtils.d("ContactsUtils", list.size());
//        for (int i=0; i<list.size(); i++){
//            LogUtils.d("ContactsUtils", list.get(i).toString());
//        }
//    }

    public static void changeNick(ContactsBean bean, String nickName){
//        ContentValues values = new ContentValues();
//        values.put("nickName", nickName);
//        DataSupport.update(ContactsBean.class, values, bean.getUserId());
       // bean.setNickName(nickName);
        //bean.update(bean.getUserId());
    }

    public static void findAll(){
        List<ContactsBean> bean = DataSupport.findAll(ContactsBean.class);
        for (ContactsBean p : bean) {
            LogUtils.d("ContactsBean", p.toString());
        }

    }
}
