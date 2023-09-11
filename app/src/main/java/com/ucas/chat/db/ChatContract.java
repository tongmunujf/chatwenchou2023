package com.ucas.chat.db;

import android.provider.BaseColumns;

public class ChatContract {

//    public final static String TABLE_NAME_MAIL_LIST = "table_name_mail_list_";
//    public final static String TABLE_UNIT = ".db";
//    public final static String TABLE_NAME_CHAR = "table_name_char";

    public static class MailListEntry implements BaseColumns{
        public static final String USER_ID = "userId";
        public static final String USER_NAME="userName";
        public static final String PASS_WORD="passWord";
        public static final String NICK_NAME="nickName";
        public static final String ORION_ID = "orionId";
        public static final String ORION_HASH_ID = "orionHashId";
        public static final String IMAGE_ID = "imageId";
    }

    public static class MsgListEntry implements BaseColumns{
        public static final String SEND_TIME = "sendTime";
        public static final String CHAT_TYPE = "chat_type";
        public static final String TEXT_CONTENT = "textContent";
        public static final String FILE_PATH = "filePath";
        public static final String FILE_NAME = "fileName";
        public static final String FILE_SIZE = "fileSize";
        public static final String FILE_PROGRESS = "file_progress";
        public static final String FROM = "fromID";
        public static final String TO = "toID";
        public static final String IS_ACKED = "is_acked";
        public static final String MESSAGE_ID = "message_id";//唯一标记这条消息

        public static final String FRIEND_ORIONID = "friend_orionId";// TODO: 2022/3/22 增加数据库的信息标记，便于修改昵称和news页面的 显示
        public static final String FRIEND_NICKNAME = "friend_nickname";//唯一标记这条消息

    }

    public static class ServerInfoEntry implements BaseColumns{
        public static final String NODE_SERVER_ID = "Node_Server_ID";
        public static final String COMMUNICATION_SERVER_ID = "Communication_Server_ID";

    }

    public static class GuardNodeEntry implements BaseColumns{
        public static final String GUARD_NODE = "Guard_Node";
        public static final String GUARD_COMMUNICATION = "Guard_Communication";
        public static final String VALID_TIME = "Valid_Time";
    }

    public static class MYSELFINFO implements BaseColumns{
        public static final String ACCOUNT = "account";
        public static final String PASSWORD = "password";
        public static final String NICK_NAME = "nick_name";
        public static final String GENDER = "gender";
        public static final String HEADIMAGEPATH = "head_portrait";
        public static final String ONIONNAME = "identification";
        public static final String RSA_PRIVATE_KEY = "private_key";
        public static final String RSA_PUBLIC_KEY = "public_key";
    }

    public static class KEYFINFO implements BaseColumns{
        public static final String KEY_NAME = "key_name";
        public static final String KEY_VALUE = "key_value";
    }


    public static class ADDRESSBOOK implements BaseColumns{
        public static final String NICK_NAME = "nick_name";
        public static final String GENDER = "gender";
        public static final String HEADIMAGEPATH = "head_portrait";
        public static final String REMOTEONIONNAME = "identification";
        public static final String REMARKS = "remarks";
        public static final String REMOTE_RSA_PUBLIC_KEY = "public_key";
    }

    public static class NODE implements BaseColumns{
        public static final String GUARDNODE = "guard_node";
        public static final String VALIDTIME = "valid_time";
    }


}
