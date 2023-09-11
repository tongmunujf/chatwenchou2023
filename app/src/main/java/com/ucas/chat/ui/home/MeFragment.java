package com.ucas.chat.ui.home;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ucas.chat.R;
import com.ucas.chat.TorManager;
import com.ucas.chat.base.BaseFragment;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.db.ServiceInfoHelper;
import com.ucas.chat.ui.ChangePasswordActivity;
import com.ucas.chat.ui.home.InterfaceOffline.sendMyStatus;
import com.ucas.chat.ui.home.set.LanguageActivity;
import com.ucas.chat.ui.home.set.SettingActivity;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;

import org.apaches.commons.codec.digest.DigestUtils;

import static com.ucas.chat.MyApplication.getContext;

/**
 * 我
 */
public class MeFragment extends BaseFragment {

    private TextView mName;
    private ImageView mImHead;
    private LinearLayout mSetting;
    private LinearLayout mPass;
    private LinearLayout mQuit;
    private LinearLayout mLang;
    private SQLiteDatabase mDatabase;
    private UserBean mUserBean;
    private ServiceInfoHelper mServiceHelper;
    private static boolean exited = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, null);
        UserBean bean= SharedPreferencesUtil.getUserBeanSharedPreferences(getContext());
        Toast toast=Toast.makeText(getContext(), bean.getUserName(), Toast.LENGTH_SHORT);
        toast.show();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImHead = view.findViewById(R.id.im_head);
        mName = view.findViewById(R.id.nameTextView);
        UserBean bean= SharedPreferencesUtil.getUserBeanSharedPreferences(getContext());
        mName.setText(bean.getUserName());
        mImHead.setImageResource(ConstantValue.imHeadIcon[bean.getImPhoto()]);
        mSetting = view.findViewById(R.id.setting);

        mSetting.setClickable(true);
        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //这个可以返回自己的activity
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });
        mPass = view.findViewById(R.id.password_change);
        mPass.setClickable(true);
        mPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
        mQuit = view.findViewById(R.id.quit);
        mQuit.setClickable(true);
        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mUserBean = SharedPreferencesUtil.getUserBeanSharedPreferences(getActivity());
//                String from = DigestUtils.sha256Hex(mUserBean.getOnionName().replace(".onion","").replace(" ","")); //M
                UserBean bean= SharedPreferencesUtil.getUserBeanSharedPreferences(getActivity());
                String from = DigestUtils.sha256Hex(bean.getOnionName().trim()); //M
                mServiceHelper = new ServiceInfoHelper(getActivity());
                mDatabase = mServiceHelper.getReadableDatabase();
                sendMyStatus sendMyStatus = new sendMyStatus(from,"0",mServiceHelper.getSecond());
                sendMyStatus.start();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TorManager.stopTor(getContext());
                System.exit(0);

            }
        });
        mLang = view.findViewById(R.id.Language_change);
        mLang.setClickable(true);
        mLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LanguageActivity.class);
                startActivity(intent);
            }
        });
    }

}
