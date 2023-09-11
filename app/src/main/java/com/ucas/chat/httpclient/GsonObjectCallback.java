package com.ucas.chat.httpclient;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class GsonObjectCallback <T> implements Callback {

    private static final String TAG = "GsonObjectCallback";
    private Handler handler = OkHttp3Utils.getInstance().getHandler();

    //主线程处理
    public abstract void onSuccess(T t);
    //主线程处理
    public abstract void onFailed(Call call, IOException e);
    //请求失败
    @Override
    public void onFailure(final Call call, final IOException e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFailed(call, e);
            }
        });
    }

    //请求json 并直接返回泛型的对象 主线程处理
    @SuppressLint("LongLogTag")
    @Override
    public void onResponse(final Call call, Response response) throws IOException {
        String json = response.body().string();
        if (json.contains("<HTML>")){
            Log.e(TAG, "onResponse: json = "+json);
        }else {
            Log.d(TAG, "onResponse: json = "+json);
        }
        Class<T> cls = null;
        Class clz = this.getClass();
        ParameterizedType type = (ParameterizedType) clz.getGenericSuperclass();
        Type[] types = type.getActualTypeArguments();
        cls = (Class<T>) types[0];
        Gson gson = new Gson();
        try {
            final T t = gson.fromJson(json, cls);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(t);
                }
            });
        }catch (Exception e){
            Log.e(TAG, "onResponse: catch error = "+e);
        }

    }

}
