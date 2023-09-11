package com.ucas.chat.bean.session;

/**
 * 附件传输状态
 */
public enum AttachStatusEnum {
    /**
     * 默认状态，未开始
     */
    def(0),
    /**
     * 正在传输
     */
    transferring(1),
    /**
     * 传输成功
     */
    transferred(2),
    /**
     * 传输失败
     */
    fail(3);

    private int value;

    AttachStatusEnum(int value){
        this.value = value;
    }

    public static AttachStatusEnum statusOfValue(int status) {
        for (AttachStatusEnum e : values()) {
            if (e.getValue() == status) {
                return e;
            }
        }
        return def;
    }

    public int getValue(){
        return value;
    }
}
