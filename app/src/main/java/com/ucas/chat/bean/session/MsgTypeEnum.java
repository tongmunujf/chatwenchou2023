package com.ucas.chat.bean.session;

/**
 * 消息类型
 */
public enum MsgTypeEnum {

    /**
     * 未知消息类型
     */
    undef(MsgTypeState.unknown, "Unknown"),

    /**
     * 文本消息类型
     */
    text(MsgTypeState.text, ""),

    /**
     * 图片消息
     */
    image(MsgTypeState.image, "图片"),

    /**
     * 音频消息
     */
    audio(MsgTypeState.audio, "语音"),

    /**
     * 视频消息
     */
    video(MsgTypeState.video, "视频"),

    /**
     * 位置消息
     */
    location(MsgTypeState.location, "位置"),

    /**
     * 文件消息
     */
    file(MsgTypeState.file, "文件"),

    /**
     * 音视频通话
     */
    avchat(MsgTypeState.avChat, "音视频通话"),

    /**
     * 通知消息
     */
    notification(MsgTypeState.notification, "通知消息"),

    /**
     * 提醒类型消息
     */
    tip(MsgTypeState.tip, "提醒消息"),

    /**
     * 机器人消息
     */
    robot(MsgTypeState.robot, "机器人消息"),

    /**
     * 第三方APP自定义消息
     */
    custom(MsgTypeState.custom, "自定义消息"),

    // 本地使用的消息类型
    ;

    final private int value;
    final String sendMessageTip;

    MsgTypeEnum(int value, String sendMessageTip) {
        this.value = value;
        this.sendMessageTip = sendMessageTip;
    }

    public final int getValue() {
        return value;
    }

    public final String getSendMessageTip() {
        return sendMessageTip;
    }
}
