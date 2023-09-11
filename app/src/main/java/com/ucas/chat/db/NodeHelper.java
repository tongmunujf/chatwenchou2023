package com.ucas.chat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ucas.chat.bean.NodeBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.ToastUtils;

import java.util.ArrayList;

public class NodeHelper extends SQLiteOpenHelper {
    public static final String TAG = ConstantValue.TAG_CHAT + "NodeHelper";
    public static final String DB_NAME = "Node.db";
    private final static int VERSION = 6;
    public static NodeHelper mInstance = null;
    public static String table_name="Node";
    private NodeTool nodeTool=  NodeTool.getInstance();

    public synchronized static NodeHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NodeHelper(context);
        }
        return mInstance;
    };

    public NodeHelper(Context context){
        super(context,DB_NAME,null,VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, "onCreate");
        String sql="CREATE TABLE IF NOT EXISTS "+
                this.table_name+" ("+
                ChatContract.NODE.GUARDNODE+" Text,"+
                ChatContract.NODE.VALIDTIME+" Text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtils.d(TAG, "onUpgrade:: " + oldVersion + " "+newVersion);
        if (oldVersion < newVersion){
            onCreate(db);
        }
    }

    public void insertData(Context context, NodeBean nodeBean) {
        LogUtils.e(TAG, " insertData：" + nodeBean.toString());
        ContentValues values = new ContentValues();
        values.put(ChatContract.NODE.GUARDNODE, nodeBean.getGuard_node());
        values.put(ChatContract.NODE.VALIDTIME, nodeBean.getValid_time());
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
        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name ,null);
        String guardNode = null;
        String validTime;
        if(cursor.moveToFirst()){
                guardNode = cursor.getString(cursor.getColumnIndex(ChatContract.NODE.GUARDNODE));
                validTime = cursor.getString(cursor.getColumnIndex(ChatContract.NODE.VALIDTIME));
        }
        cursor.close();
        database.close();
        return guardNode;
    }
    
    /**
     * 删除所有
     * @return
     */
    public void deleteAll() {
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name ,null);
        if(cursor.moveToFirst()){
            do{
                String guardNode = cursor.getString(cursor.getColumnIndex(ChatContract.NODE.GUARDNODE));
                String validTime = cursor.getString(cursor.getColumnIndex(ChatContract.NODE.VALIDTIME));
                database.delete(table_name,ChatContract.NODE.GUARDNODE+"=?", new String[]{guardNode});
                database.delete(table_name,ChatContract.NODE.VALIDTIME+"=?", new String[]{validTime});

            }
            while(cursor.moveToNext());
        }
        cursor.close();
        database.close();
    }

    /**
     * 查询所有
     * @return
     */
    public ArrayList<NodeBean> queryAll() {
        ArrayList<NodeBean> contacts = new ArrayList<>();
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name ,null);
        if(cursor.moveToFirst()){
            do{
                String guardNode = cursor.getString(cursor.getColumnIndex(ChatContract.NODE.GUARDNODE));
                String validTime = cursor.getString(cursor.getColumnIndex(ChatContract.NODE.VALIDTIME));
                NodeBean person=new NodeBean(guardNode,validTime);
                contacts.add(person);
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return contacts;
    }

}
