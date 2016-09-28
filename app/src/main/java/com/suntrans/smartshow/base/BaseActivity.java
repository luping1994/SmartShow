package com.suntrans.smartshow.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;


import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;


/**
 * Created by Looney on 2016/8/9.
 */
public abstract  class BaseActivity extends RxAppCompatActivity {

    public MainService1.ibinder binder;  //用于Activity与Service通信

    private ServiceConnection con = new ServiceConnection() {
        //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("绑定成功");
            binder=(MainService1.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
//            binder.sendOrder(addr+"f003 000e",4);
            //    Log.v("Time", "绑定后时间：" + String.valueOf(System.currentTimeMillis()));
        }

        @Override   //service因异常而断开的时候调用此方法
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplication(), "网络错误！", Toast.LENGTH_SHORT).show();

        }
    };   ///用于绑定activity与service
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
        initData();
    }

    @Override
    protected void onStart() {
        Intent intent = new Intent(getApplicationContext(), MainService1.class);    //指定要绑定的service
        this.bindService(intent, con, Context.BIND_AUTO_CREATE);   //绑定主service
        super.onStart();
    }

    @Override
    protected void onPause() {
        LogUtil.i("解除绑定成功");
        unbindService(con);   //解除Service的绑定
        super.onPause();
    }

    public abstract void initViews(Bundle savedInstanceState);
    public abstract void initToolBar();
    public abstract int getLayoutId();
    public abstract void initData();


}
