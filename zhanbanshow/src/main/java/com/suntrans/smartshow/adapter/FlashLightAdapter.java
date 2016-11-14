package com.suntrans.smartshow.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.bean.FlashlightInfo;
import com.suntrans.smartshow.views.Switch;

/**
 * Created by Looney on 2016/9/18.
 */
public class FlashLightAdapter extends  RecyclerView.Adapter <RecyclerView.ViewHolder>{

    private Context context;
    private FlashlightInfo data;

    public FlashLightAdapter(Context context, FlashlightInfo data) {
        this.context = context;
        this.data = data;
    }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            if (viewType==3){
                RecyclerView.ViewHolder holder= new viewHolder4(LayoutInflater.from(context)
                        .inflate(R.layout.meter_listview, parent,false));
                return holder;
            }else if (viewType == 0){
                RecyclerView.ViewHolder holder= new viewHolder1(LayoutInflater.from(context)
                        .inflate(R.layout.flashlightitem_title, parent,false));
                return holder;
            } else if (viewType == 1){
                RecyclerView.ViewHolder holder= new viewHolder2(LayoutInflater.from(context)
                        .inflate(R.layout.flashlightitem_switchstate, parent,false));
                return holder;
            }else {
                RecyclerView.ViewHolder holder= new viewHolder3(LayoutInflater.from(context)
                        .inflate(R.layout.flashlightitem_level, parent,false));
                return holder;
            }

        }

        /***
         * 绑定数据
         * @param holder   绑定哪个holder，用if(holder instanceof mViewHolder1)来判断类型，再绑定数据
         * @param position
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
        {
            //判断holder是哪个类，从而确定是哪种布局
            /////布局1
            if(holder instanceof viewHolder1) {
                ((viewHolder1)holder).setdata(position);
            }else if (holder instanceof  viewHolder2){
                ((viewHolder2)holder).setData(position);
                ((viewHolder2)holder).value.setOnChangeListener(new Switch.OnSwitchChangedListener() {
                    @Override
                    public void onSwitchChange(Switch switchView, boolean isChecked) {
                        mOnSwitchListener.onChanged(switchView,isChecked);
                    }
                });

            }else if (holder instanceof  viewHolder3){
                ((viewHolder3)holder).setData(position);

            }else if (holder instanceof viewHolder4){
                ((viewHolder4)holder).setData(position);
            }
        }

    public interface onSwitchListener{
        void onChanged(Switch switchView, boolean isChecked);
        void upButtonClick();
        void lowButtonClick();
    }

    public void setmOnSwitchListener(onSwitchListener mOnSwitchListener) {
        this.mOnSwitchListener = mOnSwitchListener;
    }

    private onSwitchListener mOnSwitchListener;

        @Override
        public int getItemCount()
        {
            return 15;
        }
        /**
         * 决定元素的布局使用哪种类型
         *在本activity中，布局1使用R.layout.roomgridview，
         * @param position 数据源的下标
         * @return 一个int型标志，传递给onCreateViewHolder的第二个参数 */
        @Override
        public int getItemViewType(int position) {
            if (position==0||position==3){
                return 0;
            }else if (position==1)
                return 1;
            else if (position==2){
                return 2;
            }else
                return 3;
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 用户控制 用电参数 条目
         */
        class viewHolder1 extends RecyclerView.ViewHolder
        {
            TextView name;    //名称
            public viewHolder1(View view)
            {
                super(view);
                name = (TextView) view.findViewById(R.id.name);

            }

            public void setdata(int position) {
                name.setText(position==0?"用户控制":"用电参数");
            }
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 开关状态 条目
         */
        class viewHolder2 extends RecyclerView.ViewHolder
        {
            Switch value;    //参数值
            public viewHolder2(View view)
            {
                super(view);
                value = (Switch) view.findViewById(R.id.value);
            }

            public void setData(int position) {
                    value.setState(data.isOpen());
            }
        }
    int level = 0;
    /**
     * 自定义继承RecyclerView.ViewHolder的viewholder
     * 调光等级条目
     */
    class  viewHolder3 extends RecyclerView.ViewHolder{
        TextView textView;
        Button btLose;
        Button btUp;
        public viewHolder3(View itemView) {
            super(itemView);
            btLose = (Button) itemView.findViewById(R.id.button_lose);
            btUp = (Button) itemView.findViewById(R.id.button_up);
            textView = (TextView) itemView.findViewById(R.id.text_level);
        }
        public void setData(int position){
            textView.setText(data.getGrade()+"级");
            btLose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnSwitchListener.lowButtonClick();
                }
            });
            btUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnSwitchListener.upButtonClick();
                }
            });
        }
    }

    /**
     * 其它条目
     */
    class  viewHolder4 extends RecyclerView.ViewHolder{

        ImageView image;    //图标
        TextView name;    //名称
        TextView value;    //参数值
        public viewHolder4(View view)
        {
            super(view);
            image=(ImageView)view.findViewById(R.id.image);
            name = (TextView) view.findViewById(R.id.name);
            value = (TextView) view.findViewById(R.id.value);
        }
        public void setData(int position){
            switch (position) {

                case 4:
                    image.setImageResource(R.drawable.ic_voltage);
                    name.setText("交流电压");
                    value.setText(data.getAlter_UV()+"V");
                    break;
                case 5:
                    image.setImageResource(R.drawable.ic_current);
                    name.setText("交流电流");
                    value.setText(data.getAlter_current()+"A");
                    break;
                case 6:
                    image.setImageResource(R.drawable.ic_power);
                    name.setText("交流功率");
                    value.setText(data.getAlter_rate()+"W");
                    break;
                case 7:
                    image.setImageResource(R.drawable.ic_elec);
                    name.setText("用电量");
                    value.setText(data.getElec_power()+"kWh");
                    break;
                case 8:
                    image.setImageResource(R.drawable.ic_powerrate);
                    name.setText("功率因素");
                    value.setText(data.getPower_rate()+"");
                    break;
                case 9:
                    image.setImageResource(R.drawable.ic_voltage);
                    name.setText("输出电压");
                    value.setText(data.getOut_UV()+"V");
                    break;
                case 10:
                    image.setImageResource(R.drawable.ic_current);
                    name.setText("输出电流");
                    value.setText(data.getOut_current()+"A");
                    break;
                case 11:
                    image.setImageResource(R.drawable.ic_power);
                    name.setText("输出功率");
                    value.setText(data.getOut_power()+"w");
                    break;
                case 12:
                    image.setImageResource(R.mipmap.ic_temp);
                    name.setText("电路工作温度");
                    value.setText(data.getTem()+"℃");
                    break;
                case 13:
                    image.setImageResource(R.drawable.light);
                    name.setText("光照强度");
                    value.setText(data.getLight()+" ");
                    break;
                case 14:
                    image.setImageResource(R.drawable.ic_power);
                    name.setText("能耗比");
                    value.setText( data.getK()+" ");
                    break;
                default:
                    break;
            }
        }
    }

}



