package com.suntrans.smartshow.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.FlashLightAdapter;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.adapter.RoadBulbAdapter;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.bean.FlashlightInfo;
import com.suntrans.smartshow.utils.RxBus;
import com.suntrans.smartshow.views.Switch;

import java.util.ArrayList;
import java.util.Map;

import rx.Subscriber;
import rx.Subscription;

/**
 * 公共区域管理中的氙气灯主页面，控制和显示氙气灯信息
 * Created by Looney on 2016/9/26.
 */

public class Flashlight_Activity extends BaseActivity {
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private FlashlightInfo data;
    private TextView textView;//标题
    private Toolbar toolbar;
    private FlashLightAdapter adapter;

    @Override
    public int getLayoutId() {
        return R.layout.flashlight_activity;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        initRx();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        textView = (TextView) findViewById(R.id.tv_title);
    }

    @Override
    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
            textView.setText("氙气灯");
        }
    }


    @Override
    public void initData() {
        data= new FlashlightInfo();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        adapter = new FlashLightAdapter(this,data);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL));  //设置分割线
        recyclerView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
        adapter.setmOnSwitchListener(new FlashLightAdapter.onSwitchListener() {
            @Override
            public void onChanged(Switch switchView, boolean isChecked) {
                if (isChecked){
                    binder.sendOrder("F5 01 01 08 06 02 00 00 01 D8 37",5);
                }else {
                    binder.sendOrder("F5 01 01 08 06 02 00 00 00 19 F7",5);
                }
            }
        });
    }

    private Subscription rxsub;
    private void initRx() {
        rxsub = RxBus.getInstance().toObserverable(byte[].class).subscribe(new Subscriber<byte[]>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(byte[] bytes) {
                //bytes 是返回的16进制命令
            }
        });
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
