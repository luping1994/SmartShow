package com.suntrans.smartshow.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import com.suntrans.smartshow.R;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Looney on 2016/9/18.
 */
public class RoadBulbAdapter extends  RecyclerView.Adapter <RoadBulbAdapter.MyViewHolder>{

    private  ArrayList<Map<String,String>> datas;
    private  Context context;

    private  OnItemClickListener mOnItemClickListener;



    public void setOnItemClickListener(OnItemClickListener listener ) {
        mOnItemClickListener =listener;
    }
    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

    public RoadBulbAdapter(Context context, ArrayList<Map<String,String>> datas){
        this.datas=datas;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.road_bulb_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.setData(position);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onClick(v,holder.getPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return 11;
    }
    class  MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView ;
        TextView textView;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.iv);
        }
        public void setData(int position){

        }
    }
}
