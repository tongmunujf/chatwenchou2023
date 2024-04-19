package com.ucas.chat.ui.home.InterfaceOffline;

import android.util.Log;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.eventbus.Event;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.FileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.*;
import java.net.*;

public class getOfflinePic extends Thread{
    static String TAG = ConstantValue.TAG_CHAT + "getOfflinePic";
    private String id,messageID,filePath,onion_name,name;
    public getOfflinePic(String id, String messageID, String filePath,String onion_name,String name){
        this.id = id;
        this.messageID = messageID;
        this.filePath = filePath;
        this.onion_name = onion_name;
        this.name = name;
    }
    /**
     * HttpURLConnection post请求通用函数
     */
    public static String send_post(URL url, String body,String filePath,String name) {
        Log.d(TAG, " send_post:: url = " + url);
        Log.d(TAG, " send_post:: filePath = " + filePath);
        Log.d(TAG, " send_post:: name = " + name);
        OutputStreamWriter out;
        String result = null;
        BufferedReader bufferedReader = null;
        StringBuffer buffer = new StringBuffer();
        Proxy proxy1 = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
        Log.d(TAG, " send_post:: 请求地址url = " + url);
        try {
            // http协议传输
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection(proxy1);
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）,这里即使设置了get也没用
            httpUrlConn.setRequestMethod("POST");
            httpUrlConn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            //2.传入参数部分
            // 得到请求的输出流对象
            try {
                out = new OutputStreamWriter(httpUrlConn.getOutputStream(), "UTF-8");
                // 把数据写入请求的Body，参数形式跟在地址栏的一样
                out.write(body);
                out.flush();
                out.close();
            } catch (Exception e) {
                Log.d(TAG, " send_post:: httpUrlConn.getOutputStream() 错误 = " + e.toString());
            }
            //3.获取数据
            // 将返回的输入流转换成字符串
            Log.d(TAG, " formUpload:: ResponseCode = " + httpUrlConn.getResponseCode());
            if (httpUrlConn.getResponseCode() == 200) {
                InputStream inputStream = httpUrlConn.getInputStream();
                byte[] buf = new byte[4096];
                int readLen = 0;
                //OutputStream outputStream = new FileOutputStream(filePath);
                Log.d(TAG, " formUpload:: filePath = " + filePath);
                OutputStream outputStream = null;
                try {
                     outputStream = new FileOutputStream(filePath);
                }catch (FileNotFoundException e){
                    Log.d(TAG, " formUpload:: FileNotFoundException e = " + e.toString());
                }

                while ((readLen = inputStream.read(buf)) != -1) {
                    outputStream.write(buf,0,readLen);
                }
                Log.d(TAG, " formUpload:: outputStream = " + outputStream.toString());
                inputStream.close();
                outputStream.close();
                buffer.append("success");

                //TODO 看需求是否在此处解密
                FileUtils.cipherBytesToFile(filePath);
            } else {
                Log.d(TAG, " formUpload:: 发生错误 = " + httpUrlConn.getResponseMessage());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpUrlConn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new Event(Event.GET_OFFLINE_PIC, buffer.toString(), name));
        return buffer.toString();
    }
    /**
     * 接收离线文件
     */
    public static String get_offline_file_message(String client_id, String message_id,String filePath,String onion_name,String name) {
        String result = null;
        try {
            String body = "client_id=" + URLEncoder.encode(client_id, "UTF-8") +
                    "&message_id=" + URLEncoder.encode(message_id, "UTF-8");
            //1.连接部分
            String url1 = "http://"+onion_name+"/get_offline_file_message/";
            URL url = new URL(url1);
            result = send_post(url, body,filePath,name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void run() {
        System.out.println("当前线程(get_offline_pic)：" + Thread.currentThread().getName());
        System.out.println("get_offline_pic:" + get_offline_file_message(id, messageID,filePath,onion_name,name));
    }

    public static void main(String[] args) {
        //接收离线文件
        String id = "25b7d8a11bf3fe39452593b121c0435e8a4ca0f246f6c3601fd7ff9340031c9a";
        String messageID = "10313";
        String onion_name = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        //要存储的路径加文件名
        String filePath = "E:\\dui.jpeg";
        String name = "name";
        getOfflinePic getOfflineFile = new getOfflinePic(id,messageID,filePath,onion_name,name);
        getOfflineFile.start();
    }
}
