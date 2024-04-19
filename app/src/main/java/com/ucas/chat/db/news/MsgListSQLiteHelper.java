package com.ucas.chat.db.news;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ucas.chat.bean.MsgListBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.db.ChatContract;
import com.ucas.chat.db.MailListSQLiteHelper;
import com.ucas.chat.db.MyInforTool;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.TextUtils;
import com.ucas.chat.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ucas.chat.MyApplication.getContext;

//存放消息的数据库
public class MsgListSQLiteHelper extends SQLiteOpenHelper {

    public static final String TAG = ConstantValue.TAG_CHAT + "MsgListSQLiteHelper";
    public static final String DB_NAME="MsgList.db";
    private final static int VERSION= 7;
    private static String table_name = "chat_message_table";

    private static MsgListSQLiteHelper mInstance = null;
    public synchronized static MsgListSQLiteHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MsgListSQLiteHelper(context);
        }
        return mInstance;
    };

    public MsgListSQLiteHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    /**
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, "onCreate");
        String sql =
                "CREATE TABLE IF NOT EXISTS " + this.table_name + " (" +
                        ChatContract.MsgListEntry.SEND_TIME + " TEXT," +
                        ChatContract.MsgListEntry.CHAT_TYPE + " Integer," +
                        ChatContract.MsgListEntry.TEXT_CONTENT + " TEXT," +
                        ChatContract.MsgListEntry.FILE_PATH + " TEXT," +
                        ChatContract.MsgListEntry.FILE_NAME + " TEXT," +
                        ChatContract.MsgListEntry.FILE_SIZE + " Integer," +
                        ChatContract.MsgListEntry.FILE_PROGRESS + " Integer," +
                        ChatContract.MsgListEntry.FROM + " TEXT," +
                        ChatContract.MsgListEntry.TO + " TEXT," +
                        ChatContract.MsgListEntry.IS_ACKED + " Integer,"+
                        ChatContract.MsgListEntry.MESSAGE_ID+" TEXT,"+
                        ChatContract.MsgListEntry.FRIEND_ORIONID+" TEXT,"+
                        ChatContract.MsgListEntry.FRIEND_NICKNAME+" TEXT"+")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtils.d(TAG, "onUpgrade:: " + oldVersion + " "+newVersion);
        if (oldVersion < newVersion){
            onCreate(db);
        }
    }

    /**
     * 插入单条数据
     */
    public void insertData(Context context, ContentValues contentValues){
        LogUtils.e(TAG, " insertData:: 成功 contentValues = " + contentValues.toString() );
        SQLiteDatabase database = getWritableDatabase();
        try {
            database.insert(table_name, null, contentValues);
        }catch (Exception e){
            e.printStackTrace();
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
    public List<MsgListBean> queryAll() {
        List<MsgListBean> personList = new ArrayList<>();
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name,null);
        if(cursor.moveToFirst()){
            do{
                MsgListBean person=new MsgListBean(
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.SEND_TIME)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.MsgListEntry.CHAT_TYPE)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.TEXT_CONTENT)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FILE_PATH)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FILE_NAME)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.MsgListEntry.FILE_SIZE)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.MsgListEntry.FILE_PROGRESS)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FROM)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.TO)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.MsgListEntry.IS_ACKED)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FRIEND_ORIONID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FRIEND_NICKNAME)));// TODO: 2022/3/22 增加字段，用于显示正常的newsfragment
                personList.add(person);
            }
            while(cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return personList;
    }

    public List<MsgListBean> queryLast(Context context){//首页显示信息用
        List<MsgListBean> personList = new ArrayList<>();

        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor=database.rawQuery("SELECT * FROM "+ table_name,null);

        MailListSQLiteHelper mailListSQLiteHelper = MailListSQLiteHelper.getInstance(context);
        List<ContactsBean> contactsBeans = mailListSQLiteHelper.queryAll();// TODO: 2022/3/23 获取全部的好友信息
        LogUtils.d(TAG, " queryLast:: 全部好友个人信息 转化前contactsBeans = " + contactsBeans.toString());

        if(cursor.moveToLast()){// TODO: 2022/3/22 目前就只得到最后一条
            do{
                MsgListBean person=new MsgListBean(
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.SEND_TIME)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.MsgListEntry.CHAT_TYPE)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.TEXT_CONTENT)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FILE_PATH)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FILE_NAME)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.MsgListEntry.FILE_SIZE)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.MsgListEntry.FILE_PROGRESS)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FROM)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.TO)),
                        cursor.getInt(cursor.getColumnIndex(ChatContract.MsgListEntry.IS_ACKED)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FRIEND_ORIONID)),
                        cursor.getString(cursor.getColumnIndex(ChatContract.MsgListEntry.FRIEND_NICKNAME)));// TODO: 2022/3/22 增加字段，用于显示正常的newsfragment


                int contactsBeansSize = contactsBeans.size();

                if(contactsBeansSize>0) {

                    for (int i = 0; i < contactsBeansSize ; i++) {

                        LogUtils.d(TAG, " queryLast:: 转化前orionId = " + contactsBeans.get(i).getOrionId());
                        String transOrionId = AesTools.getDecryptContent(contactsBeans.get(i).getOrionId(), AesTools.AesKeyTypeEnum.COMMON_KEY);
                        if (transOrionId == null || transOrionId.isEmpty()){
                            transOrionId = contactsBeans.get(i).getOrionId();
                        }
                        LogUtils.d(TAG, " queryLast:: 转化后orionId = " + transOrionId);

                        LogUtils.d(TAG, " queryLast:: 转化前friendOrionId = " + person.getFriendOrionid());
                        String transformFriendOrionId = AesTools.getDecryptContent(person.getFriendOrionid(), AesTools.AesKeyTypeEnum.COMMON_KEY);
                        if (transformFriendOrionId == null || transformFriendOrionId.isEmpty()){
                            transformFriendOrionId = person.getFriendOrionid();
                        }
                        LogUtils.d(TAG, " queryLast:: 转化后friendOrionId = " + transformFriendOrionId);

                        if (transOrionId.equals(transformFriendOrionId)) {
                            personList.add(person);
                            LogUtils.d(TAG, " queryLast:: 转化后person = " + person.toString());
                            contactsBeans.remove(i);//找到了与这个好友最新发的一条信息后，下一次的遍历不在用它了
                            break;
                        }
                    }
                }else
                    break;
            }
            while(cursor.moveToPrevious());// TODO: 2022/3/23 从后往前遍历
        }

        cursor.close();
        database.close();
        return personList;
    }

    /**
     * 根据内容更新ack
     * @param content
     * @param ack
     */
    public void updateIsAck(String content, int ack){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(ChatContract.MsgListEntry.IS_ACKED, ack);
        database.update(this.table_name,value,ChatContract.MsgListEntry.TEXT_CONTENT+ "=?", new String[]{content});
    }


    /**
     * 根据messageID标签更新ack
     * @param messageID
     * @param ack
     */
    public void updateIsAck2(String messageID, int ack){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(ChatContract.MsgListEntry.IS_ACKED, ack);
        database.update(this.table_name,value,ChatContract.MsgListEntry.MESSAGE_ID+ "=?", new String[]{messageID});
    }


    /**
     * 根据文件名字更新文件进度
     * @param fileName
     * @param fileProgress
     */
    public void updateFileProgress(String fileName, int fileProgress){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(ChatContract.MsgListEntry.FILE_PROGRESS, fileProgress);
        database.update(this.table_name,value,ChatContract.MsgListEntry.FILE_NAME+ "=?", new String[]{fileName});
    }


    /**
     * 根据文件唯一标签messageID更新文件进度
     * @param messageID
     * @param fileProgress
     */
    public void updateFileProgress2(String messageID, int fileProgress){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(ChatContract.MsgListEntry.FILE_PROGRESS, fileProgress);
        database.update(this.table_name,value,ChatContract.MsgListEntry.MESSAGE_ID+ "=?", new String[]{messageID});
    }



    /**
     * 根据两个人的userId获取两个人的聊天记录
     * @param fromUserId
     * @param fromUserId
     * @return
     */
    @SuppressLint("LongLogTag")
    public List<MsgListBean> queryFriendChatRecord(String fromUserId, String orionId){// TODO: 2022/3/23 全部改成 Orionid来对比
        List<MsgListBean> allChatRecordList = queryAll();
        List<MsgListBean> friendChatRecordList = new ArrayList<>();

//        for (int i=0; i<allChatRecordList.size(); i++){
//            MsgListBean bean = allChatRecordList.get(i);
//            Log.d(TAG, " queryFriendChatRecord:: MsgListBean = " + bean.toString());
//            Log.d(TAG, " queryFriendChatRecord:: msg = " + bean.getTextContent()+"\t"+ bean.getFileName());
//            Log.d(TAG, " queryFriendChatRecord:: fromUserId = " + fromUserId);
//            Log.d(TAG, " queryFriendChatRecord:: orionId = " + orionId);
//            //fromId加密的
//            Log.d(TAG, " queryFriendChatRecord:: fromId = " + bean.getFrom()+"\t"+ " toId = " + bean.getTo());
//            Log.d(TAG, " queryFriendChatRecord:: 转换前friendOrionId = " + bean.getFriendOrionid());
//
//            String fromId = AesTools.getDecryptContent(bean.getFrom(), AesTools.AesKeyTypeEnum.COMMON_KEY);
//            if (fromId == null || fromId.isEmpty()){
//                fromId = bean.getFrom();
//            }
//            Log.d(TAG, " queryFriendChatRecord:: 转换后fromId = " + fromId);
//
//            String friendOrionId = AesTools.getDecryptContent(bean.getFriendOrionid(), AesTools.AesKeyTypeEnum.COMMON_KEY);
//            if (friendOrionId == null || friendOrionId.isEmpty()){
//                friendOrionId = bean.getFriendOrionid();
//            }
//            Log.d(TAG, " queryFriendChatRecord:: 转换后friendOrionId = " + friendOrionId);
//
//
//
//
//            if ((fromUserId.equals(fromId) && orionId.equals(friendOrionId) ||
//                    (orionId.equals(friendOrionId) && fromUserId.equals(bean.getTo())))){// TODO: 2022/3/23 全部改成 Orionid来对比
//                Log.d(TAG, " queryFriendChatRecord:: add " );
//                friendChatRecordList.add(bean);
//            }
//        }
        return allChatRecordList;
    }



    /**
     * 清除表数据
     */
    public void cleanUpData(){
        SQLiteDatabase database=getWritableDatabase();
        String cleanUpDataSQL = "delete from " + table_name;
        try {
            database.execSQL(cleanUpDataSQL);
        }catch (Exception e){
            e.printStackTrace();
//            ToastUtils.showMessage(context, "清除表的数据异常"+ e.toString());
            LogUtils.e(TAG, "insert多条异常：" + e.toString());
        }finally {
            if (null != database){
                database.close();
            }
        }
    }



}
