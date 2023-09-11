package com.ucas.chat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.TextUtils;
import com.ucas.chat.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class MailListSQLiteHelper extends SQLiteOpenHelper {

    public static final String TAG = ConstantValue.TAG_CHAT + "MailListSQLiteHelper";
    public static final String DB_NAME="MailList.db";
    private final static int VERSION= 8;
    public static final String TABLE_NAME_TAG="contacts_list";
    public static String table_name="contacts";

    private static MailListSQLiteHelper mInstance = null;
    public synchronized static MailListSQLiteHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MailListSQLiteHelper(context);
        }
        return mInstance;
    };

    public MailListSQLiteHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, "onCreate");
        String sql="CREATE TABLE IF NOT EXISTS "+
                this.table_name+" ("+
                ChatContract.MailListEntry.USER_ID+" Text,"+
                ChatContract.MailListEntry.USER_NAME+" Text,"+
                ChatContract.MailListEntry.PASS_WORD+" Text,"+
                ChatContract.MailListEntry.NICK_NAME+" Text,"+
                ChatContract.MailListEntry.ORION_ID+" Text,"+
                ChatContract.MailListEntry.ORION_HASH_ID+" Text,"+
                ChatContract.MailListEntry.IMAGE_ID+" Text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 插入单条数据m
     */
    public void insertData(Context context, ContentValues contentValues){
        LogUtils.e(TAG, " insertData::" + contentValues.toString());
        SQLiteDatabase database=getWritableDatabase();
        try {
            database.insert(table_name, ChatContract.MailListEntry.USER_NAME, contentValues);//直接添加会重复
        }catch (Exception e){
            e.printStackTrace();
            ToastUtils.showMessage(context, "insert单条异常 "+ e.toString());
            LogUtils.e(TAG, "insert单条异常：" + e.toString());
        }finally {
            if (null != database) {
                database.close();
            }
        }
    }

    /**
     * 插入多条数据
     * @param list
     */
    public void insertData(Context context, List<ContactsBean> list){
        SQLiteDatabase database=getWritableDatabase();
        try {
            for (ContactsBean bean : list) {
                ContentValues contentValues = new ContentValues();
                contentValues.clear();
                contentValues.put(ChatContract.MailListEntry.USER_ID,bean.getUserId());
                contentValues.put(ChatContract.MailListEntry.USER_NAME, bean.getUserName());
                contentValues.put(ChatContract.MailListEntry.PASS_WORD, bean.getPassWord());
                contentValues.put(ChatContract.MailListEntry.NICK_NAME, bean.getNickName());
                contentValues.put(ChatContract.MailListEntry.ORION_ID, bean.getOrionId());
                contentValues.put(ChatContract.MailListEntry.ORION_HASH_ID, bean.getOrionHashId());
                contentValues.put(ChatContract.MailListEntry.IMAGE_ID, bean.getImageId());
                database.insert(table_name, null, contentValues);
            }
        }catch (Exception e){
            e.printStackTrace();
            ToastUtils.showMessage(context, "insert多条数据异常 "+ e.toString());
            LogUtils.e(TAG, "insert多条异常：" + e.toString());
        }finally {
            if (null != database){
                database.close();
            }
        }
    }

    /**
     * 清除表数据
     */
    public void cleanUpData(Context context){
        SQLiteDatabase database=getWritableDatabase();
        String cleanUpDataSQL = "delete from " + table_name;
        try {
            database.execSQL(cleanUpDataSQL);
        }catch (Exception e){
            e.printStackTrace();
            ToastUtils.showMessage(context, "清除表的数据异常"+ e.toString());
            LogUtils.e(TAG, "insert多条异常：" + e.toString());
        }finally {
            if (null != database){
                database.close();
            }
        }
    }

    /**
     * 更新密码
     * @param table_name
     * @param userName
     * @param passWord
     */
    public void updatePassWord(String table_name, String userName, String passWord){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(ChatContract.MailListEntry.PASS_WORD, passWord);
        database.update(table_name,value,ChatContract.MailListEntry.USER_NAME+ "=?", new String[]{userName});
    }

    /**
     * 更新备注
     * @param table_name
     * @param userName
     * @param nickName
     */
    public void updateNickName(String table_name, String userName, String nickName){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(ChatContract.MailListEntry.NICK_NAME, nickName);
        database.update(table_name,value,ChatContract.MailListEntry.USER_NAME+ "=?", new String[]{userName});
    }

    /**
     * 删除表，会把表给删除，慎用
     * drop 表
     * */
    public void dropTable(){
        SQLiteDatabase database = getWritableDatabase();
        String dropSQL = "drop table if exists " + TABLE_NAME_TAG+10000;
        try {
            database.execSQL(dropSQL);
        }catch (Exception e){
            e.printStackTrace();
            LogUtils.d("MySQLiteHelper", " drop异常：" + e.toString());
        }finally {
            if (null != database){
                database.close();
            }
        }
    }

    /**
     * 查询所有
     * @return
     */
    public List<ContactsBean> queryAll() {
        LogUtils.e(TAG, " queryAll::");
        List<ContactsBean> personList = new ArrayList<>();
        SQLiteDatabase database = getWritableDatabase();
        if (TextUtils.isEmpty(table_name)) {
            return personList;
        }
        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name,null);
        if(cursor.moveToFirst()){
            do{
                ContactsBean person=new ContactsBean(
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.USER_ID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.USER_NAME)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.PASS_WORD)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.NICK_NAME)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.ORION_ID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.ORION_HASH_ID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.IMAGE_ID)));
                personList.add(person);
            }
            while(cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return personList;
    }

     /* 判断某张表是否存在
      * @param tabName 表名
      * @return
     */
    public boolean tabIsExist(String tabName){
        boolean result = false;
        if(tabName == null){
            return false;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();//此this是继承SQLiteOpenHelper类得到的
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"+tabName.trim()+"' ";
            cursor = db.rawQuery(sql, null);
            if(cursor.moveToNext()){
                int count = cursor.getInt(0);
                if(count>0){
                    result = true;
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }




    public ContactsBean getContactsBean(String peerHostname){// TODO: 2022/3/22 获取 peerHostname对应的好友名

        SQLiteDatabase database = getWritableDatabase();

        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name,null);
        if(cursor.moveToFirst()){
            do{

                ContactsBean person=new ContactsBean(
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.USER_ID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.USER_NAME)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.PASS_WORD)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.NICK_NAME)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.ORION_ID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.ORION_HASH_ID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MailListEntry.IMAGE_ID)));
                String orionID = AesTools.getDecryptContent(person.getOrionId(), AesTools.AesKeyTypeEnum.COMMON_KEY);
                LogUtils.d(TAG, " getContactsBean:: 转换后orionID: " + orionID);
                if(orionID.equals(peerHostname)) {
                    return person;

                }

            }
            while(cursor.moveToNext());
        }


        return null;
    }





}
