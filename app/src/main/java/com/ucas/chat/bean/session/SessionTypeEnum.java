package com.ucas.chat.bean.session;

/**
 * 会话类型
 */
public enum  SessionTypeEnum {

    None(SessionTypeState.none),

    /**
     * 单聊
     */
    P2P(SessionTypeState.p2p),

    /**
     * 群聊
     */
    Team(SessionTypeState.team),

    /**
     * 系统消息
     */
    System(SessionTypeState.system),

    /**
     * 聊天室
     */
    ChatRoom(SessionTypeState.chatRoom);

    private int value;

    SessionTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SessionTypeEnum typeOfValue(int value) {
        for (SessionTypeEnum e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return P2P;
    }
}
