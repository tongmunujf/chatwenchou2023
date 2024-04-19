package com.ucas.chat.ui.home.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.joooonho.SelectableRoundedImageView;
import com.ucas.chat.R;
import com.ucas.chat.bean.MsgListBean;
import com.ucas.chat.bean.UserBean;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.bean.litepal.ContactsBean;
import com.ucas.chat.bean.session1.MsgTypeStateNew;
import com.ucas.chat.db.MailListUserNameTool;
import com.ucas.chat.db.MyInforTool;
import com.ucas.chat.tor.util.FilePathUtils;
import com.ucas.chat.ui.listener.OnItemClickListener;
import com.ucas.chat.ui.login.PhotoActivity;
import com.ucas.chat.ui.view.PassWordDialog;
import com.ucas.chat.ui.view.RoundProgressBar;
import com.ucas.chat.ui.view.chat.RViewHolder;
import com.ucas.chat.utils.AesTools;
import com.ucas.chat.utils.BitmapUtil;
import com.ucas.chat.utils.LogUtils;
import com.ucas.chat.utils.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.zlylib.upperdialog.utils.ResUtils.getResources;


public class MessageListAdapter extends BaseAdapter {
    public static final String TAG = ConstantValue.TAG_CHAT + "MessageListAdapter";

    //好友消息
    private static final int MSG_TEXT_L = 0;
    private static final int MSG_IMG_L = 1;
    private static final int MSG_VIDEO_L = 2;
    private static final int MSG_FILE_L = 3;
    //本人消息
    private static final int MSG_TEXT_R = 4;
    private static final int MSG_IMG_R = 5;
    private static final int MSG_VIDEO_R = 6;
    private static final int MSG_FILE_R = 7;

    private static final int LAYOUT_TYPE = 8;

    private Context mContext;
    private LayoutInflater mInflater;
    private List<MsgListBean> mMessageList;
    private ContactsBean mBean;//对方情况
    private UserBean mUserBean;
    private SimpleDateFormat mDateFormat;
    private List<View> mViewList;

    public MessageListAdapter(Context context, List<MsgListBean> messages, ContactsBean contactsBean) {
        mContext = context;
        mViewList = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mMessageList = messages;
        mBean = contactsBean;
        mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        mUserBean = SharedPreferencesUtil.getUserBeanSharedPreferences(mContext);
    }

    public List<View> getViewList() {
        return mViewList;
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getViewTypeCount() {
        return LAYOUT_TYPE;
    }

    public void refreshAllHeadPicture(){// TODO: 2021/8/27  刷新全部头像
        for(int i=0;i<mViewList.size();i++){
            View view = mViewList.get(i);
            if (view.getTag() instanceof MessageListAdapter.TextLViewHolder ||
                    view.getTag() instanceof MessageListAdapter.FileLViewHolder||
                    view.getTag() instanceof MessageListAdapter.ImgLViewHolder||
                    view.getTag() instanceof MessageListAdapter.VideoLViewHolder) {//只更新左边的头像！

                MessageListAdapter.BaseViewHolder viewHolder = (MessageListAdapter.BaseViewHolder) view.getTag();
                if (mBean.getOnlineStatus().equals("1"))
                    viewHolder.imageView.setImageResource(R.mipmap.b1);//刷新本条消息的头像为在线状态
                else {
                    viewHolder.imageView.setImageResource(R.mipmap.touxiang_1);//刷新本条消息的头像为在线状态
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        //from = asdf
       // LogUtils.d(TAG, " getItemViewType:: 转化前from = " + mMessageList.get(position).getFrom());
        LogUtils.d(TAG, " getItemViewType:: msgType = " + mMessageList.get(position).getMsgType());
       // String from = mMessageList.get(position).getFrom();
        String from = null;
        from = AesTools.getDecryptContent(mMessageList.get(position).getFrom(), AesTools.AesKeyTypeEnum.COMMON_KEY);
        LogUtils.d(TAG, " getItemViewType:: 解密from = " + from);
        if (from == null || from.isEmpty()){
            //对没有进行加密的聊天内容进行兼容
            from =mMessageList.get(position).getFrom();
        }

        LogUtils.d(TAG, " getItemViewType:: %%%%%%%%% from = " + from);
        LogUtils.d(TAG, " getMsgViewType:: userId = " + mUserBean.getUserId());
        return getMsgViewType(from, mMessageList.get(position).getMsgType());
    }

    private int getMsgViewType(String from, int type) {
        //userId = asdf
        if (!from.isEmpty()){
            // 收到的消息
            if (!from.equals(mUserBean.getUserId())) {
                if (type == MsgTypeStateNew.text) {
                    return MSG_TEXT_L;
                } else if (type == MsgTypeStateNew.image) {
                    return MSG_IMG_L;
                } else if (type == MsgTypeStateNew.video) {
                    return MSG_VIDEO_L;
                } else if (type == MsgTypeStateNew.file){
                    return MSG_FILE_L;
                }else {
                    LogUtils.d(TAG, " getMsgViewType:: if return 0" );
                    return 0;
                }
            } else{ // 发出的消息
                if (type == MsgTypeStateNew.text) {
                    return MSG_TEXT_R;
                } else if (type == MsgTypeStateNew.image) {
                    return MSG_IMG_R;
                } else if (type == MsgTypeStateNew.video) {
                    return MSG_VIDEO_R;
                } else if (type == MsgTypeStateNew.file){
                    return MSG_FILE_R;
                }else {
                    LogUtils.d(TAG, " getMsgViewType:: else return 0" );
                    return 0;
                }
            }
        }
        LogUtils.d(TAG, " getMsgViewType:: return 0" );
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("ddw",position+"");
        switch (getItemViewType(position)){
            case MSG_TEXT_L:
                return handleGetTextL(position,convertView,parent);
            case MSG_TEXT_R:
                return handleGetTextR(position,convertView,parent);
            case MSG_IMG_L:
                case MSG_FILE_L:
                return handleGetFileL(position,convertView,parent);
//            case MSG_IMG_R:
            case MSG_FILE_R:
                return handleGetFileR(position,convertView,parent);
//            case MSG_IMG_L:
//                return handleGetImgL(position,convertView,parent);
            case MSG_IMG_R:
                return handleGetImgR(position,convertView,parent);
            case MSG_VIDEO_L:
                return handleGetVideoL(position,convertView,parent);
            case MSG_VIDEO_R:
                return handleGetVideoR(position,convertView,parent);
            default:
                return null;
        }
    }

    /**
     * 接收文本
     */
    private View handleGetTextL(int position, View convertView, ViewGroup parent){
        TextLViewHolder holder;
        if (convertView == null){
            holder = new TextLViewHolder();
            convertView = mInflater.inflate(R.layout.item_msg_text_left, parent, false);
            holder.imageView = convertView.findViewById(R.id.iv_head_picture);
            holder.tv_chat_msg = convertView.findViewById(R.id.tv_chat_msg);
            convertView.setTag(holder);
        }else {
            holder = (TextLViewHolder)convertView.getTag();
        }
        MsgListBean bean = mMessageList.get(position);

        LogUtils.d(TAG, " handleGetTextL:: message = " + bean.getTextContent());
        String message = null;

        message = AesTools.getDecryptContent(bean.getTextContent(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        LogUtils.d(TAG, " handleGetTextL:: 解密message = " + message);
        if (message == null || message.isEmpty()){
            //对没有进行加密的聊天内容进行兼容
            message = bean.getTextContent();
            LogUtils.d(TAG, " handleGetTextL:: dddddd 解密message = " + message);
        }
        LogUtils.d(TAG, " handleGetTextL:: message = " + message);

        String deMessage = AesTools.getDecryptContent(message, AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        if (deMessage == null || deMessage.isEmpty()){
            //对没有进行加密的聊天内容进行兼容
            deMessage = message;
        }
        holder.tv_chat_msg.setText(deMessage);

        String onlineStatus=mBean.getOnlineStatus();
        if (onlineStatus.equals("0")) {
            holder.imageView.setImageResource(R.mipmap.touxiang_1);//显示对方离线状态
        }else {
            holder.imageView.setImageResource(R.mipmap.b1);// TODO: 2021/9/26 修复 convertView复用的问题
        }


        if (bean.getIsAcked() == 1){
            holder.tv_chat_msg.setTextColor(mContext.getColor(R.color.green3));
        }else {
            holder.tv_chat_msg.setTextColor(mContext.getColor(R.color.app_black_color));// TODO: 2021/9/26 修复 convertView复用的问题
        }

        convertView.setTag(R.id.msg_listview, position);//标记，用于MyAsyncTask更新时遍历
        mViewList.add(convertView);// TODO: 2021/9/27  直接加入可能导致后期mViewList过多，下次优化
        return convertView;
    }

    /**
     * 发送文本
     */
    private View handleGetTextR(int position, View convertView, ViewGroup parent){
        TextRViewHolder holder;
        if (convertView == null){
            holder = new TextRViewHolder();
            convertView = mInflater.inflate(R.layout.item_msg_text_right, parent, false);
            holder.imageView = convertView.findViewById(R.id.iv_head_picture);
            holder.tv_chat_msg = convertView.findViewById(R.id.tv_chat_msg);
            convertView.setTag(holder);
        }else {
            holder = (TextRViewHolder)convertView.getTag();//这里直接复用旧的convertView会bug，这个旧的可能改了文字颜色为绿色，新的文字消息使用会导致下面的文字颜色问题
        }
        MsgListBean bean = mMessageList.get(position);

        LogUtils.d(TAG, " handleGetTextR:: 加密message = " + bean.getTextContent());
        String message = null;

        message = AesTools.getDecryptContent(bean.getTextContent(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
       LogUtils.d(TAG, " handleGetTextR:: 解密message = " + message);
        if (message == null || message.isEmpty()){
            //对没有进行加密的聊天内容进行兼容
            message = bean.getTextContent();
            LogUtils.d(TAG, " handleGetTextR:: message = " + message);
        }

        LogUtils.d(TAG, " handleGetTextL:: message = " + message);

        String deMessage = AesTools.getDecryptContent(message, AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        if (deMessage == null || deMessage.isEmpty()){
            //对没有进行加密的聊天内容进行兼容
            deMessage = message;
        }
        holder.tv_chat_msg.setText(deMessage);

        Log.i("asffwdw1",holder.tv_chat_msg.getCurrentTextColor()+"");//检测文字的颜色

        if (bean.getIsAcked() == 1){// TODO: 2021/9/24 有bug, IsAcked为0也会变绿色
            holder.tv_chat_msg.setTextColor(mContext.getColor(R.color.green3));
            Log.i("asffwdw2",bean.getTextContent()+"  "+bean.getIsAcked());
        }else {// TODO: 2021/9/24 重新将0的变色的变回黑色,// TODO: 2021/9/26 修复 convertView复用的问题
            holder.tv_chat_msg.setTextColor(mContext.getColor(R.color.app_black_color));
            Log.i("asffwdw3",bean.getTextContent()+"  "+bean.getIsAcked());
        }

        Log.i("asffwdw4",holder.tv_chat_msg.getCurrentTextColor()+"");

        convertView.setTag(R.id.msg_listview, position);//标记，用于MyAsyncTask更新时遍历
        mViewList.add(convertView);
        return convertView;
    }

    /**
     * 接收文件
     */
    private View handleGetFileL(int position, View convertView, ViewGroup parent){
        FileLViewHolder holder;
        if (convertView == null){
            holder = new FileLViewHolder();
            convertView = mInflater.inflate(R.layout.item_msg_file_left, parent, false);
            holder.imageView = convertView.findViewById(R.id.iv_head_picture);
            holder.rc_msg_iv_file_type_image = convertView.findViewById(R.id.rc_msg_iv_file_type_image);
            holder.msg_tv_file_name = convertView.findViewById(R.id.msg_tv_file_name);
            holder.msg_tv_file_size = convertView.findViewById(R.id.msg_tv_file_size);
            holder.msg_tv_speed = convertView.findViewById(R.id.msg_tv_speed);
            holder.progress_rate = convertView.findViewById(R.id.progress_rate);
            holder.ll_text_receive = convertView.findViewById(R.id.ll_text_receive);
            convertView.setTag(holder);
        }else {
            holder = (FileLViewHolder)convertView.getTag();
        }

        holder.rc_msg_iv_file_type_image.setImageResource(R.mipmap.rc_file_icon_file);//因为会复用上一消息的设计，这里要还原

        MsgListBean bean = mMessageList.get(position);
        LogUtils.d(TAG, " handleGetFileL:: bean = " + bean.toString());

        String filePath = AesTools.getDecryptContent(bean.getFilePath(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        LogUtils.d(TAG, " handleGetFileL:: bean.getFilePath() = " + bean.getFilePath());
        LogUtils.d(TAG, " handleGetFileL:: 解密filePath = " + filePath);
        if (filePath == null || filePath.isEmpty()){
            //对没有进行加密的聊天内容进行兼容
            filePath = bean.getFilePath();
        }
        String last_third = bean.getFilePath().substring(filePath.length()-3,filePath.length());// TODO: 2022/3/29 图片预览
        LogUtils.d("last_third", last_third);
        if (last_third.equals("png")||last_third.equals("peg")||last_third.equals("jpj")||last_third.equals("ico")||last_third.equals("jpg")) {

            Bitmap bitmap = BitmapFactory.decodeFile(bean.getFilePath());
            holder.rc_msg_iv_file_type_image.setImageBitmap(bitmap);

        }else if(last_third.equals("mp3")||last_third.equals("wav")){

            holder.rc_msg_iv_file_type_image.setImageResource(R.drawable.ic_voice_foreground);

        }

        String onlineStatus=mBean.getOnlineStatus();
        if (onlineStatus.equals("0")) {
            holder.imageView.setImageResource(R.mipmap.touxiang_1);//显示对方离线状态
        }else {
            holder.imageView.setImageResource(R.mipmap.b1);// TODO: 2021/9/26 修复 convertView复用的问题
        }

        String fileName = AesTools.getDecryptContent(bean.getFileName(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        if (fileName == null || fileName.isEmpty()){
            fileName = bean.getFileName();
        }
        Log.d(TAG, " handleGetFileL:: fileName = " + fileName);
        holder.msg_tv_file_name.setText(fileName);

        String fileSize = AesTools.getDecryptContent(bean.getFileName(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        if (fileSize == null || fileSize.isEmpty()){
            fileSize = bean.getFileSize() + "";
        }
        Log.d(TAG, " handleGetFileL:: fileSize = " + fileSize);
        holder.msg_tv_file_size.setText(fileSize);

        holder.progress_rate.setProgress(bean.getFileProgress());

        convertView.setTag(R.id.msg_listview, position);//标记，用于MyAsyncTask更新时遍历
        mViewList.add(convertView);

        holder.ll_text_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = bean.getFileName();
                String filePath = BitmapUtil.filePathIsExists(fileName);
                if (BitmapUtil.isPicFile(filePath)){
                    BitmapUtil.picturePreview(mContext, fileName);
                }
            }
        });

        holder.ll_text_receive.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PassWordDialog passWordDialog = new PassWordDialog(mContext);
                passWordDialog.setFileName(bean.getFileName());
                passWordDialog.show();
                return false;
            }
        });

        return convertView;
    }

    /**
     * 发送文件
     */
    private View handleGetFileR(int position, View convertView, ViewGroup parent){
        FileRViewHolder holder;
        if (convertView == null){
            holder = new FileRViewHolder();
            convertView = mInflater.inflate(R.layout.item_msg_file_right, parent, false);
            holder.imageView = convertView.findViewById(R.id.iv_head_picture);
            holder.rc_msg_iv_file_type_image = convertView.findViewById(R.id.rc_msg_iv_file_type_image);
            holder.msg_tv_file_name = convertView.findViewById(R.id.msg_tv_file_name);
            holder.msg_tv_file_size = convertView.findViewById(R.id.msg_tv_file_size);
            holder.msg_tv_speed = convertView.findViewById(R.id.msg_tv_speed);
            holder.progress_rate = convertView.findViewById(R.id.progress_rate);
            convertView.setTag(holder);
        }else {
            holder = (FileRViewHolder)convertView.getTag();
        }

        MsgListBean bean = mMessageList.get(position);
        LogUtils.d(TAG, " handleGetFileR:: bean = " + bean.toString());
        holder.rc_msg_iv_file_type_image.setImageResource(R.mipmap.rc_file_icon_file);//因为会复用上一消息的设计，这里要还原

        String filePath = AesTools.getDecryptContent(bean.getFilePath(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        LogUtils.d(TAG, " handleGetFileR:: 解密filePath = " + filePath);
        if (filePath == null || filePath.isEmpty()){
            filePath = bean.getFilePath();
        }
        String last_third = bean.getFilePath().substring(filePath.length()-3,filePath.length());

        LogUtils.d("last_third", last_third);
        if (last_third.equals("png")||last_third.equals("peg")||last_third.equals("jpj")||last_third.equals("ico")||last_third.equals("jpg")) {

            Bitmap bitmap = BitmapFactory.decodeFile(bean.getFilePath());
            holder.rc_msg_iv_file_type_image.setImageBitmap(bitmap);

        }else if(last_third.equals("mp3")||last_third.equals("wav")){

            holder.rc_msg_iv_file_type_image.setImageResource(R.drawable.ic_voice_foreground);

        }

        String fileName = AesTools.getDecryptContent(bean.getFileName(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
        if (fileName == null || fileName.isEmpty()){
            fileName = bean.getFileName();
        }
        Log.d(TAG, " handleGetFileR:: fileName = " + fileName);
        holder.msg_tv_file_name.setText(fileName);

        String fileSize = AesTools.getDecryptContent(bean.getFileName(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);

        if (fileSize == null || fileSize.isEmpty()){
            fileSize = bean.getFileSize() + "";
        }
        Log.d(TAG, " handleGetFileL:: fileSize = " + fileSize);
        holder.msg_tv_file_size.setText(fileSize);

        holder.msg_tv_speed.setText(bean.getSpeed() + " KB/s");
        holder.progress_rate.setProgress(bean.getFileProgress());
        if (bean.getFileProgress() == 100){
            holder.msg_tv_speed.setVisibility(View.GONE);
        }else {
            holder.msg_tv_speed.setVisibility(View.VISIBLE);// TODO: 2021/9/26 修复 convertView复用的问题
        }

        convertView.setTag(R.id.msg_listview, position);//标记，用于MyAsyncTask更新时遍历
        mViewList.add(convertView);

        return convertView;
    }

    /**
     * 接收图片
     */
    private View handleGetImgL(int position, View convertView, ViewGroup parent){
        ImgLViewHolder holder;
        if (convertView == null){
            holder = new ImgLViewHolder();
            convertView = mInflater.inflate(R.layout.item_msg_file_left, parent, false);
            holder.imageView = convertView.findViewById(R.id.iv_head_picture);
            holder.roundIm= convertView.findViewById(R.id.iv_msg_img);
            convertView.setTag(holder);
        }else {
            holder = (ImgLViewHolder)convertView.getTag();
        }

        convertView.setTag(R.id.msg_listview, position);//标记，用于MyAsyncTask更新时遍历
        mViewList.add(convertView);
        return convertView;
    }

    /**
     * 发送图片
     */
    private View handleGetImgR(int position, View convertView, ViewGroup parent){
        ImgRViewHolder holder;
        if (convertView == null){
            holder = new ImgRViewHolder();
//            convertView = mInflater.inflate(R.layout.item_msg_file_right, parent, false);
            convertView = mInflater.inflate(R.layout.item_msg_img_right, parent, false);// TODO: 2021/8/5  
            holder.imageView = convertView.findViewById(R.id.iv_head_picture);//头像
            holder.roundIm= convertView.findViewById(R.id.iv_msg_img);
            holder.progressRate = convertView.findViewById(R.id.progress_rate);// TODO: 2021/8/26 RoundProgressBar类型！！！换成progress_rate 进度 记得改MyAsyncTask那
//            holder.progressStatus = convertView.findViewById(R.id.progress_status);
            convertView.setTag(holder);
        }else {
            holder = (ImgRViewHolder)convertView.getTag();
        }
        MsgListBean bean = mMessageList.get(position);
//        holder.imageView.setImageBitmap(bean.getBitmap());

        if (bean.getBitmap()!=null) {
            holder.progressRate.setProgress(bean.getFileProgress());
            holder.roundIm.setImageBitmap(bean.getBitmap());//照片
        }
        else {//读数据库肯定没有图片
            holder.progressRate.setProgress(bean.getFileProgress());
            holder.roundIm.setImageResource(R.mipmap.nolook);//显示“不可见”
//            holder.progressStatus.setVisibility(View.INVISIBLE);//上面要绑定控件，这里才能赋值，不然闪退
        }




        convertView.setTag(R.id.msg_listview, position);//标记，用于MyAsyncTask更新时遍历
        mViewList.add(convertView);// TODO: 2021/8/9 记得加入
        return convertView;
    }

    /**
     * 接收视频
     */
    private View handleGetVideoL(int position, View convertView, ViewGroup parent){
        VideoLViewHolder holder;
        if (convertView == null){
            holder = new VideoLViewHolder();
            convertView = mInflater.inflate(R.layout.item_msg_video_left, parent, false);
            holder.imageView = convertView.findViewById(R.id.iv_head_picture);
            holder.videoIm= convertView.findViewById(R.id.iv_video_cover);
            convertView.setTag(holder);
        }else {
            holder = (VideoLViewHolder)convertView.getTag();
        }

        return convertView;
    }

    /**
     * 发送视频
     */
    private View handleGetVideoR(int position, View convertView, ViewGroup parent){
        VideoRViewHolder holder;
        if (convertView == null){
            holder = new VideoRViewHolder();
            convertView =mInflater.inflate(R.layout.item_msg_video_right, parent, false);
            holder.imageView = convertView.findViewById(R.id.iv_head_picture);
            holder.videoIm= convertView.findViewById(R.id.iv_video_cover);
            convertView.setTag(holder);
        }else {
            holder = (VideoRViewHolder)convertView.getTag();
        }

        return convertView;
    }
     class TextLViewHolder extends BaseViewHolder{// TODO: 2021/8/27 去除 static ，继承BaseViewHolder
//        private ImageView imageView;
        private TextView tv_chat_msg;
    }
     class TextRViewHolder extends BaseViewHolder{// TODO: 2021/8/27 去除 static ，继承BaseViewHolder
//        private ImageView imageView;
        private TextView tv_chat_msg;
    }
     class FileLViewHolder extends BaseViewHolder{// TODO: 2021/8/27 去除 static ，继承BaseViewHolder
//        private ImageView imageView;
        public ImageView rc_msg_iv_file_type_image;//文件格式图片
        private TextView msg_tv_file_size;
        private TextView msg_tv_file_name;
        public TextView msg_tv_speed;
        public RoundProgressBar progress_rate;

    }
     class FileRViewHolder extends BaseViewHolder{// TODO: 2021/8/27 去除 static ，继承BaseViewHolder
//        private ImageView imageView;
         public ImageView rc_msg_iv_file_type_image;// TODO: 2022/3/25 //文件格式图片
        private TextView msg_tv_file_size;
        private TextView msg_tv_file_name;
        public TextView msg_tv_speed;
        public RoundProgressBar progress_rate;

    }
     class ImgLViewHolder extends BaseViewHolder{// TODO: 2021/8/27 去除 static ，继承BaseViewHolder
//        public ImageView imageView;
        public SelectableRoundedImageView roundIm;
        public ProgressBar progressStatus;
    }
     class ImgRViewHolder extends BaseViewHolder{// TODO: 2021/8/27 去除 static ，继承BaseViewHolder
//        public ImageView imageView;
        public SelectableRoundedImageView roundIm;
        public ProgressBar progressStatus;
        public RoundProgressBar progressRate;// TODO: 2021/8/26 换成发送进度 RoundProgressBar类型！！！
    }
     class VideoLViewHolder extends BaseViewHolder{// TODO: 2021/8/27 去除 static ，继承BaseViewHolder
//        private ImageView imageView;
        private SelectableRoundedImageView videoIm;

    }
     class VideoRViewHolder extends BaseViewHolder{// TODO: 2021/8/27 去除 static ，继承BaseViewHolder
//        private ImageView imageView;
        private SelectableRoundedImageView videoIm;

    }

    class BaseViewHolder{// TODO: 2021/8/27 父类，用于抽离出上面各种ViewHolder的相同类型的
        public ImageView imageView;//头像都是一样的类型，便于全局更新头像
        public LinearLayout ll_text_receive;

    }


}
