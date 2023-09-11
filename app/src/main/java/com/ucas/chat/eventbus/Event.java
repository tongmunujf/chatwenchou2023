package com.ucas.chat.eventbus;

import java.util.Date;

public class Event {
    public static final String GET_OFFLINE_LIST = "get_offline_list";
    public static final String GET_OFFLINE_TEXT= "get_offline_text";
    public static final String GET_OFFLINE_FILE = "get_offline_file";
    public static final String SEND_OFFLINE_TEXT = "send_offline_text";
    public static final String SEND_OFFLINE_FILE = "send_offline_file";
    public static final String SEND_OFFLINE_PIC = "send_offline_pic";
    public static final String GET_OFFLINE_PIC = "get_offline_pic";

    public static final String RECIEVE_ONLINE_FILE ="recieve_online_file";

    public static final String GET_NODE ="get_node";
    public static final String TOR_CONNECTED = "event_bus_tor_connected";
    public static final String TOR_CONNECT_READY = "tor_connect_ready";
    public static final String SEND_PROTOCOL_FAILURE = "event_bus_send_protocol_failure";
    public static final String START_COMMUNICATION_SUCCESS = "event_bus_start_communication_success";
    public static final String PEER_HAS_RECEIVED_MESSAGE_SUCCESS = "event_bus_peer_has_received_message_success";
    public static final String PEER_HAS_RECEIVED_MESSAGE_FAILURE = "event_bus_peer_has_received_message_failure";
    public static final String HAS_RECEIVED_MESSAGE = "event_bus_has_received_message";
    public static final String CREATE_CONNECTION_SUCCESS = "create_connection_success";
    public static final String FILE_MESSAGE = "file_message";
    public static final String CHECK_PEER_ONLINE_STATUS="check_peer_status";
    public static final String RECEIVE_PIC_OVER = "receive_pic_over";// TODO: 2021/8/17 照片接收完毕

    private String type;
    private String message;
    private String peerHostname;
    public Event(String type, String message, String peerHostname) {
        this.type = type;
        this.message = message;
        this.peerHostname = peerHostname;
    }
    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getPeerHostname() {
        return peerHostname;
    }

    public static class FileMetaMessage{
        private String fileName;
        private long totalSize;
        private Date startTime;// TODO: 2021/8/13 增加时间参数作为该文件的标识

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }

//        public Date getStartTime() {
//            return startTime;
//        }
//
//        public void setStartTime(Date startTime) {
//            this.startTime = startTime;
//        }

        public FileMetaMessage(String fileName, long totalSize) {
            this.fileName = fileName;
            this.totalSize = totalSize;
//            this.startTime = startTime;
        }
    }


    public static class FileMessage{
        private String fileName;
        private String filePercent;
        private String fileSpeed;

        public FileMessage(String fileName, String filePercent, String fileSpeed) {
            this.filePercent = filePercent;
            this.fileSpeed = fileSpeed;
            this.fileName=fileName;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePercent() {
            return filePercent;
        }

        public void setFilePercent(String filePercent) {
            this.filePercent = filePercent;
        }

        public String getFileSpeed() {
            return fileSpeed;
        }

        public void setFileSpeed(String fileSpeed) {
            this.fileSpeed = fileSpeed;
        }

        @Override
        public String toString() {
            return "FileMessage{" +
                    "fileName='" + fileName + '\'' +
                    ", filePercent='" + filePercent + '\'' +
                    ", fileSpeed='" + fileSpeed + '\'' +
                    '}';
        }
    }

    public static class PeerOnlineStatusMessage{
        private String onionHash;
        private String onlineStatus;
        private String statusUpdateTime;

        public PeerOnlineStatusMessage(String onionHash, String onlineStatus, String statusUpdateTime) {
            this.onionHash = onionHash;
            this.onlineStatus = onlineStatus;
            this.statusUpdateTime = statusUpdateTime;
        }

        public String getOnionHash() {
            return onionHash;
        }

        public void setOnionHash(String onionHash) {
            this.onionHash = onionHash;
        }

        public String getOnlineStatus() {
            return onlineStatus;
        }

        public void setOnlineStatus(String onlineStatus) {
            this.onlineStatus = onlineStatus;
        }

        public String getStatusUpdateTime() {
            return statusUpdateTime;
        }

        public void setStatusUpdateTime(String statusUpdateTime) {
            this.statusUpdateTime = statusUpdateTime;
        }

        @Override
        public String toString() {
            return "OnlineStatusMessage{" +
                    "onionHash='" + onionHash + '\'' +
                    ", onlineStatus='" + onlineStatus + '\'' +
                    ", statusUpdateTime='" + statusUpdateTime + '\'' +
                    '}';
        }
    }



}
