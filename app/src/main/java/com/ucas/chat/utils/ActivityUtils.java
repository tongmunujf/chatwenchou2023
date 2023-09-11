package com.ucas.chat.utils;

import android.app.Activity;


import java.util.ArrayList;
import java.util.List;

public class ActivityUtils {

    private final static String TAG = "ActivityUtils";

    private List<Activity> mActivityList = new ArrayList<>();
    private static ActivityUtils mInstance;

    public static synchronized ActivityUtils getInstance() {
        if (null == mInstance) {
            mInstance = new ActivityUtils();
        }
        return mInstance;
    }

    public void addActivity(Activity activity) {
        if (mActivityList == null){
            mActivityList = new ArrayList<>();
        }
        mActivityList.add(activity);
        LogUtils.d(TAG, "addActivity: ActivityList size is : " + mActivityList.size());
    }

    public void removeActivity(Activity activity) {
        if (mActivityList != null){
            mActivityList.remove(activity);
        }
        LogUtils.d(TAG, "removeActivity: ActivityList size is : " + mActivityList.size());
    }

    public void exitSystem() {
        for (Activity activity : mActivityList) {
            if (activity != null)
                activity.finish();
        }
        System.exit(0);
    }

}
