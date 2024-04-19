package com.ucas.chat.ui.home.InterfaceOffline;

import android.util.Log;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.eventbus.Event;

import org.greenrobot.eventbus.EventBus;

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

public class sendOfflineText extends Thread {
    private static String TAG = ConstantValue.TAG_CHAT + "sendOfflineText";

    private String from, to, message,onion_name;
    private String messageID;
    public sendOfflineText(String from,String to,String message,String onion_name,String messageID ){
        this.from = from;
        this.to = to;
        this.message = message;
        this.onion_name = onion_name;
        this.messageID = messageID;
        Log.d(TAG, " sendOfflineText:: onion_name = " + this.onion_name);
    }
    /**
     * HttpURLConnection post请求通用函数
     */
    public static String send_post(URL url, String body,String messageID) {
        OutputStreamWriter out;
        String result = null;
        BufferedReader bufferedReader = null;
        StringBuffer buffer = new StringBuffer();
        Proxy proxy1 = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
        Log.d(TAG, " send_post:: 请求地址 = " + url);
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
            out.write(body);//发出去,里面有messageID了
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
                System.out.println(TAG + " 发生错误：" + httpUrlConn.getResponseMessage());
                System.out.println(TAG + " 错误码：" + httpUrlConn.getResponseCode());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpUrlConn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
//                    buffer.append(line).append("\n");
                    System.out.println(TAG + " line: " + line);
                }
                buffer.append("错误");
                reader.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new Event(Event.SEND_OFFLINE_TEXT, buffer.toString(), messageID));// TODO: 2021/8/10 发messageID好知道是哪条 
        return buffer.toString();
    }

    /**
     * 发送离线文本消息
     */
    public static String send_offline_text_message(String from_id, String to_id,  String message,String onion_name,String messageID ) {
        Log.d(TAG, " send_offline_text_message:: from_id = " + from_id);
        Log.d(TAG, " send_offline_text_message:: to_id = " + to_id);
        Log.d(TAG, " send_offline_text_message:: message = " + message);
        String result = null;
        try {
            String body = "from_id=" + URLEncoder.encode(from_id, "UTF-8") +
                    "&to_id=" + URLEncoder.encode(to_id, "UTF-8") +
                    "&type=" + URLEncoder.encode("text", "UTF-8") +
                    "&timestamp=" + URLEncoder.encode(get_UTCTime(), "UTF-8") +
                    "&message=" + URLEncoder.encode(message, "UTF-8")
                    +"&messageID=" + URLEncoder.encode(messageID, "UTF-8")// TODO: 2021/8/10 新加的，不知道会不会影响到服务器的解析  
                    ;
            String url1 = "http://"+onion_name+"/receive_message/";
            URL url = new URL(url1);
            result = send_post(url, body ,messageID);
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
        System.out.println(TAG + " 当前线程(send_offline_text)：" + Thread.currentThread().getName());
        System.out.println(TAG + " send_offline_text:" + send_offline_text_message(from, to, message,onion_name ,messageID));
    }

    public static void main(String[] args) {
        String from_id = "19720eaf21365c54e86714548a825e10cf975dd408c25cc23cf1eb1eaeeea082";
        String to_id = "25b7d8a11bf3fe39452593b121c0435e8a4ca0f246f6c3601fd7ff9340031c9a";
        String message = "wyx from android";
        String onion_name = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        String messageID = "";
        sendOfflineText sendOfflineText = new sendOfflineText(from_id, to_id, message,onion_name,messageID);
        sendOfflineText.start();

    }

}

