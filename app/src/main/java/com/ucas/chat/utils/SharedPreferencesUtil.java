package com.ucas.chat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.tor.util.RecordXOR;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SharedPreferencesUtil {

    /**
     * 存List
     * @param
     * @param context
     * @param key
     * @param dataList
     */
    public static void setListSharedPreferences(Context context, String key, List<ContactsBean> dataList) {
        if (null == dataList || dataList.size() <= 0){
            LogUtils.d("SharedPreferencesUtil "," dataList is null");
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.USER_NAME_SHARE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String strJson = gson.toJson(dataList);
        editor.clear();
        editor.putString(key, strJson);
        editor.commit();
        LogUtils.d("SharedPreferencesUtil ","dataList = " + strJson);
    }

    public static List<ContactsBean> getListSharedPreferences(Context context, String key) {
        List<ContactsBean> dataList = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.USER_NAME_SHARE_NAME, Activity.MODE_PRIVATE);
        String strJson = sharedPreferences.getString(key, null);
        if (null == strJson) {
            return dataList;
        }
        Gson gson = new Gson();
        dataList = gson.fromJson(strJson, new TypeToken<List<ContactsBean>>() {
        }.getType());
        return dataList;
    }

    public static void setUserBeanSharedPreferences(Context context, UserBean bean) {

        System.out.println("网络--写入BeanSharedPreferences： "+bean.getOnlineStatus());

        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.USER_BEAN_STARE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonStr=gson.toJson(bean);
        editor.putString(ConstantValue.USER_BEAN_STARE_KEY, jsonStr);
        editor.commit();
    }

    public static UserBean getUserBeanSharedPreferences(Context context) {
        UserBean bean = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.USER_BEAN_STARE_NAME, Activity.MODE_PRIVATE);
        String jsonStr = sharedPreferences.getString(ConstantValue.USER_BEAN_STARE_KEY,"");
        if(jsonStr!=""){
            Gson gson = new Gson();
            bean = gson.fromJson(jsonStr, UserBean.class);
        }
        return bean;
    }

    public static void setContactBeanSharedPreferences(Context context, ContactsBean bean) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.CONTACT_BEAN_STARE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonStr=gson.toJson(bean);
        editor.putString(ConstantValue.CONTACT_BEAN_STARE_KEY, jsonStr);
        editor.commit();
    }

    public static ContactsBean getContactBeanSharedPreferences(Context context) {
        ContactsBean bean = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.CONTACT_BEAN_STARE_NAME, Activity.MODE_PRIVATE);
        String jsonStr = sharedPreferences.getString(ConstantValue.CONTACT_BEAN_STARE_KEY,"");
        if(jsonStr!=""){
            Gson gson = new Gson();
            bean = gson.fromJson(jsonStr, ContactsBean.class);
        }
        return bean;
    }


    public static void setIntSharedPreferences(Context context, String name, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setBooleanSharedPreferences(Context context, String name, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setFloatSharedPreferences(Context context, String name, String key, float value) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static void setStringSharedPreferences(Context context, String name, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.contains(key)) {
            editor.remove(key);
            editor.commit();
        }
        editor.putString(key, value);
        editor.commit();
    }

    public static void setLongSharedPreferences(Context context, String name, String key, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void setStringSetSharedPreferences(Context context, String name, String key, Set<String> value) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }

    public static int getIntSharedPreferences(Context context, String name, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        int value = sharedPreferences.getInt(key, 0);
        return value;
    }

    public static boolean getBooleanSharedPreferences(Context context, String name, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        boolean value = sharedPreferences.getBoolean(key, true);
        return value;
    }

    public static float getFloatSharedPreferences(Context context, String name, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        float value = sharedPreferences.getFloat(key, 0);
        return value;
    }

    public static String getStringSharedPreferences(Context context, String name, String key,String defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        String value = sharedPreferences.getString(key, defValue);
        return value;
    }

    public static long getLongSharedPreferences(Context context, String name, String key) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        long value = sharedPreferences.getLong(key, 0);
        return value;
    }

    public static Set<String> getStringSetSharedPreferences(Context context, String name, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        Set<String> value = sharedPreferences.getStringSet(key, null);
        return value;
    }


    public static void saveCommonRecordXOR(Context context , RecordXOR recordXOR){// TODO: 2021/10/26 保存全局文件xor异或指针

        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.COMMON_RECORDXOR, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        System.out.println("将要保存的指针："+recordXOR);

        Gson gson = new Gson();
        String jsonStr=gson.toJson(recordXOR);

        editor.putString(ConstantValue.MESSAGE_RECORDXOR, jsonStr);
        editor.commit();//保存

    }


    public static RecordXOR getCommonRecordXOR(Context context){// TODO: 2021/10/26  获取的保存全局文件xor异或指针

        RecordXOR recordXOR = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.COMMON_RECORDXOR, Activity.MODE_PRIVATE);
        String jsonStr = sharedPreferences.getString(ConstantValue.MESSAGE_RECORDXOR,"");
        if(jsonStr!=""){
            Gson gson = new Gson();
            recordXOR = gson.fromJson(jsonStr, RecordXOR.class);
        }

        System.out.println("已保存的指针："+recordXOR);

        return recordXOR;//注意为空


    }



    public static void saveDestroyData(Context context){// TODO: 2021/10/26 保存销毁数据的标签

        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.DESTROY_DATA, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        editor.putBoolean(ConstantValue.BOOLER, true);
        editor.commit();//保存

    }


    public static boolean getDestroyDataboolean(Context context){// TODO: 2021/10/26 获取销毁数据的标签

        boolean b = false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.DESTROY_DATA, Activity.MODE_PRIVATE);
        b = sharedPreferences.getBoolean(ConstantValue.BOOLER,false);

        return b;//注意为空


    }



    public static void saveMailListData(Context context){// TODO: 2022/3/21   保存MailList数据的标签

        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.MAILLIST_DATA, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        editor.putBoolean(ConstantValue.BOOLER, true);
        editor.commit();//保存

    }


    public static boolean getMailListDataboolean(Context context){// TODO: 2022/3/21 获取maillist数据的标签

        boolean b = false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValue.MAILLIST_DATA, Activity.MODE_PRIVATE);
        b = sharedPreferences.getBoolean(ConstantValue.BOOLER,false);

        return b;//注意为空


    }








}
