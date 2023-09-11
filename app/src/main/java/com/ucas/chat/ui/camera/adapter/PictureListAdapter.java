package com.ucas.chat.ui.camera.adapter;


import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.ucas.chat.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @auther :haoyunlai
 * date         :2020/8/31 19:33
 * e-mail       :2931945387@qq.com
 * usefulness   :VPN列表适配器类
 */
public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.ViewHolder> {

    private List<PictureParam> pictureList = new ArrayList<>();//先用列表存储每一个// TODO: 2022/3/16 改成PictureParam类型

    private final ItemClickListener mListener;



    public PictureListAdapter(ItemClickListener listener) {//构造器+接口！
        mListener = listener;

    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_picture, parent, false);



        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PictureParam pictureParam = pictureList.get(position);

        final Bitmap picture = pictureParam.getBitmap();

//        holder.connect_state.setImageAlpha();
//        holder.flag.setImageAlpha();
        holder.imageView.setImageBitmap(picture);





    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }





    public void add(List<PictureParam> pictureList) {//经典蓝牙

        this.pictureList = pictureList;


        notifyDataSetChanged();
    }










    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

//        TextView text;
        ImageView imageView;


        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

//            text = itemView.findViewById(R.id.text);

            imageView = itemView.findViewById(R.id.pic);


        }


        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d("afcsad","hggh");

            if (pos >= 0 && pos < pictureList.size()) {
                mListener.onItemClick(pictureList.get(pos),pos);

            }

        }




    }





    public interface ItemClickListener {//列表点击接口
        void onItemClick(PictureParam picture,int position);
    }




    public interface DetailClickListener{//点击详情按钮的接口
        void onDetailClick(Bitmap picture);

    }


    private DetailClickListener detailClickListener;

    public void setDetailClickListener(DetailClickListener detailClickListener){

        this.detailClickListener = detailClickListener;

    }




}
