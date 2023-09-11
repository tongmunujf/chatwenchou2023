package com.ucas.chat.tor.util;

import java.io.Serializable;

/**
 * @auther :haoyunlai
 * date         :2021/10/2 13:19
 * e-mail       :2931945387@qq.com
 * usefulness   :TODO 记录XOR片段使用的情况，便于用后删除和防止错乱
 */
public class RecordXOR implements Serializable {

    private int startFileName;//起始文件的文件名，严格按从小到大排序,根据XORutil.startFile2Byte()设计，最大能放65535
    private int startFileIndex;//使用的xor片段起始点在起始文件的位置，该位置定义为起始点距离起始文件尾的byte数。最大是999999999
    private int endFileName;//结束文件的文件名
    private int endFileIndex;//使用的xor片段结束点在结束文件的位置，定义为结束点距离结束文件尾的byte数
    private String messageID;//该条消息的messageID，由它检索这条消息是否要删除xor片段

    public RecordXOR() {
    }

    public RecordXOR(int startFileName, int startFileIndex, int endFileName, int endFileIndex, String messageID) {
        this.startFileName = startFileName;
        this.startFileIndex = startFileIndex;
        this.endFileName = endFileName;
        this.endFileIndex = endFileIndex;
        this.messageID = messageID;
    }


    public int getStartFileName() {
        return startFileName;
    }

    public void setStartFileName(int startFileName) {
        this.startFileName = startFileName;
    }

    public int getStartFileIndex() {
        return startFileIndex;
    }

    public void setStartFileIndex(int startFileIndex) {
        this.startFileIndex = startFileIndex;
    }

    public int getEndFileName() {
        return endFileName;
    }

    public void setEndFileName(int endFileName) {
        this.endFileName = endFileName;
    }

    public int getEndFileIndex() {
        return endFileIndex;
    }

    public void setEndFileIndex(int endFileIndex) {
        this.endFileIndex = endFileIndex;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }


    @Override
    public String toString() {
        return "RecordXOR{" +
                "startFileName=" + startFileName +
                ", startFileIndex=" + startFileIndex +
                ", endFileName=" + endFileName +
                ", endFileIndex=" + endFileIndex +
                ", messageID='" + messageID + '\'' +
                '}';
    }
}