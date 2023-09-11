package com.ucas.chat.bean;

import com.ucas.chat.bean.litepal.ContactsBean;

import java.io.Serializable;

/**
 * 消息列表Bean
 */
public class NewsBean implements Serializable {
    private String friendName;
    private String friendHeadNum;
    private String lastNewsTime;
    private int isReadNews;
    private String lastNews;
    private ContactsBean contactsBean;

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendHeadNum() {
        return friendHeadNum;
    }

    public void setFriendHeadNum(String friendHeadNum) {
        this.friendHeadNum = friendHeadNum;
    }

    public String getLastNewsTime() {
        return lastNewsTime;
    }

    public void setLastNewsTime(String lastNewsTime) {
        this.lastNewsTime = lastNewsTime;
    }

    public int getIsReadNews() {
        return isReadNews;
    }

    public void setIsReadNews(int isReadNews) {
        this.isReadNews = isReadNews;
    }

    public String getLastNews() {
        return lastNews;
    }

    public void setLastNews(String lastNews) {
        this.lastNews = lastNews;
    }

    public ContactsBean getContactsBean() {
        return contactsBean;
    }

    public void setContactsBean(ContactsBean contactsBean) {
        this.contactsBean = contactsBean;
    }

    public static NewsBean format(ContactsBean contactsBean, MsgListBean msgListBean) {
        NewsBean newsBean = new NewsBean();
        newsBean.contactsBean = contactsBean;
        newsBean.friendName = contactsBean.getUserName();
        newsBean.friendHeadNum = contactsBean.getImageId();
        newsBean.lastNewsTime = msgListBean.getSendTime();
        newsBean.lastNews = msgListBean.getTextContent();
        return newsBean;
    }

    @Override
    public String toString() {
        return "NewsBean{" +
                "friendName='" + friendName + '\'' +
                ", friendHeadNum=" + friendHeadNum +
                ", lastNewsTime='" + lastNewsTime + '\'' +
                ", isReadNews=" + isReadNews +
                ", lastNews='" + lastNews + '\'' +
                '}';
    }
}
