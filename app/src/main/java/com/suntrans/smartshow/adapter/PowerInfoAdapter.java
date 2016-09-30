package com.suntrans.smartshow.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.bean.SingleMeter;
import com.suntrans.smartshow.bean.SixSensor;

/**
 * Created by Looney on 2016/9/26.
 */
public class PowerInfoAdapter extends RecyclerView.Adapter {
    private Context context;
    private SingleMeter data;

    public PowerInfoAdapter(Context context, SingleMeter data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            RecyclerView.ViewHolder holder = new viewHolder1(LayoutInflater.from(context)
                    .inflate(R.layout.meter_listview, parent, false));
            return holder;
        }else {

            RecyclerView.ViewHolder holder = new viewHolder2(LayoutInflater.from(context)
                    .inflate(R.layout.meter_listview_time, parent, false));
            return holder;
        }
    }

        @Override
        public void onBindViewHolder (RecyclerView.ViewHolder holder,int position){
            if (holder instanceof viewHolder2){
                ((viewHolder2)holder).value.setText(
                        data.getTime());
            }else {
                ((viewHolder1)holder).setData(position);
            }
        }

        @Override
        public int getItemCount () {
            return 6;
        }

        @Override
        public int getItemViewType ( int position){
            if (position == 0) {
                return 0;
            } else return 1;
        }

    /**
     * 其它条目
     */
    class viewHolder1 extends RecyclerView.ViewHolder {

        ImageView image;    //图标
        TextView name;    //名称
        TextView value;    //参数值

        public viewHolder1(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            name = (TextView) view.findViewById(R.id.name);
            value = (TextView) view.findViewById(R.id.value);
        }

        public void setData(int position) {
            switch (position) {
                case 1:
                    image.setImageResource(R.mipmap.ic_pm);
                    name.setText("用电量");
                    value.setText(data.getEletricity()+"kW·h");
                    break;
                case 2:
                    image.setImageResource(R.mipmap.ic_pm);
                    name.setText("电压");
                    value.setText(data.getUValue()+"V");
                    break;
                case 3:
                    image.setImageResource(R.mipmap.ic_pm);
                    name.setText("电流");
                    value.setText(data.getIValue()+"I");
                    break;
                case 4:
                    image.setImageResource(R.drawable.ic_power);
                    name.setText("有功功率");
                    value.setText(data.getPower()+"W");
                    break;
                case 5:
                    image.setImageResource(R.drawable.ic_power);
                    name.setText("功率因素");
                    value.setText(data.getPowerRate()+" ");
                    break;
                default:
                    break;
            }
        }
    }

    class viewHolder2 extends RecyclerView.ViewHolder
    {
        LinearLayout layout;   //整体布局

        TextView value;    //参数值
        public viewHolder2(View view)
        {
            super(view);
            layout=(LinearLayout)view.findViewById(R.id.layout);
            value = (TextView) view.findViewById(R.id.value);
        }
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
