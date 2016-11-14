package com.suntrans.smartshow.utils;

import android.os.Message;
import android.support.v7.widget.RecyclerView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.bean.SingleMeter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Looney on 2016/10/24.
 */

public abstract class SuntransUtils {
    private byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private String warning_state[]=new String[]{"1","1","1","1","1","1"};   //是否报警，依次是振动、PM2.5、甲醛、烟雾、温度，人员
    private static int SIXSENSOR=1;
    private static int SWITCH=2;
    private static int SINGLEMETER=3;


    public void parseData(int device,byte[] data1,String RSAddr, ArrayList<Map<String, Object>> data, RecyclerView.Adapter adapter){

        String s = Converts.Bytes2HexString(data1);
        s=s.toLowerCase();
        byte[] a = Converts.HexString2Bytes(s);
        if (device== SIXSENSOR){
            if (s.length()<=20||!s.substring(0,8).equals("ab68"+RSAddr)){
                return;
            }
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

//                    sixSensorData(tmp,humidity,atm,arofene,smoke,staff,light,pm1,pm10,pm25,xdegree,ydegree,zdegree,shake);


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
                    if ((a[9] & bits[5]) == bits[5] && warning_state[5].equals("1"))    //人员报警，如果相等则表示该位报警
                    {
                        If_warning = 2;
                        str_warning += "房间有人进入！\n";
                    }
                    if ((a[9] & bits[0]) == bits[0] && warning_state[0].equals("1"))    //振动报警，如果相等则表示该位报警
                    {
                            If_warning = 1;
                            str_warning += "振动强度";
                            warning_flag += 1;
                    }
                    if ((a[9] & bits[1]) == bits[1] && warning_state[1].equals("1"))    //PM2.5报警，如果相等则表示该位报警
                    {
                            if (If_warning == 1)
                                str_warning += "、PM2.5";
                            else
                                str_warning += "PM2.5";
                            If_warning = 1;
                            warning_flag+=10;
                    }
                    if ((a[9] & bits[2]) == bits[2] && warning_state[2].equals("1"))    //甲醛报警，如果相等则表示该位报警
                    {
                        if (If_warning == 1)
                            str_warning += "、甲醛";
                        else
                            str_warning += "甲醛";
                        If_warning = 1;
                    }
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
                            if (If_warning == 1)
                                str_warning += "、温度";
                            else
                                str_warning += "温度";
                            If_warning = 1;
                            warning_flag+=1000;
                    }
                    if (If_warning >= 1) {
                        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");   //hh为小写是12小时制，为大写HH时时24小时制
                        String date = sDateFormat.format(new java.util.Date());
                        if (If_warning == 1)
                            str_warning = date+" " + "发出火灾报警警告！";
                        onWarnning(str_warning,date);
//                        Message msg1 = new Message();
//                        msg1.obj = str_warning;
//                        msg1.what=warning_flag;  //有哪几个在报警，用于语音提示
//                        handler3.sendMessage(msg1);   //通知handler3进行报警
                    }
                }
            }
        }
    }

    public abstract void onWarnning(String warningStr,String time);
    public abstract void sixSensorData();
//    public abstract void
}
