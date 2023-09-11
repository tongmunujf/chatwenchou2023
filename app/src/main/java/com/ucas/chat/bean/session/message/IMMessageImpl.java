package com.ucas.chat.bean.session.message;

import com.ucas.chat.bean.session.AttachStatusEnum;
import com.ucas.chat.bean.session.MsgDirectionEnum;
import com.ucas.chat.bean.session.MsgStatusEnum;
import com.ucas.chat.bean.session.MsgTypeEnum;
import com.ucas.chat.bean.session.SessionTypeEnum;
import com.ucas.chat.utils.StringUtils;

import java.io.Serializable;
import java.util.Map;

public class IMMessageImpl implements IMMessage {

    private MsgTypeEnum msgType;
    private String sessionId;
    private SessionTypeEnum sessionType;
    private long time;
    private MsgDirectionEnum direct;
    private String content;
    private MsgAttachment msgAttachment;

    @Override
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public SessionTypeEnum getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionTypeEnum sessionType) {
        this.sessionType = sessionType;
    }

    public void setMsgType(MsgTypeEnum msgType) {
        this.msgType = msgType;
    }

    @Override
    public MsgTypeEnum getMsgType() {
        return msgType;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public boolean isTheSame(IMMessage message) {
        return false;
    }

    @Override
    public String getFromNick() {
        return null;
    }

    @Override
    public MsgStatusEnum getStatus() {
        return null;
    }

    @Override
    public void setStatus(MsgStatusEnum status) {

    }

    @Override
    public MsgDirectionEnum getDirect() {
        return direct;
    }

    @Override
    public void setDirect(MsgDirectionEnum direct) {
        this.direct = direct;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setAttachment(MsgAttachment attachment) {
        this.msgAttachment = attachment;
    }

    @Override
    public MsgAttachment getAttachment() {
        return msgAttachment;
    }








    @Override
    public void setFromAccount(String account) {

    }

    @Override
    public String getFromAccount() {
        return null;
    }

    @Override
    public AttachStatusEnum getAttachStatus() {
        return null;
    }

    @Override
    public void setAttachStatus(AttachStatusEnum attachStatus) {

    }

    @Override
    public Map<String, Object> getRemoteExtension() {
        return null;
    }

    @Override
    public void setRemoteExtension(Map<String, Object> remoteExtension) {

    }

    @Override
    public Map<String, Object> getLocalExtension() {
        return null;
    }

    @Override
    public void setLocalExtension(Map<String, Object> localExtension) {

    }

    @Override
    public String getPushContent() {
        return null;
    }

    @Override
    public void setPushContent(String pushContent) {

    }

    @Override
    public Map<String, Object> getPushPayload() {
        return null;
    }

    @Override
    public void setPushPayload(Map<String, Object> pushPayload) {

    }

    @Override
    public boolean isRemoteRead() {
        return false;
    }

    @Override
    public int getFromClientType() {
        return 0;
    }
}
