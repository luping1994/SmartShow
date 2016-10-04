package com.suntrans.smartshow.base;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

/**
 * Created by Looney on 2016/8/11.
 * Des:代表当前应用.
 */
public class BaseApplication extends Application {
    private static BaseApplication application;
    private static int mainTid;
    private static Handler mHandler;
    private static SharedPreferences msharedPreferences;
    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
        mainTid=android.os.Process.myTid();
        mHandler=new Handler();
        boolean frist = getSharedPreferences().getBoolean("isFristCome",true);
        //假如是第一次启动app则保存ip地址默认为内网ip
        if (frist){
            getSharedPreferences().edit().putBoolean("isFristCome",false).commit();
            getSharedPreferences().edit().putString("sixIpAddress","192.168.1.4").commit();
            getSharedPreferences().edit().putInt("sixPort",2000).commit();

            getSharedPreferences().edit().putString("chunkouIpAddress","192.168.1.213").commit();
            getSharedPreferences().edit().putInt("chunkouPort",8000).commit();
        }
    }
    public static SharedPreferences getSharedPreferences(){
        if (msharedPreferences==null){
            msharedPreferences = getApplication().getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        return msharedPreferences;
    }
    public static Context getApplication() {
        return application;
    }

    public static int getMainTid() {
        return mainTid;
    }
    public static Handler getHandler() {
        return mHandler;
    }


}
