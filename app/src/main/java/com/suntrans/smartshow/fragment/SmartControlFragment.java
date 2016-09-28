package com.suntrans.smartshow.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.adapter.RoomConditionAdapter;
import com.suntrans.smartshow.adapter.SmartControlAdapter;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.bean.SixSensor;
import com.suntrans.smartshow.bean.SmartSwitch;
import com.suntrans.smartshow.utils.LogUtil;

import java.text.BreakIterator;

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
        adapter.setOnItemClickListener(new SmartControlAdapter.onItemClickListener() {
            @Override
            public void onclick(View v, int position) {
                switch (position){
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                }
            }
        });
    }


    @Override
    protected void parseObtainedMsg(byte[] bytes) {

    }
}
