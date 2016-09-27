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
    }
    public static SharedPreferences getSharedPreferences(){
        msharedPreferences = getApplication().getSharedPreferences("config",Context.MODE_PRIVATE);
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
