package com.ucas.chat.ui.home.set;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ucas.chat.R;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.ui.login.LoginActivity;
import com.ucas.chat.utils.LanguageUtil;
import com.ucas.chat.utils.SharedPreferencesUtil;
import com.ucas.chat.utils.SpUserUtils;
import com.ucas.chat.utils.ToastUtils;

import java.util.Locale;

public class LanguageActivity extends BaseActivity {

    private ImageView mImBack;
    private ListView mListView; //首页的ListView
    private int selectPosition = 0;//用于记录用户选择的变量
    private String[] languageCodeArr = {"zh", "zh_rHK", "en"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_language);
        initView();
        initDatas();
        String language = SpUserUtils.getString(this, "language");
        switch (language){
            case "zh":
                selectPosition = 0;
                break;
            case "zh_rHK":
                selectPosition = 1;
                break;

            default:
                selectPosition = 2;
                break;
        }
    }

    private void initView(){
        mListView = findViewById(R.id.lv);
        mImBack = findViewById(R.id.im_back);
        mImBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initDatas(){

        final MyAdapter myAdapter = new MyAdapter(this);
        mListView.setAdapter(myAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取选中的参数
                selectPosition = position;
                myAdapter.notifyDataSetChanged();
                showSaveLanguage(languageCodeArr[position]);

            }
        });
    }

    /**
     * 保存设置的语言
     */
    private void showSaveLanguage(String language){
        //设置的语言
        LanguageUtil.changeAppLanguage(this, language, LoginActivity.class);
        //保存设置的语言
        SpUserUtils.putString(this, "language", language);
    }

    public class MyAdapter extends BaseAdapter {
        Context context;
        LayoutInflater mInflater;
        private String[] languageArr ;
        public MyAdapter(Context context){
            this.context = context;
            languageArr = new String[]{context.getString(R.string.ch), context.getString(R.string.ch_hk), context.getString(R.string.en)};
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return languageArr.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.adapter_language,parent,false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView)convertView.findViewById(R.id.id_name);
                viewHolder.select = (RadioButton)convertView.findViewById(R.id.id_select);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.name.setText(languageArr[position]);
            if(selectPosition == position){
                viewHolder.select.setChecked(true);
            }
            else{
                viewHolder.select.setChecked(false);
            }
            return convertView;
        }
    }
    public class ViewHolder{
        TextView name;
        RadioButton select;
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
