package com.suntrans.smartshow.bean;

/**
 * Created by 石奋斗 on 2016/6/22.
 */
public abstract class Device {


    private String Address;   //设备地址，开关地址长度是8，第六感地址长度是4，电表地址长度是12
    private String Name;   //设备名称

    //构造器，无参数
    public Device(){
        Address = "";
        Name="";
    }

    public Device(String address){
        Address=address;
        Name="";
    }

    public Device(String address, String name){
        Address=address;
        Name=name;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }


}
