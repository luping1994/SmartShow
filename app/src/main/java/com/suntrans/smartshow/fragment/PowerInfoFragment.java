package com.suntrans.smartshow.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.PowerInfoAdapter;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.adapter.RoomConditionAdapter;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.bean.SingleMeter;
import com.suntrans.smartshow.bean.SixSensor;

/**
 * 智能家居页面中第3个Fragment,显示电表数据。
 * Created by Looney on 2016/9/26.
 */
public class PowerInfoFragment extends BaseFragment {
    private SingleMeter data = new SingleMeter();
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private PowerInfoAdapter adapter;



    @Override
    public int getLayoutId() {
        return R.layout.roomcondition_fragment;
    }

    @Override
    public void initViews() {
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        adapter = new PowerInfoAdapter(getActivity(),data);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new RecyclerViewDivider(getActivity(), LinearLayoutManager.VERTICAL));  //设置分割线
        recyclerView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
    }


    @Override
    protected void parseObtainedMsg(byte[] bytes) {

    }
}
