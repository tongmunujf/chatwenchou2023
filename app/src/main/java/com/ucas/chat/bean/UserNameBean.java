package com.ucas.chat.bean;

import java.io.Serializable;

public class UserNameBean implements Serializable {
    private String userName;
    private String userId;
    private String identityId;//聊天需要用到

    public UserNameBean(String userName, String userId, String identityId) {
        this.userName = userName;
        this.userId = userId;
        this.identityId = identityId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    @Override
    public String toString() {
        return "UserNameBean{" +
                "userName='" + userName + '\'' +
                ", userId='" + userId + '\'' +
                ", identityId='" + identityId + '\'' +
                '}';
    }
}
