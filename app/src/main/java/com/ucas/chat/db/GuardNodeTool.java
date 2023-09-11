package com.ucas.chat.db;


import android.provider.MediaStore;

import com.ucas.chat.bean.GuardNodeBean;
import com.ucas.chat.bean.contact.ConstantValue;

import java.util.ArrayList;
import java.util.List;

public class GuardNodeTool {
    public static final String TAG = ConstantValue.TAG_CHAT + "GuardNodeTool";
    private static final GuardNodeTool mInstance = new GuardNodeTool();
    private GuardNodeTool(){

    }
    public static GuardNodeTool getInstance(){return mInstance;}
    public static final int Guard_Node_LENGTH = 1;
    private final static String[] GUARD_NODE_ONE = new String[]{"00410438E5E2D6367AB67F9851351329714F27A6"};
    private final static String[] GUARD_NODE_TWO = new String[]{"00410438E5E2D6367AB67F9851351329714F27A6"};
    private final static String[] VALID_TIME = new String[]{"2021/7/3  13:00:00"};

    public static List<GuardNodeBean> initGuardNode(){
        List<GuardNodeBean> list = new ArrayList<>();

       for (int i = 0; i < Guard_Node_LENGTH;i++){
           GuardNodeBean bean = new GuardNodeBean(GUARD_NODE_ONE[i],GUARD_NODE_TWO[i],VALID_TIME[i]);
           list.add(bean);
       }
        return list;
    }

}
