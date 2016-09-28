package com.suntrans.smartshow.fragment;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.activity.Fourmeters_Activity;
import com.suntrans.smartshow.activity.Meter_Activity;
import com.suntrans.smartshow.activity.SmartRoomDetails_Activity;
import com.suntrans.smartshow.activity.SmartRoonDetails_Activity;
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
        return R.layout.roomcontrol_fragment;
    }

    @Override
    public void initViews() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new GridLayoutManager(getActivity(),3);
        adapter = new SmartControlAdapter(getActivity(),data);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new SmartControlAdapter.onItemClickListener() {
            @Override
            public void onclick(View v, int position) {
                Intent intent = new Intent();
                switch (position){
                    case 0:
                        intent.putExtra("area",0);
                        break;
                    case 1:
                        intent.putExtra("area",1);
                        break;
                    case 2:
                        intent.putExtra("area",2);
                        break;
                    case 3:
                        intent.putExtra("area",3);
                        break;
                    case 4:
                        intent.putExtra("area",4);
                        break;
                    case 5:
                        intent.putExtra("area",5);
                        break;
                    case 6:
                        intent.putExtra("area",6);
                        break;
                    case 7:
                        intent.putExtra("area",7);
                        break;
                    case 8:
                        intent.putExtra("area",8);
                        break;
                }
                intent.setClass(getActivity(), SmartRoomDetails_Activity.class);
                startActivity(intent);   //跳转到单相表详细信息页面
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }


    @Override
    protected void parseObtainedMsg(byte[] bytes) {

    }
}
