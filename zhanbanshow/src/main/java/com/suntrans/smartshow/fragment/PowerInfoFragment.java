package com.suntrans.smartshow.fragment;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.activity.Smartroom_Activity;
import com.suntrans.smartshow.adapter.PowerInfoAdapter;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.bean.SingleMeter;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.ThreadManager;
import com.suntrans.smartshow.utils.UiUtils;

import org.xml.sax.helpers.LocatorImpl;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * 智能家居页面中第3个Fragment,显示电表数据。
 * Created by Looney on 2016/9/26.
 */
public class PowerInfoFragment extends BaseFragment {
    private SingleMeter data;
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private PowerInfoAdapter adapter;
    private String date = " ";//刷新的时间

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Map<String, Object> map = (Map<String, Object>) msg.obj;
            int count =msg.what;
            LogUtil.i(count+"");
            if (count<31){
                return;
            }
            byte[] bytes = (byte[]) (map.get("data"));
            String s =Converts.Bytes2HexString(bytes);
            if (MainService1.IsInnerNet){
                s = s.substring(2,s.length());//截掉内网加的协议f2
            }else {
                s = s.substring(16,s.length());
            }
            LogUtil.e("命令为:"+s);
            String return_addr = s.substring(6, 18);   //电表反向表号
            try {
                byte[] a = Converts.HexString2Bytes(s);
                double VA = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[13] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[12] & 0xff) - 51)}))) / 10.0;  //A相电压。单位是V
                double IA = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[16] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[15] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[14] & 0xff) - 51)}))) / 1000.0;  //A相电流，单位是A
                double Active_Power = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[19] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[18] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[17] & 0xff) - 51)}))) / 10000.0;  //有功功率，单位是kW
                double Powerrate = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[21] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[20] & 0xff) - 51)}))) / 1000.0;  //功率因数
                double Electricity = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[25] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[24] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[23] & 0xff) - 51)}))) + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[22] & 0xff) - 51)})) / 100.0;  //总用电量，单位是kWh

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                date = sdf.format(new java.util.Date());

                data.setAddress(return_addr);
                data.setTime(date);
                data.setUValue(VA);
                data.setIValue(IA);
                data.setPower(Active_Power);
                data.setPowerRate(Powerrate);
                data.setEletricity(Electricity);

                adapter.notifyDataSetChanged();
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
            }catch (Exception e){
                return;
            }
        }
    };
    SwipeRefreshLayout.OnRefreshListener listener;
    @Override
    public int getLayoutId() {
        return R.layout.roomcondition_fragment;
    }

    @Override
    public void initViews() {
        data = new SingleMeter();
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        adapter = new PowerInfoAdapter(getActivity(),data);
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.bg_action);
        refreshLayout.setColorSchemeResources(R.color.white);
        refreshLayout.setSize(SwipeRefreshLayout.LARGE);
        listener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (((Smartroom_Activity)getActivity()).binder!=null)
                ((Smartroom_Activity)getActivity()).binder.sendOrder(order,9);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()){
                            if (refreshLayout.isRefreshing()){
                                refreshLayout.setRefreshing(false);
                            }
                        }
                    }
                },1800);
            }
        };
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                listener.onRefresh();
            }
        });
        refreshLayout.setOnRefreshListener(listener);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new RecyclerViewDivider(getActivity(), LinearLayoutManager.VERTICAL));  //设置分割线
        recyclerView.setAdapter(adapter);
        new RefreshThread().start();
    }
    private String order = "fe 68 34 27 00 07 14 20 68 1f 00";//刷新数据命令
    private boolean isRefresh =true;//是否刷新
    @Override
    public void onDestroy() {
        isRefresh = false;
        super.onDestroy();
    }

    @Override
    protected void parseObtainedMsg(byte[] bytes) {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser){
            try {
                isRefresh=true;
                new RefreshThread().start();
            }catch (Exception e){

            }
            if (refreshLayout!=null){
                refreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(true);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (refreshLayout.isRefreshing()){
                                    if (refreshLayout.isRefreshing()){
                                        refreshLayout.setRefreshing(false);
                                    }
                                }
                            }
                        },2000);
                    }
                });
            }

        }else {
            isRefresh = false;
        }
    }

    class RefreshThread extends Thread{
        @Override
        public void run() {
            while (isRefresh){
                try {
                    if (((Smartroom_Activity)getActivity()).binder!=null){
                        ((Smartroom_Activity)getActivity()).binder.sendOrder(order,9);
                    }
                    Thread.sleep(5000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
