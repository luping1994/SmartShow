package com.suntrans.smartshow.fragment;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SeekBar;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.Convert.ParseSixSensor;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.activity.Smartroom_Activity;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.adapter.RoomConditionAdapter;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.bean.SixSensor;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.ThreadManager;
import com.suntrans.smartshow.utils.UiUtils;
import com.suntrans.smartshow.views.Switch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static android.R.attr.order;

/**
 * 智能家居页面中第二个Fragment,显示第六感的状态信息
 * Created by Looney on 2016/9/26.
 */
public class RoomConditionFragment extends BaseFragment {
    private SixSensor data ;
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private RoomConditionAdapter adapter;
    private Socket client;//连接
    private DataOutputStream out;
    private DataInputStream in;
    private String ipAddress;//ip地址
    int port=8000;//端口号
    String addr ="0001";
    Handler handler = new Handler();
    private static boolean isRuning=true;
    private static boolean isRefresh = true;//是否刷新
    @Override
    public int getLayoutId() {
        return R.layout.roomcondition_fragment;
    }
    public Handler handler1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Map<String, Object> map = (Map<String, Object>) msg.obj;
            byte[] bytes = (byte[]) (map.get("data"));
            String s = Converts.Bytes2HexString(bytes);
            s = s.replace(" ", ""); //去掉空格
            String[] single_str = s.split("0d0a");
            String result = single_str[0] + "0d0a";
            LogUtil.i("Fuck you!收到结果为==>" + result);
            SixSensor parseData = ParseSixSensor.parseData(result);
            if (parseData==null){
                return;
            }
            data.setPm1(parseData.getPm1());
            data.setPm10(parseData.getPm10());
            data.setPm25(parseData.getPm25());
            data.setArofene(parseData.getArofene());
            data.setSmoke(parseData.getSmoke());
            data.setTmp(parseData.getTmp());
            data.setHumidity(parseData.getHumidity());
            data.setAtm(parseData.getAtm());
            data.setStaff(parseData.getStaff());
            data.setLight(parseData.getLight());
            data.setXdegree(parseData.getXdegree());
            data.setYdegree(parseData.getYdegree());
            data.setZdegree(parseData.getZdegree());
            refreshLayout.setRefreshing(false);
            adapter.notifyDataSetChanged();

        }
    };
    @Override
    public void initViews() {
        data =  new SixSensor();
//        connectToServer();
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
        refreshLayout.setColorSchemeResources(android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_light);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                getAllParameter();
                try {
                    getAllParameter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                            UiUtils.showToast(UiUtils.getContext(),"刷新失败，请重试！");
                        }
                    }
                },2000);
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String order="ab68"+addr+"f003 0100 0011";
                ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor(order,4);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                            UiUtils.showToast(UiUtils.getContext(),"刷新失败，请重试！");
                        }
                    }
                },2000);
            }
        });
        ThreadManager.getInstance().createLongPool().execute(new Runnable() {
            @Override
            public void run() {
                while (isRefresh){
                    getAllParameter();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser){
            isRefresh = true;
            isRuning = true;
        }else {
            isRefresh = false;
            isRuning = false;
        }
    }

    @Override
    public void onDestroy() {
        isRefresh = false;
        isRuning = false;
        if (client!=null){
            try {
                in.close();
                out.close();
                client.close();
                LogUtil.i("一切都已经关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    protected void parseObtainedMsg(byte[] bytes) {
    }
    public void  getAllParameter(){
        boolean run = true;
        String order="ab68"+addr+"f003 0100 0011";
        while (run){
            if (((Smartroom_Activity)getActivity()).binder!=null){
                ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor(order,4);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((Smartroom_Activity)getActivity()).binder.sendOrder2Sixsenor("ab68"+addr+"f003 0200 0006",4);
                    }
                },500);
            }
            run=false;
        }

    }
}
