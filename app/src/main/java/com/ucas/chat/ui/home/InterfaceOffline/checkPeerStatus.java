package com.ucas.chat.ui.home.InterfaceOffline;

import android.util.Log;

import com.google.gson.Gson;
import com.ucas.chat.eventbus.Event;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;

public class checkPeerStatus extends Thread{
    private String fromId,toId;
    public checkPeerStatus(String fromId,String toId){
        this.fromId = fromId;
        this.toId = toId;
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
        System.out.println("request url" + url);
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
        return buffer.toString();
    }

    /**
     * 检测对方是否在线
     */
    public static String check_peer_online(String fromId, String toId) {
        String result = null;
        try {
            String body = "from=" + URLEncoder.encode(fromId, "UTF-8") +
                    "&to=" + URLEncoder.encode(toId, "UTF-8");
            String url1 = "http://utye3rrlplncmnkogiecviv3c32q56pgy5wlrk2mimf6njqiv5pjuwyd.onion/probing_client/";
            URL url = new URL(url1);
            result = send_post(url, body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public void run() {
        System.out.println("当前线程(check_peer_online)：" + Thread.currentThread().getName());
        String result = check_peer_online(fromId, toId);
        JSONObject gs=null;
        try {
            gs= new JSONObject(result);
        } catch (JSONException e) {
//            e.printStackTrace();
            System.out.println("CheckPeerStatus "+e.getMessage());
            gs=null;
        }
        if(gs!=null){
            int onlineStatus=0;
            String updateTime="";
            try {
                onlineStatus= gs.getInt("online");
                updateTime = gs.getString("time_updated");
            } catch (JSONException e) {
//                e.printStackTrace();
                System.out.println("CheckPeerStatus "+e.getMessage());
                onlineStatus=0;
                updateTime="";
            }
            Event.PeerOnlineStatusMessage mess = new Event.PeerOnlineStatusMessage(this.toId,onlineStatus+"",updateTime);
            Gson gson = new Gson();
            String messJson = gson.toJson(mess);
            Log.d("CheckPeerStatus" , " messJson = " + messJson );
            EventBus.getDefault().post(new Event(Event.CHECK_PEER_ONLINE_STATUS, messJson, ""));
        }

    }

    public static void main(String[] args) {
        String fromId = "25b7d8a11bf3fe39452593b121c0435e8a4ca0f246f6c3601fd7ff9340031c9a";
        String toId = "19720eaf21365c54e86714548a825e10cf975dd408c25cc23cf1eb1eaeeea082";
        checkPeerStatus checkPeerStatus = new checkPeerStatus(fromId,toId);
        checkPeerStatus.start();


    }
}
