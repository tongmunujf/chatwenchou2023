package com.ucas.chat.bean;

import java.io.Serializable;

public class AddressBookBean implements Serializable {

    private String nickName;
    private int gender;
    private String headImagePath;
    private String  remoteOnionName;
    private String remotePublicKey;
    private String remaks;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getHeadImagePath() {
        return headImagePath;
    }

    public void setHeadImagePath(String headImagePath) {
        this.headImagePath = headImagePath;
    }

    public String getRemoteOnionName() {
        return remoteOnionName;
    }

    public void setRemoteOnionName(String remoteOnionName) {
        this.remoteOnionName = remoteOnionName;
    }

    public String getRemotePublicKey() {
        return remotePublicKey;
    }

    public void setRemotePublicKey(String remotePublicKey) {
        this.remotePublicKey = remotePublicKey;
    }

    public String getRemaks() {
        return remaks;
    }

    public void setRemaks(String remaks) {
        this.remaks = remaks;
    }

    public AddressBookBean(String nickName, int gender, String headImagePath, String remoteOnionName, String remotePublicKey, String remaks) {
        this.nickName = nickName;
        this.gender = gender;
        this.headImagePath = headImagePath;
        this.remoteOnionName = remoteOnionName;
        this.remotePublicKey = remotePublicKey;
        this.remaks = remaks;
    }

    @Override
    public String toString() {
        return "AddressBookBean{" +
                "nickName='" + nickName + '\'' +
                ", gender=" + gender +
                ", headImagePath='" + headImagePath + '\'' +
                ", remoteOnionName='" + remoteOnionName + '\'' +
                ", remotePublicKey='" + remotePublicKey + '\'' +
                ", remaks='" + remaks + '\'' +
                '}';
    }
}
