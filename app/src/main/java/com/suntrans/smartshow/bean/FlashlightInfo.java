package com.suntrans.smartshow.bean;

import static com.suntrans.smartshow.R.id.state;

/**
 * Created by Looney on 2016/9/26.
 */

public class FlashlightInfo {
    private double alter_UV;//交流电压
    private double alter_current;//交流功率
    private double alter_rate;//交流功率
    private double elec_power; //总用电量
    private double power_rate;//功率因素
    private double out_UV;//输出电压
    private double out_current;//输出功率
    private double out_power;//输出功率
    private double tem;//温度
    private double light;//光照强度
    private double k;//能耗比

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    private boolean open;//总开关状态
    private int grade;//光照等级

    public FlashlightInfo() {
       open = true;
    }

    public double getAlter_UV() {
        return alter_UV;
    }

    public void setAlter_UV(double alter_UV) {
        this.alter_UV = alter_UV;
    }

    public double getAlter_current() {
        return alter_current;
    }

    public void setAlter_current(double alter_current) {
        this.alter_current = alter_current;
    }

    public double getAlter_rate() {
        return alter_rate;
    }

    public void setAlter_rate(double alter_rate) {
        this.alter_rate = alter_rate;
    }

    public double getElec_power() {
        return elec_power;
    }

    public void setElec_power(double elec_power) {
        this.elec_power = elec_power;
    }

    public double getPower_rate() {
        return power_rate;
    }

    public void setPower_rate(double power_rate) {
        this.power_rate = power_rate;
    }

    public double getOut_UV() {
        return out_UV;
    }

    public void setOut_UV(double out_UV) {
        this.out_UV = out_UV;
    }

    public double getOut_current() {
        return out_current;
    }

    public void setOut_current(double out_current) {
        this.out_current = out_current;
    }

    public double getOut_power() {
        return out_power;
    }

    public void setOut_power(double out_power) {
        this.out_power = out_power;
    }

    public double getTem() {
        return tem;
    }

    public void setTem(double tem) {
        this.tem = tem;
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public double getK() {
        return k;
    }

    public void setK(double k) {
        this.k = k;
    }



    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }
}
