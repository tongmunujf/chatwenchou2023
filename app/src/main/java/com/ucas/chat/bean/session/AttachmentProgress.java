package com.ucas.chat.bean.session;

import java.io.Serializable;

/**
 * 附件发送/接收进度通知
 */
public class AttachmentProgress implements Serializable {

    private final String uuid;
    private final long transferred;
    private final long total;

    /**
     * 构造函数
     * @param uuid 消息的UUID
     * @param transferred 消息附件文件当前已传输的字节数
     * @param total 消息附件文件总大小
     */
    public AttachmentProgress(String uuid, long transferred, long total) {
        this.uuid = uuid;
        this.transferred = transferred;
        this.total = total;
    }

    /**
     * 获取附件对应的消息的uuid
     * @return uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * 获取已经传输的字节数
     * @return 已经传输的字节数
     */
    public long getTransferred() {
        return transferred;
    }

    /**
     * 获取文件总长度
     * @return 文件总长度
     */
    public long getTotal() {
        return total;
    }
}
