package com.ucas.chat.bean.litepal;

import org.litepal.LitePalBase;
import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 通讯录
 */
public class ContactsBean extends DataSupport implements Serializable {

    private String userId;
    private String userName;
    private String passWord;
    private String nickName;
    private String orionId; //聊天ID
    private String orionHashId;
    private String imageId; //头像ID

    //####################
    private String onlineStatus="0";



    public ContactsBean() {
    }

    public ContactsBean(String userId, String userName, String passWord, String nickName, String orionId, String orionHashId, String imageId) {
        this.userId = userId;
        this.userName = userName;
        this.passWord = passWord;
        this.nickName = nickName;
        this.orionId = orionId;
        this.orionHashId = orionHashId;
        this.imageId = imageId;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
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

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getOrionId() {
        return orionId;
    }

    public void setOrionId(String orionId) {
        this.orionId = orionId;
    }

    public String getOrionHashId() {
        return orionHashId;
    }

    public void setOrionHashId(String orionHashId) {
        this.orionHashId = orionHashId;
    }

    public void setIdentityId(String identityId) {
        this.orionId = orionId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        return "ContactsBean{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", nickName='" + nickName + '\'' +
                ", orionId='" + orionId + '\'' +
                ", orionHashId='" + orionHashId + '\'' +
                ", imageId='" + imageId + '\'' +
                '}';
    }
}
