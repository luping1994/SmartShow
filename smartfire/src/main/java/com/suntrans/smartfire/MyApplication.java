package com.suntrans.smartfire;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by Looney on 2016/10/18.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(this, "900057204", true);
    }
}
