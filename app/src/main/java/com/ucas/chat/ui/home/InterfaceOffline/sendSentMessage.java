package com.ucas.chat.ui.home.InterfaceOffline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class sendSentMessage extends Thread{
    private String from, to, messageID,message_content,onion_name;
    public sendSentMessage(String from,String to,String messageID,String message_content,String onion_name){
        this.from = from;
        this.to = to;
        this.messageID = messageID;
        this.message_content = message_content;
        this.onion_name = onion_name;
    }
    /**
     * HttpURLConnection post请求通用函数
     */
    public static String send_post(URL url, String body) {
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
//            System.out.println("header:" + httpUrlConn.getHeaderFields());
            //3.获取数据
            // 将返回的输入流转换成字符串
            if (httpUrlConn.getResponseCode() == 200) {
                InputStream inputStream = httpUrlConn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                bufferedReader = new BufferedReader(inputStreamReader);
                while ((result = bufferedReader.readLine()) != null) {
                    buffer.append(result);
                }
                inputStream.close();
                httpUrlConn.disconnect();
            } else {
                System.out.println("发生错误：" + httpUrlConn.getResponseMessage());
                System.out.println("错误码：" + httpUrlConn.getResponseCode());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpUrlConn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
//                    buffer.append(line).append("\n");
                    System.out.println(line);
                }
                reader.close();
                buffer.append("错误");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

    /**
     * 发送离线文本消息
     */
    public static String send_offline_text_message(String from_id, String to_id,  String messageID,String message_content,String onion_name) {
        String result = null;
        try {
            String body = "from_id=" + URLEncoder.encode(from_id, "UTF-8") +
                    "&target_id=" + URLEncoder.encode(to_id, "UTF-8") +
                    "&message_id=" + URLEncoder.encode(messageID, "UTF-8")+
                    "&message_content=" + URLEncoder.encode(message_content, "UTF-8");
            String url1 = "http://"+onion_name+"/reporting_sent_message/";
            URL url = new URL(url1);
            result = send_post(url, body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String get_UTCTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    @Override
    public void run() {
        System.out.println("当前线程(send_sent_message)：" + Thread.currentThread().getName());
        System.out.println("send_sent_message:" + send_offline_text_message(from, to, messageID,message_content,onion_name));
    }

    public static void main(String[] args) {
        String from_id = "25b7d8a11bf3fe39452593b121c0435e8a4ca0f246f6c3601fd7ff9340031c9a";
        String to_id = "19720eaf21365c54e86714548a825e10cf975dd408c25cc23cf1eb1eaeeea082";
        String messageID = "231741";
        String message_content = "test.txt";
        String onion_name = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        sendSentMessage sendSentMessage = new sendSentMessage(from_id, to_id, messageID,message_content,onion_name);
        sendSentMessage.start();

    }
}
