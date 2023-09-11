package com.ucas.chat.ui.home.adapter;

import android.os.Parcelable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.ucas.chat.base.BaseFragment;

import java.util.List;

public class HomeFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private List<BaseFragment> mFragments;

    public HomeFragmentPagerAdapter(FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments != null ? mFragments.size() : 0;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
