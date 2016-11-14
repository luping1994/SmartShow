package com.suntrans.smartshow.bean;

/**
 * Created by 石奋斗 on 2016/6/26.
 */
public class ThreeMeter extends  Device {

    private double IA;   //A相电流
    private double IB;   //B相电流
    private double IC;    //C相电流
    private double UA;   //A相电压
    private double UB;     //B相电压
    private double UC;   //C相电压
    private double ActivePower;   //有功功率
    private double ReactivePower;    //无功功率
    private double PowerRate;   //功率因数
    private double Electricity;  //电表读数

    public String getRSAddrOpposite() {
        return RSAddrOpposite;
    }

    public void setRSAddrOpposite(String RSAddrOpposite) {
        this.RSAddrOpposite = RSAddrOpposite;
    }

    private String RSAddrOpposite;   //反向电表号

    /***三个构造参数***/
    public ThreeMeter(){

    }

    public ThreeMeter(String address){

    }

    public ThreeMeter(String address, String name){
        super(address,name);
        IA=0;
        IB=0;
        IC=0;
        UA=0;
        UB=0;
        UC=0;
        ActivePower=0;
        ReactivePower=0;
        PowerRate=0;
        Electricity=0;
    }

    public double getIA() {
        return IA;
    }

    public void setIA(double IA) {
        this.IA = IA;
    }

    public double getIB() {
        return IB;
    }

    public void setIB(double IB) {
        this.IB = IB;
    }

    public double getIC() {
        return IC;
    }

    public void setIC(double IC) {
        this.IC = IC;
    }

    public double getUA() {
        return UA;
    }

    public void setUA(double UA) {
        this.UA = UA;
    }

    public double getUB() {
        return UB;
    }

    public void setUB(double UB) {
        this.UB = UB;
    }

    public double getUC() {
        return UC;
    }

    public void setUC(double UC) {
        this.UC = UC;
    }

    public double getActivePower() {
        return ActivePower;
    }

    public void setActivePower(double activePower) {
        ActivePower = activePower;
    }

    public double getReactivePower() {
        return ReactivePower;
    }

    public void setReactivePower(double reactivePower) {
        ReactivePower = reactivePower;
    }

    public double getPowerRate() {
        return PowerRate;
    }

    public void setPowerRate(double powerRate) {
        PowerRate = powerRate;
    }

    public double getElectricity() {
        return Electricity;
    }

    public void setElectricity(double electricity) {
        Electricity = electricity;
    }


}
