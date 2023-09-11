package com.ucas.chat.bean;

import java.io.Serializable;

public class MyInforBean implements Serializable {

    private String account ;
    private String password;//MsgTypeEnum
    private String nickName;
    private int gender;
    private String headImagePath;
    private String  onionName;
    private String publicKey;
    private String privateKey;


    /**
     *
     * @param account  从userinfo.txt中获取到
     * @param password 从userinfo.txt中获取到
     * @param nickName 从userinfo.txt中获取到
     * @param gender
     * @param headImagePath
     * @param onionName 从hostname.txt中获取到 onionName就是hostName
     * @param publicKey
     * @param privateKey
     */
    public MyInforBean(String account, String password, String nickName, int gender, String headImagePath, String onionName, String publicKey, String privateKey) {
        this.account = account;
        this.password = password;
        this.nickName = nickName;
        this.gender = gender;
        this.headImagePath = headImagePath;
        this.onionName = onionName;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public String getOnionName() {
        return onionName;
    }

    public void setOnionName(String onionName) {
        this.onionName = onionName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String toString() {
        return "MyInforBean{" +
                "account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", nickName='" + nickName + '\'' +
                ", gender=" + gender +
                ", headImagePath='" + headImagePath + '\'' +
                ", onionName='" + onionName + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", privateKey='" + privateKey + '\'' +
                '}';
    }
}
