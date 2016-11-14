package com.suntrans.smartfire;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Adapter.RecyclerViewDivider;
import convert.Converts;
import services.MainService;
import views.TouchListener;

import static android.os.Build.VERSION_CODES.M;

public class Parameter_Activity extends AppCompatActivity {
    private TextView title = null;
    private LinearLayout layout_setting;   //设置按钮
    private LinearLayout layout_alert;    //报警按钮
    private SwipeRefreshLayout refreshLayout;    //下拉框架
    private RecyclerView recyclerView;    //列表
    private byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private String warning_state[]=new String[]{"1","1","1","1","1","1"};   //是否报警，依次是振动、PM2.5、甲醛、烟雾、温度，人员
    private mAdapter adapter;  //列表的适配器
    private int IsFinish=1;  //刷新是否完成
    private SoundPool soundPool;  //声音播放类
    private Map<Integer,Integer> SoundPoolMap;   //载入声音文件所对应的ID
    private String RSAddr="0000";
    private ArrayList<Map<String, String>> data = new ArrayList<>();   //数据
    public MainService.ibinder binder;  //用于Activity与Service通信
    private LinearLayout layout_back ;
    private ServiceConnection con = new ServiceConnection() {
        @Override   //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        public void onServiceConnected(ComponentName name, IBinder service) {
            //  Toast.makeText(getApplication(), "绑定成功！", Toast.LENGTH_SHORT).show();
            binder=(MainService.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
            binder.sendOrder("ab68"+RSAddr+"f0 03 01000011", MainService.SIXSENSOR);   //请求开关状态
//            Log.v("Time", "绑定后时间：" + String.valueOf(System.currentTimeMillis()));
        }

        @Override   //service因异常而断开的时候调用此方法
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplication(), "网络错误！", Toast.LENGTH_SHORT).show();

        }
    };;   ///用于绑定activity与service
    //新建广播接收器，接收服务器的数据并解析，根据第六感官的地址和开关的地址将数据转发到相应的Fragment
    private BroadcastReceiver broadcastreceiver=new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent){
            int count = intent.getIntExtra("ContentNum", 0);   //byte数组的长度
            byte[] data = intent.getByteArrayExtra("Content");  //内容数组
           /* String content = "";   //接收的字符串
            for (int i = 0; i < count; i++) {
                String s1 = Integer.toHexString((data[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                if (s1.length() == 1)
                    s1 = "0" + s1;
                content = content + s1;
            }*/
            if(count>13)   //通过handler将数据传过去
            {
                Message msg=new Message();
                msg.obj=data;
                msg.what=data.length;
                handler1.sendMessage(msg);
            }

        }
    };//广播接收器
    private boolean isDebug = true;

    private Handler handler1=new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            byte[] a1 = (byte[])msg.obj;    //byte数组a即为客户端发回的数据，aa68 0006是单个开通道，aa68 0003是所有的通道
            // String ipaddr = (String) (map.get("ipaddr"));    //开关的IP地址
            String s = "";                       //保存命令的十六进制字符串
            for (int i = 0; i < msg.what; i++) {
                String s1 = Integer.toHexString((a1[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                if (s1.length() == 1)
                    s1 = "0" + s1;
                s = s + s1;
            }
            //   String crc=Converts.GetCRC(a, 2, msg.what-2-2);    //获取返回数据的校验码，倒数第3、4位是验证码，倒数第1、2位是包尾0d0a
            s = s.replace(" ", ""); //去掉空格
            //   Log.i("Order", "收到数据：" + s);
            int IsEffective = 1;    //指令是否有效，0表示无效，1表示有效；对于和第六感官通讯而言，包头为ab68的数据才有效
//            if (msg.what > 10) {
//                if (s.substring(0, 4).equals("ab68"))
//                    IsEffective = 1;    //数据有效
//            }


            if (msg.what<=10||!s.substring(0,8).equals("ab68"+RSAddr)){
                return;
            }
            //ab 68
            // 00 0f f0 04
            // 01 00
            // 22
            // 0a 19 0e 51 01 6c 01 f2 27 2b 5a d6 00 23 00 00 00 00 00 02 00 09 00 09 ff ff 23 28 21 fc 01 2c 00 00 03 71 0d 0a
            s=s.toLowerCase();
            byte[] a = Converts.HexString2Bytes(s);
            if (IsEffective == 1)   //如果数据有效，则进行解析，并更新页面
            {
                if (s.substring(10, 12).equals("04") || s.substring(10, 12).equals("03"))   //如果是读寄存器状态，则判断是读寄存器1（室内参数），还是寄存器2（灯光信息）的状态
                {
                    if (s.substring(16, 18).equals("22") && a.length > 40)  //寄存器1，参数信息，长度34个字节
                    {
                        //计算得到各个参数的值，顺序是按寄存器顺序来的
                        double tmp_old = ((a[9] + 256) % 256) * 256 + (a[10] + 256) % 256;   //原始温度，即正常温度的100倍
                        if (tmp_old > 30000)
                            tmp_old = tmp_old - 65536;   //负温度值
                        double tmp = tmp_old / 100.0;   //温度
                        double humidity = (((a[15] + 256) % 256) * 256 + (a[16] + 256) % 256) / 10.0;   //湿度
                        while (humidity > 100)
                            humidity = humidity / 10.0;    //防止湿度出现大于100的数字
                        double atm = (((a[17] + 256) % 256) * 256 + (a[18] + 256) % 256) / 100.0;       //大气压
                        double arofene = (((a[19] + 256) % 256) * 256 + (a[20] + 256) % 256) / 1000.0;    //甲醛
                        double smoke = (((a[21] + 256) % 256) * 256 + (a[22] + 256) % 256);       //烟雾
                        double staff = (((a[23] + 256) % 256) * 256 + (a[24] + 256) % 256);     //人员信息
                        double light = (((a[25] + 256) % 256) * 256 + (a[26] + 256) % 256);  //光感
                        double pm1 = (((a[27] + 256) % 256) * 256 + (a[28] + 256) % 256);     //PM1
                        double pm25 = (((a[29] + 256) % 256) * 256 + (a[30] + 256) % 256);     //PM2.5
                        double pm10 = (((a[31] + 256) % 256) * 256 + (a[32] + 256) % 256);     //PM10
                        double xdegree = (((a[35] + 256) % 256) * 256 + (a[36] + 256) % 256) / 100.0;   //X轴角度
                        double ydegree = (((a[37] + 256) % 256) * 256 + (a[38] + 256) % 256) / 100.0;  //Y轴角度
                        double zdegree = (((a[39] + 256) % 256) * 256 + (a[40] + 256) % 256) / 100.0;   //水平角度
                        double shake = (((a[41] + 256) % 256) * 256 + (a[42] + 256) % 256);   //振动强度

                        String switch_shake = (a[34] & bits[0]) == bits[0] ? "1" : "0";     //振动报警开关状态
                        String switch_pm25 = (a[34] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5报警开关状态
                        String switch_arofene = (a[34] & bits[2]) == bits[2] ? "1" : "0";      //甲醛报警开关状态
                        String switch_smoke = (a[34] & bits[3]) == bits[3] ? "1" : "0";        //烟雾报警开关状态
                        String switch_tmp = (a[34] & bits[4]) == bits[4] ? "1" : "0";          //温度报警开关状态
                        String switch_person = (a[34] & bits[5]) == bits[5] ? "1" : "0";          //人员报警开关状态

                        data.get(5).put("Value", String.valueOf(tmp) + " ℃");  //温度值
                        data.get(6).put("Value", String.valueOf(humidity) + " %RH");  //湿度
                        data.get(7).put("Value", String.valueOf(atm) + " kPa");   //大气压
                        data.get(9).put("Value", String.valueOf(staff) + " ");     //人员信息
                        data.get(8).put("Value", String.valueOf(light) + " ");    //光感

                        data.get(4).put("Value", String.valueOf(smoke) + " ppm");    //烟雾
                        data.get(10).put("Value", String.valueOf(arofene) + " V");   //电池电压（原甲醛）
                        data.get(1).put("Value", String.valueOf(pm1) + " ");     //PM1
                        data.get(3).put("Value", String.valueOf(pm25) + " ");   //PM2.5
                        data.get(2).put("Value", String.valueOf(pm10) + " ");   //PM10

//                        data.get(0).put("Value", String.valueOf(xdegree) + " °"); //x轴角度
//                        data.get(1).put("Value", String.valueOf(ydegree) + " °");  //y轴角度
                        if (zdegree > 10) {
                            data.get(11).put("Value", String.valueOf(zdegree) + " °");   //水平角度
                            data.get(11).put("Evaluate", "倾斜");
                        }
                        else {
                            data.get(11).put("Value", String.valueOf(zdegree) + " °");   //水平角度
                            data.get(11).put("Evaluate", "正常" );
                        }
                        data.get(12).put("Value", String.valueOf(shake));//振动强度

                        warning_state[0] = switch_shake;
                        warning_state[1] = switch_pm25;
                        warning_state[2] = switch_arofene;
                        warning_state[3] = switch_smoke;
                        warning_state[4] = switch_tmp;
                        warning_state[5] = switch_person;  //人员报警开关
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
                        data.get(4).put("Evaluate", eva);     //烟雾0-10000
                        data.get(4).put("Progress", String.valueOf(progress));

                       /* eva = "null";   //评估甲醛，0-1000
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
                        data.get(1).put("Evaluate", eva);     //甲醛，假设是0-1
                        data.get(1).put("Progress", String.valueOf(progress));*/

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
                        data.get(1).put("Evaluate", eva);     //PM1
                        data.get(1).put("Progress", String.valueOf(progress));


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
                        data.get(3).put("Evaluate", eva);     //PM2.5
                        data.get(3).put("Progress", String.valueOf(progress));
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
                        data.get(2).put("Evaluate", eva);     //PM10
                        data.get(2).put("Progress", String.valueOf(progress));

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
                        data.get(5).put("Evaluate", eva);     //温度
                        data.get(5).put("Progress", String.valueOf(progress));

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
                        if (progress > 90)
                            progress = 90;
                        data.get(6).put("Evaluate", eva);     //湿度
                        data.get(6).put("Progress", String.valueOf(progress));

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
                        data.get(7).put("Evaluate", eva);     //大气压
                        data.get(7).put("Progress", String.valueOf(progress));

                        data.get(9).put("Evaluate", staff == 1 ? "有人" : "无人");     //评估人员信息

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
                        data.get(8).put("Evaluate", eva);     //光感
                        data.get(8).put("Progress", String.valueOf(progress));
                        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");   //hh为小写是12小时制，为大写HH时时24小时制
                        String date = sDateFormat.format(new    java.util.Date());
                        data.get(0).put("Value", date);
                        adapter.notifyDataSetChanged();
                    }

                } else if (s.substring(10, 12).equals("06"))   //如果是控制单个寄存器返回命令，则可能是报警信息，也可能是修改语音开关、报警开关返回的数据
                {
                    if (s.substring(12, 16).equals("0305"))   //报警信息，判断是哪个参数在报警,a[8]是高八位，a[9]是低八位
                    {
                        String str_warning = "";   //要报警的内容
                        int warning_flag=0;   //哪几个需要进行报警的，个位代表振动，十位代表PM2.5，百位代表烟雾，千位代表温度
                        int If_warning = 0;   //是否要报警
//                        if ((a[9] & bits[5]) == bits[5] && warning_state[5].equals("1"))    //人员报警，如果相等则表示该位报警
//                        {
//                            If_warning = 2;
//                            str_warning += "房间有人进入！\n";
//                        }
                        if ((a[9] & bits[0]) == bits[0] && warning_state[0].equals("1"))    //振动报警，如果相等则表示该位报警
                        {
//                            If_warning = 1;
//                            str_warning += "振动强度";
//                            warning_flag += 1;
                        }
                        if ((a[9] & bits[1]) == bits[1] && warning_state[1].equals("1"))    //PM2.5报警，如果相等则表示该位报警
                        {
//                            if (If_warning == 1)
//                                str_warning += "、PM2.5";
//                            else
//                                str_warning += "PM2.5";
//                            If_warning = 1;
//                            warning_flag+=10;
                        }
//                        if ((a[9] & bits[2]) == bits[2] && warning_state[2].equals("1"))    //甲醛报警，如果相等则表示该位报警
//                        {
//                            if (If_warning == 1)
//                                str_warning += "、甲醛";
//                            else
//                                str_warning += "甲醛";
//                            If_warning = 1;
//                        }
                        if ((a[9] & bits[3]) == bits[3] && warning_state[3].equals("1"))    //烟雾报警，如果相等则表示该位报警
                        {
                            if (If_warning == 1)
                                str_warning += "";
                            else
                                str_warning += "";
                            If_warning = 1;
                            warning_flag+=100;
                        }
                        if ((a[9] & bits[4]) == bits[4] && warning_state[4].equals("1"))    //温度报警，如果相等则表示该位报警
                        {
//                            if (If_warning == 1)
//                                str_warning += "、温度";
//                            else
//                                str_warning += "温度";
//                            If_warning = 1;
//                            warning_flag+=1000;
                        }
                        if (If_warning >= 1) {
                            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");   //hh为小写是12小时制，为大写HH时时24小时制
                            String date = sDateFormat.format(new java.util.Date());
                            if (If_warning == 1)
                                str_warning = date+" "+title_name + "发出火灾报警警告！";
                            Message msg1 = new Message();
                            msg1.obj = str_warning;
                            msg1.what=warning_flag;  //有哪几个在报警，用于语音提示
                            handler3.sendMessage(msg1);   //通知handler3进行报警
                        }
                    }
                }
            }
        }
    };

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
            builder = new AlertDialog.Builder(Parameter_Activity.this);
            LayoutInflater factory = LayoutInflater.from(Parameter_Activity.this);
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

            try {    //播放系统通知声音
                AudioManager mgr=(AudioManager) Parameter_Activity.this.getSystemService(Context.AUDIO_SERVICE);
                int volume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);   //获取系统音乐声音
                int warning_flag = msg.what;   //是哪项在报警
                if (warning_flag / 1000 > 0) { //温度
//                    soundPool.play(4,1, 1, 4, 0, 1);   //id，左声道，右声道，优先级(0最低)，是否循环（-1表示循环，其他表示循环次数），播放比率（0.5到2，一般为1，表示正常播放）
                }
                if((warning_flag%1000)/100>0) {  //烟雾
                    soundPool.play(3,1, 1, 3, 0, 1);
                }
                if((warning_flag%100)/10>0) {   //PM2.5
//                    soundPool.play(2,1, 1, 2, 0, 1);
                }
                if((warning_flag%10)/1>0) {   //振动
//                    soundPool.play(1,1,1,1,0,1);
                }

            }
            catch (Exception e){
                Log.i("Order", "播放失败！" + e.toString());
            }
            // 震动效果的系统服务
            Vibrator vibrator = (Vibrator) Parameter_Activity.this.getSystemService(Parameter_Activity.this.VIBRATOR_SERVICE);
            vibrator.hasVibrator();   //检测当前硬件是否有vibrator
            vibrator.vibrate(1500);//振动1.5秒

        }
    };
    String title_name="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameter);
        Converts.setTranslucentStatus(Parameter_Activity.this, true);   //设置状态栏透明
        Intent intent1 = getIntent();
        RSAddr= intent1.getStringExtra("RSAddr");
        title_name = intent1.getStringExtra("Name");
        title = (TextView) findViewById(R.id.title_name);
        title.setText(title_name);
        layout_setting = (LinearLayout) findViewById(R.id.layout_setting);
        layout_alert = (LinearLayout) findViewById(R.id.layout_alert);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        layout_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(Parameter_Activity.this, Setting_Activity.class);
                intent.putExtra("RSAddr",RSAddr);
                intent.putExtra("Name",title_name);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        layout_alert.setOnClickListener(new View.OnClickListener() {   //自检按钮，发送一条指令
            @Override
            public void onClick(View v) {
                String order ="ab68 "+RSAddr+"f0 06 00 03 0001";
                binder.sendOrder(order, MainService.SIXSENSOR);   //发送自检命令

            }
        });
        LinearLayoutManager lm = new LinearLayoutManager(Parameter_Activity.this);
        recyclerView.setLayoutManager(lm);   //设置布局管理器
        recyclerView.addItemDecoration(new RecyclerViewDivider(Parameter_Activity.this, LinearLayoutManager.VERTICAL));   //添加分割线
        adapter = new mAdapter();
        recyclerView.setAdapter(adapter);

        refreshLayout.setSize(SwipeRefreshLayout.LARGE);  //设置大小
        refreshLayout.setColorSchemeResources(R.color.white);   //设置滚动条颜色，可以设置多个
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.bg_action);  //设置背景颜色，可以设置多个
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                IsFinish=0;   //表示开始刷新
                new GetDataTask().execute();   //执行刷新任务
            }
        });
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                new GetDataTask().execute();
            }
        });
        DataInit();
        //初始化声音播放器，加载声音文件
        soundPool= new SoundPool(5,AudioManager.STREAM_SYSTEM,0);  //支持的声音数量，声音类型，声音品质
        SoundPoolMap = new HashMap<>();
        SoundPoolMap.put(1, soundPool.load(Parameter_Activity.this,R.raw.voice_shake,1));   //加载振动报警文件
        SoundPoolMap.put(2, soundPool.load(Parameter_Activity.this,R.raw.voice_pm,1));   //加载PM2.5报警文件
        SoundPoolMap.put(3, soundPool.load(Parameter_Activity.this,R.raw.voice_fire,1));   //加载烟雾报警文件
        SoundPoolMap.put(4, soundPool.load(Parameter_Activity.this,R.raw.voice_tmp,1));   //加载温度报警文件

        //绑定MainService
        Intent intent = new Intent(getApplicationContext(), MainService.class);    //指定要绑定的service
        bindService(intent, con, Context.BIND_AUTO_CREATE);   //绑定主service
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive
        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        soundPool.release();
        unbindService(con);   //解除Service的绑定
        unregisterReceiver(broadcastreceiver);  //注销广播接收者
    }
    private void DataInit(){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");   //hh为小写是12小时制，为大写HH时时24小时制
        String date = sDateFormat.format(new    java.util.Date());
        Map<String, String> map0 = new HashMap<String, String>();
        map0.put("Name", "时间");
        map0.put("Value", date);  //当前时间
        map0.put("Title","");  //此栏是否需要显示标题,=-1表示不需要，否则，就填写标题的名称
        map0.put("Image", String.valueOf(R.mipmap.time));   //时间图标
        data.add(map0);

        Map<String,String> map7=new HashMap<String,String>();
        map7.put("Name", "PM1");
        map7.put("Value", "null");
        map7.put("Evaluate", "null");
        map7.put("Title", "");
        map7.put("Image", String.valueOf(R.mipmap.pm));
        data.add(map7);
        Map<String,String> map9=new HashMap<String,String>();
        map9.put("Name", "PM10");
        map9.put("Value", "null");
        map9.put("Evaluate", "null");
        map9.put("Title", "-1");
        map9.put("Image", String.valueOf(R.mipmap.pm));
        data.add(map9);
        Map<String,String> map8=new HashMap<String,String>();
        map8.put("Name", "PM2.5");
        map8.put("Value", "null");
        map8.put("Evaluate", "null");
        map8.put("Title", "-1");
        map8.put("Image", String.valueOf(R.mipmap.pm));
        data.add(map8);
        Map<String,String> map5=new HashMap<String,String>();
        map5.put("Name", "烟雾");
        map5.put("Value", "null ppm");
        map5.put("Evaluate", "null");
        map5.put("Title", "-1");
        map5.put("Image", String.valueOf(R.mipmap.smoke));  //烟雾图标
        data.add(map5);



        Map<String,String> map1=new HashMap<String,String>();
        map1.put("Name", "温度");     //参数名称
        map1.put("Value", "null ℃");         //值
        map1.put("Evaluate", "null");    //评估，冷、热等
        map1.put("Title","");
        map1.put("Image", String.valueOf(R.mipmap.tmp));  //温度图标
        data.add(map1);
        Map<String,String> map2=new HashMap<String,String>();
        map2.put("Name", "湿度");
        map2.put("Value", "null %RH");
        map2.put("Evaluate", "null");
        map2.put("Title", "-1");
        map2.put("Image", String.valueOf(R.mipmap.humidity));    //湿度图标
        data.add(map2);
        Map<String,String> map3=new HashMap<String,String>();
        map3.put("Name", "大气压");
        map3.put("Value", "null kPa");
        map3.put("Evaluate", "null");
        map3.put("Title", "-1");
        map3.put("Image", String.valueOf(R.mipmap.atm));    //大气压图标
        data.add(map3);
        Map<String,String> map6=new HashMap<String,String>();
        map6.put("Name", "光线强度");
        map6.put("Value", "null");
        map6.put("Evaluate", "null");
        map6.put("Title", "-1");
        map6.put("Image", String.valueOf(R.mipmap.light));   //光线强度图标
        data.add(map6);
        Map<String,String> map10=new HashMap<String,String>();
        map10.put("Name", "人员信息");
        map10.put("Value", "0");
        map10.put("Evaluate", "null");
        map10.put("Title", "-1");
        map10.put("Image", String.valueOf(R.mipmap.people));   //人员信息图标
        data.add(map10);


        Map<String,String> map4=new HashMap<String,String>();
        map4.put("Name", "电池电压");
        map4.put("Value", "null V");
        map4.put("Title", "");
        map4.put("Image", String.valueOf(R.mipmap.voltage));    //电池电压
        data.add(map4);
        Map<String, String> map32 = new HashMap<>();
        map32.put("Name", "平面倾斜角");
        map32.put("Value", "null");
        map32.put("Evaluate", "null");
        map32.put("Title","-1");
        map32.put("Image", String.valueOf(R.mipmap.angle));   //角度图标
        data.add(map32);
        Map<String, String> map33 = new HashMap<>();
        map33.put("Name", "振动强度");
        map33.put("Value", "null");
        map33.put("Title", "-1");
        map33.put("Image", String.valueOf(R.mipmap.shake));   //振动强度图标
        data.add(map33);

    }
    ///下拉刷新处理的函数。
    private class GetDataTask extends AsyncTask<Void, Void, String> {
        // 后台处理部分
        @Override
        protected String doInBackground(Void... params) {
            // Simulates a background job.
            String str = "1";
            try {
                Thread.sleep(500);
                binder.sendOrder("ab68"+RSAddr+"f0 03 0100 0011", MainService.SIXSENSOR);   //请求开关状态
                str = "1"; // 表示请求成功
            } catch (InterruptedException e1) {

                e1.printStackTrace();
                str = "0"; // 表示请求失败
            }
            return str;
        }

        //这里是对刷新的响应，可以利用addFirst（）和addLast()函数将新加的内容加到LISTView中
        //根据AsyncTask的原理，onPostExecute里的result的值就是doInBackground()的返回值
        @Override
        protected void onPostExecute(String result) {

            if(result.equals("1"))  //请求数据成功，根据显示的页面重新初始化listview
            {

            }
            else //请求数据失败
            {
                Toast.makeText(getApplicationContext(), "刷新失败！", Toast.LENGTH_SHORT).show();
            }
            // Call onRefreshComplete when the list has been refreshed.
            refreshLayout.setRefreshing(false);   //结束加载动作
            super.onPostExecute(result);//这句是必有的，AsyncTask规定的格式
        }
    }

    /**
     * RecyclerView适配器
     **自定义Recyclerview的适配器,主要的执行顺序：getItemViewType==>onCreateViewHolder==>onBindViewHolder
     */
    class mAdapter extends RecyclerView.Adapter{
        /****
         * 渲染具体的布局，根据viewType选择使用哪种布局
         * @param parent   父容器
         * @param viewType    布局类别，多种布局的情况定义多个viewholder
         * @return
         */
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            RecyclerView.ViewHolder holder= new viewHolder1(LayoutInflater.from(
                    Parameter_Activity.this).inflate(R.layout.elecinfo_listview, parent,false));

            return holder;
        }

        /***
         * 绑定数据
         * @param holder   绑定哪个holder，用if(holder instanceof mViewHolder1)来判断类型，再绑定数据
         * @param position
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position)
        {
            //判断holder是哪个类，从而确定是哪种布局
            /////布局1
            if(holder instanceof viewHolder1) {
                viewHolder1 viewholder = (viewHolder1) holder;
                Map<String, String> map = data.get(position);
                String Name = map.get("Name");
                String Value = map.get("Value");
                String Evaluate = map.get("Evaluate");
                String Title = map.get("Title");
                if(Evaluate!=null)
                    Value = Value + "(" + Evaluate + ")";
                if(Title.equals("-1"))
                    viewholder.list_header.setVisibility(View.GONE);
                else{
                    viewholder.list_header.setVisibility(View.VISIBLE);
                    viewholder.list_header.setText(Title);
                }

                Bitmap bitmap = BitmapFactory.decodeResource(Parameter_Activity.this.getResources(), Integer.valueOf(map.get("Image")));
                viewholder.image.setImageBitmap(bitmap);
                viewholder.name.setText(Name);
                viewholder.value.setText(Value);
            }
        }



        @Override
        public int getItemCount()
        {
            return data.size();
        }
        /**
         * 决定元素的布局使用哪种类型
         *在本activity中，布局1使用R.layout.roomgridview，
         * @param position 数据源的下标
         * @return 一个int型标志，传递给onCreateViewHolder的第二个参数 */
        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 布局类型1对应的ViewHolder，R.layout.listmain_userinfo
         */
        class viewHolder1 extends RecyclerView.ViewHolder
        {
            LinearLayout layout;   //整体布局
            TextView list_header;   //小标题
            ImageView image;    //图标
            TextView name;    //名称
            TextView value;    //参数值
            public viewHolder1(View view)
            {
                super(view);
                layout=(LinearLayout)view.findViewById(R.id.layout);
                list_header = (TextView) view.findViewById(R.id.list_header);
                image=(ImageView)view.findViewById(R.id.image);
                name = (TextView) view.findViewById(R.id.name);
                value = (TextView) view.findViewById(R.id.value);
            }
        }
    }
}
