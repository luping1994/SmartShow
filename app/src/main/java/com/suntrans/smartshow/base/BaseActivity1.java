package com.suntrans.smartshow.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;


import com.suntrans.smartshow.service.MainService;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Looney on 2016/8/9.
 */
public abstract  class BaseActivity1 extends RxAppCompatActivity {
 ///用于绑定activity与service
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this,Color.TRANSPARENT);//设置状态栏为透明颜色
        setContentView(getLayoutId());
        //初始化控件
        initViews(savedInstanceState);
        //初始化ToolBar
        initToolBar();

    }

    @Override
    protected void onStart() {

        initData();
        super.onStart();
    }

    @Override
    protected void onPause() {
        LogUtil.i("解除绑定成功");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public abstract void initViews(Bundle savedInstanceState);
    public abstract void initToolBar();
    public abstract int getLayoutId();
    public abstract void initData();


}
