package com.ucas.chat.ui.home.InterfaceOffline;

import android.util.Log;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.eventbus.Event;

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

/**
 * @auther :haoyunlai
 * date         :2021/8/6 13:35
 * e-mail       :2931945387@qq.com
 * usefulness   :Chat
 */
public class sendOfflinePic2 extends Thread  {
    static String TAG = ConstantValue.TAG_CHAT + "sendOfflinePic2";
    private String from,to,type,filePath,onion_name;
    private byte[] picByte;//图片的

    private String messageID;// TODO: 2021/8/8 用来唯一标记这一次的发送，以免错乱

    public sendOfflinePic2(String from,String to,String type,byte[] picByte,String onion_name,String messageID){
        this.from = from;
        this.to = to;
        this.type = type;
//        this.filePath = filePath;
        this.picByte = picByte;
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
                                    byte[] picByte, String contentType,Long fileLength,String messageID) {
        Log.d(TAG, " formUpload:: urlStr = " + urlStr);
        Log.d(TAG, " formUpload:: contentType = " + contentType);
        Log.d(TAG, " formUpload:: fileLength = " + fileLength);
        long start = System.currentTimeMillis()/1000;
        String filename = "filename.jpg";
        String res = "";
        HttpURLConnection conn = null;
        // boundary就是request头和上传文件内容的分隔符
        String BOUNDARY = "---------------------------123821742118716";
        try {
            URL url = new URL(urlStr);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
            conn = (HttpURLConnection) url.openConnection(proxy);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            // conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
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
                Log.d(TAG, " formUpload:: 发图片配置信息 = " + strBuf.toString());
                out.write(strBuf.toString().getBytes());
            }
            if (picByte != null) {
//                Iterator iter = fileMap.entrySet().iterator();
//                while (iter.hasNext()) {
//                    Map.Entry entry = (Map.Entry) iter.next();
//                    String inputName = (String) entry.getKey();
//                    String inputValue = (String) entry.getValue();
//                    if (inputValue == null) {
//                        continue;
//                    }
//                    File file = new File(inputValue);
//                    filename = file.getName();
                    //没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
//                    contentType = new MimetypesFileTypeMap().getContentType(file);
                    //contentType非空采用filename匹配默认的图片类型
//                    if (!"".equals(contentType)) {
//                        if (filename.endsWith(".png")) {
//                            contentType = "image/png";
//                        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".jpe")) {
//                            contentType = "image/jpeg";
//                        } else if (filename.endsWith(".gif")) {
//                            contentType = "image/gif";
//                        } else if (filename.endsWith(".ico")) {
//                            contentType = "image/image/x-icon";
//                        }
//                    }
                    if (contentType == null || "".equals(contentType)) {
                        contentType = "application/octet-stream";
                    }
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + "message" + "\"; filename=\"" + "filename.jpg" + "\"\r\n");
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
                    out.write(strBuf.toString().getBytes());
                    Log.d(TAG, " formUpload:: 发图片信息 = " + strBuf.toString());
//                    DataInputStream in = new DataInputStream(new FileInputStream(file));
//                    int bytes = 0;
//                    byte[] bufferOut = new byte[2048];
//                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(picByte, 0, picByte.length);//发文件
//                    }
//                    in.close();
//                }
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            Log.d(TAG, " formUpload:: 图片发送错误码ResponseCode = " + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                Log.d(TAG, " formUpload:: 发图片成功");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    strBuf.append(line).append("\n");
                }
                res = strBuf.toString();
                reader.close();
            }else{
                Log.d(TAG, " formUpload::图片发送发生错误 = " + conn.getResponseMessage());
//                StringBuffer strBuf = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                res = strBuf.append("错误").toString();

                reader.close();

            }
        } catch (Exception e) {
            Log.d(TAG, " formUpload::图片发送发送POST请求出错 = " +urlStr);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        long end = System.currentTimeMillis()/1000;
        long speed = fileLength/(end-start);
        if(speed>1024){
            speed = speed/1024;
            Log.d(TAG, " formUpload:: 文件大小fileLength = " + fileLength);
            Log.d(TAG, " formUpload:: 文件发送开始时间 = " + start);
            Log.d(TAG, " formUpload:: 文件发送结束时间 = " + end);
            Log.d(TAG, " formUpload:: 文件传输速率 = " + speed);
            EventBus.getDefault().post(new Event(Event.SEND_OFFLINE_PIC, res, messageID+","+speed+" KB/s"));
        }else{
            Log.d(TAG, " formUpload:: 文件大小fileLength = " + fileLength);
            Log.d(TAG, " formUpload:: 文件发送开始时间 = " + start);
            Log.d(TAG, " formUpload:: 文件发送结束时间 = " + end);
            Log.d(TAG, " formUpload:: 文件传输速率 = " + speed);
            EventBus.getDefault().post(new Event(Event.SEND_OFFLINE_PIC, res, messageID+","+speed+" b/s"));
        }
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
    public static String sendPic(String from,String to,String type, byte[] picByte,String onion_name,String randomString) {
        String url = "http://"+onion_name+"/receive_message/";
        Log.d(TAG, " sendPic:: 请求地址url = " + url);
        String fileHash = null;
//        File file = new File(filePath);
        long fileLength =  picByte.length;
//        try {
//            DataInputStream in = new DataInputStream(new FileInputStream(file));
//            DataInputStream ins = new DataInputStream(new FileInputStream(file));
//            int num = 0;
//            int bytes;
//            byte[] bufferOut = new byte[3060];
//            //从in里读向bufferOut写
//            while ((bytes = in.read(bufferOut)) != -1) {
//                num+=bytes;
//            }
//            byte[] fileContent = new byte[num];
//            ins.read(fileContent);//一次性读完所有
//            System.out.println("文件哈希："+fileHash);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        fileHash = DigestUtils.sha256Hex(picByte);
        Map<String, String> textMap = new HashMap<String, String>();
        textMap.put("from_id", from);
        textMap.put("to_id", to);
        textMap.put("type", type);
        textMap.put("timestamp", get_UTCTime());
        textMap.put("hash", fileHash);
        //设置file的name，路径
//        Map<String, String> fileMap = new HashMap<String, String>();
//        fileMap.put("message", filePath);
        String contentType = "";
        String ret = formUpload(url, textMap, picByte, contentType,fileLength,randomString);
        return ret;
    }

    @Override
    public void run() {
        Log.d(TAG, " run:: 当前线程(send_offline_pic)："+Thread.currentThread().getName());
        System.out.println("图片send_offline_pic2:"+sendPic(from,to,type,picByte,onion_name,messageID));
    }

    public static void main(String[] args) {
        String from = "19720eaf21365c54e86714548a825e10cf975dd408c25cc23cf1eb1eaeeea082";
        String to = "25b7d8a11bf3fe39452593b121c0435e8a4ca0f246f6c3601fd7ff9340031c9a";
        String type = "file";
        String filePath = "C:/Users/PC/Desktop/新建文本文档.txt";
        String onion_name = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        String messageID = "";
        sendOfflinePic2 sendOfflinePic2 = new sendOfflinePic2(from,to,type,new byte[5],onion_name,messageID);
        sendOfflinePic2.start();


    }
}