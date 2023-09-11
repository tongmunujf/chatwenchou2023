package org.torproject.android.service.nodedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class NodeHelper extends SQLiteOpenHelper {
    public static final String TAG = "Chat_NodeHelper";
    public static final String DB_NAME = "Node.db";
    private final static int VERSION = 6;
    public static NodeHelper mInstance = null;
    public static String table_name = "Node";
    private NodeTool nodeTool = NodeTool.getInstance();

    public static final String GUARDNODE = "guard_node";
    public static final String VALIDTIME = "valid_time";

    public synchronized static NodeHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NodeHelper(context);
        }
        return mInstance;
    }

    ;

    public NodeHelper(Context context) {
        super(context, DB_NAME, null, VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " +
                this.table_name + " (" +
                GUARDNODE + " Text," +
                VALIDTIME + " Text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < newVersion) {
            onCreate(db);
        }
    }

    public void insertData(Context context, NodeBean nodeBean) {

        ContentValues values = new ContentValues();
        values.put(GUARDNODE, nodeBean.getGuard_node());
        values.put(VALIDTIME, nodeBean.getValid_time());
        SQLiteDatabase database = getWritableDatabase();
        try {
            database.insert(table_name, null, values);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (null != database) {
                database.close();
            }
        }
    }

    /**
     * 查询所有guard_node
     */
    public String queryAllGuardNode() {
        String guard_node_t = "";
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + table_name, null);
        if (cursor.moveToFirst()) {
            do {
                String guardNode = cursor.getString(cursor.getColumnIndex(GUARDNODE));
                guard_node_t += (guardNode + ",");
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        String guard_node = guard_node_t.substring(0, guard_node_t.length() - 1);
        return guard_node;
    }

    /**
     * 获取第一条信息
     */
    public String getFirst() {
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + table_name, null);
        String guardNode = null;
        String validTime;
        if (cursor.moveToFirst()) {
            guardNode = cursor.getString(cursor.getColumnIndex(GUARDNODE));
            validTime = cursor.getString(cursor.getColumnIndex(VALIDTIME));
        }
        cursor.close();
        database.close();
        return guardNode;
    }

    /**
     * 删除所有
     *
     * @return
     */
    public void deleteAll() {
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + table_name, null);
        if (cursor.moveToFirst()) {
            do {
                String guardNode = cursor.getString(cursor.getColumnIndex(GUARDNODE));
                String validTime = cursor.getString(cursor.getColumnIndex(VALIDTIME));
                database.delete(table_name, GUARDNODE + "=?", new String[]{guardNode});
                database.delete(table_name, VALIDTIME + "=?", new String[]{validTime});

            }
            while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
    }

    /**
     * 查询所有
     *
     * @return
     */
    public ArrayList<NodeBean> queryAll() {
        ArrayList<NodeBean> contacts = new ArrayList<>();
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + table_name, null);
        if (cursor.moveToFirst()) {
            do {
                String guardNode = cursor.getString(cursor.getColumnIndex(GUARDNODE));
                String validTime = cursor.getString(cursor.getColumnIndex(VALIDTIME));
                NodeBean person = new NodeBean(guardNode, validTime);
                contacts.add(person);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return contacts;
    }

}
