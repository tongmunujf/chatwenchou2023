package com.example.android_play;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PatternMatcher;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private TextView MyTextView;
    private Button delete_one,delete_all;
    private Button add,subtract,multiply,devide;
    private Button one,two,three,four,five,six,seven,eight,nine,zero;
    private Button point,equal;
    private String s="";
    private int equal_num=0;

    private TextView destroyTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setListener();
        initSdcardPernission(MainActivity.this);
    }


    private void initSdcardPernission(Context context){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                // writeFile();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                startActivityForResult(intent, 200);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // writeFile();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            }
        } else {
            // writeFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // writeFile();
            } else {
                Toast.makeText(MainActivity.this, "存储权限获取失败", Toast.LENGTH_SHORT).show();

            }
        }
    }


    private void setListener(){
        OnClick onClick=new OnClick();
        MyTextView=this.findViewById(R.id.tv1);
        MyTextView.setOnClickListener(onClick);
        delete_all=(Button)this.findViewById(R.id.Del_all);
        delete_all.setOnClickListener(onClick);
        delete_one=this.findViewById(R.id.Del_one);
        delete_one.setOnClickListener(onClick);

        add=this.findViewById(R.id.add);
        add.setOnClickListener(onClick);
        subtract=this.findViewById(R.id.subtract);
        subtract.setOnClickListener(onClick);
        multiply=this.findViewById(R.id.multiply);
        multiply.setOnClickListener(onClick);
        devide=this.findViewById(R.id.devide);
        devide.setOnClickListener(onClick);
        one=this.findViewById(R.id.one);
        one.setOnClickListener(onClick);
        two=this.findViewById(R.id.two);
        two.setOnClickListener(onClick);
        three=this.findViewById(R.id.three);
        three.setOnClickListener(onClick);
        four=this.findViewById(R.id.four);
        four.setOnClickListener(onClick);
        five=this.findViewById(R.id.five);
        five.setOnClickListener(onClick);
        six=this.findViewById(R.id.six);
        six.setOnClickListener(onClick);
        seven=this.findViewById(R.id.seven);
        seven.setOnClickListener(onClick);
        eight=this.findViewById(R.id.eight);
        eight.setOnClickListener(onClick);
        nine=this.findViewById(R.id.nine);
        nine.setOnClickListener(onClick);
        zero=this.findViewById(R.id.zero);
        zero.setOnClickListener(onClick);

        point=this.findViewById(R.id.point);
        point.setOnClickListener(onClick);
        equal=this.findViewById(R.id.equal);
        equal.setOnClickListener(onClick);

//        destroyTextView = findViewById(R.id.destroy);
//        destroyTextView.setOnLongClickListener(new LongClickListener());

    }


    class LongClickListener implements View.OnLongClickListener{

        @Override
        public boolean onLongClick(View v) {// TODO: 2021/10/27 长按销毁全部信息

//            destroyAndroiddata();//销毁/sdcard/Android/data下该私有空间的file,包括全部xor文件
//
//            destroydatadata();//销毁/data/data/下的所有文件
//
//
//            SharedPreferencesUtil.saveDestroyData(MainActivity.this);//保存销毁状态
//
//            Toast.makeText(MainActivity.this,R.string.destroyed_tips,Toast.LENGTH_SHORT).show();


            return true;
        }
    }

    private void destroydatadata() {//销毁/data/data/下的所有文件

        String databasesPath = "/data/data/com.ucas.chat/databases";//数据库内的
        DelFolder.delFolder(databasesPath);

        String filesPath = "/data/data/com.ucas.chat/files";//files
        DelFolder.delFolder(filesPath);


        String shared_prefsPath = "/data/data/com.ucas.chat/shared_prefs";//shared_prefsPath内的
        DelFolder.delFolder(shared_prefsPath);

        String tordataPath = "/data/data/com.ucas.chat/tordata";//tordata内的
        DelFolder.delFolder(tordataPath);


    }


    private void destroyAndroiddata() {//销毁全部files文件夹下的文件，包括xor文件
        String XOR_PATH = "/sdcard/Android/data/com.ucas.chat/files";//为多个xor文件的文件包路径

        DelFolder.delFolder(XOR_PATH);



    }


    public void destroyChat(View view) {// TODO: 2021/10/27 销毁全部信息
    }
 
    public void toChat(View view) {// TODO: 2021/10/27 跳转到

//        boolean b = SharedPreferencesUtil.getDestroyDataboolean(MainActivity.this);
//
//        if (!b){
//
//            String url = "scheme://main/mainDetail?";//这个就是刚刚前面在AndroidManManifest中设置的，问号后面的参数可带可不带，参考intent用法
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            startActivity(intent);
//
//
//        }



    }

    public class OnClick implements View.OnClickListener{

        public void display(String str){
            MyTextView.setText(str.trim());
        }
        public String compute(String str){
            StringBuilder stringBuilder=new StringBuilder(str);
            Pattern pattern = Pattern.compile("([\\d.]+)\\s*([*/])\\s*([\\d.]+)");
            Matcher matcher=pattern.matcher(stringBuilder.toString());
            while(matcher.find()){
                Double d1=Double.parseDouble(matcher.group(1));
                Double d2=Double.parseDouble(matcher.group(3));
                Double res=0.0;
                switch (matcher.group(2)){
                    case "*":
                        res=d1*d2;
                        break;
                    case "/":
                        res=d1/d2;
                        break;
                }
                stringBuilder.replace(matcher.start(), matcher.end(), String.valueOf(res));
                matcher.reset(stringBuilder.toString());
                Log.d("edittext",stringBuilder.toString());
            }

            pattern = Pattern.compile("([\\d.]+)\\s*([+-])\\s*([\\d.]+)");
            matcher=pattern.matcher(stringBuilder.toString());
            while(matcher.find()){
                Double d1=Double.parseDouble(matcher.group(1));
                Double d2=Double.parseDouble(matcher.group(3));
                Double res=0.0;
                switch (matcher.group(2)){
                    case "+":
                        res=d1+d2;
                        break;
                    case "-":
                        res=d1-d2;
                        break;
                }
                stringBuilder.replace(matcher.start(), matcher.end(), String.valueOf(res));
                matcher.reset(stringBuilder.toString());
                Log.d("edittext",stringBuilder.toString());
            }
            return stringBuilder.toString();
        }
        private void add_display(String str){
            if(equal_num==1){
                s="";
                equal_num=0;
                display(s);
            }
            s+=str;
            display(s);
        }



        @Override
        public void onClick(View v) {

            int id = v.getId();
            if (id == R.id.Del_all) {
                s = "";
                display(s);
            } else if (id == R.id.Del_one) {
                if (s.length() != 0)
                    s = s.substring(0, s.length() - 1);
                display(s);
            } else if (id == R.id.add) {
                add_display("+");
            } else if (id == R.id.subtract) {
                add_display("-");
            } else if (id == R.id.multiply) {
                add_display("*");
            } else if (id == R.id.devide) {
                add_display("/");
            } else if (id == R.id.one) {
                add_display("1");
            } else if (id == R.id.two) {
                add_display("2");
            } else if (id == R.id.three) {
                add_display("3");
            } else if (id == R.id.four) {
                add_display("4");
            } else if (id == R.id.five) {
                add_display("5");
            } else if (id == R.id.six) {
                add_display("6");
            } else if (id == R.id.seven) {
                add_display("7");
            } else if (id == R.id.eight) {
                add_display("8");
            } else if (id == R.id.nine) {
                add_display("9");
            } else if (id == R.id.zero) {
                add_display("0");
            } else if (id == R.id.point) {
                add_display(".");
            } else if (id == R.id.equal) {
                String temp = compute(s);
                Toast.makeText(MainActivity.this, "结果为:" + temp, Toast.LENGTH_SHORT).show();
                display(temp);
                equal_num = 1;



                if(temp.equals("++++")){
                    boolean b = SharedPreferencesUtil.getDestroyDataboolean(MainActivity.this);
                    Log.d("MainActivity", " onClick:: b = " + b + " RootUtils.isDeviceRooted() = " + RootUtils.isDeviceRooted());
                    if (!b){
                        String url = "scheme://main/mainDetail?";//这个就是刚刚前面在AndroidManManifest中设置的，问号后面的参数可带可不带，参考intent用法
//                        if(RootUtils.isDeviceRooted()==false) {
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                            startActivity(intent);
//                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }

                }else if(temp.equals("----")){

                    destroyAndroiddata();//销毁/sdcard/Android/data下该私有空间的file,包括全部xor文件

                    destroydatadata();//销毁/data/data/下的所有文件


                    SharedPreferencesUtil.saveDestroyData(MainActivity.this);//保存销毁状态

                    Toast.makeText(MainActivity.this,R.string.destroyed_tips,Toast.LENGTH_SHORT).show();

                }
            }
        }
    }



}
