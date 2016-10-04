package com.suntrans.smartshow.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.Convert.ParseSixSensor;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.activity.Main_Activity;
import com.suntrans.smartshow.activity.Smartroom_Activity;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.adapter.RoomConditionAdapter;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.bean.SixSensor;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.ThreadManager;
import com.suntrans.smartshow.utils.UiUtils;
import com.suntrans.smartshow.views.Switch;
import com.suntrans.smartshow.views.TouchListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.suntrans.smartshow.R.drawable.light;
import static com.suntrans.smartshow.R.id.layout;
import static com.suntrans.smartshow.R.id.seekbar_b;
import static com.suntrans.smartshow.R.id.seekbar_g;
import static com.suntrans.smartshow.R.id.seekbar_r;
import static com.suntrans.smartshow.R.id.switch_b;
import static com.suntrans.smartshow.R.id.switch_g;
import static com.suntrans.smartshow.R.id.switch_r;
import static com.suntrans.smartshow.utils.LogUtil.i;

/**
 * 智能家居页面中第二个Fragment,显示第六感的状态信息
 * Created by Looney on 2016/9/26.
 */
public class RoomConditionFragment extends BaseFragment {
    private SixSensor data ;
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private RoomConditionAdapter adapter;

    String addr ="0001";
    private ArrayList<Map<String, String>> data_room=new ArrayList<Map<String, String>>();    //室内环境
    private ArrayList<Map<String, String>> data_air=new ArrayList<Map<String, String>>();    //空气质量
    private ArrayList<Map<String, String>> data_posture = new ArrayList<>();   //姿态信息
    private byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private String warning_state[]=new String[]{"1","1","1","1","1","1"};   //外间是否报警，依次是振动、PM2.5、甲醛、烟雾、温度，人员
    private int progress_r,progress_g,progress_b;      //外间三个滚动条的进度，0-255
    private int state_r,state_g,state_b;    //外间三个灯的开关状态，0表示关，1表示开
    private Map<String,String> map_state = new HashMap<>();
    Handler handler = new Handler();
    private  boolean isVisible =false;
    private  boolean isRefresh = true;//是否刷新
    @Override
    public int getLayoutId() {
        return R.layout.roomcondition_fragment;
    }
    //从activity收命令消息
    public Handler handler1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Map<String, Object> map = (Map<String, Object>) msg.obj;
            byte[] bytes = (byte[]) (map.get("data"));
            String s = Converts.Bytes2HexString(bytes);
            s = s.replace(" ", ""); //去掉空格
            String[] single_str = s.split("0d0a");
            String result = single_str[0] + "0d0a";
            System.out.println("Fuck you!收到结果为==>" + result);
            byte [] a = Converts.HexString2Bytes(result);
            for (int i = 0; i < msg.what; i++) {
                String s1 = Integer.toHexString((a[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                if (s1.length() == 1)
                    s1 = "0" + s1;
                s = s + s1;
            }
            //   String crc=Converts.GetCRC(a, 2, msg.what-2-2);    //获取返回数据的校验码，倒数第3、4位是验证码，倒数第1、2位是包尾0d0a
            s = s.replace(" ", ""); //去掉空格
            //   Log.i("Order", "收到数据：" + s);
            int IsEffective = 1;    //指令是否有效，0表示无效，1表示有效；对于和第六感官通讯而言，包头为ab68的数据才有效

            if (IsEffective == 1)   //如果数据有效，则进行解析，并更新页面，外间
            {
                if (s.substring(10, 12).equals("04")||s.substring(10,12).equals("03"))   //如果是读寄存器状态，则判断是读寄存器1（室内参数），还是寄存器2（灯光信息）的状态
                {
                    if (s.substring(12, 14).equals("22")&&a.length>40)  //寄存器1，参数信息，长度34个字节
                    {
                        //计算得到各个参数的值，顺序是按寄存器顺序来的
                        double tmp_old=((a[7] + 256) % 256) * 256 + (a[8] + 256) % 256;   //原始温度，即正常温度的100倍
                        if(tmp_old>30000)
                            tmp_old=tmp_old-65536;   //负温度值
                        double tmp = tmp_old/ 100.0;   //温度
                        double humidity = (((a[13] + 256) % 256) * 256 + (a[14] + 256) % 256) /10.0;   //湿度
                        while(humidity>100)
                            humidity=humidity/10.0;    //防止湿度出现大于100的数字
                        double atm = (((a[15] + 256) % 256) * 256 + (a[16] + 256) % 256) / 100.0;       //大气压
                        double arofene = (((a[17] + 256) % 256) * 256 + (a[18] + 256) % 256) / 1000.0;    //甲醛
                        double smoke = (((a[19] + 256) % 256) * 256 + (a[20] + 256) % 256);       //烟雾
                        double staff = (((a[21] + 256) % 256) * 256 + (a[22] + 256) % 256);     //人员信息
                        double light = (((a[23] + 256) % 256) * 256 + (a[24] + 256) % 256);  //光感
                        double pm1 = (((a[25] + 256) % 256) * 256 + (a[26] + 256) % 256);     //PM1
                        double pm25 = (((a[27] + 256) % 256) * 256 + (a[28] + 256) % 256);     //PM2.5
                        double pm10 = (((a[29] + 256) % 256) * 256 + (a[30] + 256) % 256);     //PM10
                        double xdegree= (((a[33] + 256) % 256) * 256 + (a[34] + 256) % 256)/100.0;   //X轴角度
                        double ydegree= (((a[35] + 256) % 256) * 256 + (a[36] + 256) % 256)/100.0;  //Y轴角度
                        double zdegree= (((a[37] + 256) % 256) * 256 + (a[38] + 256) % 256)/100.0;   //水平角度
                        double shake=(((a[39] + 256) % 256) * 256 + (a[40] + 256) % 256);   //振动强度

                        String switch_shake = (a[32] & bits[0]) == bits[0] ? "1" : "0";     //振动报警开关状态
                        String switch_pm25 = (a[32] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5报警开关状态
                        String switch_arofene = (a[32] & bits[2]) == bits[2] ? "1" : "0";      //甲醛报警开关状态
                        String switch_smoke = (a[32] & bits[3]) == bits[3] ? "1" : "0";        //烟雾报警开关状态
                        String switch_tmp = (a[32] & bits[4]) == bits[4] ? "1" : "0";          //温度报警开关状态
                        String switch_person = (a[32] & bits[5]) == bits[5] ? "1" : "0";          //人员报警开关状态

                        data_room.get(0).put("Value", String.valueOf(tmp) + " ℃");  //温度值
                        data_room.get(1).put("Value", String.valueOf(humidity) + " %RH");  //湿度
                        data_room.get(2).put("Value", String.valueOf(atm) + " kPa");   //大气压
                        data_room.get(3).put("Value", String.valueOf(staff) + " ");     //人员信息
                        data_room.get(4).put("Value", String.valueOf(light) + " ");    //光感

                        data_air.get(0).put("Value", String.valueOf(smoke) + " ppm");    //烟雾
                        data_air.get(1).put("Value", String.valueOf(arofene) + " ppm");   //甲醛
                        data_air.get(2).put("Value", String.valueOf(pm1) + " ");     //PM1
                        data_air.get(3).put("Value", String.valueOf(pm25) + " ");   //PM2.5
                        data_air.get(4).put("Value", String.valueOf(pm10) + " ");   //PM10

                        data_posture.get(0).put("Value", String.valueOf(xdegree) + " °"); //x轴角度
                        data_posture.get(1).put("Value", String.valueOf(ydegree) + " °");  //y轴角度
                        if(zdegree>10)
                            data_posture.get(2).put("Value", String.valueOf(zdegree) + " °(倾斜)");   //水平角度
                        else
                            data_posture.get(2).put("Value", String.valueOf(zdegree) + " °(正常)");   //水平角度
                        data_posture.get(3).put("Value", String.valueOf(shake) );//振动强度

                        warning_state[0]=switch_shake;
                        warning_state[1]=switch_pm25;
                        warning_state[2]=switch_arofene;
                        warning_state[3]=switch_smoke;
                        warning_state[4]=switch_tmp;
                        warning_state[5]=switch_person;  //人员报警开关
                        //评估，空气质量部分
                        String eva = "null";//评估，优、良、轻度污染、中度污染、重度污染、严重污染
                        int progress = 0;//进度
                        if (smoke <= 750) {
                            eva = "清洁";
                            progress = (int) (smoke / 750 * 100 / 6);
                        } else {
                            eva = "污染";
                            progress = (int) (100 / 6 + (smoke - 750) * 500 / 9250 / 6);
                        }
                        data_air.get(0).put("Evaluate", eva);     //烟雾0-10000
                        data_air.get(0).put("Progress", String.valueOf(progress));

                        eva = "null";   //评估甲醛，0-1000
                        progress = 0;
                        if (arofene <= 0.1) {
                            eva = "清洁";
                            progress = (int) (arofene / 0.1 * 100 / 6);
                        } else {
                            eva = "超标";
                            progress = (int) (100 / 6 + (arofene - 0.1) * 500 / 6);
                            if (progress >= 80)
                                progress = 80;
                        }
                        data_air.get(1).put("Evaluate", eva);     //甲醛，假设是0-1
                        data_air.get(1).put("Progress", String.valueOf(progress));

                        eva = "null";
                        progress = 0;
                        //评估,PM1
                        if (pm1 <= 35) {
                            eva = "优";
                            progress = (int) (pm1 / 35 / 6 * 100);
                        } else if (pm1 <= 75) {
                            eva = "良";
                            progress = (int) ((pm1 - 35) / 240 * 100 + 100 / 6);
                        } else if (pm1 <= 115) {
                            eva = "轻度污染";
                            progress = (int) ((pm1 - 75) / 240 * 100 + 200 / 6);
                        } else if (pm1 <= 150) {
                            eva = "中度污染";
                            progress = (int) ((pm1 - 115) / 35 / 6 * 100 + 300 / 6);
                        } else if (pm1 <= 250) {
                            eva = "重度污染";
                            progress = (int) ((pm1 - 150) / 6 + 400 / 6);
                        } else {
                            eva = "严重污染";
                            progress = 90;
                        }
                        data_air.get(2).put("Evaluate", eva);     //PM1
                        data_air.get(2).put("Progress", String.valueOf(progress));


                        eva = "null";  //评估,PM2.5
                        progress = 0;
                        if (pm25 <= 35) {
                            eva = "优";
                            progress = (int) (pm25 / 35 / 6 * 100);
                        } else if (pm25 <= 75) {
                            eva = "良";
                            progress = (int) ((pm25 - 35) / 240 * 100 + 100 / 6);
                        } else if (pm25 <= 115) {
                            eva = "轻度污染";
                            progress = (int) ((pm25 - 75) / 240 * 100 + 200 / 6);
                        } else if (pm25 <= 150) {
                            eva = "中度污染";
                            progress = (int) ((pm25 - 115) / 35 / 6 * 100 + 300 / 6);
                        } else if (pm25 <= 250) {
                            eva = "重度污染";
                            progress = (int) ((pm25 - 150) / 6 + 400 / 6);
                        } else {
                            eva = "严重污染";
                            progress = 90;
                        }
                        data_air.get(3).put("Evaluate", eva);     //PM2.5
                        data_air.get(3).put("Progress", String.valueOf(progress));
                        // Log.i("Order","计算得到："+String.valueOf(progress));

                        eva = "null";  //评估,PM10
                        progress = 0;
                        if (pm10 <= 50) {
                            eva = "优";
                            progress = (int) (pm10 / 50 * 100 / 6);
                        } else if (pm10 <= 150) {
                            eva = "良";
                            progress = (int) ((pm10 - 50) / 6 + 100 / 6);
                        } else if (pm10 <= 250) {
                            eva = "轻度污染";
                            progress = (int) ((pm10 - 150) / 6 + 200 / 6);
                        } else if (pm10 <= 350) {
                            eva = "中度污染";
                            progress = (int) ((pm10 - 250) / 6 + 300 / 6);
                        } else if (pm10 <= 420) {
                            eva = "重度污染";
                            progress = (int) ((pm10 - 350) / 420 + 400 / 6);
                        } else {
                            eva = "严重污染";
                            progress = 90;
                        }
                        data_air.get(4).put("Evaluate", eva);     //PM10
                        data_air.get(4).put("Progress", String.valueOf(progress));


                        //评估，室内信息部分

                        eva = "null";    //评估温度
                        progress = 0;
                        if (tmp <= 10) {
                            eva = "极寒";
                            progress = 50 / 6;
                        } else if (tmp <= 15) {
                            eva = "寒冷";
                            progress = (int) ((tmp - 10) / 5 * 100 / 6 + 100 / 6);
                        } else if (tmp <= 20) {
                            eva = "凉爽";
                            progress = (int) ((tmp - 15) / 5 * 100 / 6 + 200 / 6);
                        } else if (tmp <= 28) {
                            eva = "舒适";
                            progress = (int) ((tmp - 20) / 8 * 100 / 6 + 300 / 6);
                        } else if (tmp <= 34) {
                            eva = "闷热";
                            progress = (int) ((tmp - 28) / 6 * 100 / 6 + 400 / 6);
                        } else {
                            eva = "极热";
                            progress = 550 / 6;
                        }
                        data_room.get(0).put("Evaluate", eva);     //温度
                        data_room.get(0).put("Progress", String.valueOf(progress));

                        eva = "null";   //评估湿度
                        progress = 0;
                        if (humidity <= 40) {
                            eva = "干燥";
                            progress = (int) (humidity / 40.0 * 100 / 3.0);
                        } else if (humidity <= 70) {
                            eva = "舒适";
                            progress = (int) ((humidity - 40) / 30.0 * 100 / 3.0 + 100 / 3.0);
                        } else {
                            eva = "潮湿";
                            progress = (int) ((humidity - 70) / 30.0 * 100 / 3.0 + 200 / 3.0);
                        }
                        if(progress>90)
                            progress=90;
                        data_room.get(1).put("Evaluate", eva);     //湿度
                        data_room.get(1).put("Progress", String.valueOf(progress));

                        eva = "null";  //评估气压
                        progress = 0;
                        if (atm >= 110) {
                            eva = "气压高";
                            progress = 80;
                        } else if (atm <= 90) {
                            eva = "气压低";
                            progress = 20;
                        } else {
                            eva = "正常";
                            progress = 50;
                        }
                        data_room.get(2).put("Evaluate", eva);     //大气压
                        data_room.get(2).put("Progress", String.valueOf(progress));

                        data_room.get(3).put("Evaluate", staff == 1 ? "有人" : "无人");     //评估人员信息

                        eva = "null";   //评估光感
                        progress = 0;
                        if (light == 0) {
                            eva = "极弱";
                            progress = 10;
                        } else if (light == 1) {
                            eva = "适中";
                            progress = 30;
                        } else if (light == 2) {
                            eva = "强";
                            progress = 50;
                        } else if (light == 3) {
                            eva = "很强";
                            progress = 70;
                        } else {
                            eva = "极强";
                            progress = 90;
                        }
                        data_room.get(4).put("Evaluate", eva);     //光感
                        data_room.get(4).put("Progress", String.valueOf(progress));
                        UiUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (refreshLayout.isRefreshing())
                                refreshLayout.setRefreshing(false);
                                adapter.notifyDataSetChanged();
                            }
                        });

                    }
                    else if(s.substring(12,14).equals("0C"))  //寄存器2，灯光信息，长度12个字节
                    {
//                   String s1 = "ab 68 00 01 f0 03 0c 00 01 00 45 00 01 00 45 00 01 00 4c 25 61 0d 0a";
                        state_r=a[8]&0xff;    //红灯开关状态
                        state_g=a[12]&0xff;     //绿灯开关状态
                        state_b=a[16]&0xff;    //蓝灯开关状态
                        progress_r=a[10]&0xff;     //红灯pwm大小
                        progress_g=a[14]&0xff;     //绿灯PWM
                        progress_b=a[18]&0xff;    //蓝灯PWM
                        //  ListInit();

                        map_state.put("state_r", String.valueOf(state_r));
                        map_state.put("state_g", String.valueOf(state_g));
                        map_state.put("state_b", String.valueOf(state_b));

                        map_state.put("progress_r", String.valueOf(progress_r));
                        map_state.put("progress_g", String.valueOf(progress_g));
                        map_state.put("progress_b", String.valueOf(progress_b));
                        System.out.println("红灯状态是"+state_r+"绿灯灯状态是"+state_b+"蓝灯状态是"+state_b+"进度条："+progress_b+" "+progress_g+" "+progress_r);
                        UiUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });

                }
                else if(s.substring(10,12).equals("06"))   //如果是控制单个寄存器返回命令，则可能是报警信息，也可能是修改语音开关、报警开关返回的数据
                {
                    if(s.substring(12,16).equals("0305"))   //报警信息，判断是哪个参数在报警,a[8]是高八位，a[9]是低八位
                    {
                        String str_warning="";   //要报警的内容
                        int If_warning=0  ;   //是否要报警
                        if((a[9]&bits[5])==bits[5]&&warning_state[5].equals("1"))    //人员报警，如果相等则表示该位报警
                        {
                            If_warning=2;
                            str_warning+="房间有人进入！\n";
                        }
                        if((a[9]&bits[0])==bits[0]&&warning_state[0].equals("1"))    //振动报警，如果相等则表示该位报警
                        {
                            If_warning=1;
                            str_warning+="振动强度";
                        }
                        if((a[9]&bits[1])==bits[1]&&warning_state[1].equals("1"))    //PM2.5报警，如果相等则表示该位报警
                        {
                            if(If_warning==1)
                                str_warning+="、PM2.5";
                            else
                                str_warning += "PM2.5";
                            If_warning=1;
                        }
                        if((a[9]&bits[2])==bits[2]&&warning_state[2].equals("1"))    //甲醛报警，如果相等则表示该位报警
                        {
                            if(If_warning==1)
                                str_warning+="、甲醛";
                            else
                                str_warning += "甲醛";
                            If_warning=1;
                        }
                        if((a[9]&bits[3])==bits[3]&&warning_state[3].equals("1"))    //烟雾报警，如果相等则表示该位报警
                        {
                            if(If_warning==1)
                                str_warning+="、烟雾";
                            else
                                str_warning += "烟雾";
                            If_warning=1;
                        }
                        if((a[9]&bits[4])==bits[4]&&warning_state[4].equals("1"))    //温度报警，如果相等则表示该位报警
                        {
                            if(If_warning==1)
                                str_warning+="、温度";
                            else
                                str_warning += "温度";
                            If_warning=1;
                        }
                        if(If_warning>=1) {

                            if(If_warning==1)
                                str_warning = str_warning + "超过预警值！";
                            Message msg1 = new Message();
                            msg1.obj=str_warning;
                            handler3.sendMessage(msg1);   //通知handler3进行报警
                        }

                    }
                    else if(s.substring(12, 16).equals("0200"))  //地址为0200，是红灯开关
                    {
                        state_r=(s.substring(19, 20).equals("1"))?1:0;
                        map_state.put("state_r", String.valueOf(state_r));
                    }
                    else if(s.substring(12, 16).equals("0202"))  //地址为0202，是绿灯开关
                    {

                        state_g=(s.substring(19, 20).equals("0"))?0:1;
                        map_state.put("state_g", String.valueOf(state_g));

                        //ListInit();
                    }
                    else if(s.substring(12, 16).equals("0204"))  //地址为0204，是蓝灯开关
                    {

                        state_b=(s.substring(19, 20).equals("1"))?1:0;
                        map_state.put("state_b", String.valueOf(state_b));


                    }
                    UiUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                }
            }

        }
        }
    };


    @Override
    public void initViews() {
        data =  new SixSensor();
        initData();
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        adapter = new RoomConditionAdapter(getActivity(),data_air,data_room,data_posture,map_state);
        //给灯光控制模块设置点击监听
        adapter.setLightControlListener(new RoomConditionAdapter.onLightControlListener() {
            @Override
            public void onSwitchChange(Switch switchView, boolean isChecked) {
                switch (switchView.getId()){
                    case switch_r:
                        ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor("ab68"+addr+"f006 0200 000" + (isChecked ? "1" : "0"), 4);
                        break;
                    case switch_g:
                        ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor("ab68"+addr+"f006 0202 000" + (isChecked ? "1" : "0"), 4);
                        break;
                    case switch_b:
                        ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor("ab68"+addr+"f006 0204 000" + (isChecked ? "1" : "0"), 4);

                        break;
                }
            }

            @Override
            public void onSeekBarChangedListener(SeekBar seekBar, int progress, boolean fromUser) {
                switch (seekBar.getId()){
                    case seekbar_r:
                        map_state.put("progress_r", String.valueOf(progress));
                        byte byt=(byte)progress;
                        String str=Converts.Bytes2HexString(new byte[]{byt});
                        ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor("ab68"+ addr+"f006 0201 00" + str, 4);
                        break;
                    case seekbar_g:
                        map_state.put("progress_g", String.valueOf(progress));
                        byte byt1=(byte)progress;
                        String str1=Converts.Bytes2HexString(new byte[]{byt1});
                        ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor("ab68"+ addr+"f006 0203 00" + str1, 4);
                        break;
                    case seekbar_b:
                        map_state.put("progress_b", String.valueOf(progress));
                        byte byt2=(byte)progress;
                        String str2=Converts.Bytes2HexString(new byte[]{byt2});
                        ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor("ab68"+ addr+"f006 0205 00" + str2, 4);
                        break;
                }
            }
        });

        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new RecyclerViewDivider(getActivity(), LinearLayoutManager.VERTICAL));  //设置分割线
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String order="ab68"+addr+"f003 0100 0011";
                ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor(order,4);
                String order1 = "ab68"+addr+"f003 0200 0006";
                ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor(order1,4);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                            UiUtils.showToast(UiUtils.getContext(),"刷新失败，请重试！");
                        }
                    }
                },2000);
            }
        });
        refreshLayout.setColorSchemeResources(android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_light);


        ThreadManager.getInstance().createLongPool().execute(new Runnable() {
            @Override
            public void run() {
                while (isRefresh){
                    boolean run = true;
                    while (run){
                        if (((Smartroom_Activity)getActivity()).binder!=null){
                            ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor(order,4);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String order1 = "ab68"+addr+"f003 0200 0006";
                            ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor(order1,4);
                        }
                        run=false;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initData() {
        //室内环境，5个参数：温度、湿度、大气压、人员信息、光线强度
        Map<String,String> map1=new HashMap<String,String>();
        map1.put("Name", "温度");     //参数名称
        map1.put("Value", "null ℃");         //值
        map1.put("Evaluate", "null");    //评估，冷、热等
        map1.put("Progress", "0");
        data_room.add(map1);

        Map<String,String> map2=new HashMap<String,String>();
        map2.put("Name", "湿度");
        map2.put("Value", "null %RH");
        map2.put("Evaluate", "null");
        map2.put("Progress", "0");
        data_room.add(map2);

        Map<String,String> map3=new HashMap<String,String>();
        map3.put("Name", "大气压");
        map3.put("Value", "null kPa");
        map3.put("Evaluate", "null");
        map3.put("Progress", "0");
        data_room.add(map3);


        Map<String,String> map10=new HashMap<String,String>();
        map10.put("Name", "人员信息");
        map10.put("Value", "0");
        map10.put("Evaluate", "null");
        map10.put("Progress", "0");
        data_room.add(map10);

        Map<String,String> map6=new HashMap<String,String>();
        map6.put("Name", "光线强度");
        map6.put("Value", "null");
        map6.put("Evaluate", "null");
        map6.put("Progress", "0");
        data_room.add(map6);


        //环境质量，5个参数：甲醛、烟雾、PM1，PM2.5，PM10
        Map<String,String> map5=new HashMap<String,String>();
        map5.put("Name", "烟雾");
        map5.put("Value", "null ppm");
        map5.put("Evaluate", "null");
        map5.put("Progress", "0");
        data_air.add(map5);

        Map<String,String> map4=new HashMap<String,String>();
        map4.put("Name", "甲醛");
        map4.put("Value", "null ppm");
        map4.put("Evaluate", "null");
        map4.put("Progress", "0");    //占颜色标准条的比例
        data_air.add(map4);

        Map<String,String> map7=new HashMap<String,String>();
        map7.put("Name", "PM1");
        map7.put("Value", "null");
        map7.put("Evaluate", "null");
        map7.put("Progress", "0");
        data_air.add(map7);

        Map<String,String> map8=new HashMap<String,String>();
        map8.put("Name", "PM2.5");
        map8.put("Value", "null");
        map8.put("Evaluate", "null");
        map8.put("Progress", "0");
        data_air.add(map8);

        Map<String,String> map9=new HashMap<String,String>();
        map9.put("Name", "PM10");
        map9.put("Value", "null");
        map9.put("Evaluate", "null");
        map9.put("Progress", "0");
        data_air.add(map9);

        //  姿态信息4个参数，x轴角度，y轴角度，水平角度，振动强度
        Map<String, String> map30 = new HashMap<>();
        map30.put("Name", "X轴角度");
        map30.put("Value", "null");
        data_posture.add(map30);

        Map<String, String> map31 = new HashMap<>();
        map31.put("Name", "y轴角度");
        map31.put("Value", "null");
        data_posture.add(map31);

        Map<String, String> map32 = new HashMap<>();
        map32.put("Name", "水平夹角");
        map32.put("Value", "null");
        data_posture.add(map32);

        Map<String, String> map33 = new HashMap<>();
        map33.put("Name", "振动强度等级");
        map33.put("Value", "null");
        data_posture.add(map33);

        map_state.put("state_r","0");
        map_state.put("state_g","0");
        map_state.put("state_b","0");
        map_state.put("progress_r","0");
        map_state.put("progress_g","0");
        map_state.put("progress_b","0");

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser){
            isRefresh = true;
            isVisible = true;
            refreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(true);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (refreshLayout.isRefreshing()){
                                refreshLayout.setRefreshing(false);
                                UiUtils.showToast(UiUtils.getContext(),"刷新失败，请重试！");
                            }
                        }
                    },2000);
                }
            });
            new Thread(){
                @Override
                public void run() {
                    ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor(order,4);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String order1 = "ab68"+addr+"f003 0200 0006";
                    ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor(order1,4);
                }
            }.start();
        }else {
            isRefresh = false;
            isVisible = false;
        }
    }

    @Override
    public void onDestroy() {
        isRefresh = false;
        isVisible = false;
        super.onDestroy();
    }
    String order="ab68"+addr+"f003 0100 0011";
    @Override
    protected void parseObtainedMsg(byte[] bytes) {
    }
    public void  getAllParameter(){
        boolean run = true;
        while (run){
            if (((Smartroom_Activity)getActivity()).binder!=null){
                ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor(order,4);
                ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor("ab68"+addr+"f003 0200 0006",4);
            }
            run=false;
        }

    }
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    //handler3用于显示报警
    Handler handler3=new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String str_warning=msg.obj.toString();
            if(dialog!=null)
            {
                try {
                    dialog.dismiss();
                }
                catch(Exception ex){ex.printStackTrace();}
            }
            builder = new AlertDialog.Builder(getActivity());
            LayoutInflater factory = LayoutInflater.from(getActivity());
            final View view = factory.inflate(R.layout.warning, null);
            TextView tx_warning = (TextView) view.findViewById(R.id.tx_warning);   //报警信息
            tx_warning.setText(str_warning);
            builder.setCancelable(true);
            builder.setView(view);
            dialog=builder.create();
            dialog.show();
            Button button = (Button) view.findViewById(R.id.button);
            button.setOnTouchListener(new TouchListener());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dialog != null)
                        dialog.dismiss();
                }
            });
            // 震动效果的系统服务
            Vibrator vibrator = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
            vibrator.vibrate(2000);//振动两秒
            try {    //播放系统通知声音
                Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer player = new MediaPlayer();
                player.setDataSource(getActivity(), alert);
                final AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
                    player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                    player.setLooping(false);   //是否循环播放
                    player.prepare();   //准备音频文件，相当于缓存
                    player.start();      //开始播放
                }
            }
            catch (IOException e){}

        }
    };
}
