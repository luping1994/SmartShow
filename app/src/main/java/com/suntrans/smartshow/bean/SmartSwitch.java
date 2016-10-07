package com.suntrans.smartshow.bean;

import android.graphics.Bitmap;

/**
 * Created by 石奋斗 on 2016/6/21.
 */
public class SmartSwitch extends Device {

    private double UValue;   //电压值
    private double IValue;   //电流值
    private double Power;   //功率
    private double PowerRate;   //功率因数
    private double OV;  //过压
    private double UV;  //欠压
    private double MaxI;   //最大电流
    private String MainState="0";    //总开关状态
    private String[] ChannelState=new String[]{"0","0","0","0","0","0","0","0","0","0","0"};   //开关状态，依次是总开关，通道1，通道2，。。。通道10
    private String[] ChannelName=new String[]{"通道1","通道2","通道3","通道4","通道5","通道6","通道7","通道8","通道9","通道10"};   //10个通道的名称
    private Bitmap[] ChannelImg=new Bitmap[]{null,null,null,null,null,null,null,null,null,null};    //10个通道的图标
    private String[] AreaName= {"","","","","","","","","",""};//各个通道所属的区域
    /**三个构造函数****/
    public SmartSwitch(){        
        this("","");


    }

    public SmartSwitch(String address){
        this(address,"");  //调用父类构造方法，一定要放在第一句

    }

    public SmartSwitch(String address, String name){
        super(address,name);  //调用父类构造方法，一定要放在第一句
        UValue=0;
        IValue=0;
        Power=0;
        PowerRate=0;
        OV=0;
        UV=0;
        MaxI=0;
    }

    public void setAreaName(int channel,String name){
        AreaName[channel] = name;
    }

    public void setAreaName(String[] names){
        AreaName = names;
    }
    public String[] getAreaName(){
        return AreaName;
    };
    public String getAreaName(int channel){
        return AreaName[channel];
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

    public double getOV() {
        return OV;
    }

    public void setOV(double OV) {
        this.OV = OV;
    }

    public double getUV() {
        return UV;
    }

    public void setUV(double UV) {
        this.UV = UV;
    }

    public double getMaxI() {
        return MaxI;
    }

    public void setMaxI(double maxI) {
        MaxI = maxI;
    }

    public String[] getChannelState() {
        return ChannelState;
    }

    /***
     * 获取某个通道的状态，1是开，0是关
     * @param channel   通道号-1，即从0开始
     * @return   通道状态
     */
    public String getChannelState(int channel){
        return ChannelState[channel];   //通道i+1的状态
    }

    public void setChannelState(String[] channelState) {
        ChannelState = channelState;
    }

    /***
     * 设置某个通道的状态
     * @param channel  （通道号-1）
     * @param state   通道状态
     */
    public void setChannelState(int channel,String state){
        ChannelState[channel]=state;
    }

    public String getMainState() {
        return MainState;
    }

    public void setMainState(String mainState) {
        MainState = mainState;
    }

    /***
     * 获取某个通道的通道名称
     * @param channel   通道号-1
     * @return    通道名称
     */
    public String getChannelName(int channel) {
        return ChannelName[channel];
    }

    public String[] getChannelName() {
        return ChannelName;
    }

    /***
     * 设置某个通道的通道名称
     * @param channel    通道号-1
     * @param channelName    通道名称
     */
    public void setChannelName(int channel,String channelName){
        ChannelName[channel]=channelName;
    }
    public void setChannelName(String[] channelName) {
        ChannelName = channelName;
    }

    /***
     * 获取通道图标
     * @param channel  通道号-1
     * @return   通道图标
     */
    public Bitmap getChannelImg(int channel){
        return ChannelImg[channel];
    }

    public Bitmap[] getChannelImg() {
        return ChannelImg;
    }

    /***
     * 设置通道图标
     * @param channel   通道号-1
     * @param channelImg    通道图标
     */
    public void setChannelImg(int channel ,Bitmap channelImg){
        ChannelImg[channel]=channelImg;
    }
    public void setChannelImg(Bitmap[] channelImg) {
        ChannelImg = channelImg;
    }

    /**
     * CRC校验计算    返回两个字节的数据，字符串形式， 高字节在前。 开关从第三个字节开始计算校验
     * @param data byte[]   字节数组
     * @param recv int    结束位置，通常为字节数组长度
     * @return String
     */
    public static String GetCRC(byte[] data, int recv)
    {
        int start=2;
        int CRC_SEED = (int) 0XFFFF;
        int CRC16Poly = (int) 0XA001;
        int CRCReg = CRC_SEED;       //int型数据32位
        String CRC="";
        for (int i = start; i < recv; i++)
        {
            CRCReg ^= (data[i]&0x000000ff);
            for (int j = 0; j < 8; j++)
            {
                if ((CRCReg & 0x0001) != 0)
                {
                    CRCReg = ((CRCReg >> 1) ^ CRC16Poly)&0xffffffff;
                }
                else
                {
                    CRCReg = (CRCReg >> 1)&0xffffffff;
                }
            }
        }
        int uper = 0xffffffff&(CRCReg % 256);   //原高八位变为低八位  存在lower中
        int lower = 0xffffffff&(CRCReg / 256);   //原低八位变为高八位  存在uper中
        //  CRCReg = (int)(uper * 256 + lower);            //得到将高八位与低八位互换的新的CRCReg
        String bh= Integer.toHexString(uper);         //获取高八位
        String bl= Integer.toHexString(lower);	     //获取低八位
        bh=bh.length()==2?bh:(bh.length()>2?bh.substring(bh.length()-2,bh.length()):("0"+bh));//将字符串长度调为2
        bl=bl.length()==2?bl:(bl.length()>2?bl.substring(bl.length()-2,bl.length()):("0"+bl));//将字符串长度调为2
        CRC+=bh+bl;   //得到校验码的最终四位字符串
        return CRC;    //返回字符串形式的校验码
    }


}
