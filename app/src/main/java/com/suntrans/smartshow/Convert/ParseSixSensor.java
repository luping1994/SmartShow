package com.suntrans.smartshow.Convert;


import com.suntrans.smartshow.bean.SixSensor;

import static com.suntrans.smartshow.R.id.switch_g;

/**
 * Created by Looney on 2016/9/18.
 */
public class ParseSixSensor {
    private static byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private static String warning_state[]=new String[]{"1","1","1","1","1","1"};   //外间是否报警，依次是振动、PM2.5、甲醛、烟雾、温度，人员
    /**
     * 解析第六感回传的数据
     * @param result
     */
    public static final SixSensor parseData(String result) {

        String[] split = result.split("0d0a");
        byte[] a = Converts.HexString2Bytes(split[0]);
//		 String s = "";                       //保存命令的十六进制字符串
//         for (int i = 0; i < a.length; i++) {
//             String s1 = Integer.toHexString((a[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
//             if (s1.length() == 1)
//                 s1 = "0" + s1;
//             s = s + s1;
//         }
        String s = split[0];
        if (s.substring(10, 12).equals("04")||s.substring(10,12).equals("03"))   //如果是读寄存器状态，则判断是读寄存器1（室内参数），还是寄存器2（灯光信息）的状态
        {
            if (s.substring(12, 14).equals("22")&&a.length>40)  //寄存器1，参数信息，长度34个字节
            {
                double atm = (((a[15] + 256) % 256) * 256 + (a[16] + 256) % 256) / 100.0;
                double tmp_old=((a[7] + 256) % 256) * 256 + (a[8] + 256) % 256;   //原始温度，即正常温度的100倍
                if(tmp_old>30000)
                    tmp_old=tmp_old-65536;   //负温度值
                double tmp = tmp_old/ 100.0;   //温度
                double arofene = (((a[17] + 256) % 256) * 256 + (a[18] + 256) % 256) / 1000.0;    //甲醛
                double smoke = (((a[19] + 256) % 256) * 256 + (a[20] + 256) % 256);       //烟雾
                int staff = (((a[21] + 256) % 256) * 256 + (a[22] + 256) % 256);     //人员信息
                int light = (((a[23] + 256) % 256) * 256 + (a[24] + 256) % 256);  //光感
                double pm1 = (((a[25] + 256) % 256) * 256 + (a[26] + 256) % 256);     //PM1
                double pm25 = (((a[27] + 256) % 256) * 256 + (a[28] + 256) % 256);     //PM2.5
                double pm10 = (((a[29] + 256) % 256) * 256 + (a[30] + 256) % 256);     //PM10
                double xdegree= (((a[33] + 256) % 256) * 256 + (a[34] + 256) % 256)/100.0;   //X轴角度
                double ydegree= (((a[35] + 256) % 256) * 256 + (a[36] + 256) % 256)/100.0;  //Y轴角度
                double zdegree= (((a[37] + 256) % 256) * 256 + (a[38] + 256) % 256)/100.0;   //水平角度
                double shake=(((a[39] + 256) % 256) * 256 + (a[40] + 256) % 256);   //振动强度
                double humidity = (((a[13] + 256) % 256) * 256 + (a[14] + 256) % 256) /10.0;   //湿度
                while(humidity>100)
                    humidity=humidity/10.0;    //防止湿度出现大于100的数字
//
//                System.out.println("温度 为 "+tmp+"℃");
//                System.out.println("大气压 "+atm+"kPa");
//                System.out.println("甲醛 "+arofene+"ppm");
//                System.out.println("烟雾 "+smoke+"ppm");
//                System.out.println("大气压 "+atm+"kPa");
//                System.out.println("人员信息 "+staff+"");
//                System.out.println("水平角度"+zdegree+"°");

                String switch_shake = (a[32] & bits[0]) == bits[0] ? "1" : "0";     //振动报警开关状态
                String switch_pm25 = (a[32] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5报警开关状态
                String switch_arofene = (a[32] & bits[2]) == bits[2] ? "1" : "0";      //甲醛报警开关状态
                String switch_smoke = (a[32] & bits[3]) == bits[3] ? "1" : "0";        //烟雾报警开关状态
                String switch_tmp = (a[32] & bits[4]) == bits[4] ? "1" : "0";          //温度报警开关状态
                String switch_person = (a[32] & bits[5]) == bits[5] ? "1" : "0";          //人员报警开关状态

                SixSensor sixSensorInfo = new SixSensor();

                sixSensorInfo.setAtm(atm);
                sixSensorInfo.setTmp(tmp);
                sixSensorInfo.setArofene(arofene);
                sixSensorInfo.setSmoke(smoke);
                sixSensorInfo.setStaff(staff);
                sixSensorInfo.setLight(light);
                sixSensorInfo.setPm1(pm1);
                sixSensorInfo.setPm25(pm25);
                sixSensorInfo.setPm10(pm10);
                sixSensorInfo.setXdegree(xdegree);
                sixSensorInfo.setYdegree(ydegree);
                sixSensorInfo.setZdegree(zdegree);
                sixSensorInfo.setShake(shake);
                sixSensorInfo.setHumidity(humidity);

                sixSensorInfo.setSwitch_shake(switch_shake);
                sixSensorInfo.setSwitch_pm25(switch_pm25);
                sixSensorInfo.setSwitch_arofene(switch_arofene);
                sixSensorInfo.setSwitch_smoke(switch_smoke);
                sixSensorInfo.setSwitch_shake(switch_shake);
                sixSensorInfo.setSwitch_tmp(switch_tmp);
                sixSensorInfo.setSwitch_person(switch_person);
                return sixSensorInfo;

            }
            else if(s.substring(12,14).equals("0c"))  //寄存器2，灯光信息，长度12个字节
            {
                int state_r,state_g,state_b;    //外间三个灯的开关状态，0表示关，1表示开
                int progress_r,progress_g,progress_b;      //外间三个滚动条的进度，0-255

                state_r=a[8]&0xff;    //红灯开关状态
                state_g=a[12]&0xff;     //绿灯开关状态
                state_b=a[16]&0xff;    //蓝灯开关状态
                progress_r=a[10]&0xff;     //红灯pwm大小
                progress_g=a[14]&0xff;     //绿灯PWM
                progress_b=a[18]&0xff;    //蓝灯PWM
                SixSensor info = new SixSensor();
                info.setState_r(state_r);
                info.setState_g(state_g);
                info.setState_b(state_b);
                info.setProgress_b(progress_b);
                info.setProgress_r(progress_r);
                info.setProgress_g(progress_g);
                return info;
            }
            return null;
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
//                    if(If_warning==1)
//                        str_warning = str_warning + "超过预警值！";
//                    Message msg1 = new Message();
//                    msg1.obj=str_warning;
//                    handler3.sendMessage(msg1);   //通知handler3进行报警
                }else if(s.substring(12, 16).equals("0200"))  //地址为0200，是红灯开关
                {   int state_r,state_g,state_b;    //外间三个灯的开关状态，0表示关，1表示开
                    state_g=(s.substring(19, 20).equals("0"))?0:1;
                }
                else if(s.substring(12, 16).equals("0202"))  //地址为0202，是绿灯开关
                {
//                    state_g=(s.substring(19, 20).equals("0"))?0:1;
                }
                else if(s.substring(12, 16).equals("0204"))  //地址为0204，是蓝灯开关
                {
//                     state_b=(s.substring(19, 20).equals("1"))?1:0;
                }
            }
        }
        return new SixSensor();
    }
}
