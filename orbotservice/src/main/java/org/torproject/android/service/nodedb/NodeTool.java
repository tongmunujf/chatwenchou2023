package org.torproject.android.service.nodedb;



import java.util.ArrayList;
import java.util.List;

public class NodeTool {
    private String guard_node="";
    private String valid_time="";


    private static final NodeTool mInstance = new NodeTool();
    private  NodeTool(){}
    public static NodeTool getInstance(){
        return mInstance;
    }

    public List<NodeBean> insertNodeBean(){
        List<NodeBean> list = new ArrayList<>();
        NodeBean bean = new NodeBean(guard_node,valid_time);
        list.add(bean);
        return list;
    }
}
