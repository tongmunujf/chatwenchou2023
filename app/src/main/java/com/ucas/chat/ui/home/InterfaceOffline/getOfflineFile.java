package com.ucas.chat.ui.home.InterfaceOffline;

import com.ucas.chat.eventbus.Event;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;

public class getOfflineFile extends Thread{
    private String id,messageID,filePath;
    private String onion_name;
    private String name;
    public getOfflineFile(String id,String messageID,String filePath,String onion_name,String name){
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
        OutputStreamWriter out;
        String result = null;
        BufferedReader bufferedReader = null;
        StringBuffer buffer = new StringBuffer();
        Proxy proxy1 = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
        System.out.println("请求地址：" + url);
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
            out = new OutputStreamWriter(httpUrlConn.getOutputStream(), "UTF-8");
            // 把数据写入请求的Body，参数形式跟在地址栏的一样
            out.write(body);
            out.flush();
            out.close();
            //3.获取数据
            // 将返回的输入流转换成字符串
            if (httpUrlConn.getResponseCode() == 200) {
                InputStream inputStream = httpUrlConn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                bufferedReader = new BufferedReader(inputStreamReader);
                BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
                while ((result = bufferedReader.readLine()) != null) {
                    buffer.append(result);
                    bw.write(result);
                    bw.newLine();
                }
                inputStream.close();
                if(bufferedReader!=null){
                    bufferedReader.close();
                }
                if(bw!=null){
                    bw.close();
                }
            } else {
                System.out.println("发生错误：" + httpUrlConn.getResponseMessage());
                System.out.println("错误码：" + httpUrlConn.getResponseCode());
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
//        buffer.append(filePath).append("\n");
        EventBus.getDefault().post(new Event(Event.GET_OFFLINE_FILE, buffer.toString(), name));
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
        System.out.println("当前线程(get_offline_file)：" + Thread.currentThread().getName());
        System.out.println("get_offline_file:" + get_offline_file_message(id, messageID,filePath,onion_name,name));
    }

    public static void main(String[] args) {
        //接收离线文件
        String id = "19720eaf21365c54e86714548a825e10cf975dd408c25cc23cf1eb1eaeeea082";
        String messageID = "1262895";
        //要存储的路径加文件名
        String filePath = "E:\\ssh-key";
        String onion_name = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        String name = "name";
        getOfflineFile getOfflineFile = new getOfflineFile(id,messageID,filePath,onion_name,name);
        getOfflineFile.start();
    }
}
