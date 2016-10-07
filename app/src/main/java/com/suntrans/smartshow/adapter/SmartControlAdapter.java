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

/**
 * Created by Looney on 2016/9/26.
 */
public class SmartControlAdapter extends RecyclerView.Adapter {
    private Context context;
    private SmartSwitch data;
    private static String[] names = {"客厅","餐厅","厨房","书房","卫生间","主卧","次卧","走廊","阳台"};
    private static int [] imageId = {R.drawable.keting,R.drawable.none,R.drawable.chufang,
            R.drawable.shufang,R.drawable.weishengjian,R.drawable.woshi,R.drawable.woshi,R.drawable.zoulang
            , R.drawable.none
    };
    public SmartControlAdapter(Context context, SmartSwitch data) {
        this.context = context;
        this.data = data;
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

//    data.clear();   //先清空数据
//    Map<String, String> map1 = new HashMap<>();
//    map1.put("Name","PM1");
//    map1.put("Value","null");
//    map1.put("Image", String.valueOf(R.mipmap.ic_pm));
//    data.add(map1);
//
//    Map<String, String> map2 = new HashMap<>();
//    map2.put("Name","PM10");
//    map2.put("Value","null");
//    map2.put("Image", String.valueOf(R.mipmap.ic_pm));
//    data.add(map2);
//
//    Map<String, String> map3 = new HashMap<>();
//    map3.put("Name","PM25");
//    map3.put("Value","null");
//    map3.put("Image", String.valueOf(R.mipmap.ic_pm));
//    data.add(map3);
//
//    Map<String, String> map4 = new HashMap<>();
//    map4.put("Name","甲醛");
//    map4.put("Value","null");
//    map4.put("Image", String.valueOf(R.drawable.ic_power));
//
//    data.add(map4);
//    Map<String, String> map5 = new HashMap<>();
//    map5.put("Name","烟雾");
//    map5.put("Value","null");
//    map5.put("Image", String.valueOf(R.mipmap.ic_smoke));
//    data.add(map5);
//
//    Map<String, String> map6 = new HashMap<>();
//    map6.put("Name","温度");
//    map6.put("Value","null");
//    map6.put("Image", String.valueOf(R.mipmap.ic_temp));
//    data.add(map6);
//
//    Map<String, String> map7 = new HashMap<>();
//    map7.put("Name","湿度");
//    map7.put("Value","null");
//    map7.put("Image", String.valueOf(R.mipmap.ic_humidity));
//    data.add(map7);
//
//    Map<String, String> map8 = new HashMap<>();
//    map8.put("Name","气压");
//    map8.put("Value","null");
//    map8.put("Image", String.valueOf(R.mipmap.ic_atm));
//    data.add(map8);
//
//    Map<String, String> map9 = new HashMap<>();
//    map9.put("Name","光线强度");
//    map9.put("Value","null");
//    map9.put("Image", String.valueOf(R.mipmap.ic_lightintensity));
//    data.add(map9);
//
//    Map<String, String> map10 = new HashMap<>();
//    map10.put("Name","人员信息");
//    map10.put("Value","null");
//    map10.put("Image", String.valueOf(R.mipmap.ic_peopleinfo));
//    data.add(map10);
//
//    Map<String, String> map11 = new HashMap<>();
//    map11.put("Name","X轴倾斜角");
//    map11.put("Value","null");
//    map11.put("Image", String.valueOf(R.mipmap.ic_gradient));
//    data.add(map11);
//
//    Map<String, String> map12 = new HashMap<>();
//    map12.put("Name","Y轴倾斜角");
//    map12.put("Value","null");
//    map12.put("Image", String.valueOf(R.mipmap.ic_gradient));
//    data.add(map12);
//
//    Map<String, String> map13 = new HashMap<>();
//    map6.put("Name","Z轴倾斜角");
//    map6.put("Value","null");
//    map6.put("Image", String.valueOf(R.mipmap.ic_gradient));
//    data.add(map13);
