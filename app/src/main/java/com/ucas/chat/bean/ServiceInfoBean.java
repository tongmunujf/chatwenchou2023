package com.ucas.chat.bean;


import java.io.Serializable;

public class ServiceInfoBean implements Serializable {
    public static final String SERVER_INFO_NAME = "SERVER_INFO.db";
    public final static int VERSION = 1;
    private String node_server_id;
    private String communication_server_id;

    public ServiceInfoBean(String node_server_id, String communication_server_id){
        this.communication_server_id = communication_server_id;
        this.node_server_id = node_server_id;
    }

    public String getNode_server_id(){
        return node_server_id;
    }
    public void setNode_server_id(String node_server_id){
        this.node_server_id = node_server_id;
    }
    public String getCommunication_server_id(){
        return communication_server_id;
    }
    public void setCommunication_server_id(String communication_server_id){
        this.communication_server_id = communication_server_id;
    }

    @Override
    public String toString() {
        return "ServiceInfoBean{" +
                "node_server_id='" + node_server_id + '\'' +
                ", communication_server_id='" + communication_server_id + '\'' +
                '}';
    }
}
