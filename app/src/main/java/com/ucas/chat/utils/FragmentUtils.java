package com.ucas.chat.utils;

import androidx.fragment.app.Fragment;

import com.ucas.chat.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class FragmentUtils {
    private final static String TAG = "FragmentUtils";

    private List<BaseFragment> mFragmentList = new ArrayList<>();
    private static FragmentUtils mInstance;

    public static synchronized FragmentUtils getInstance() {
        if (null == mInstance) {
            mInstance = new FragmentUtils();
        }
        return mInstance;
    }

    public void addFragment(BaseFragment fragment) {
        if (mFragmentList == null){
            mFragmentList = new ArrayList<>();
        }
        mFragmentList.add(fragment);
        LogUtils.d(TAG, "addFragment: FragmentList is : " + mFragmentList.toString());
    }

    public void removeFragment(Fragment fragment) {
        if (mFragmentList != null){
            mFragmentList.remove(fragment);
        }
        LogUtils.d(TAG, "removeFragment: FragmentList is : " + mFragmentList.toString());
    }
    
    public BaseFragment getStackTopFragment(){
        if (mFragmentList != null){
            return mFragmentList.get(0);
        }else {
            LogUtils.d(TAG, "getStackTopFragment: FragmentList is null");
            return null; 
        }
    }

    public BaseFragment getStackBottomFragment(){
        if (mFragmentList != null){
            return mFragmentList.get(mFragmentList.size() - 1);
        }else {
            LogUtils.d(TAG, "getStackTopFragment: FragmentList is null");
            return null;
        }
    }
}
