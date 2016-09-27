package com.suntrans.smartshow.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.adapter.RoomConditionAdapter;
import com.suntrans.smartshow.adapter.SmartControlAdapter;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.bean.SixSensor;
import com.suntrans.smartshow.bean.SmartSwitch;
import com.suntrans.smartshow.utils.LogUtil;

/**
 * 智能家居页面中第1个Fragment,显示第开关信息。
 * Created by Looney on 2016/9/26.
 */
public class SmartControlFragment extends BaseFragment {
    private SmartSwitch data = new SmartSwitch();
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private SmartControlAdapter adapter;



    @Override
    public int getLayoutId() {
        return R.layout.roomcondition_fragment;
    }

    @Override
    public void initViews() {
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new GridLayoutManager(getActivity(),3);
        adapter = new SmartControlAdapter(getActivity(),data);
        recyclerView.setLayoutManager(manager);
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
