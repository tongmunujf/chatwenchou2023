package com.ucas.chat.tor.util;

import java.util.Arrays;
import java.util.List;

public class Constant {

    public static byte[] APPLICATION_ID = {0x0f, 0x0f};
    //hostname.txt读取 ——》明文
    public static String MY_ONION_HOSTNAME = "xlv76wpa7xzzpe73k2vr27x53m27s23t62ukhrizsdvbkeilxpnd6eyd.onion";
    //public String MY_ONION_HOSTNAME = "wb7ohwznvqvucnzp7evryfymcoir77jjiqv35c42stndfr26gtes7dyd.onion";


    public static String TOR_SOCKS_PROXY_SERVER = "127.0.0.1";

    public static int TOR_SOCKS_PROXY_PORT = 9050;
    //public int TOR_SOCKS_PROXY_PORT = 6677;

    List<String> PEER_ONION_HOSTNAME_LIST = Arrays.asList("clbhu52pxrervpyahwqonij6xnkilk4yvnewsijxashqahbl6gsb7tqd.onion");
    List<Integer> PEER_ONION_PORT_LIST = Arrays.asList(6677, 6677);
    //contact.txt读取 ——》明文
    public static final String REMOTE_ONION_NAME="clbhu52pxrervpyahwqonij6xnkilk4yvnewsijxashqahbl6gsb7tqd.onion";
    public static final int REMOTE_ONION_PORT=6677; 
    
    //    Must Have
    public static int BYTE_APPLICATION_ID = 2;
    public static int BYTE_UTC_TIMESTAMP = 4;
    public static int BYTE_MESSAGE_NO = 4;

    //6
    public static int BYTE_MESSAGE_TIMESTAMP_BEGIN = BYTE_APPLICATION_ID + BYTE_UTC_TIMESTAMP;// TODO: 2021/8/24 时间戳结束的位置

    //10
    public static int BYTE_MESSAGE_TYPE_BEGIN = BYTE_APPLICATION_ID + BYTE_UTC_TIMESTAMP + BYTE_MESSAGE_NO;//报头前10字节，看报头格式
    public static int BYTE_MESSAGE_TYPE = 1;
    //12
    public static int BYTE_PAYLOAD_LENGTH_BEGIN = BYTE_MESSAGE_TYPE_BEGIN + BYTE_MESSAGE_TYPE;
    public static int BYTE_PAYLOAD_LENGTH = 2;
    //14 = x + 2
    public static int BYTE_internalPayload_BEGIN = BYTE_PAYLOAD_LENGTH_BEGIN + BYTE_PAYLOAD_LENGTH;
    public static int SHAREDKEY_LENGTH = 32;

    public static final int BYTE_EXTERNAL_HASH = 20;
    public static final int BYTE_ONION_HASH = 32;

    public static final int BYTE_STARTXORFILENAME_LENGTH = 2;//开始xor文件名的byte大小
    public static final int BYTE_STARTXORINDEX_LENGTH = 4;//使用该xor文件的位置，定义为起始点距离文件尾的byte数组大小

    public static final int BYTE_ENDXORFILENAME_LENGTH = 2;//结束xor文件名的byte大小
    public static final int BYTE_ENDXORINDEX_LENGTH = 4;//使用该xor文件的位置，定义为结束点距离文件尾的byte数组大小

    //protocol type
    public static int PROTOCOL_TYPE_HANDSHAKE = 1;
    public static int PROTOCOL_TYPE_DATA = 2;

    //message type
    public static int SESSION_REQUEST = 1;
    public static int SESSION_AUTH = 2;
    public static int SESSION_EXCHANGE = 3;
    public static int SESSION_DONE = 4;

    public final static int DATA_MESSAGE = 5;
    public final static int DATA_ACK = 12;

    public final static int DATA_FILE_META = 6;
    public final static int DATA_FILE_READY = 7;
    public final static int DATA_FILE_DATA = 8;
    public final static int DATA_FILE_DONE = 9;

    public static int CLOSE_REQUEST = 10;
    public static int CLOSE_ACK = 11;
    
    public static int EXTERNAL_HANDSHAKE_LENGTH=1790;
    public static int CELL_LENGETH = 2048;
    public static int EXTERNAL_DATA_LEGTH = CELL_LENGETH - 1;

    

    //    ACK
    public static int BYTE_TTYPE = 1;
    public static int BYTE_RECEIVE_NUMBER = 2;
    public static int BYTE_RECEIVE_PIECE_NUMBER = 1;
    public static int BYTE_OK = 2;
    public static int TTYPE_MESSAGE = 0;
    public static int TTYPE_FILE = 1;
    public static int TTYPE_PHOTO = 2;
    public static int TTYPE_VIDAO = 3;

    public static final String PASSWORD="c";

    public static int BEFORE_ENCRYPT = 1392;
    public static int AFTER_ENCRYPT = 1880;
    public static int ENCRYPT_AES_KEY = 172;
    public static int SIGNATURE_LENGTH = 172;
    public static int FILL_TO_1220 = 1220;
    public static int FILL_TO_1520 = 1520;

    public static int CELL_LENGETH_2052 = 2052;
    public static int CELL_LENGETH_2048 = 2048;
    
    public static String DATA_ACK_CONTENT ="ok";
    public static String DATA_CK_CONTENT ="ck";


    // CLIENT_PRIVATE_KEY读取——》明文
    public static  String CLIENT_PRIVATE_KEY = "";
   //CLIENT_PUBLIC_KEY.txt读取 ——》明文
    public static  String CLIENT_PUBLIC_KEY = "";

    // recieve file 
    public static final String RECIEVE_FILE_PATH="/sdcard/Android/data/com.ucas.chat/files/";
    public static final int FILE_PIECE_SIZE=1968;// TODO: 2021/10/26 1990改为1968，让出12字节给指针存放

    
    public static final int SOCKET_PURPOSE_TEXT=0;
    public static final int SOCKET_PURPOSE_PIC=1;
    public static final int SOCKET_PURPOSE_FILE=2;
    public static final int SOCKET_PURPOSE_VIDEO=3;
    
    public static final int CONNECT_RETRY_COUNT=3;
    public static final int CONNECT_TIME_OUT=10000;

    public static final String PACKAGE = "com.ucas.chat";
    public static final String TOR_BROAD_CAST_PATH = "com.ucas.chat.broadcast.TorBroadCastReceiver";
    public static final String TOR_BROAD_CAST_ACTION = "TorReceiver";
    public static final String TOR_BROAD_CAST_INTENT_KEY = "Status";

    public static final String SEND_PROTOCOL_FAILURE = "send_protocol_failure";
    public static final String START_COMMUNICATION_SUCCESS = "start_communication_success";
    public static final String PEER_HAS_RECEIVED_MESSAGE = "peer_has_received_message";
    public static final String HAS_RECEIVED_MESSAGE = "has_received_message";

    
}


