package com.ucas.chat.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.ucas.chat.bean.session.ChatSession;
import com.ucas.chat.bean.session.MessageBuilder;
import com.ucas.chat.bean.session.MsgTypeEnum;
import com.ucas.chat.bean.session.SessionTypeEnum;
import com.ucas.chat.bean.session.message.IMMessage;
import com.ucas.chat.bean.session.message.IMMessageImpl;

import java.io.File;
import java.util.List;

/**
 * 聊天工具类
 */

public class ChatMsgHandler {

    private static final String TAG = ChatMsgHandler.class.getSimpleName();

    private static final int ONE_QUERY_LIMIT = 20;
    private static final long TEN_MINUTE = 1000 * 60 * 10;

    private Context mContext;
    private ChatSession mChatSession;

    public ChatMsgHandler(Context context, ChatSession session) {
        mContext = context;
        mChatSession = session;
    }

    /**
     * 发送文本消息
     *
     * @param text 文本
     */
    public IMMessage createTextMessage(String text) {
        // 创建文本消息
        return MessageBuilder.createTextMessage(mChatSession.getChatAccount(),
                mChatSession.getSessionType(), text);
    }

    /**
     * 发送图片消息
     *
     * @param path 图片路径
     */
    public IMMessage createImageMessage(String path, String thumbPath) {
        return MessageBuilder.createImageMessage(mChatSession.getSessionId(),
                mChatSession.getSessionType(), new File(path), new File(thumbPath));
    }


    /**
     * 发送语音消息
     *
     * @param path 语音文件路径
     * @param time 录音时间 ms
     */
//    public IMMessage createAudioMessage(String path, long time) {
//        return MessageBuilder.createAudioMessage(mChatSession.getSessionId(),
//                mChatSession.getSessionType(), new File(path), time);
//    }

    /**
     * 发送视频消息
     *
     * @param path 视频文件路径
     */
    public IMMessage createVideoMessage(String path) {
        File file = new File(path);
        MediaPlayer player = MediaPlayer.create(mContext, Uri.fromFile(file));
        int duration = player.getDuration();
        int height = player.getVideoHeight();
        int width = player.getVideoWidth();
        return MessageBuilder.createVideoMessage(mChatSession.getSessionId(),
                mChatSession.getSessionType(), file, duration, width, height, null);
    }

    public IMMessage createAudioMessage(String path, long time) {
        return MessageBuilder.createAudioMessage(mChatSession.getSessionId(),
                mChatSession.getSessionType(), new File(path), time);
    }


    public IMMessage createFileMessage(String path) {
        return MessageBuilder.createFileMessage(mChatSession.getSessionId(),
                mChatSession.getSessionType(), new File(path));
    }

    /**
     * 加载历史消息记录
     *
     * @param anchorMessage 锚点消息
     * @param listener      加载回调
     */
//    public void loadMessage(final IMMessage anchorMessage, final OnLoadMsgListener listener) {
//        NIMClient.getService(MsgService.class).queryMessageListEx(anchorMessage,
//                QueryDirectionEnum.QUERY_OLD, ONE_QUERY_LIMIT, true)
//                .setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
//                    @Override
//                    public void onResult(int code, List<IMMessage> result, Throwable exception) {
//                        if (exception != null) {
//                            listener.loadFail(exception.getMessage());
//                            return;
//                        }
//                        if (code != 200) {
//                            listener.loadFail("code:" + code);
//                            return;
//                        }
//
//                        listener.loadSuccess(result, anchorMessage);
//
//                    }
//                });
//    }

    /**
     * 处理历史消息记录，如果两条消息之间相隔大于 TEN_MINUTE,则需要在两条之间新增时间点文本消息
     *
     * @param messages      历史消息列表
     * @param anchorMessage 锚点消息
     * @return 处理完成后的消息列表
     */
//    public List<IMMessage> dealLoadMessage(List<IMMessage> messages, IMMessage anchorMessage) {
//        IMMessage lastMsg = messages.get(messages.size() - 1);
//        if (anchorMessage.getTime() - lastMsg.getTime() >= TEN_MINUTE) {
//            messages.add(messages.size() - 1,createTimeMessage(lastMsg));
//        }
//
//        for (int i = messages.size() - 2; i > 0; i--) {
//            if (!TextUtils.isEmpty(messages.get(i).getUuid()) &&
//                    !TextUtils.isEmpty(messages.get(i-1).getUuid())){
//                if (messages.get(i).getTime() - messages.get(i-1).getTime() >= TEN_MINUTE) {
//                    messages.add(i , createTimeMessage(messages.get(i)));
//                }
//            }
//        }
//
//        return messages;
//    }


//    public IMMessage createTimeMessage(IMMessage message) {
//        return MessageBuilder.createEmptyMessage(message.getSessionId(),
//                message.getSessionType(), message.getTime());
//    }

    public interface OnLoadMsgListener {
        void loadSuccess(List<IMMessage> messages, IMMessage anchorMessage);

        void loadFail(String message);
    }
}
