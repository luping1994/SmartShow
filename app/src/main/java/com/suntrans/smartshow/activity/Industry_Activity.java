package com.suntrans.smartshow.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.fragment.IndustryControlFragment;

import static com.suntrans.smartshow.R.id.layout_back;
import static com.suntrans.smartshow.R.id.toolbar;

/**
 * Created by Looney on 2016/9/24.
 * 控制电机主页面
 */

public class Industry_Activity extends BaseActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private FrameLayout frameLayout;
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
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_content,new IndustryControlFragment()).commit();

    }

    @Override
    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setTitle("三相电动机控制器");
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case layout_back:
//                finish();
//                break;
//            case R.id.ll_state:
//
//                break;
//            case R.id.ll_con:
//                break;
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
