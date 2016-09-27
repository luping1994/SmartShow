package com.suntrans.smartshow.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.bean.SixSensor;
import com.suntrans.smartshow.views.Switch;

/**
 * Created by Looney on 2016/9/26.
 */

public class RoomConditionAdapter extends RecyclerView.Adapter {
    private Context context;
    private SixSensor data;

    public RoomConditionAdapter(Context context, SixSensor data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            RecyclerView.ViewHolder holder = new viewHolder1(LayoutInflater.from(context)
                    .inflate(R.layout.roomcondition_item1, parent, false));
            return holder;
        }else if (viewType==0){

            RecyclerView.ViewHolder holder = new viewHolder2(LayoutInflater.from(context)
                    .inflate(R.layout.meter_listview_time, parent, false));
            return holder;
        }else if (viewType==2){

            RecyclerView.ViewHolder holder = new viewHolder3(LayoutInflater.from(context)
                    .inflate(R.layout.parameter_lights, parent, false));
            return holder;
        }else
            return new viewHolder4(LayoutInflater.from(context)
                    .inflate(R.layout.list_header, parent, false));
    }

    @Override
    public void onBindViewHolder (RecyclerView.ViewHolder holder,int position){
        if (holder instanceof viewHolder2){
            ((viewHolder2)holder).value.setText("2016/9/26");
        }else if(holder instanceof viewHolder1) {
            ((viewHolder1)holder).setData(position);
        }else if (holder instanceof  viewHolder3){
            ((viewHolder3)holder).setListener();
        }else
            ((viewHolder4)holder).setData(position);

    }

    @Override
    public int getItemCount () {
        return 19;
    }

    @Override
    public int getItemViewType ( int position){
        if (position == 0) {
            return 0;
        } else if (position==18){
            return 2;
        }else if (position==1||position==7||position==17||position==13){
            return 3;
        }
        return 1;
    }

    /**
     * 具体参数的条目ViewHolder
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
                case 2:
                    image.setImageResource(R.mipmap.ic_pm);
                    name.setText("PM1");
                    value.setText(data==null?"null":data.getPm1()+"");
                    break;
                case 3:
                    image.setImageResource(R.mipmap.ic_pm);
                    name.setText("PM10");
                    value.setText(data==null?"null":data.getPm10()+"");
                    break;
                case 4:
                    image.setImageResource(R.mipmap.ic_pm);
                    name.setText("PM2.5");
                    value.setText(data==null?"null":data.getPm25()+"");
                    break;
                case 5:
                    image.setImageResource(R.drawable.ic_power);
                    name.setText("甲醛");
                    value.setText(data==null?"null":data.getArofene()+"");
                    break;
                case 6:
                    image.setImageResource(R.mipmap.ic_smoke);
                    name.setText("烟雾");
                    value.setText(data==null?"null":data.getSmoke()+"");
                    break;
                case 8:
                    image.setImageResource(R.mipmap.ic_temp);
                    name.setText("温度");
                    value.setText(data==null?"null":data.getTmp()+"");
                    break;
                case 9:
                    image.setImageResource(R.mipmap.ic_humidity);
                    name.setText("湿度");
                    value.setText(data==null?"null":data.getHumidity()+"");
                    break;
                case 10:
                    image.setImageResource(R.mipmap.ic_atm);
                    name.setText("气压");
                    value.setText(data==null?"null":data.getAtm()+"");
                    break;
                case 11:
                    image.setImageResource(R.mipmap.ic_lightintensity);
                    name.setText("光线强度");
                    value.setText(data==null?"null":data.getLight()+"");
                    break;
                case 12:
                    image.setImageResource(R.mipmap.ic_peopleinfo);
                    name.setText("人员信息");
                    value.setText(data==null?"null":data.getStaff()+"");
                    break;
                case 14:
                    image.setImageResource(R.mipmap.ic_gradient);
                    name.setText("X轴倾斜角");
                    value.setText(data==null?"null":data.getXdegree()+"");
                    break;
                case 15:
                    image.setImageResource(R.mipmap.ic_gradient);
                    name.setText("Y轴倾斜角");
                    value.setText(data==null?"null":data.getYdegree()+"");
                    break;
                case 16:
                    image.setImageResource(R.mipmap.ic_gradient);
                    name.setText("Z轴倾斜角");
                    value.setText(data==null?"null":data.getZdegree()+"");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 时间ViewHolder
     */
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

    public onLightControlListener getmLightControlListener() {
        return mLightControlListener;
    }

    public void setLightControlListener(onLightControlListener mLightControlListener) {
        this.mLightControlListener = mLightControlListener;
    }

    private onLightControlListener mLightControlListener;
    /**
     * 灯光控制布局ViewHolder
     */
    class viewHolder3 extends RecyclerView.ViewHolder implements Switch.OnSwitchChangedListener, SeekBar.OnSeekBarChangeListener {
        Switch red;
        Switch green;
        Switch blue;
        private SeekBar seekbar_r,seekbar_g,seekbar_b;   //外间三个滚动条，红绿蓝

        public viewHolder3(View view)
        {
            super(view);
            red = (Switch) view.findViewById(R.id.switch_r);
            green = (Switch) view.findViewById(R.id.switch_g);
            blue = (Switch) view.findViewById(R.id.switch_b);

            seekbar_r=(SeekBar)view.findViewById(R.id.seekbar_r);   //红灯滚动条
            seekbar_g=(SeekBar)view.findViewById(R.id.seekbar_g);   //绿灯滚动条
            seekbar_b=(SeekBar)view.findViewById(R.id.seekbar_b);   //蓝灯滚动条
        }

        public void setListener(){
            red.setOnChangeListener(this);
            green.setOnChangeListener(this);
            blue.setOnChangeListener(this);

            seekbar_b.setOnSeekBarChangeListener(this);
            seekbar_g.setOnSeekBarChangeListener(this);
            seekbar_r.setOnSeekBarChangeListener(this);

        }

        @Override
        public void onSwitchChange(Switch switchView, boolean isChecked) {
            mLightControlListener.onSwitchChange(switchView,isChecked);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mLightControlListener.onSeekBarChangedListener(seekBar,progress,fromUser);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    /**
     * 小标题ViewHolder
     */
    class viewHolder4 extends RecyclerView.ViewHolder
    {
        TextView textView;   //整体布局

        public viewHolder4(View view)
        {
            super(view);
            textView = (TextView) view.findViewById(R.id.list_header_title);
        }
        public void setData(int position){
            if (position==1)
                textView.setText("空气质量");
            else if (position==7)
                textView.setText("室内环境");
            else if (position==13)
                textView.setText("姿态信息");
            else
                textView.setText("灯光控制");
        }

    }

    /**
     * 监听接口
     */
    public interface onLightControlListener{

        /**
         * 三个RGB开关监听
         * @param switchView
         * @param isChecked
         */
        void onSwitchChange(Switch switchView, boolean isChecked);

        /**
         * 三个进度条状态监听
         * @param seekBar
         * @param progress
         * @param fromUser
         */
        void onSeekBarChangedListener(SeekBar seekBar, int progress, boolean fromUser);
    }
}

