package com.suntrans.smartshow.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.base.BaseActivity1;

/**
 * 四个表主页面，展示四个表条目
 * Created by pc on 2016/9/14.
 */
public class Fourmeters_Activity extends BaseActivity1 implements View.OnClickListener{
    private LinearLayout layout_elec;    //智能电表
    private LinearLayout layout_water;      //数字水表
    private LinearLayout layout_heat;     //数字热量表
    private LinearLayout layout_gas;  //数字气表
    private Toolbar toolbar;
    private TextView textView;
    @Override
    public int getLayoutId() {
        return R.layout.fourmeters;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView = (TextView) findViewById(R.id.tv_title);
        layout_elec = (LinearLayout) findViewById(R.id.layout_elec);
        layout_water = (LinearLayout) findViewById(R.id.layout_water);
        layout_heat = (LinearLayout) findViewById(R.id.layout_heat);
        layout_gas = (LinearLayout) findViewById(R.id.layout_gas);


        layout_elec.setOnClickListener(this);
        layout_water.setOnClickListener(this);
        layout_heat.setOnClickListener(this);
        layout_gas.setOnClickListener(this);
    }

    @Override
    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            textView .setText("四表合一");
        }
    }

    @Override
    public void initData() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Log.i("ID", String.valueOf(v.getId()));
        Intent intent = new Intent();
        switch(v.getId()){
            case R.id.layout_elec:
                intent.putExtra("Meter_Type", 1);
                break;
            case R.id.layout_water:
                intent.putExtra("Meter_Type", 2);
                break;
            case R.id.layout_heat:
                intent.putExtra("Meter_Type", 3);
                break;
            case R.id.layout_gas:
                intent.putExtra("Meter_Type", 4);
                break;
            default:break;

        }
        intent.setClass(Fourmeters_Activity.this, Meter_Activity.class);
        startActivity(intent);   //跳转到单相表详细信息页面
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
