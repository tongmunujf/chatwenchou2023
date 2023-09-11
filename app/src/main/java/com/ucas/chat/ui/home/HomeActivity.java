package com.ucas.chat.ui.home;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ucas.chat.R;
import com.ucas.chat.base.BaseActivity;
import com.ucas.chat.base.BaseFragment;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.db.ServiceInfoHelper;
import com.ucas.chat.ui.home.InterfaceOffline.getNode;
import com.ucas.chat.ui.home.InterfaceOffline.sendMyStatus;
import com.ucas.chat.ui.home.adapter.HomeFragmentPagerAdapter;
import com.ucas.chat.ui.login.LoginActivity;
import com.ucas.chat.ui.view.ViewPagerFixed;
import com.ucas.chat.utils.LogUtil;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;

import org.apaches.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends BaseActivity implements ViewPager.OnPageChangeListener{

    public static final String TAG = ConstantValue.TAG_CHAT + "HomeActivity";
    private BottomNavigationView mBottomNavigationView;
    private ViewPagerFixed mContentViewPager;
    private SQLiteDatabase mDatabase;

    private ServiceInfoHelper mServiceHelper;
    private List<BaseFragment> mFragmentList = new ArrayList<>(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_home);

        UserBean bean= SharedPreferencesUtil.getUserBeanSharedPreferences(HomeActivity.this);
        Toast toast=Toast.makeText(getApplicationContext(), bean.getUserName(), Toast.LENGTH_SHORT);
        toast.show();
        int time = SharedPreferencesUtil.getIntSharedPreferences(HomeActivity.this,"time","key");
        LogUtil.d(TAG + "得到的time"+time);
        //#########

//        只执行一次，否则会陷入死循环
        String hostname = bean.getOnionName();
//        String hostnameWithoutOnion = hostname.replace(" ","").replace(".onion","");
        String from =DigestUtils.sha256Hex(hostname.trim());
        System.out.println(TAG + "我的onion地址:" + hostname.trim());
        System.out.println(TAG + "sha256:" + from);

        mServiceHelper = new ServiceInfoHelper(this);
//        mDatabase = mServiceHelper.getReadableDatabase();

        if(LoginActivity.num_restart_tor==1){
            getNode getNode = new getNode(from,mServiceHelper.getFirst());
            getNode.start();
            LoginActivity.num_restart_tor--;
        }

        LogUtils.d(TAG + "get:firstppp:",mServiceHelper.getSecond());
        sendMyStatus sendMyStatus = new sendMyStatus(from, "1",mServiceHelper.getSecond());
        sendMyStatus.start();

        initView();
    }

    private void initView() {

        mBottomNavigationView= findViewById(R.id.bottomNavigationView);
        mContentViewPager = findViewById(R.id.contentViewPager);
        //设置ViewPager的最大缓存页面
        mContentViewPager.setOffscreenPageLimit(2);

        NewsFragment newsFragment = new NewsFragment();
        ContactListFragment contactListFragment = new ContactListFragment();
        MeFragment meFragment = new MeFragment();
        mFragmentList.add(newsFragment);
        mFragmentList.add(contactListFragment);
        mFragmentList.add(meFragment);
        mContentViewPager.setAdapter(new HomeFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList));
        mContentViewPager.setOnPageChangeListener(this);

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.contact:
                        mContentViewPager.setCurrentItem(0);
                        setTitle(getString(R.string.news));
                    break;

                    case R.id.discovery:
                        mContentViewPager.setCurrentItem(1);
                        setTitle(getString(R.string.mail_list));
                        break;
                    case R.id.me:
                        mContentViewPager.setCurrentItem(2);
                        setTitle(getString(R.string.me));
                    break;
                default:
                    break;
            }
                return true;
            }
        });

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        switch (position) {
            case 0:
                mBottomNavigationView.setSelectedItemId(R.id.contact);
                break;
            case 1:
                mBottomNavigationView.setSelectedItemId(R.id.discovery);
                break;
            case 2:
                mBottomNavigationView.setSelectedItemId(R.id.me);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
