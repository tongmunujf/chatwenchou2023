package com.ucas.chat.bean.session;

import com.ucas.chat.bean.session.message.AudioAttachment;
import com.ucas.chat.bean.session.message.FileAttachment;
import com.ucas.chat.bean.session.message.IMMessage;
import com.ucas.chat.bean.session.message.IMMessageImpl;
import com.ucas.chat.utils.StringUtils;
import com.ucas.chat.utils.TimeUtils;

import java.io.File;

/**
 * 消息构造器
 */
public class MessageBuilder {

    /**
     * 创建一条普通文本消息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param text        文本消息内容
     * @return IMMessage 生成的消息对象
     */
    public static IMMessage createTextMessage(String sessionId, SessionTypeEnum sessionType, String text) {
        IMMessageImpl msg = initSendMessage(sessionId, sessionType);
        msg.setMsgType(MsgTypeEnum.text);
        msg.setContent(text);
        if (sessionId.equals("123")){
            msg.setDirect(MsgDirectionEnum.Out);
        }
        return msg;
    }

    /**
     * 创建一条图片消息, 显示名默认为null
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param file        图片文件
     * @return IMMessage 生成的消息对象
     */
    public static IMMessage createImageMessage(String sessionId, SessionTypeEnum sessionType, File file, File file1) {
        return createImageMessage(sessionId, sessionType, file, file1, null);
    }

    /**
     * 创建一条图片消息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param file        图片文件
     * @param displayName 图片文件的显示名，可不同于文件名
     * @return IMMessage 生成的消息对象
     */
    public static IMMessage createImageMessage(String sessionId, SessionTypeEnum sessionType, File file, File file1, String displayName) {
        IMMessageImpl msg = initSendMessage(sessionId, sessionType);
        msg.setMsgType(MsgTypeEnum.image);
        final ImageAttachment attachment = new ImageAttachment();
        attachment.setThumbPath(file1.getPath());
        attachment.setPath(file.getPath());
        attachment.setSize(file.length());
        //int[] dimension = ImageUtils.getImageWidthHeight(file.getPath());
//        attachment.setWidth(dimension[0]);
//        attachment.setHeight(dimension[1]);
        attachment.setDisplayName(displayName);
        attachment.setExtension(StringUtils.getExtension(file.getName()));
        msg.setAttachment(attachment);
       return msg;
    }

    private static IMMessageImpl initSendMessage(String toId, SessionTypeEnum sessionType) {
        IMMessageImpl msg = new IMMessageImpl();

//        msg.setUuid(StringUtil.get32UUID());
         msg.setSessionId(toId);
//        msg.setFromAccount(SDKCache.getAccount());
        msg.setDirect(MsgDirectionEnum.Out);
//        msg.setStatus(MsgStatusEnum.sending);
        msg.setSessionType(sessionType);
        msg.setTime(TimeUtils.currentTimeMillis());

        return msg;
    }

    /**
     * 创建一条视频消息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param file        视频文件对象
     * @param duration    视频文件持续时间
     * @param width       视频宽度
     * @param height      视频高度
     * @param displayName 视频文件显示名，可以为空
     * @return 视频消息
     */
    public static IMMessage createVideoMessage(String sessionId, SessionTypeEnum sessionType, File file, long duration, int width, int height, String displayName) {
        IMMessageImpl msg = initSendMessage(sessionId, sessionType);
        msg.setMsgType(MsgTypeEnum.video);

        final VideoAttachment attachment = new VideoAttachment();
        attachment.setPath(file.getPath());
        attachment.setSize(file.length());
        attachment.setDuration(duration);
        attachment.setWidth(width);
        attachment.setHeight(height);
        attachment.setDisplayName(displayName);
        attachment.setExtension(StringUtils.getExtension(file.getName()));
        msg.setAttachment(attachment);

       // BitmapDecoder.extractThumbnail(file.getPath(), attachment.getThumbPathForSave());
        return msg;
    }

    /**
     * 创建一条音频消息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param file        音频文件对象
     * @param duration    音频文件持续时间，单位是ms
     * @return IMMessage 生成的消息对象
     */
    public static IMMessage createAudioMessage(String sessionId, SessionTypeEnum sessionType, File file, long duration) {
        IMMessageImpl msg = initSendMessage(sessionId, sessionType);
        msg.setMsgType(MsgTypeEnum.audio);

        final AudioAttachment attachment = new AudioAttachment();
        attachment.setPath(file.getPath());
        attachment.setSize(file.length());
        if (duration > 0 && duration < 1000) {// 最低显示1秒
            duration = 1000;
        }
        attachment.setDuration(duration);// ms
        attachment.setExtension(StringUtils.getExtension(file.getName()));
        msg.setAttachment(attachment);
        return msg;
    }

    public static IMMessage createFileMessage(String sessionId, SessionTypeEnum sessionType,File file){
        IMMessageImpl msg = initSendMessage(sessionId, sessionType);
        msg.setMsgType(MsgTypeEnum.file);
//        msg.setContent(file.getName());
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setSize(file.length());
        fileAttachment.setDisplayName(file.getName());
        fileAttachment.setExtension(StringUtils.getExtension(file.getName()));
        msg.setAttachment(fileAttachment);
        return msg;
    }
}
