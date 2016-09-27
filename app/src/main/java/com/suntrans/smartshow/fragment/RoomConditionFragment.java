package com.suntrans.smartshow.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SeekBar;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.activity.Smartroom_Activity;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.adapter.RoomConditionAdapter;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.bean.SixSensor;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.views.Switch;

/**
 * 智能家居页面中第二个Fragment,显示第六感的状态信息
 * Created by Looney on 2016/9/26.
 */
public class RoomConditionFragment extends BaseFragment {
    private SixSensor data = new SixSensor();
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private RoomConditionAdapter adapter;



    @Override
    public int getLayoutId() {
        return R.layout.roomcondition_fragment;
    }

    @Override
    public void initViews() {
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        adapter = new RoomConditionAdapter(getActivity(),data);
        //给灯光控制模块设置点击监听
        adapter.setLightControlListener(new RoomConditionAdapter.onLightControlListener() {
            @Override
            public void onSwitchChange(Switch switchView, boolean isChecked) {
                switch (switchView.getId()){
                    case R.id.switch_r:
                        LogUtil.i("红色开关的状态是"+isChecked);
                        String order = isChecked?"":"";
                        ((Smartroom_Activity)getActivity()).binder.sendOrder(order,3);
                        break;
                    case R.id.switch_g:
                        LogUtil.i("绿色开关的状态是"+isChecked);
                        break;
                    case R.id.switch_b:
                        LogUtil.i("蓝色开关的状态是"+isChecked);
                        break;
                }
            }

            @Override
            public void onSeekBarChangedListener(SeekBar seekBar, int progress, boolean fromUser) {
                switch (seekBar.getId()){
                    case R.id.seekbar_r:
                        LogUtil.i("红色进度条="+progress);
                        break;
                    case R.id.seekbar_g:
                        LogUtil.i("绿色进度条="+progress);
                        break;
                    case R.id.seekbar_b:
                        LogUtil.i("蓝色进度条="+progress);
                        break;
                }
            }
        });

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
        LogUtil.e("RoomConditionFragment收到拉="+bytes.toString());
    }

}
