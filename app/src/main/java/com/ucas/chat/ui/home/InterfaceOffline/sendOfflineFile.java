package com.ucas.chat.ui.home.InterfaceOffline;

import android.util.Log;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.eventbus.Event;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.utils.FileUtils;

import org.apaches.commons.codec.digest.DigestUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class sendOfflineFile extends Thread {
    static String TAG = ConstantValue.TAG_CHAT + "sendOfflineFile";
    private String from,to,type,filePath,onion_name;
    private String messageID;
    public sendOfflineFile(String from,String to,String type,String filePath,String onion_name,String messageID){
        Log.d(TAG, " sendOfflineFile:: filePath = " + filePath);
        this.from = from;
        this.to = to;
        this.type = type;
        this.filePath = filePath;
        this.onion_name = onion_name;
        this.messageID = messageID;
    }

    public static String get_UTCTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 上传图片、文件
     */
    public static String formUpload(String urlStr, Map<String, String> textMap,
                                    Map<String, String> fileMap, String contentType,Long fileLength,String messageID) {
        Log.d(TAG, " formUpload:: urlStr = " + urlStr);
        Log.d(TAG, " formUpload:: contentType = " + contentType);
        Log.d(TAG, " formUpload:: fileLength = " + fileLength);
        long start = System.currentTimeMillis()/1000;
        String filename = "";
        String res = "";
        HttpURLConnection conn = null;
        // boundary就是request头和上传文件内容的分隔符
        String BOUNDARY = "---------------------------123821742118716";
        try {
            URL url = new URL(urlStr);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
            conn = (HttpURLConnection) url.openConnection(proxy);
            conn.setConnectTimeout(2*60000);
            conn.setReadTimeout(2*60000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            // conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = null;
            try {
                out = new DataOutputStream(conn.getOutputStream());
            }catch (IOException e){
                Log.d(TAG, " formUpload:: conn.getOutputStream 错误" + e.toString());
            }
            // text
            if (textMap != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator iter = textMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                    strBuf.append(inputValue);
                }
                out.write(strBuf.toString().getBytes());
            }
            if (fileMap != null) {
                Iterator iter = fileMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    //inputValue：文件路径
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    filename = file.getName();
                    Log.d(TAG, " formUpload:: 文件路径inputValue = " + inputValue);
                    Log.d(TAG, " formUpload:: filename = " + filename);
                    //没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
//                    contentType = new MimetypesFileTypeMap().getContentType(file);
                    //contentType非空采用filename匹配默认的图片类型
                    if (!"".equals(contentType)) {
                        if (filename.endsWith(".png")) {
                            contentType = "image/png";
                        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".jpe")) {
                            contentType = "image/jpeg";
                        } else if (filename.endsWith(".gif")) {
                            contentType = "image/gif";
                        } else if (filename.endsWith(".ico")) {
                            contentType = "image/image/x-icon";
                        }
                    }
                    if (contentType == null || "".equals(contentType)) {
                        contentType = "application/octet-stream";
                    }
                    Log.d(TAG, " formUpload:: contentType = " + contentType);
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
                    out.write(strBuf.toString().getBytes());
                    DataInputStream in = null;
                    try {
                        in = new DataInputStream(new FileInputStream(file));
                    } catch (FileNotFoundException e) {
                        Log.e(TAG,"  formUpload:: new FileInputStream(file)错误 = " + e.toString());
                    }
                    int bytes = 0;
                    byte[] bufferOut = new byte[2048];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);//一段一段的发文件
                    }
                    in.close();
                }
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            Log.d(TAG, " formUpload:: ResponseCode = " + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    strBuf.append(line).append("\n");
                }
                res = strBuf.toString();
                Log.d(TAG, " formUpload:: 发文件正常 = " + res);
                reader.close();
            }else{
                Log.d(TAG, " formUpload:: 发生错误 = " + conn.getResponseMessage());
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                res = strBuf.append("错误").toString();

                reader.close();

            }
        } catch (Exception e) {
            Log.e(TAG, " formUpload:: 发送POST请求出错 = " + urlStr);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            String tempDecryptPath = FilePathUtils.TEMP_FILE + filename;
            Log.d(TAG, " formUpload:: tempDecryptPath = " + tempDecryptPath);
            File tempDecryptFile = new File(tempDecryptPath);
            if (tempDecryptFile.exists()){
                tempDecryptFile.delete();
            }
        }
        long end = System.currentTimeMillis()/1000;
        Log.d(TAG, " formUpload:: end = " + end);
        Log.d(TAG, " formUpload:: start = " + start);
        try {
            long speed = fileLength/(end-start);

            if(speed>1024){
                speed = speed/1024;
                Log.d(TAG, " formUpload:: 文件大小fileLength = " + fileLength);
                Log.d(TAG, " formUpload:: 文件发送开始时间 = " + start);
                Log.d(TAG, " formUpload:: 文件发送结束时间 = " + end);
                Log.d(TAG, " formUpload:: 文件传输速率 = " + speed);
                EventBus.getDefault().post(new Event(Event.SEND_OFFLINE_FILE, res, messageID+","+speed+" KB/s"));// TODO: 2021/8/10 替换 filename为messageID
            }else{
                Log.d(TAG, " formUpload:: 文件大小fileLength = " + fileLength);
                Log.d(TAG, " formUpload:: 文件发送开始时间 = " + start);
                Log.d(TAG, " formUpload:: 文件发送结束时间 = " + end);
                Log.d(TAG, " formUpload:: 文件传输速率 = " + speed);
                EventBus.getDefault().post(new Event(Event.SEND_OFFLINE_FILE, res, messageID+","+speed+" b/s"));
            }
        }catch (ArithmeticException e){
            Log.e(TAG,  "formUpload:: 运算错误 e = " + e.toString());
        }

        //TODO 删除临时生成的密文件

        return res;
    }

    /**
     * 把两个byte数组合并
     */
    public static byte[] bytes_merger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }
    /**
     * 封装接口
     */
    public static String sendPic(String from,String to,String type, String filePath,String onion_name,String messageID) {
        Log.d(TAG, " sendPic:: from = " + from);
        Log.d(TAG, " sendPic:: to = " + to);
        Log.d(TAG, " sendPic:: filePath = " + filePath);
        Log.d(TAG, " sendPic:: onion_name = " + onion_name);
        Log.d(TAG, " sendPic:: messageID = " + messageID);
        String url = "http://"+onion_name+"/receive_message/";
        Log.d(TAG, " sendPic:: 请求地址url = " + url);
        String fileHash = null;

        File file = new File(filePath);
        long fileLength =  file.length();
        String name = file.getName();
        String tempDecryptFilePath = "";
        Log.d(TAG, " sendPic:: fileName = " + name);
        if (name.endsWith(".mp4") || name.endsWith(".mp3") || name.endsWith(".png") || name.endsWith(".jpg") ||
                name.endsWith(".peg") || name.endsWith(".jpj") || name.endsWith(".ico") || name.endsWith(".gif") ){
            //把filePath的内容加密写入到本地备份
            tempDecryptFilePath = FileUtils.writeBytesCiphertextToFile(filePath);
            Log.d(TAG, "sendPic:: 发送流文件");
        }else {
             tempDecryptFilePath = FileUtils.writeCiphertextToFile(filePath);
            Log.d(TAG, "sendPic:: 发送文本文件");
        }
        Log.d(TAG, " sendPic:: tempDecryptFilePath = " + tempDecryptFilePath);
        File decryptFile = new File(tempDecryptFilePath);
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(decryptFile));
            DataInputStream ins = new DataInputStream(new FileInputStream(decryptFile));
            int num = 0;
            int bytes;
            byte[] bufferOut = new byte[3060];
            //从in里读向bufferOut写
            while ((bytes = in.read(bufferOut)) != -1) {
               num+=bytes;
            }
            byte[] fileContent = new byte[num];
            ins.read(fileContent);//一次性读完所有
            fileHash = DigestUtils.sha256Hex(fileContent);
            Log.d(TAG, " sendPic:: 密文fileHash = " + fileHash);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> textMap = new HashMap<String, String>();
        textMap.put("from_id", from);
        textMap.put("to_id", to);
        textMap.put("type", type);
        textMap.put("timestamp", get_UTCTime());
        textMap.put("hash", fileHash);
        //设置file的name，路径
        Map<String, String> fileMap = new HashMap<String, String>();
        fileMap.put("message", tempDecryptFilePath);
        String contentType = "";
        String ret = formUpload(url, textMap, fileMap, contentType,fileLength,messageID);
        return ret;
    }

    @Override
    public void run() {
        Log.d(TAG, " run:: 当前线程(send_offline_file)："+Thread.currentThread().getName());
        System.out.println("send_offline_file:"+sendPic(from,to,type,filePath,onion_name,messageID));
    }

    public static void main(String[] args) {
        String from = "19720eaf21365c54e86714548a825e10cf975dd408c25cc23cf1eb1eaeeea082";
        String to = "25b7d8a11bf3fe39452593b121c0435e8a4ca0f246f6c3601fd7ff9340031c9a";
        String type = "file";
        String filePath = "C:/Users/PC/Desktop/新建文本文档.txt";
        String onion_name = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        String messageID ="";
        sendOfflineFile sendOfflineFile = new sendOfflineFile(from,to,type,filePath,onion_name,messageID);
        sendOfflineFile.start();


    }
}