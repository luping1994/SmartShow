package com.suntrans.smartshow.bean;

/**
 * Created by 石奋斗 on 2016/6/26.
 */
public class SingleMeter extends Device {
    private double IValue;    //电流
    private double UValue;   //电压
    private double Power;    //功率
    private double PowerRate;  //功率因数
    private double Eletricity;   //电表读数

    public String getRSAddrOpposite() {
        return RSAddrOpposite;
    }

    public void setRSAddrOpposite(String RSAddrOpposite) {
        this.RSAddrOpposite = RSAddrOpposite;
    }

    private String RSAddrOpposite;//反向电表号

    /**三个构造函数**/
    public SingleMeter(){
        this("","");
    }

    public SingleMeter(String address){
        this(address, "");
    }

    public SingleMeter(String address, String name){
        super(address,name);
        IValue=0;
        UValue=0;
        Power=0;
        PowerRate=0;
        Eletricity=0;
    }

    public double getIValue() {
        return IValue;
    }

    public void setIValue(double IValue) {
        this.IValue = IValue;
    }

    public double getUValue() {
        return UValue;
    }

    public void setUValue(double UValue) {
        this.UValue = UValue;
    }

    public double getPower() {
        return Power;
    }

    public void setPower(double power) {
        Power = power;
    }

    public double getPowerRate() {
        return PowerRate;
    }

    public void setPowerRate(double powerRate) {
        PowerRate = powerRate;
    }

    public double getEletricity() {
        return Eletricity;
    }

    public void setEletricity(double eletricity) {
        Eletricity = eletricity;
    }


}
