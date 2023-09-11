package com.ucas.chat.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ucas.chat.bean.AddressBookBean;
import com.ucas.chat.bean.MyInforBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class AddressBookHelper extends SQLiteOpenHelper {
    public static final String TAG = ConstantValue.TAG_CHAT + "AddressBookHelper";
    public static final String DB_NAME = "AddressBook.db";
    private final static int VERSION = 8;
    public static AddressBookHelper mInstance = null;
    public static String table_name="address_book";
    private AddressBookTool addressBookTool=AddressBookTool.getInstance();

    public synchronized static AddressBookHelper getInstance(Context context){
        if (mInstance == null) {
            mInstance = new AddressBookHelper(context);
        }
        return mInstance;
    }
    public AddressBookHelper(Context context){super(context,DB_NAME,null,VERSION);
        //############
        if(this.queryAll().size()==0){
            List<AddressBookBean> contacts=addressBookTool.insertAddressBookBean();
            LogUtils.d(TAG, " AddressBookBean: " + contacts.toString());
            for(int i=0;i<contacts.size();i++){
                AddressBookBean bean = contacts.get(i);
                this.insertData(context,bean);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtils.d(TAG, "onUpgrade:: " + oldVersion + " "+newVersion);
        if (oldVersion < newVersion){
            onCreate(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, "onCreate");
        String sql="CREATE TABLE IF NOT EXISTS "+
                this.table_name+" ("+
                ChatContract.ADDRESSBOOK.NICK_NAME+" Text,"+
                ChatContract.ADDRESSBOOK.GENDER+" Integer,"+
                ChatContract.ADDRESSBOOK.HEADIMAGEPATH+" Text,"+
                ChatContract.ADDRESSBOOK.REMOTEONIONNAME+" Text,"+
                ChatContract.ADDRESSBOOK.REMARKS+" Text,"+
                ChatContract.ADDRESSBOOK.REMOTE_RSA_PUBLIC_KEY+" Text)";
        db.execSQL(sql);
    }

    public void insertData(Context context, AddressBookBean addressBookBean){
        LogUtils.e(TAG, " insertData：" +addressBookBean.toString());
        ContentValues values = new ContentValues();
        values.put(ChatContract.ADDRESSBOOK.NICK_NAME, addressBookBean.getNickName());
        values.put(ChatContract.ADDRESSBOOK.GENDER, addressBookBean.getGender());
        values.put(ChatContract.ADDRESSBOOK.HEADIMAGEPATH, addressBookBean.getHeadImagePath());
        values.put(ChatContract.ADDRESSBOOK.REMOTEONIONNAME, addressBookBean.getRemoteOnionName());
        values.put(ChatContract.ADDRESSBOOK.REMARKS, addressBookBean.getRemaks());
        values.put(ChatContract.ADDRESSBOOK.REMOTE_RSA_PUBLIC_KEY, addressBookBean.getRemotePublicKey());
        SQLiteDatabase database = getWritableDatabase();
        try {
            database.insert(table_name, null, values);
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
     * 查询所有
     * @return
     */
    public ArrayList<AddressBookBean>  queryAll() {
        ArrayList<AddressBookBean> contacts = new ArrayList<AddressBookBean>();
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name ,null);
        if(cursor.moveToFirst()){
            do{
                AddressBookBean person=new AddressBookBean(
                        cursor.getString(cursor.getColumnIndex(ChatContract.ADDRESSBOOK.NICK_NAME)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.ADDRESSBOOK.GENDER)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.ADDRESSBOOK.HEADIMAGEPATH)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.ADDRESSBOOK.REMOTEONIONNAME)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.ADDRESSBOOK.REMOTE_RSA_PUBLIC_KEY)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.ADDRESSBOOK.REMARKS)));
                contacts.add(person);
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return contacts;
    }

}
