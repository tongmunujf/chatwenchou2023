package com.ucas.chat.ui.home.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;

import com.ucas.chat.R;
import com.ucas.chat.bean.MsgListBean;
import com.ucas.chat.bean.session1.MsgTypeStateNew;
import com.ucas.chat.utils.LogUtils;

import java.util.List;

public class MyAsyncTask extends AsyncTask<MsgListBean, Integer, MsgListBean> {

    private MsgListBean bean;       //单个数据，用于完成后的处理
    private List<View> viewList;   //视图对象集合，用于设置样式
    private Integer updatePostion; //视图标识，用于匹配视图对象
    private boolean isSend;

    public MyAsyncTask(List<View> viewList, Integer updatePostion, boolean isSend) {
        this.viewList = viewList;
        this.updatePostion = updatePostion;
        this.isSend = isSend;
    }

    @Override
    protected MsgListBean doInBackground(MsgListBean... msgListBeans) {
        bean = msgListBeans[0];
        return bean;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    }

    @Override
    protected void onPostExecute(MsgListBean bean) {//更新加载状态
        if(bean ==null)
            return;
        View view = null;

        if (updatePostion.intValue()==-1)// TODO: 2021/9/27 减少更新界面的循环
            return;

        for (int i = 0; i < viewList.size(); i++) {  /* 匹配视图对象 */
            System.out.println("MyAsyncTask更新 "+viewList.get(i).getTag(R.id.msg_listview));
//            int tag = (int)viewList.get(i).getTag(R.id.msg_listview);
            if ((int)viewList.get(i).getTag(R.id.msg_listview) == updatePostion.intValue()) {// TODO: 2021/9/26 要强制类型转化，不然数据多了后，数值一样但是还是不相等 // TODO: 2021/8/10  根据R.id.msg_listview标签得出view
                //检查所有视图ID，如果ID匹配则取出该对象
                view = viewList.get(i);
                break;
            }
        }

//        view = viewList.get(updatePostion);
        System.out.println("MyAsyncTask更新 "+view);

        if (view != null) {
            System.out.println("MyAsyncTask更新 ");
//            if (isSend) {
                if (view.getTag() instanceof MessageListAdapter.FileRViewHolder) {//右边发文件
                    MessageListAdapter.FileRViewHolder viewHolder = (MessageListAdapter.FileRViewHolder) view.getTag();
                    viewHolder.msg_tv_speed.setText(bean.getSpeed() + "KB/s");
                    viewHolder.progress_rate.setProgress(bean.getFileProgress());

                    String last_third = bean.getFilePath().substring(bean.getFilePath().length()-3,bean.getFilePath().length());// TODO: 2022/3/29 图片预览
                    LogUtils.d("last_third", last_third);
                    if (last_third.equals("png")||last_third.equals("peg")||last_third.equals("jpj")||last_third.equals("ico")||last_third.equals("jpg")) {

                        Bitmap bitmap = BitmapFactory.decodeFile(bean.getFilePath());
                        viewHolder.rc_msg_iv_file_type_image.setImageBitmap(bitmap);

                    }else if(last_third.equals("mp3")||last_third.equals("wav")){

                        viewHolder.rc_msg_iv_file_type_image.setImageResource(R.drawable.ic_voice_foreground);

                    }



                }else if (view.getTag() instanceof MessageListAdapter.FileLViewHolder) {//左边收
                    {
                        MessageListAdapter.FileLViewHolder viewHolder = (MessageListAdapter.FileLViewHolder) view.getTag();
                        System.out.println("MyAsyncTask "+viewHolder);
                        System.out.println("MyAsyncTask "+bean);
                        viewHolder.msg_tv_speed.setText(bean.getSpeed() + "KB/s");
                        viewHolder.progress_rate.setProgress(bean.getFileProgress());
                        System.out.println("MyAsyncTask "+viewHolder.progress_rate);


                        if(bean.getFileProgress()==100) {
//                            MessageListAdapter.ImgLViewHolder imgLViewHolder = (MessageListAdapter.ImgLViewHolder) view.getTag();


                            String last_third = bean.getFilePath().substring(bean.getFilePath().length()-3,bean.getFilePath().length());// TODO: 2022/3/29 图片预览
                            LogUtils.d("last_third", last_third);
                            if (last_third.equals("png")||last_third.equals("peg")||last_third.equals("jpj")||last_third.equals("ico")||last_third.equals("jpg")) {

                                Bitmap bitmap = BitmapFactory.decodeFile(bean.getFilePath());
                                viewHolder.rc_msg_iv_file_type_image.setImageBitmap(bitmap);

                            }else if(last_third.equals("mp3")||last_third.equals("wav")){

                                viewHolder.rc_msg_iv_file_type_image.setImageResource(R.drawable.ic_voice_foreground);

                            }




                        }

                    }
                } else if(view.getTag() instanceof MessageListAdapter.ImgRViewHolder){//发照片
                    MessageListAdapter.ImgRViewHolder viewHolder = (MessageListAdapter.ImgRViewHolder) view.getTag();
                    System.out.println("MyAsyncTask "+viewHolder);
                    System.out.println("MyAsyncTask "+bean);
//                    viewHolder.msg_tv_speed.setText(bean.getSpeed() + "KB/s");
//                    viewHolder.progressStatus.setProgress(bean.getFileProgress());
//                    viewHolder.progressStatus.setVisibility(View.INVISIBLE);
//                    System.out.println("MyAsyncTask "+viewHolder.progressStatus.getProgress());
                    viewHolder.progressRate.setProgress(bean.getFileProgress());

                } else if(view.getTag() instanceof MessageListAdapter.ImgLViewHolder){

                }




//            }
        }

    }


    public static void setFl(View view,MsgListBean bean ){

        if (view != null) {
            System.out.println("MyAsyncTask更新 ");
//            if (isSend) {
            if (view.getTag() instanceof MessageListAdapter.FileRViewHolder) {//右边发文件
                MessageListAdapter.FileRViewHolder viewHolder = (MessageListAdapter.FileRViewHolder) view.getTag();
                viewHolder.msg_tv_speed.setText(bean.getSpeed() + "KB/s");
                viewHolder.progress_rate.setProgress(bean.getFileProgress());




            }else if (view.getTag() instanceof MessageListAdapter.FileLViewHolder) {//左边收
                {
                    MessageListAdapter.FileLViewHolder viewHolder = (MessageListAdapter.FileLViewHolder) view.getTag();
                    System.out.println("MyAsyncTask "+viewHolder);
                    System.out.println("MyAsyncTask "+bean);
                    viewHolder.msg_tv_speed.setText(bean.getSpeed() + "KB/s");
                    viewHolder.progress_rate.setProgress(bean.getFileProgress());
                    System.out.println("MyAsyncTask "+viewHolder.progress_rate);
                }
            } else if(view.getTag() instanceof MessageListAdapter.ImgRViewHolder){//发照片
                MessageListAdapter.ImgRViewHolder viewHolder = (MessageListAdapter.ImgRViewHolder) view.getTag();
                System.out.println("MyAsyncTask "+viewHolder);
                System.out.println("MyAsyncTask "+bean);
//                    viewHolder.msg_tv_speed.setText(bean.getSpeed() + "KB/s");
//                    viewHolder.progressStatus.setProgress(bean.getFileProgress());
//                    viewHolder.progressStatus.setVisibility(View.INVISIBLE);
//                    System.out.println("MyAsyncTask "+viewHolder.progressStatus.getProgress());
                viewHolder.progressRate.setProgress(bean.getFileProgress());

            } else if(view.getTag() instanceof MessageListAdapter.ImgLViewHolder){

            }




//            }
        }



    }



}
