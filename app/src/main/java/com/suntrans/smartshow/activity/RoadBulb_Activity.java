package com.suntrans.smartshow.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.RoadBulbAdapter;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.utils.RxBus;

import java.util.ArrayList;
import java.util.Map;

import rx.Subscriber;
import rx.Subscription;


/**
 * Created by pc on 2016/9/16.
 * 公共区域中路灯控制页面
 */
public class RoadBulb_Activity extends BaseActivity {

    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private ArrayList<Map<String, String>> data = new ArrayList<>();
    private TextView textView;//标题
    private Toolbar toolbar;
    private RoadBulbAdapter adapter;
    private ArrayList<Map<String,String>> datas;

    @Override
    public int getLayoutId() {
        return R.layout.road_bulb_activity;
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
    public void initData(){
        GridLayoutManager manager = new GridLayoutManager(this,3);
        adapter = new RoadBulbAdapter(this,datas);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
    }

    @Override
    protected void onDestroy() {
//        unbindService(connection);   //解除Service的绑定
        super.onDestroy();
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
    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            textView.setText("路灯");
        }
    }

}
