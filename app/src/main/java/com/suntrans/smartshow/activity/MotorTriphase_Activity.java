package com.suntrans.smartshow.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.print.PrinterCapabilitiesInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.fragment.IndustryControlFragment;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.UiUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.data;
import static com.suntrans.smartshow.R.id.layout_back;
import static com.suntrans.smartshow.R.id.toolbar;

/**
 * Created by Looney on 2016/9/24.
 * 控制电机主页面
 */

public class MotorTriphase_Activity extends BaseActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private FrameLayout frameLayout;
    private IndustryControlFragment fragment;


    @Override
    public int getLayoutId() {
        return R.layout.template_common;
    }
    @Override
    public void initViews(Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        frameLayout = (FrameLayout) findViewById(R.id.fl_content);
    }

    @Override
    public void initData() {
        fragment = new IndustryControlFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content,fragment).commit();

    }

    @Override
    public void parseData(Context context, Intent intent) {
        byte[] bytes = intent.getByteArrayExtra("Content");
        String s = Converts.Bytes2HexString(bytes);
        s=s.toLowerCase();
        System.out.println("sbbbbbbbbbbbbbb"+s);
        if (MainService1.IsInnerNet){
            if (s.substring(0,6).equals("f5aa0b")&&s.length()>10){
                s=s.substring(2,s.length());
                Map<String,String> map = new HashMap<>();
                map.put("data",s);
                Message msg = new Message();
                msg.obj = map;
                msg.what = s.length();
                fragment.handler.sendMessage(msg);
            }
        }else {
            if (s.substring(0,20).equals("020000ff00571f95aa0b")&&s.length()>10){
                s=s.substring(16,s.length());
                Map<String,String> map = new HashMap<>();
                map.put("data",s);
                Message msg = new Message();
                msg.obj = map;
                msg.what = s.length();
                fragment.handler.sendMessage(msg);
            }
        }

    }

    @Override
    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
