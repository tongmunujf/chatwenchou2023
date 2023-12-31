package com.ucas.chat.ui.home.InterfaceOffline;

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
 * @auther :haoyun
 * date         :2021/8/5 15:21
 * e-mail       :2931945387@qq.com
 * usefulness   :Chat
 * 暂时没用
 */
public class sendOfflinePic extends Thread {

    private String from,to,type,onion_name;
    private byte[] picByte;//图片的
    private String messageID;

    public sendOfflinePic(String from,String to,String type,byte[] picByte,String onion_name,String messageID){
        this.from = from;
        this.to = to;
        this.type = type;
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
                                    byte[] picByte, String contentType,int picLength,String messageID) {
        long start = System.currentTimeMillis()/1000;
        String picName = "";
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
            System.out.println("sendOfflinePic连上服务器");

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
            if (picLength != 0) {
//                Iterator iter = fileMap.entrySet().iterator();

                contentType = "image/jpeg";
                StringBuffer strBuf = new StringBuffer();
                strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                strBuf.append("Content-Disposition: form-data; name=\"" + "" + "\"; picname=\"" + "" + "\"\r\n");
                strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
                out.write(strBuf.toString().getBytes());
//                DataInputStream in = new DataInputStream(new FileInputStream(file));
//                int bytes = 0;
//                byte[] bufferOut = new byte[2048];
//                while ((bytes = in.read(bufferOut)) != -1) {
//                    out.write(bufferOut, 0, bytes);//发文件
//                }
                out.write(picByte,0,picLength);//发送图片的
//                in.close();

//                while (iter.hasNext()) {
//                    Map.Entry entry = (Map.Entry) iter.next();
//                    String inputName = (String) entry.getKey();
//                    String inputValue = (String) entry.getValue();//文件路径
//                    if (inputValue == null) {
//                        continue;
//                    }
//                    File file = new File(inputValue);
//                    filename = file.getName();
//                    //没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
////                    contentType = new MimetypesFileTypeMap().getContentType(file);
//                    //contentType非空采用filename匹配默认的图片类型
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
//                    if (contentType == null || "".equals(contentType)) {
//                        contentType = "application/octet-stream";
//                    }
//                    StringBuffer strBuf = new StringBuffer();
//                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
//                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");
//                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
//                    out.write(strBuf.toString().getBytes());
//                    DataInputStream in = new DataInputStream(new FileInputStream(file));
//                    int bytes = 0;
//                    byte[] bufferOut = new byte[2048];
//                    while ((bytes = in.read(bufferOut)) != -1) {
//                        out.write(bufferOut, 0, bytes);//发文件
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
            if (conn.getResponseCode() == 200) {
                System.out.println("发图片正常");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    strBuf.append(line).append("\n");
                }
                res = strBuf.toString();
                reader.close();
            }else{
                System.out.println("图片发送发生错误：" + conn.getResponseMessage());
                System.out.println("图片发送错误码：" + conn.getResponseCode());
//                StringBuffer strBuf = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                res = strBuf.append("发送图片错误").toString();

                reader.close();

            }
        } catch (Exception e) {
            System.out.println("发送图片的POST请求出错。" + urlStr);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        long end = System.currentTimeMillis()/1000;
        long speed = picLength/(end-start);
        if(speed>1024){
            speed = speed/1024;
            System.out.println("图片大小："+picLength);
            System.out.println("图片发送开始时间："+start);
            System.out.println("图片发送结束时间："+end);
            System.out.println("图片传输速率："+speed);
            EventBus.getDefault().post(new Event(Event.SEND_OFFLINE_PIC, res, messageID+","+speed+" KB/s"));
        }else{
            System.out.println("图片大小："+picLength);
            System.out.println("图片发送开始时间："+start);
            System.out.println("图片发送结束时间："+end);
            System.out.println("图片传输速率："+speed);
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
    public static String sendPic(String from,String to,String type, byte[] picByte,String onion_name,String messageID) {
        String url = "http://"+onion_name+"/receive_message/";
        System.out.println("请求地址："+url);
//        String fileHash = null;
//        File file = new File(filePath);
//        long fileLength =  file.length();
        int picLength = picByte.length;

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
//            fileHash = DigestUtils.sha256Hex(fileContent);
////            System.out.println("文件哈希："+fileHash);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Map<String, String> textMap = new HashMap<String, String>();
        textMap.put("from_id", from);
        textMap.put("to_id", to);
        textMap.put("type", type);
        textMap.put("timestamp", get_UTCTime());
//        textMap.put("hash", fileHash);
        //设置pic的
//        Map<String, byte[]> picMap = new HashMap<String, byte[]>();
//        picMap.put("message", picByte);
        String contentType = "";
        String ret = formUpload(url, textMap, picByte, contentType,picLength,messageID);
        return ret;
    }

    @Override
    public void run() {
        System.out.println("当前线程(send_offline_pic)："+Thread.currentThread().getName());
        System.out.println("图片send_offline_pic:"+sendPic(from,to,type,picByte,onion_name,messageID));
    }

    public static void main(String[] args) {
        String from = "19720eaf21365c54e86714548a825e10cf975dd408c25cc23cf1eb1eaeeea082";
        String to = "25b7d8a11bf3fe39452593b121c0435e8a4ca0f246f6c3601fd7ff9340031c9a";
        String type = "file";
        String filePath = "C:/Users/PC/Desktop/新建文本文档.txt";
        String onion_name = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        String messageID ="";
        sendOfflinePic sendOfflinePic = new sendOfflinePic(from,to,type,new byte[5],onion_name,messageID);
        sendOfflinePic.start();


    }


}