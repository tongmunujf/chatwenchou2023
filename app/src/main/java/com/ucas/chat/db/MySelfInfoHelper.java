package com.ucas.chat.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ucas.chat.bean.MsgListBean;
import com.ucas.chat.bean.MyInforBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.session1.MsgTypeStateNew;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.TimeUtils;
import com.ucas.chat.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ucas.chat.MyApplication.getContext;

public class MySelfInfoHelper extends SQLiteOpenHelper {
    public static final String TAG = ConstantValue.TAG_CHAT + "MySelfInfoHelper";
    public static final String DB_NAME = "MyInfo.db";
    private final static int VERSION = 7;
    public static MySelfInfoHelper mInstance = null;
    public static String table_name="myself_information";
    private static MyInforTool myInforTool=MyInforTool.getInstance();
    public synchronized static MySelfInfoHelper getInstance(Context context){
        if (mInstance == null) {
            mInstance = new MySelfInfoHelper(context);
        }
        return mInstance;
    }
    public MySelfInfoHelper(Context context){super(context,DB_NAME,null,VERSION);
        if(this.queryAll()==null){
            MyInforBean bean=myInforTool.insertMyInforBean();
            this.insertData(context,bean);
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
                ChatContract.MYSELFINFO.ACCOUNT+" Text,"+
                ChatContract.MYSELFINFO.PASSWORD+" Text,"+
                ChatContract.MYSELFINFO.NICK_NAME+" Text,"+
                ChatContract.MYSELFINFO.GENDER+" Integer,"+
                ChatContract.MYSELFINFO.HEADIMAGEPATH+" Text,"+
                ChatContract.MYSELFINFO.ONIONNAME+" Text,"+
                ChatContract.MYSELFINFO.RSA_PRIVATE_KEY+" Text,"+
                ChatContract.MYSELFINFO.RSA_PUBLIC_KEY+" Text)";
        db.execSQL(sql);
    }

    public void insertData(Context context, MyInforBean myInforBean){
        LogUtils.e(TAG, " insertData：" +myInforBean.toString());
        ContentValues values = new ContentValues();
        values.put(ChatContract.MYSELFINFO.ACCOUNT, myInforBean.getAccount());
        values.put(ChatContract.MYSELFINFO.PASSWORD, myInforBean.getPassword());
        values.put(ChatContract.MYSELFINFO.NICK_NAME, myInforBean.getNickName());
        values.put(ChatContract.MYSELFINFO.GENDER, myInforBean.getGender());
        values.put(ChatContract.MYSELFINFO.HEADIMAGEPATH, myInforBean.getHeadImagePath());
        values.put(ChatContract.MYSELFINFO.ONIONNAME, myInforBean.getOnionName());
        values.put(ChatContract.MYSELFINFO.RSA_PRIVATE_KEY, myInforBean.getPrivateKey());
        values.put(ChatContract.MYSELFINFO.RSA_PUBLIC_KEY, myInforBean.getPublicKey());
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
    public MyInforBean queryAll() {
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name +" limit 1",null);
        MyInforBean person=null;
        if(cursor.moveToFirst()){
            do{
                person=new MyInforBean(
                        cursor.getString(cursor.getColumnIndex(ChatContract.MYSELFINFO.ACCOUNT)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MYSELFINFO.PASSWORD)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MYSELFINFO.NICK_NAME)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.MYSELFINFO.GENDER)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MYSELFINFO.HEADIMAGEPATH)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MYSELFINFO.ONIONNAME)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MYSELFINFO.RSA_PUBLIC_KEY)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MYSELFINFO.RSA_PRIVATE_KEY)));// TODO: 2021/7/16 公私钥要对应 MyInforBean构造器
                break;
            }
            while(cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return person;
    }

}
