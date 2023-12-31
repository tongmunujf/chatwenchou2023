package com.ucas.chat.ui.home.set;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ucas.chat.R;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;
import com.zlylib.fileselectorlib.FileSelector;
import com.zlylib.fileselectorlib.utils.Const;

import org.apaches.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ucas.chat.MyApplication.getContext;

public class SettingActivity extends BaseActivity {
    private LinearLayout mvoice;
    private LinearLayout mFilePath;
    private TextView tv;
    private LinearLayout mQuickFilePath;
    private ImageView  mImBack;
    public static final String TAG = ConstantValue.TAG_CHAT + "SettingActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_setting);
//        mvoice = findViewById(R.id.voice_layout);
//        mvoice.setClickable(true);
//        mvoice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(SettingActivity.this, Voice_Activity.class);
//                startActivity(intent);
//            }
//        });
        mImBack = findViewById(R.id.im_back);
        mImBack.setOnClickListener(this);
        mFilePath = findViewById(R.id.file_path);
        mFilePath.setClickable(true);
        tv = findViewById(R.id.quick_path);
        mFilePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                //intent.setType(“image/*”);//选择图片
//                //intent.setType(“audio/*”); //选择音频
//                //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
//                //intent.setType(“video/*;image/*”);//同时选择视频和图片
//                intent.setType("*/*");//无类型限制
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, 1);
                openDefaultFile();
            }
        });
//        mUserBean = SharedPreferencesUtil.getUserBeanSharedPreferences(getContext());
//        mSelfUserId = mUserBean.getUserId();
//        UserBean bean= SharedPreferencesUtil.getUserBeanSharedPreferences(SettingActivity.this);
//        Toast toast=Toast.makeText(getApplicationContext(), bean.getUserName(), Toast.LENGTH_SHORT);
//        toast.show();


//        String from = DigestUtils.sha256Hex(MailListUserNameTool.getOrionId(SettingActivity.this,bean.getUserName()).replace(".onion","")); //M


//        sendMyStatus sendMyStatus = new sendMyStatus(from, "1");
//        sendMyStatus.start();



        mQuickFilePath = findViewById(R.id.file_quick_path);
        mQuickFilePath.setClickable(true);
        mQuickFilePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOnlyFolder();
            }
        });


        Spinner spinner = (Spinner) findViewById(R.id.spinner);
// 建立数据源
        String[] mItems = {"1   ", "2   ", "3   ", "4   "};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // TODO
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO
            }
        });
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
// 建立数据源
        String[] mItems2 = {"1day   ", "2day   ", "3day   ", "4day   "};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mItems2);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // TODO
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO
            }
        });

        Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
// 建立数据源
        List<String> datas = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            datas.add( i +"");
        }

//        String[] mItems3 = {"1   ", "2   ", "3   "};
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, datas);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Toast.makeText(SettingActivity.this, "MaxTimes" + datas.get(pos), Toast.LENGTH_SHORT).show();

//                int time = SharedPreferencesUtil.setIntSharedPreferences(this,"time",);
                SharedPreferencesUtil.setIntSharedPreferences(getContext(),"time","key",Integer.parseInt(datas.get(pos)));
                LogUtils.d(TAG, " TryMaxLinkTimes = " + Integer.parseInt(datas.get(pos)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                SharedPreferencesUtil.setIntSharedPreferences(getContext(),"time","key",3);

            }
        });

    }
    String path;
    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
//            Uri uri = data.getData();
//            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
//                path = uri.getPath();
//                tv.setText(path);
//                Toast.makeText(this, path + "11111", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
//                path = getPath(this, uri);
//                tv.setText(path);
//                Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
//            } else {//4.4以下下系统调用方法
//                path = getRealPathFromURI(uri);
//                tv.setText(path);
//                Toast.makeText(SettingActivity.this, path + "222222", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public void openOnlyFolder( ) {
        FileSelector.from(this)
                .onlySelectFolder()  //只能选择文件夹
                .requestCode(1) //设置返回码
                .start();
    }
    public void openDefaultFile( ) {
        FileSelector.from(this)
                .onlySelectFolder()  //只能选择文件夹
                .requestCode(2) //设置返回码
                .start();
    }
    @SuppressLint("ResourceType")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(data!=null){
                ArrayList<String> essFileList = data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION);
                StringBuilder builder = new StringBuilder();
                for (String file :
                        essFileList) {
                    builder.append(file).append("\n");
                }

                //TextView tvName = (TextView) findViewById(R.id.quick_path);
                //String str = getText().toString();
//                String str;
//                str = tv.getText().toString();
//                tv.setText(str);
//                tv.setText(R.id.quick_path);
                //tv.setText();
                tv.append(builder.toString());

                //tv.setText();
            }
        }
        if (requestCode == 2) {
            if(data!=null){
                ArrayList<String> essFileList = data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION);
                StringBuilder builder = new StringBuilder();
                for (String file :
                        essFileList) {
                    builder.append(file).append("\n");
                }
                TextView dtv;
                dtv = findViewById(R.id.text_file_path);
                dtv.append(builder.toString());

            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.im_back:
                finish();
                break;
        }
    }
}
