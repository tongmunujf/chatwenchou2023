package com.ucas.chat.bean.session;

/**
 * 消息本身发送/接收状态
 */
public enum MsgStatusEnum {
    /**
     * 草稿
     */
    draft(-1),

    /**
     * 正在发送中
     */
    sending(0),

    /**
     * 发送成功
     */
    success(1),

    /**
     * 发送失败
     */
    fail(2),

    /**
     * 消息已读
     * 发送消息时表示对方已看过该消息
     * 接收消息时表示自己已读过，一般仅用于音频消息
     */
    read(3),

    /**
     * 未读状态
     */
    unread(4),
    ;

    private int value;

    MsgStatusEnum(int value){
        this.value = value;
    }

    public static MsgStatusEnum statusOfValue(int status) {
        for (MsgStatusEnum e : values()) {
            if (e.getValue() == status) {
                return e;
            }
        }
        return sending;
    }

    public int getValue(){
        return value;
    }
}
