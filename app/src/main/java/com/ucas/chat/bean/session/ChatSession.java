package com.ucas.chat.bean.session;

public class ChatSession {

    //会话类型
    private SessionTypeEnum mSessionType;
    private String mSessionId;
    private String mMyAccount;
    //用户账号
    private String mChatAccount;
    private String mChatNick;
//    private NimUserInfo mMyInfo;
//    private NimUserInfo mChatInfo;

    public SessionTypeEnum getSessionType() {
        return mSessionType;
    }

    public void setSessionType(SessionTypeEnum sessionType) {
        mSessionType = sessionType;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public void setSessionId(String sessionId) {
        mSessionId = sessionId;
    }

    public String getMyAccount() {
        return mMyAccount;
    }

    public void setMyAccount(String myAccount) {
        mMyAccount = myAccount;
    }

    public String getChatAccount() {
        return mChatAccount;
    }


    public void setChatAccount(String chatAccount) {
        mChatAccount = chatAccount;
    }

    public String getChatNick() {
        return mChatNick;
    }

    public void setChatNick(String chatNick) {
        mChatNick = chatNick;
    }

//    public NimUserInfo getMyInfo() {
//        return mMyInfo;
//    }
//
//    public void setMyInfo(NimUserInfo myInfo) {
//        mMyInfo = myInfo;
//    }
//
//    public NimUserInfo getChatInfo() {
//        return mChatInfo;
//    }
//
//    public void setChatInfo(NimUserInfo chatInfo) {
//        mChatInfo = chatInfo;
//    }
}
