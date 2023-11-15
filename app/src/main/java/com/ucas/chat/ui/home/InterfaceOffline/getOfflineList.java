package com.ucas.chat.ui.home.InterfaceOffline;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.ucas.chat.db.ServiceInfoHelper;
import com.ucas.chat.eventbus.Event;
import com.ucas.chat.utils.LogUtils;

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

public class getOfflineList extends Thread{
    static String TAG = "Chat_" + "getOfflineList";
    private SQLiteDatabase mDatabase;
    private Context mContext;
    private ServiceInfoHelper mServiceHelper;
    private String from;
    private String onion_name;
    public getOfflineList(String from,String onion_name){
        this.from = from;
        this.onion_name = onion_name;

    }
    /**
     * HttpURLConnection post请求通用函数
     */
    public static String send_post(URL url, String body) {
        Log.d(TAG, " send_post:: 请求地址url = " + url);
        Log.d(TAG, " send_post:: body = " + body);
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
            out = new OutputStreamWriter(httpUrlConn.getOutputStream(), "UTF-8");
            // 把数据写入请求的Body，参数形式跟在地址栏的一样
            out.write(body);
            out.flush();
            out.close();
//            System.out.println("header:" + httpUrlConn.getHeaderFields());
            Log.d(TAG, " send_post:: ResponseCode = " + httpUrlConn.getResponseCode());
            //3.获取数据
            // 将返回的输入流转换成字符串
            if (httpUrlConn.getResponseCode() == 200) {
                Log.d(TAG, " send_post:: getOfflineList发生正确 ResponseCode = " + httpUrlConn.getResponseCode());
                InputStream inputStream = httpUrlConn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                bufferedReader = new BufferedReader(inputStreamReader);
                while ((result = bufferedReader.readLine()) != null) {
                    buffer.append(result);
                }
                inputStream.close();

            } else {
                Log.d(TAG, " send_post:: getOfflineList发生错误 ResponseCode = " + httpUrlConn.getResponseCode());
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
        Log.d(TAG, " send_post:: EventBus GET_OFFLINE_LIST ");
        EventBus.getDefault().post(new Event(Event.GET_OFFLINE_LIST, buffer.toString(), null));

        return buffer.toString();
    }
    /**
     * 获取离线消息列表包括文字和文件
     */
    public String get_offline_message_list(String client_id,String onion_name) {
        String result = null;
        try {
            String body = "client_id=" + URLEncoder.encode(client_id, "UTF-8");
            //1.连接部分
//            mServiceHelper = new ServiceInfoHelper(mContext);
//            mDatabase = mServiceHelper.getReadableDatabase();
//            LogUtils.d("get:first111:",mServiceHelper.getSecond());
//            System.out.println("请求地址：" + mServiceHelper.getSecond());
            String url1 = "http://"+onion_name+"/get_offline_message_list/";
            URL url = new URL(url1);
            result = send_post(url, body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public void run() {
        System.out.println("当前线程(get_offline_message_list)：" + Thread.currentThread().getName());
        System.out.println("get_offline_message_list:" + get_offline_message_list(from,onion_name));
    }

    public static void main(String[] args) {
        String from = "19720eaf21365c54e86714548a825e10cf975dd408c25cc23cf1eb1eaeeea082";
        String onion_name = "utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion";
        getOfflineList getOfflineList =new getOfflineList(from,onion_name);
        getOfflineList.start();
    }
}
