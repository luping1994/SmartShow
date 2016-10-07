package com.suntrans.smartshow.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.suntrans.smartshow.R;

/**
 * Created by Looney on 2016/10/5.
 */

public class Setting_Activity extends AppCompatActivity implements View.OnClickListener {
    private TextView textView;//标题
    private Toolbar toolbar;
    private TextView tv_yuyin;
    private TextView tv_baojing;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        initViews();
        initToolBar();
    }

    private void initViews() {
        textView = (TextView) findViewById(R.id.tv_title);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tv_yuyin = (TextView) findViewById(R.id.tv_yuyin);
        tv_baojing = (TextView) findViewById(R.id.tv_baojing);
        tv_yuyin.setOnClickListener(this);
        tv_baojing.setOnClickListener(this);
    }

    private void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal1);
            textView.setText("设置");
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_yuyin:
                startActivity(new Intent(Setting_Activity.this,Setting_voice_Activity.class));
                break;
            case R.id.tv_baojing:
                Intent intent = new Intent();
                intent.setClass(Setting_Activity.this,WarningConfig_Activity.class);
                startActivity(intent);
                break;
        }

    }
}
