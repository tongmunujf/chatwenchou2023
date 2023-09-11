package com.ucas.chat.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

//import com.ucas.chat.InterfaceAndroid.main.Constant;
import com.ucas.chat.bean.ServiceInfoBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.ToastUtils;

//服务器节点的数据库
public class ServiceInfoHelper extends SQLiteOpenHelper {
    public static final String TAG = ConstantValue.TAG_CHAT + "ServiceHelper";
    public static final String DB_NAME= "ServiceInfoList.db";
    public final static int VERSION = 1;

    public static String table_name = "tablename";

    private static ServiceInfoHelper mInstance = null;

    public ServiceInfoHelper(Context context) {
        super(context,DB_NAME,null,VERSION);

        ServiceInfoBean bean =ServiceInfoTool.getInstance().readInfoFromFile();
        insertData(context,bean);

    }


    public synchronized static ServiceInfoHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ServiceInfoHelper(context);
        }
        return mInstance;
    };
    //    public ServiceInfoHelper(Context context,String NodeServiceId, String CommunicationServerId,
//                             int version){
//        super(context,DB_NAME,null,VERSION);
//    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, "onCreate");
        String sql="CREATE TABLE IF NOT EXISTS "+
                table_name+"("+
                ChatContract.ServerInfoEntry.NODE_SERVER_ID+" Text,"+
                ChatContract.ServerInfoEntry.COMMUNICATION_SERVER_ID+" Text)";
        db.execSQL(sql);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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

    public void insertData(Context context, ServiceInfoBean serviceInfoBean){
        LogUtils.e(TAG, " insertData：serviceInfoBean = " +serviceInfoBean.toString());
        ContentValues values = new ContentValues();
        values.put(ChatContract.ServerInfoEntry.NODE_SERVER_ID, serviceInfoBean.getNode_server_id());
        values.put(ChatContract.ServerInfoEntry.COMMUNICATION_SERVER_ID, serviceInfoBean.getCommunication_server_id());

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
     * 获取第一条信息
     */
    public String getFirst() {
        SQLiteDatabase database = getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + table_name, null);
        String NODE_SERVER_ID = null;
        String COMMUNICATION_SERVER_ID;
        if (cursor.moveToFirst()) {
            NODE_SERVER_ID = cursor.getString(cursor.getColumnIndex(ChatContract.ServerInfoEntry.NODE_SERVER_ID));
            COMMUNICATION_SERVER_ID = cursor.getString(cursor.getColumnIndex(ChatContract.ServerInfoEntry.COMMUNICATION_SERVER_ID));
        }
        cursor.close();
        database.close();
        return NODE_SERVER_ID;
    }
    // 获取第二条数据
    public String getSecond() {
        SQLiteDatabase database = getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + table_name, null);
        String NODE_SERVER_ID = null;
        String COMMUNICATION_SERVER_ID = null;
        if (cursor.moveToFirst()) {
            NODE_SERVER_ID = cursor.getString(cursor.getColumnIndex(ChatContract.ServerInfoEntry.NODE_SERVER_ID));
            COMMUNICATION_SERVER_ID = cursor.getString(cursor.getColumnIndex(ChatContract.ServerInfoEntry.COMMUNICATION_SERVER_ID));
        }
        cursor.close();
        database.close();
        return COMMUNICATION_SERVER_ID;
    }
}
