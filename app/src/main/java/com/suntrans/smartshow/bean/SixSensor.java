package com.suntrans.smartshow.bean;

/**
 * Created by 石奋斗 on 2016/6/23.
 */
public class SixSensor extends Device {

    private double tmp;   //温度
    private double humidity;   //湿度
    private double atm;       //大气压
    private double arofene;    //甲醛
    private double smoke;       //烟雾
    private int staff;     //人员信息
    private int light;  //光感强度
    private double pm1;     //PM1
    private double pm25;     //PM2.5
    private double pm10;     //PM10
    private double xdegree;   //X轴角度
    private double ydegree;  //Y轴角度
    private double zdegree;   //水平角度
    private double shake;   //振动强度

    private int progress_r;    //红灯的亮度，0-255。0最暗，255最亮
    private int progress_g;    //绿灯亮度
    private int progress_b;      //蓝灯的亮度，0-255
    private int state_r;     //红灯的开关状态

    private int state_g;    //绿灯的开关状态
    private int state_b;    //蓝灯的开关状态，0表示关，1表示开

    private String switch_shake;     //振动报警开关状态，“1”代表开，“0”代表关
    private String switch_pm25;        //PM2.5报警开关状态
    private String switch_arofene;      //甲醛报警开关状态
    private String switch_smoke;        //烟雾报警开关状态
    private String switch_tmp;          //温度报警开关状态
    private String switch_person;    //人员报警开关状态

    /**三个构造参数***/
    public SixSensor() {
        this("", "");
    }

    public SixSensor(String address) {
        this(address, "");
    }

    public SixSensor(String address, String name) {
        super(address, name);
        tmp=0;
        humidity=0;
        atm=0;
        arofene=0;
        smoke=0;
        staff=0;
        light=0;
        pm1=0;
        pm25=0;
        pm10=0;
        xdegree=0;
        ydegree=0;
        zdegree=0;
        shake=0;
        state_r=0;
        state_g=0;
        state_b=0;
        progress_r=0;
        progress_g=0;
        progress_b=0;
        switch_shake="0";
        switch_pm25="0";
        switch_arofene = "0";
        switch_smoke = "0";
        switch_tmp = "0";
        switch_person = "0";
    }


    public double getTmp() {
        return tmp;
    }

    public void setTmp(double tmp) {
        this.tmp = tmp;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getAtm() {
        return atm;
    }

    public void setAtm(double atm) {
        this.atm = atm;
    }

    public double getArofene() {
        return arofene;
    }

    public void setArofene(double arofene) {
        this.arofene = arofene;
    }

    public double getSmoke() {
        return smoke;
    }

    public void setSmoke(double smoke) {
        this.smoke = smoke;
    }

    public int getStaff() {
        return staff;
    }

    public void setStaff(int staff) {
        this.staff = staff;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public double getPm1() {
        return pm1;
    }

    public void setPm1(double pm1) {
        this.pm1 = pm1;
    }

    public double getPm25() {
        return pm25;
    }

    public void setPm25(double pm25) {
        this.pm25 = pm25;
    }

    public double getPm10() {
        return pm10;
    }

    public void setPm10(double pm10) {
        this.pm10 = pm10;
    }

    public double getXdegree() {
        return xdegree;
    }

    public void setXdegree(double xdegree) {
        this.xdegree = xdegree;
    }

    public double getYdegree() {
        return ydegree;
    }

    public void setYdegree(double ydegree) {
        this.ydegree = ydegree;
    }

    public double getZdegree() {
        return zdegree;
    }

    public void setZdegree(double zdegree) {
        this.zdegree = zdegree;
    }

    public double getShake() {
        return shake;
    }

    public void setShake(double shake) {
        this.shake = shake;
    }

    public int getProgress_r() {
        return progress_r;
    }

    public void setProgress_r(int progress_r) {
        this.progress_r = progress_r;
    }

    public int getProgress_g() {
        return progress_g;
    }

    public void setProgress_g(int progress_g) {
        this.progress_g = progress_g;
    }

    public int getProgress_b() {
        return progress_b;
    }

    public void setProgress_b(int progress_b) {
        this.progress_b = progress_b;
    }

    public int getState_r() {
        return state_r;
    }

    public void setState_r(int state_r) {
        this.state_r = state_r;
    }

    public int getState_g() {
        return state_g;
    }

    public void setState_g(int state_g) {
        this.state_g = state_g;
    }

    public int getState_b() {
        return state_b;
    }

    public void setState_b(int state_b) {
        this.state_b = state_b;
    }

    public String getSwitch_shake() {
        return switch_shake;
    }

    public void setSwitch_shake(String switch_shake) {
        this.switch_shake = switch_shake;
    }

    public String getSwitch_pm25() {
        return switch_pm25;
    }

    public void setSwitch_pm25(String switch_pm25) {
        this.switch_pm25 = switch_pm25;
    }

    public String getSwitch_arofene() {
        return switch_arofene;
    }

    public void setSwitch_arofene(String switch_arofene) {
        this.switch_arofene = switch_arofene;
    }

    public String getSwitch_smoke() {
        return switch_smoke;
    }

    public void setSwitch_smoke(String switch_smoke) {
        this.switch_smoke = switch_smoke;
    }

    public String getSwitch_tmp() {
        return switch_tmp;
    }

    public void setSwitch_tmp(String switch_tmp) {
        this.switch_tmp = switch_tmp;
    }

    public String getSwitch_person() {
        return switch_person;
    }

    public void setSwitch_person(String switch_person) {
        this.switch_person = switch_person;
    }
}


