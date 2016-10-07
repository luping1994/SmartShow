package com.suntrans.smartshow.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
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

import java.util.ArrayList;
import java.util.Map;

import static android.R.attr.process;
import static com.suntrans.smartshow.R.id.layout_arrow;

/**
 * Created by Looney on 2016/9/26.
 */

public class RoomConditionAdapter extends RecyclerView.Adapter {
    private final Map<String, String> map;
    private Context context;
    private SixSensor data;
    private int Pwidth=0;  //屏幕宽度，单位是pixel
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private ArrayList<Map<String, String>> data_air;
    private ArrayList<Map<String, String>> data_room;
    private ArrayList<Map<String, String>> data_posture;

    public RoomConditionAdapter(Activity context, ArrayList<Map<String, String>> air,ArrayList<Map<String, String>> room ,ArrayList<Map<String, String>> posture,Map<String,String> map) {
        this.context = context;
        this.data_air = air;
        this.data_room =room;
        this.data_posture = posture;
        this.map=map;
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);//获取屏幕大小的信息
        Pwidth=displayMetrics.widthPixels;   //屏幕宽度,先锋的宽度是800px，小米2a的宽度是720px
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            RecyclerView.ViewHolder holder = new viewHolder1(LayoutInflater.from(context)
                    .inflate(R.layout.roomcondition_item2, parent, false));
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
            ((viewHolder3)holder).setData();
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
        ImageView person;    //图标
        TextView name;    //名称
        TextView value;    //参数值
        TextView evaluate;  //标准
        LinearLayout layout_arrow;  //游标的布局
        LinearLayout layout_per;  //游标的布局

        public viewHolder1(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.standard);
            name = (TextView) view.findViewById(R.id.name);
            value = (TextView) view.findViewById(R.id.value);
            evaluate = (TextView) view.findViewById(R.id.evaluate);
            layout_arrow = (LinearLayout) view.findViewById(R.id.layout_arrow);
            person = (ImageView) view.findViewById(R.id.img_person);
            layout_per = (LinearLayout) view.findViewById(R.id.layout_person);
        }

        public void setData(int position) {
            int progress;
            switch (position) {
                case 2:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    layout_arrow.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.bg_standard);
                    name.setText("PM1");
                    value.setText(data_air.get(2).get("Value"));
                    evaluate.setText(data_air.get(2).get("Evaluate"));
                    progress =Integer.valueOf(data_air.get(2).get("Progress"));
                    setPading(progress,layout_arrow,value);
                    break;
                case 3:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    layout_arrow.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.bg_standard);
                    name.setText("PM10");
                    value.setText(data_air.get(3).get("Value"));
                    evaluate.setText(data_air.get(3).get("Evaluate"));
                    progress =Integer.valueOf(data_air.get(3).get("Progress"));
                    setPading(progress,layout_arrow,value);
                    break;
                case 4:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    layout_arrow.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.bg_standard);
                    name.setText("PM2.5");
                    value.setText(data_air.get(4).get("Value"));
                    evaluate.setText(data_air.get(4).get("Evaluate"));
                    progress =Integer.valueOf(data_air.get(4).get("Progress"));
                    setPading(progress,layout_arrow,value);
                    break;
                case 5:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    layout_arrow.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.standard_smoke);
                    name.setText("甲醛");
                    value.setText(data_air.get(1).get("Value"));
                    evaluate.setText(data_air.get(1).get("Evaluate"));
                    progress =Integer.valueOf(data_air.get(1).get("Progress"));
                    setPading(progress,layout_arrow,value);
                    break;
                case 6:
                    layout_per.setVisibility(View.GONE);
                    layout_arrow.setVisibility(View.VISIBLE);
                    image.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.standard_smoke);
                    name.setText("烟雾");
                    value.setText(data_air.get(0).get("Value"));
                    evaluate.setText(data_air.get(0).get("Evaluate"));
                    progress =Integer.valueOf(data_air.get(0).get("Progress"));
                    setPading(progress,layout_arrow,value);//根据进度设置padding
                    break;
                case 8:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    layout_arrow.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.standard_tem);
                    name.setText("温度");
                    value.setText(data_room.get(0).get("Value"));
                    evaluate.setText(data_room.get(0).get("Evaluate"));
                    progress =Integer.valueOf(data_room.get(0).get("Progress"));
                    setPading(progress,layout_arrow,value);//根据进度设置padding
                    break;
                case 9:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    layout_arrow.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.standard_humidity);
                    name.setText("湿度");
                    value.setText(data_room.get(1).get("Value"));
                    evaluate.setText(data_room.get(1).get("Evaluate"));
                    progress =Integer.valueOf(data_room.get(1).get("Progress"));
                    setPading(progress,layout_arrow,value);//根据进度设置padding
                    break;
                case 10:
                    image.setVisibility(View.VISIBLE);
                    layout_arrow.setVisibility(View.VISIBLE);
                    layout_per.setVisibility(View.GONE);
                    image.setImageResource(R.drawable.standard_humidity);
                    name.setText("气压");
                    layout_per.setVisibility(View.GONE);
                    value.setText(data_room.get(2).get("Value"));
                    evaluate.setText(data_room.get(2).get("Evaluate"));
                    progress =Integer.valueOf(data_room.get(2).get("Progress"));
                    setPading(progress,layout_arrow,value);//根据进度设置padding
                    break;
                case 11:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    layout_arrow.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.standard_light);
                    name.setText("光线强度");
                    value.setText(data_room.get(4).get("Value"));
                    evaluate.setText(data_room.get(4).get("Evaluate"));
                    progress =Integer.valueOf(data_room.get(4).get("Progress"));
                    setPading(progress,layout_arrow,value);//根据进度设置padding
                    break;
                case 12:
                    layout_per.setVisibility(View.GONE);
//                    person.setImageResource(R.drawable.person0);
                    name.setText("人员信息");
//                    value.setText(data_room.get(3).get("Value"));
                    value.setVisibility(View.GONE);
                    evaluate.setText(data_room.get(3).get("Evaluate"));
                    layout_arrow.setVisibility(View.INVISIBLE);
                    image.setVisibility(View.INVISIBLE);
                    break;
                case 14:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.INVISIBLE);
                    name.setText("X轴倾斜角");
//                    value.setText(data_posture.get(0).get("Value"));
                    layout_arrow.setVisibility(View.GONE);
                    image.setVisibility(View.GONE);
                    value.setVisibility(View.GONE);
                    evaluate.setText(data_posture.get(0).get("Value"));
                    break;
                case 15:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.INVISIBLE);
                    name.setText("Y轴倾斜角");
                    layout_arrow.setVisibility(View.GONE);
                    image.setVisibility(View.GONE);
                    value.setVisibility(View.GONE);
                    evaluate.setText(data_posture.get(1).get("Value"));
                    break;
                case 16:
                    layout_per.setVisibility(View.GONE);
                    image.setVisibility(View.INVISIBLE);
                    name.setText("水平倾斜角");
                    evaluate.setText(data_posture.get(2).get("Value"));
                    layout_arrow.setVisibility(View.INVISIBLE);
                    image.setVisibility(View.GONE);
                    value.setVisibility(View.GONE);
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

        public void setData() {
            red.setState(map.get("state_r").equals("1")?true:false);
            blue.setState(map.get("state_b").equals("1")?true:false);
            green.setState(map.get("state_g").equals("1")?true:false);

            seekbar_r.setProgress(Integer.valueOf(map.get("progress_r")));
            seekbar_g.setProgress(Integer.valueOf(map.get("progress_g")));
            seekbar_b.setProgress(Integer.valueOf(map.get("progress_b")));
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
    private void setPading(int progress,LinearLayout layout_arrow,TextView value){
        value.setVisibility(View.VISIBLE);
        layout_arrow.setPadding(Pwidth * progress / 200, 0, 0, 0);
        if(progress<50)
        {
            value.setGravity(Gravity.LEFT);
            value.setPadding(Pwidth * progress / 200, 0, 0, 0);   //设置左边距
        }
        else
        {
            value.setGravity(Gravity.RIGHT);
            value.setPadding(0, 0, Pwidth * (90 - progress) / 200, 0);  //设置右边距
        }
    }
}

