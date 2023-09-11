package com.example.android_play;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;



import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SharedPreferencesUtil {


    public final static String DESTROY_DATA = "destroy_data";// TODO: 2021/10/26 保存销毁数据的标签
    public final static String BOOLER = "booler";//键



    public static void saveDestroyData(Context context){// TODO: 2021/10/26 保存销毁数据的标签

        SharedPreferences sharedPreferences = context.getSharedPreferences(DESTROY_DATA, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        editor.putBoolean(BOOLER, true);
        editor.commit();//保存

    }


    public static boolean getDestroyDataboolean(Context context){// TODO: 2021/10/26 获取销毁数据的标签

        boolean b = false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(DESTROY_DATA, Activity.MODE_PRIVATE);
        b = sharedPreferences.getBoolean(BOOLER,false);

        return b;//注意为空


    }





}
