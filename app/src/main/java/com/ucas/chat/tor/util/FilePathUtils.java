package com.ucas.chat.tor.util;

public class FilePathUtils {

    public final static String USERINFO_NAME = "userinfo.txt";
    public final static String KEY_NAME = "key.txt";
    public final static String SERVER_NAME = "server.txt";
    public final static String CONTACT_NAME = "contact.txt";
    public final static String CLIENT_PRIVATE_KEY = "client_private_key";
    public final static String CLIENT_PUBLIC_KEY = "client_public_key";
    public final static String HOSTNAME = "hostname";
    public final static String hs_ed25519_secret_key = "hs_ed25519_secret_key";

    public final static String SDCARD_CHAT = "/sdcard/Chat/";
    public final static String USER_INFO_FILE = "/sdcard/Chat/User";

    public final static String v3Dirpath =  "/data/data/com.ucas.chat/files/hidden_service_replace";
    public final static String BASE_PATH_DATA = "/data/data/com.ucas.chat";

    //Jni加密解密 key.bin文件路径
    public static final String SECRET_KEY_FILE = "/data/data/com.ucas.chat/files/key.bin";
    public final static String BIN_FILE_PATH = BASE_PATH_DATA + "/files/binFile";
    //jni加密文件接收路径
    public final static String RECIEVE_FILE_PATH = "/data/data/com.ucas.chat/files/ReceiveFile/";
}
