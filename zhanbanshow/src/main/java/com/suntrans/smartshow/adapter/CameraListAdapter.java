package com.suntrans.smartshow.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso;
import com.suntrans.smartshow.R;
import com.videogo.openapi.bean.EZCameraInfo;

import java.util.ArrayList;

/**
 * Created by Looney on 2016/10/9.
 */

public class CameraListAdapter extends RecyclerView.Adapter {
    private final ArrayList<EZCameraInfo> data;
    private final Context context;

    public CameraListAdapter(ArrayList<EZCameraInfo> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder= new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.camera_list, parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder)holder).setData(position);
    }

    @Override
    public int getItemCount() {
        return data==null?0:data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView iv;
        TextView tv;
        public MyViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.iv);
            tv = (TextView) itemView.findViewById(R.id.tv);
        }
        public void setData(final int position){
            tv.setText("设备名称"+data.get(position).getDeviceName());
            Picasso.with(context)
                    .load(data.get(position).getPicUrl())
                    .into(iv);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.ivClick(position);
                }
            });
        }
    }

    public OnClickListener getListener() {
        return listener;
    }

    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    private OnClickListener listener;
    public interface OnClickListener{
        void ivClick(int position);
    }
}
