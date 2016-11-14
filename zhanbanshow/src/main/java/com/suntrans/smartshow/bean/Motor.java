package com.suntrans.smartshow.bean;

/**
 * Created by Looney on 2016/9/30.
 */

public class Motor extends Device {
    int state;
    /**三个构造函数**/
    public Motor(){
        this("","");
    }

    public Motor(String address){
        this(address, "");
    }

    public Motor(String address, String name){
        super(address,name);

    }
}
