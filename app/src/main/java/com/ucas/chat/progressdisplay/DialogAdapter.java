package com.ucas.chat.progressdisplay;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ucas.chat.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.VH> {
    private static final String TAG = DialogAdapter.class.getSimpleName();
    private final Listener mListener;
    private final Handler mHandler = new Handler();
    public List<ProgressNode> progressNodes = new ArrayList<>();
    public boolean isScanning;

    private Context context;


    public DialogAdapter(Context context, List<ProgressNode> progressNodes, Listener listener) {
        this.context = context;
        this.mListener = listener;
        this.progressNodes = progressNodes;
//        scanBle();
    }




    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }






    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog, parent, false);
        System.out.println("fasfwefwea1");
        return new VH(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        ProgressNode progressNode = progressNodes.get(position);

        String message = progressNode.getMessage();//加载的信息
        String loadStatus = progressNode.getLoadStatus();//加载失败（-1），正在加载（0）,加载成功（1）

        holder.tv_message.setText(message);

        if(loadStatus.equals("0")){
            holder.pb_loadstatus.setVisibility(View.VISIBLE);
            holder.im_loadstatus.setVisibility(View.GONE);
        }else if(loadStatus.equals("-1")){
            holder.pb_loadstatus.setVisibility(View.GONE);
            holder.im_loadstatus.setVisibility(View.VISIBLE);
            holder.im_loadstatus.setImageResource(R.drawable.ic_fail_foreground);
        }else {
            holder.pb_loadstatus.setVisibility(View.GONE);
            holder.im_loadstatus.setVisibility(View.VISIBLE);
            holder.im_loadstatus.setImageResource(R.drawable.ic_success_foreground);

        }



        System.out.println("fasfwefwea2");

//        holder.address.setText(String.format("广播数据{%s}", dev.scanResult.getScanRecord()));
//        holder.address.setText(String.format("广播数据{%s}", new String(dev.scanResult.getScanRecord().getManufacturerSpecificData().get(6),Charset.forName("utf-8"))));

    }

    @Override
    public int getItemCount() {
        return progressNodes.size();
    }

    class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_message;//进度内容
        ProgressBar pb_loadstatus;//正在加载图
        ImageView im_loadstatus;//加载最后情况图，与pb_loadstatus互斥



        VH(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tv_message = itemView.findViewById(R.id.tv_message);
            pb_loadstatus = itemView.findViewById(R.id.pb_loadstatus);
            im_loadstatus = itemView.findViewById(R.id.im_loadstatus);

        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(TAG, "onClick, getAdapterPosition=" + pos);
            if (pos >= 0 && pos < progressNodes.size()){
//                mListener.onItemClick(mDevices.get(pos).dev);
            }
        }




    }

    public interface Listener {
        void onItemClick(BluetoothDevice dev);
    }


}
