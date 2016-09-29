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
        if (frist){
            getSharedPreferences().edit().putBoolean("isFristCome",false).commit();
        }else {
            getSharedPreferences().edit().putString("sixIpAddress","192.168.1.235");
            getSharedPreferences().edit().putInt("sixPort",8000);

            getSharedPreferences().edit().putString("chunkouIpAddress","192.168.1.213");
            getSharedPreferences().edit().putInt("chunkouPort",8000);
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
