package com.suntrans.smartshow.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.bean.SmartSwitch;
import com.suntrans.smartshow.utils.UiUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Looney on 2016/9/26.
 */
public class SmartControlAdapter extends RecyclerView.Adapter {
    private  ArrayList<Map<String, String>> datas;
    private Context context;

    private static String[] names = {"客厅","餐厅","厨房","书房","卫生间","主卧","次卧","走廊","阳台"};
    private static int [] imageId = {R.drawable.keting,R.drawable.none,R.drawable.chufang,
            R.drawable.shufang,R.drawable.weishengjian,R.drawable.woshi,R.drawable.woshi,R.drawable.zoulang
            , R.drawable.none
    };
    public SmartControlAdapter(Context context, ArrayList<Map<String,String>> data) {
        this.context = context;
        this.datas = data;
    }
    private onItemClickListener onItemClickListener;
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = new viewHolder1(LayoutInflater.from(context)
                .inflate(R.layout.publicarea_item, parent, false));
        return holder;
    }

    public void setOnItemClickListener(SmartControlAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
        public void onBindViewHolder (final RecyclerView.ViewHolder holder, final int position){

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),imageId[position]);
            bitmap = Converts.toRoundCorner(bitmap, UiUtils.dip2px(10));
            ((viewHolder1)holder).image.setImageBitmap(bitmap);
            ((viewHolder1)holder).image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onclick(v,holder.getAdapterPosition());
                }
            });

            ((viewHolder1)holder).textView.setText(names[position]);
        }

        @Override
        public int getItemCount () {
//            return datas.size()==0?0:datas.size();
            return 9;
        }

    @Override
    public int getItemViewType(int position) {

        return super.getItemViewType(position);
    }

    /**
     * 自定义继承RecyclerView.ViewHolder的viewholder
     *
     */
    class viewHolder1 extends RecyclerView.ViewHolder {
        ImageView image;    //图标
        TextView textView;

        public viewHolder1(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.iv);
            textView = (TextView) view.findViewById(R.id.tv);
        }
    }

    public interface onItemClickListener{
        void onclick(View v, int position);
    }

}

