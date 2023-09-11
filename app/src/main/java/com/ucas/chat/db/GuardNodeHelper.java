package com.ucas.chat.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.utils.LogUtils;

public class GuardNodeHelper extends SQLiteOpenHelper {
    public static final String TAG = ConstantValue.TAG_CHAT + "GuardNodeHelper";
    public static final String DB_NAME = "GuardNode.db";
    private final static int VERSION = 6;
    public static GuardNodeHelper mInstance = null;
    public static String table_name;
    public synchronized static GuardNodeHelper getInstance(Context context){
        if (mInstance == null) {
            mInstance = new GuardNodeHelper(context);
        }
        return mInstance;
    }
    public GuardNodeHelper(Context context){super(context,DB_NAME,null,VERSION);}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, "onCreate");
        String sql="CREATE TABLE IF NOT EXISTS "+
                this.table_name+" ("+
                ChatContract.GuardNodeEntry.VALID_TIME+" Text,"+
                ChatContract.GuardNodeEntry.GUARD_COMMUNICATION+" Text,"+
                ChatContract.GuardNodeEntry.GUARD_NODE+" Text)";
        db.execSQL(sql);
    }

}
