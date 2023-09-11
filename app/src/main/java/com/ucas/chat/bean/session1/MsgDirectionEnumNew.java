package com.ucas.chat.bean.session1;

/**
 * 消息方向枚举
 */
public enum MsgDirectionEnumNew {
    /**
     * 发出去的消息
     */
    Out(0),
    /**
     * 接收到的消息
     */
    In(1);

    private int value;

    MsgDirectionEnumNew(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MsgDirectionEnumNew directionOfValue(int value) {
        for (MsgDirectionEnumNew direction : values()){
            if (direction.getValue() == value) {
                return direction;
            }
        }
        return Out;
    }
}
