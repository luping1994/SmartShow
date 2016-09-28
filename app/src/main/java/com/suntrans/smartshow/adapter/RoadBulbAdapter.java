package com.suntrans.smartshow.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import com.suntrans.smartshow.R;
import com.suntrans.smartshow.bean.SmartSwitch;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Looney on 2016/9/18.
 */
public class RoadBulbAdapter extends  RecyclerView.Adapter <RoadBulbAdapter.MyViewHolder>{

//    private  SmartSwitch datas;
    private  Context context;
    private  int[] imageId = {R.drawable.ic_bulb_off,R.drawable.ic_bulb_on};
    private  OnItemClickListener mOnItemClickListener;
    private ArrayList<Map<String,String>> datas;


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
        return 10;
    }
    class  MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView ;
        TextView textView;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.iv);
            textView = (TextView) itemView.findViewById(R.id.name);
        }
        public void setData(int position){
            Bitmap bitmap_off = BitmapFactory.decodeResource(context.getResources(),imageId[0]);
            Bitmap bitmap_on = BitmapFactory.decodeResource(context.getResources(),imageId[1]);
            imageView.setImageBitmap(TextUtils.equals(datas.get(position).get("state"),"0")?bitmap_off:bitmap_on);
            textView.setText("路灯"+(position+1));
        }
    }
}
