package com.ucas.chat.ui.home.InterfaceOffline;

import android.util.Log;

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

public class getOfflineText extends Thread {
    static String TAG = "Chat_" + "getOfflineText";
    private String from;
    private String onion_name;//服务器的
    public getOfflineText(String from,String onion_name){
        this.from = from;
        this.onion_name = onion_name;

    }
    /**
     * HttpURLConnection post请求通用函数
     */
    public static String send_post(URL url, String body) {
        Log.d(TAG, " send_post:: 请求地址 url = " + url);
        OutputStreamWriter out;
        String result = null;
        BufferedReader bufferedReader = null;
        StringBuffer buffer = new StringBuffer();
        Proxy proxy1 = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050));
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
                out = new OutputStreamWriter(httpUrlConn.getOutputStream(), "UTF-8");// TODO: 2021/8/25 直接在这里出错！
                // 把数据写入请求的Body，参数形式跟在地址栏的一样
                out.write(body);
                out.flush();
                out.close();
            }catch (IOException e){
                Log.d(TAG, " send_post:: httpUrlConn.getOutputStream错误 " + e.toString());
            }

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
                Log.d(TAG, " send_post:: 发生错误 ResponseMessage = " +  httpUrlConn.getResponseMessage());
                Log.d(TAG, " send_post:: 错误码 ResponseCode = " +  httpUrlConn.getResponseCode());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpUrlConn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                buffer.append("{\"messages\":\"错误\"}");// TODO: 2021/8/25 转为json格式
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO: 2021/7/10 加入广播
        EventBus.getDefault().post(new Event(Event.GET_OFFLINE_TEXT, buffer.toString(), null));
        return buffer.toString();
    }

    /**
     * 接收离线文字消息
     */
    public static String get_offline_text_message(String client_id,String onion_name) {
        String result = null;
        try {
            String body = "client_id=" + URLEncoder.encode(client_id, "UTF-8");
            //1.连接部分
            String url1 = "http://"+onion_name+"/get_offline_text_message/";
            URL url = new URL(url1);
            result = send_post(url, body);
            Log.d(TAG, " get_offline_text_message:: result = " + result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void run() {
        System.out.println("当前线程(get_offline_text)：" + Thread.currentThread().getName());
        System.out.println("get_offline_text:" + get_offline_text_message(from,onion_name));
    }

    public static void main(String[] args) {
        String from = "19720eaf21365c54e86714548a825e10cf975dd408c25cc23cf1eb1eaeeea082";
        //接收离线文字消息
        String onion_name = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        getOfflineText getOfflineText = new getOfflineText(from,onion_name);
        getOfflineText.start();
    }
}
