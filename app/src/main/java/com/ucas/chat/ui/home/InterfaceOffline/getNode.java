package com.ucas.chat.ui.home.InterfaceOffline;

import com.ucas.chat.db.MyInforTool;
import com.ucas.chat.eventbus.Event;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

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

import static com.ucas.chat.ui.login.LoginActivity.GET_NODE_ERROR;
import static com.ucas.chat.ui.login.LoginActivity.TAG;

public class getNode extends Thread{
    private String from,onion_name;
    public getNode(String from, String onion_name){
        this.from = from;
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

            } else {
                System.out.println("接收node发生错误：" + httpUrlConn.getResponseMessage());
                System.out.println("错误码：" + httpUrlConn.getResponseCode());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpUrlConn.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
//                    buffer.append(line).append("\n");
                    System.out.println(line);
                }
                buffer.append("错误");
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new Event(Event.GET_NODE, buffer.toString(), null));
        return buffer.toString();
    }

    /**
     * 获取节点
     */
    public static String get_node(String client_id, String onion_name) {
        String result = null;
        String body = null;
        LogUtils.d(TAG, " get_node:: 解析前onion_name = " + onion_name);
        onion_name = AesTools.getDecryptContent(onion_name, AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " get_node:: 解析后onion_name = " + onion_name);
        try {
            body = "client_id=" + URLEncoder.encode(client_id, "UTF-8");
            String url1 = "http://"+onion_name+"/get_node/";
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
        if(GET_NODE_ERROR!=2){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("当前线程(get_node)：" + Thread.currentThread().getName());
        System.out.println("get_node:"+getNode.get_node(from,onion_name));
    }

    public static void main(String[] args) {
        String from = "25b7d8a11bf3fe39452593b121c0435e8a4ca0f246f6c3601fd7ff9340031c9a";
        String onion_name = "5rcugup6diqslfychhpc2onmzuqfhvxdcygh4etn4e4hdzzm52k2tuad.onion";
        getNode getNode = new getNode(from,onion_name);
        getNode.start();

    }

}
