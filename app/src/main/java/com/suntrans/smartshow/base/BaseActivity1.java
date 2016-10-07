package com.suntrans.smartshow.base;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;


import com.suntrans.smartshow.R;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.utils.SystemBarTintManager;


/**
 * Created by Looney on 2016/8/9.
 */
public abstract  class BaseActivity1 extends AppCompatActivity {
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
