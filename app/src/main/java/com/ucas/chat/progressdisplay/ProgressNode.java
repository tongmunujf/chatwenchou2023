package com.ucas.chat.progressdisplay;

/**
 * @auther :haoyunlai
 * date         :2022/3/15 15:30
 * e-mail       :2931945387@qq.com
 * usefulness   :记录加载进度
 */
public class ProgressNode {

    String message;//加载的信息
    String loadStatus;//加载失败（-1），正在加载（0）,加载成功（1）

    public ProgressNode(String message, String loadStatus) {
        this.message = message;
        this.loadStatus = loadStatus;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(String loadStatus) {
        this.loadStatus = loadStatus;
    }


    @Override
    public String toString() {
        return "ProgressNode{" +
                "message='" + message + '\'' +
                ", loadStatus='" + loadStatus + '\'' +
                '}';
    }



}