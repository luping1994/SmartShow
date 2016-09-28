package com.suntrans.smartshow.fragment;

import android.os.Handler;
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
import com.suntrans.smartshow.base.BaseApplication;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.bean.SixSensor;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.UiUtils;
import com.suntrans.smartshow.views.Switch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
    private String ipAddress;
    int port=8000;
    private static boolean isRuning=true;
    private static boolean isRefresh = true;//是否刷新
    String addr = "0001";
    @Override
    public int getLayoutId() {
        return R.layout.roomcondition_fragment;
    }

    @Override
    public void initViews() {
        data =  new SixSensor();
        connectToServer();
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
                String order="ab68"+addr+"f003 0100 0011";
                sendOrder(order);
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String order="ab68"+addr+"f003 0100 0011";
                sendOrder(order);
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        sendOrder("ab68"+addr+"f003 0200 0006");
//                    }
//                },500);
            }
        });
    }
    Handler handler = new Handler();
    /**
     * 发送命令
     * @param order
     */
    private void sendOrder(final String order) {

        if (client!=null)
            if (!client.isOutputShutdown()){
                new Thread(){
                    @Override
                    public void run() {
                        String order1 = order.replace(" ","");
                        byte[] bt=null;
                        bt =Converts.HexString2Bytes(order1);
                        String  string =  order1+Converts.GetCRC(bt, 4, bt.length)+"0d0a";
                        System.out.println("Fuckkkkkkkkkkkkkkk===>"+string);/////////////////////
                        byte[] bt1 = Converts.HexString2Bytes(string);
                        try {
                            out.write(bt1);
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }


    }

    /**
     * 连接到服务器
     */
    private void connectToServer() {
        ipAddress = BaseApplication.getSharedPreferences().getString("ipAddress","192.168.1.235");
        new Thread(){
            @Override
            public void run() {
                if (client==null){
                    try {
                        client = new Socket(ipAddress,port);
                        LogUtil.i("client=========>>连接成功！");
                        new TcpServerThread().start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 收消息进程
     */
    class TcpServerThread extends Thread{
        @Override
        public void run() {
            try {
                byte[] buf = new byte[100];
                int len = 0;
                out  = new DataOutputStream(client.getOutputStream());
                in = new DataInputStream(client.getInputStream());
                while (client!=null){
                    if (!client.isClosed()) {
                        if (client.isConnected()) {
                            if (!client.isInputShutdown()) {
                                while ((len = in.read(buf)) != -1) {
                                    String s = "";                       //保存命令的十六进制字符串
                                    for (int i = 0; i < len; i++) {
                                        String s1 = Integer.toHexString((buf[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                                        if (s1.length() == 1)
                                            s1 = "0" + s1;
                                        s = s + s1;
                                    }
                                    s = s.replace(" ", ""); //去掉空格
                                    String[] single_str = s.split("0d0a");
                                    String result = single_str[0] + "0d0a";
                                    System.out.println("Fuck you!收到结果为==>" + result);
                                    SixSensor parseData = ParseSixSensor.parseData(result);
//                            System.out.println(parseData.getTmp()+"ssssssssssssssssssssss");
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
                                    if (adapter != null) {
                                        UiUtils.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                refreshLayout.setRefreshing(false);
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
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
        LogUtil.e("RoomConditionFragment收到拉="+bytes.toString());
    }

}
