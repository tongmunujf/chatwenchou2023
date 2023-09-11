package com.ucas.chat.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ucas.chat.bean.AddressBookBean;
import com.ucas.chat.bean.KeyInforBean;
import com.ucas.chat.bean.MyInforBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class KeyHelper extends SQLiteOpenHelper {
    public static final String TAG = ConstantValue.TAG_CHAT + "KeyHelper";
    public static final String DB_NAME = "Key.db";
    private final static int VERSION = 1;
    public static KeyHelper keyHelper = null;
    public static String table_name="key_information";

    private static KeyHelperTool mKeyHelperTool = KeyHelperTool.getInstance();

    public synchronized static KeyHelper getInstance(Context context){
        if (keyHelper == null) {
            keyHelper = new KeyHelper(context);
        }
        return keyHelper;
    }
    public KeyHelper(Context context){
        super(context,DB_NAME,null,VERSION);
        if(this.queryAll().size() == 0){
            List<KeyInforBean> beanList = mKeyHelperTool.insertKeyInforBean();
            for (KeyInforBean bean: beanList) {
                this.insertData(context,bean);
            }

        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtils.d(TAG, " onUpgrade:: " + oldVersion + " "+newVersion);
        if (oldVersion < newVersion){
            onCreate(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, " onCreate::");
        String sql="CREATE TABLE IF NOT EXISTS "+
                this.table_name+" ("+
                ChatContract.KEYFINFO.KEY_NAME+" Text,"+
                ChatContract.KEYFINFO.KEY_VALUE+" Text)";
        db.execSQL(sql);
    }

    public void insertData(Context context, KeyInforBean keyInforBean){
        LogUtils.e(TAG, " insertData:: " + keyInforBean.toString());
        ContentValues values = new ContentValues();
        values.put(ChatContract.KEYFINFO.KEY_NAME, keyInforBean.getKeyName());
        values.put(ChatContract.KEYFINFO.KEY_VALUE, keyInforBean.getKeyValue());
        SQLiteDatabase database = getWritableDatabase();
        try {
            database.insert(table_name, null, values);
        }catch (Exception e){
            e.printStackTrace();
            ToastUtils.showMessage(context, " insert单条异常 "+ e.toString());
            LogUtils.e(TAG, " insertData:: insert单条异常: " + e.toString());
        }finally {
            if (null != database) {
                database.close();
            }
        }
    }

    public ArrayList<KeyInforBean> queryAll() {
        ArrayList<KeyInforBean> beanList = new ArrayList<KeyInforBean>();
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name ,null);
        if(cursor.moveToFirst()){
            do{
                KeyInforBean person = new KeyInforBean(
                        cursor.getString(cursor.getColumnIndex(ChatContract.KEYFINFO.KEY_NAME)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.KEYFINFO.KEY_VALUE))
                );
                beanList.add(person);
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return beanList;
    }

    /**
     * 根据keyName查询
     * @param keyName
     * @return
     */
    public KeyInforBean queryByKeyName(String keyName) {
        KeyInforBean info = null;
        String condition = String.format("key_name='%s'", keyName);
       // LogUtils.d(TAG, " queryByKeyName:: condition = " + condition);
        List<KeyInforBean> infoList = query(condition);
        if (infoList.size() > 0) { // 存在该号码的登录信息
            info = infoList.get(0);
        }
        return info;
    }

    // 根据指定条件查询记录，并返回结果数据列表
    public List<KeyInforBean> query(String condition) {
        String sql = String.format("select key_name,key_value" +
                " from %s where %s;", table_name, condition);
       // LogUtils.d(TAG, " query:: sql: " + sql);
        List<KeyInforBean> infoList = new ArrayList<KeyInforBean>();
        // 执行记录查询动作，该语句返回结果集的游标
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        // 循环取出游标指向的每条记录
        while (cursor.moveToNext()) {
            KeyInforBean info = new KeyInforBean();
            info.setKeyName( cursor.getString(0));
            info.setKeyValue(cursor.getString(1));
            infoList.add(info);
        }
        cursor.close(); // 查询完毕，关闭数据库游标
        return infoList;
    }

}
