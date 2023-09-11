package com.ucas.chat.bean;

import com.ucas.chat.R;

import java.io.Serializable;

/**
 * 用户
 */
public class UserBean implements Serializable {

    private String userId;
    private String userName;//用户名
    private String password;//密码
    private int imPhoto;//头像
    private String onionName;
    private String privateKey;
    private String publicKey;
    private String onlineStatus;

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getOnionName() {
        return onionName;
    }

    public void setOnionName(String onionName) {
        this.onionName = onionName;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getImPhoto() {
        return imPhoto;
    }

    public void setImPhoto(int imPhoto) {
        this.imPhoto = imPhoto;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", imPhoto=" + imPhoto +
                '}';
    }
}
