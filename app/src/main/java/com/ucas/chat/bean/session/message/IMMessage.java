package com.ucas.chat.bean.session.message;

import com.ucas.chat.bean.session.AttachStatusEnum;
import com.ucas.chat.bean.session.MsgDirectionEnum;
import com.ucas.chat.bean.session.MsgStatusEnum;
import com.ucas.chat.bean.session.MsgTypeEnum;
import com.ucas.chat.bean.session.SessionTypeEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * 消息实体
 */
public interface IMMessage extends Serializable {

    /**
     * 获取消息的uuid, 该域在生成消息时即会填上
     *
     * @return 消息uuid
     */
    public String getUuid();

    /**
     * 判断与参数message是否是同一条消息。<br>
     * 先比较数据库记录ID，如果没有数据库记录ID，则比较{@link #getUuid()}
     *
     * @param message 消息对象
     * @return 两条消息是否相同
     */
    public boolean isTheSame(IMMessage message);

    /**
     * 获取聊天对象的Id（好友帐号，群ID等）。
     *
     * @return 聊天对象ID
     */
    public String getSessionId();

    /**
     * 获取会话类型。
     *
     * @return 会话类型
     */
    public SessionTypeEnum getSessionType();

    /**
     * 获取消息发送者的昵称
     *
     * @return 用户的昵称
     */
    public String getFromNick();

    /**
     * 获取消息类型。
     *
     * @return 消息类型
     */
    public MsgTypeEnum getMsgType();

    /**
     * 获取消息接收/发送状态。
     *
     * @return 消息状态
     */
    public MsgStatusEnum getStatus();

    /**
     * 设置消息状态
     *
     * @param status 消息状态
     */
    public void setStatus(MsgStatusEnum status);

    /**
     * 设置消息方向
     *
     * @param direct 消息方向
     */
    public void setDirect(MsgDirectionEnum direct);

    /**
     * 获取消息方向：发出去的消息还是接收到的消息
     *
     * @return 消息方向
     */
    public MsgDirectionEnum getDirect();

    /**
     * 设置消息具体内容。<br>
     * 当消息类型{MsgTypeEnum#text}时，该域为消息内容。
     * 当为其他消息类型时，该域为可选项，如果设置，将作为iOS的apns推送文本以及android内置消息推送的显示文本。
     *
     * @param content 消息内容/推送文本
     */
    public void setContent(String content);

    /**
     * 获取消息具体内容。<br>
     * 当消息类型{MsgTypeEnum#text}时，该域为消息内容。
     * 当为其他消息类型时，该域为可选项，如果设置，将作为iOS的apns推送文本以及android内置消息推送的显示文本（1.7.0及以上版本建议使用pushContent）。
     *
     * @return 消息内容/推送文本
     */
    public String getContent();

    /**
     * 获取消息时间，单位为ms
     *
     * @return 时间
     */
    public long getTime();

    /**
     * 设置说话方的帐号。消息方向{@link #getDirect()}根据改之
     *
     * @param account 帐号
     */
    public void setFromAccount(String account);

    /**
     * 获取该条消息发送方的帐号
     */
    public String getFromAccount();

    /**
     * 设置消息附件对象。<br>
     * 如果附件内部包含状态，或是自定义附件类型，用户可自主更新，以便界面展现。<br>
     * 注意：设置之后，如需持久化到数据库，需要调用{updateIMMessageStatus}更新
     *
     * @param attachment
     */
    public void setAttachment(MsgAttachment attachment);

    /**
     * 获取消息附件对象。仅当{@link #getMsgType()}返回为非text时有效
     */
    public MsgAttachment getAttachment();

    /**
     * 获取消息附件接收/发送状态
     */
    public AttachStatusEnum getAttachStatus();

    /**
     * 设置消息附件状态
     */
    public void setAttachStatus(AttachStatusEnum attachStatus);

//    /**
//     * 获取消息配置
//     *
//     * @return 消息配置
//     */
//    public CustomMessageConfig getConfig();
//
//    /**
//     * 设置消息配置
//     *
//     * @param config 消息配置
//     */
//    public void setConfig(CustomMessageConfig config);

    /**
     * 获取扩展字段（该字段会发送到其他端）
     *
     * @return 扩展字段Map
     */
    public Map<String, Object> getRemoteExtension();

    /**
     * 设置扩展字段（该字段会发送到其他端），最大长度1024字节。
     *
     * @param remoteExtension 扩展字段Map，开发者需要保证此Map能够转换为JsonObject
     */
    public void setRemoteExtension(Map<String, Object> remoteExtension);

    /**
     * 获取本地扩展字段（仅本地有效）
     *
     * @return 扩展字段Map
     */
    public Map<String, Object> getLocalExtension();

    /**
     * 设置本地扩展字段（该字段仅在本地使用有效，不会发送给其他端），最大长度1024字节
     *
     * @param localExtension
     */
    public void setLocalExtension(Map<String, Object> localExtension);

    /**
     * 获取自定义推送文案
     *
     * @return 自定义推送文案
     */
    public String getPushContent();

    /**
     * 设置自定义推送文案（1.7.0及以上版本建议使用此字段，不要使用setContent来设置推送文案）,最大长度200字节
     *
     * @param pushContent 自定义推送文案
     */
    public void setPushContent(String pushContent);

    /**
     * 获取第三方自定义的推送属性
     *
     * @return 第三方自定义的推送属性Map
     */
    public Map<String, Object> getPushPayload();

    /**
     * 设置第三方自定义的推送属性
     *
     * @param pushPayload 第三方自定义的推送属性Map，开发者需要保证此Map能够转换为JsonObject，属性内容最大长度2048字节
     */
    public void setPushPayload(Map<String, Object> pushPayload);

//    /**
//     * 获取指定成员推送选项
//     *
//     * @return 指定成员推送选项
//     */
//    public MemberPushOption getMemberPushOption();
//
//    /**
//     * 设置指定成员推送选项
//     *
//     * @param pushOption 指定成员推送选项
//     */
//    public void setMemberPushOption(MemberPushOption pushOption);

    /**
     * 判断自己发送的消息对方是否已读
     *
     * @return true：对方已读；false：对方未读
     */
    public boolean isRemoteRead();

    /**
     * 获取消息发送方类型
     *
     * @return 发送方的客户端类型，与ClientType类比较
     */
    public int getFromClientType();

//    /**
//     * 获取易盾反垃圾配置项
//     *
//     * @return NIMAntiSpamOption
//     */
//    public NIMAntiSpamOption getNIMAntiSpamOption();
//
//    /**
//     * 设置易盾反垃圾选项
//     *
//     * @param nimAntiSpamOption
//     */
//    public void setNIMAntiSpamOption(NIMAntiSpamOption nimAntiSpamOption);
}
