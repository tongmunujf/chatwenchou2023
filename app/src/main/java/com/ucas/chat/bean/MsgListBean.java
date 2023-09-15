package com.ucas.chat.bean;

import android.graphics.Bitmap;

import com.ucas.chat.bean.session1.MsgTypeStateNew;
import com.ucas.chat.db.ChatContract;

import java.io.Serializable;

public class MsgListBean implements Serializable {

    private String sendTime ;
    private int msgType;//MsgTypeEnum
    private String textContent;
    private String filePath;
    private String fileName;
    private int fileSize;
    private String speed;
    private int percentage;
    private String from; //发送者的userId
    private String to; //接收者的userId
    private int isAcked; //好友是否收到消息
    private int fileProgress;
    private int isState;

    private Bitmap bitmap;// TODO: 2021/8/7  //图片或照片
    private int pictureSize;
    private int pictureProgress;

    private String messageID;// TODO: 2021/8/8 用来唯一标记这一次的发送，以免错乱

    private String friendOrionid;
    private String friendNickname;// TODO: 2022/3/22 增加


    /**
     * 数据库 字段
     */
    public MsgListBean(String sendTime, int msgType, String textContent, String filePath, String fileName,
                       int fileSize , int fileProgress, String from, String to, int isAcked,String messageID,String friendOrionid,String friendNickname) {
        this.sendTime = sendTime;
        this.msgType = msgType;
        this.textContent = textContent;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.from = from;
        this.to = to;
        this.isAcked = isAcked;
        this.fileProgress = fileProgress;
        this.messageID = messageID;

        this.friendOrionid = friendOrionid;
        this.friendNickname = friendNickname;

    }

    /**
     * 文本
     */
    public MsgListBean(String textContent, String from, String to, int isAcked,String messageID,String friendOrionid,String friendNickname) {
        this.sendTime = System.currentTimeMillis()+"";
        this.msgType = MsgTypeStateNew.text;
        this.msgType = msgType;
        this.textContent = textContent;
        this.from = from;
        this.to = to;
        this.isAcked = isAcked;
        this.messageID = messageID;

        this.friendOrionid = friendOrionid;
        this.friendNickname = friendNickname;

    }

    /**
     * File
     */
    public MsgListBean(String filePath, String fileName, int fileSize, int fileProgress,
                       String speed, String from, String to, int isAcked,String messageID,String friendOrionid,String friendNickname) {
        this.sendTime = System.currentTimeMillis()+"";
        this.msgType = MsgTypeStateNew.file;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileProgress = fileProgress;
        this.speed = speed;
        this.from = from;
        this.to = to;
        this.isAcked = isAcked;
        this.messageID = messageID;

        this.friendOrionid = friendOrionid;
        this.friendNickname = friendNickname;

    }

    /**
     * 图片或照片
     */
    public MsgListBean(Bitmap bitmap, int pictureSize, int pictureProgress,
                       String speed, String from, String to, int isAcked,String messageID,String picturePath,String friendOrionid,String friendNickname) {// TODO: 2021/8/4
        this.sendTime = System.currentTimeMillis()+"";
        this.msgType = MsgTypeStateNew.image;
        this.bitmap = bitmap;
        this.fileName =picturePath;// TODO: 2021/8/26 新增加文件名
        this.filePath = picturePath;// TODO: 2022/3/17 照片路径
        this.pictureSize = pictureSize;
        this.fileProgress = pictureProgress;// TODO: 2021/8/26 因为复用了文件的代码，所以这里改为fileProgress
        this.speed = speed;
        this.from = from;
        this.to = to;
        this.isAcked = isAcked;
        this.messageID = messageID;


        this.friendOrionid = friendOrionid;
        this.friendNickname = friendNickname;

    }


    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getFileProgress() {
        return fileProgress;
    }

    public void setFileProgress(int fileProgress) {
        this.fileProgress = fileProgress;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getFrom() {
        return from;
    }

    public int getIsAcked() {
        return isAcked;
    }

    public int getIsState() {
        return isState;
    }

    public void setIsState(int isState) {
        this.isState = isState;
    }

    public void setIsAcked(int isAcked) {
        this.isAcked = isAcked;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }


    public int getPictureSize() {
        return pictureSize;
    }

    public void setPictureSize(int pictureSize) {
        this.pictureSize = pictureSize;
    }

    public int getPictureProgress() {
        return pictureProgress;
    }

    public void setPictureProgress(int pictureProgress) {
        this.pictureProgress = pictureProgress;
    }

    public String getFriendOrionid() {
        return friendOrionid;
    }

    public void setFriendOrionid(String friendOrionid) {
        this.friendOrionid = friendOrionid;
    }

    public String getFriendNickname() {
        return friendNickname;
    }

    public void setFriendNickname(String friendNickname) {
        this.friendNickname = friendNickname;
    }

    @Override
    public String toString() {
        return "MsgListBean{" +
                "sendTime='" + sendTime + '\'' +
                ", msgType=" + msgType +
                ", textContent='" + textContent + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", speed='" + speed + '\'' +
                ", percentage=" + percentage +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", isAcked=" + isAcked +
                ", fileProgress=" + fileProgress +
                ", isState=" + isState +
                ", bitmap=" + bitmap +
                ", pictureSize=" + pictureSize +
                ", pictureProgress=" + pictureProgress +
                ", messageID='" + messageID + '\'' +
                ", friendOrionid='" + friendOrionid + '\'' +
                ", friendNickname='" + friendNickname + '\'' +
                '}';
    }
}
