package com.suntrans.smartshow.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.RoadBulbAdapter;
import com.suntrans.smartshow.fragment.RoomConditionFragment;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.utils.ThreadManager;
import com.suntrans.smartshow.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by pc on 2016/9/16.
 * 公共区域中路灯控制页面
 */
public class RoadBulb_Activity extends AppCompatActivity{

    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private ArrayList<Map<String, String>> data = new ArrayList<>();
    private TextView textView;//标题
    private Toolbar toolbar;
    private RoadBulbAdapter adapter;
    private boolean isRefresh=true;
    private ProgressBar progressBar;
//    private SmartSwitch datas;
    String road_addr="00010004";
    private   ArrayList<Map<String,String>> datas = new ArrayList<Map<String, String>>(10);
    private  byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private  MainService1.ibinder binder;  //用于Activity与Service通信

    private ServiceConnection con = new ServiceConnection() {
        //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("绑定成功");
            binder=(MainService1.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
            Log.v("Time", "绑定后时间：" + String.valueOf(System.currentTimeMillis()));
        }

        @Override   //service因异常而断开的时候调用此方法
        public void onServiceDisconnected(ComponentName name) {
            Log.v("Time", "绑定失败");
        }
    };   ///用于绑定activity与service

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this, Color.TRANSPARENT);//设置状态栏为透明颜色
        setContentView(R.layout.road_bulb_activity);
        Intent intent = new Intent(getApplicationContext(), MainService1.class);    //指定要绑定的service
        bindService(intent, con, Context.BIND_AUTO_CREATE);   //绑定主service
        initRx();
        //初始化控件
        initViews(savedInstanceState);
        initData();
        //初始化ToolBar
        initToolBar();
    }


    public void initViews(Bundle savedInstanceState) {

        initRx();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressBar= (ProgressBar) findViewById(R.id.pb);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        textView = (TextView) findViewById(R.id.tv_title);
    }

    public void initData(){
        isRefresh = true;
        for(int i = 0;i<10;i++){
            Map<String,String> map1= new HashMap<String,String>();
            map1.put("position",i+"");
            map1.put("state","0");
            datas.add(map1);
        }

        GridLayoutManager manager = new GridLayoutManager(this,3);
        adapter = new RoadBulbAdapter(this,datas);
        adapter.setOnItemClickListener(new RoadBulbAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, final int position) {
                progressBar.setVisibility(View.VISIBLE);
                        String ps = datas.get(position).get("state");
                        if (position!=9){
                            if (TextUtils.equals(ps,"0")){
                                String order = "aa68 "+road_addr+"06 030"+(position+1)+"0001";
                                binder.sendOrder(order,2);
                            }else {
                                String order = "aa68 "+road_addr+"06 030"+(position+1)+"0000";
                                binder.sendOrder(order,2);
                            }

                        }else {
                            if (TextUtils.equals(ps,"0")){
                                String order = "aa68 "+road_addr+"06 030"+"a"+"0001";
                                binder.sendOrder(order,2);
                            }else {
                                String order = "aa68 "+road_addr+"06 030"+"a"+"0000";
                                binder.sendOrder(order,2);
                            }
                        }
                    }
        });
        recyclerView.setLayoutManager(manager);


        refreshLayout.setColorSchemeResources(android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_light);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                getSwitchState();
            }
        });
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSwitchState();
            }
        });
//        ThreadManager.getInstance().createLongPool().execute(new Runnable() {
//            @Override
//            public void run() {
//                while (isRefresh){
//                    if (binder!=null){
//                        getSwitchState();
//                    }
//                    try {
//                        Thread.sleep(8000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onDestroy() {
//        unbindService(connection);   //解除Service的绑定
        isRefresh=false;//取消刷新
        unregisterReceiver(broadcastreceiver);  //注销广播接收者
        unbindService(con);   //解除Service的绑定
//        if (rxsub.isUnsubscribed()){
//            rxsub.unsubscribe();
//        }
        super.onDestroy();
    }

    private String return_addr;//开关地址
    private String s;//收到的命令

    private void initRx() {
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive
        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver

    }

    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            textView.setText("路灯");
        }
    }

    /**
     * 获取开关每个通道的状态
     */
    private void getSwitchState() {
        String order = "aa68"+road_addr+"03 0100"+"0007";
        LogUtil.i("准备发送命令"+order);
        binder.sendOrder(order,2);
    }

    //新建广播接收器，接收服务器的数据并解析，
    protected BroadcastReceiver broadcastreceiver=new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent){
            byte[] bytes = intent.getByteArrayExtra("Content");
            if (bytes.length<10){
                UiUtils.showToast(RoadBulb_Activity.this,"刷新失败");
                refreshLayout.setRefreshing(false);
                return;
            }
            s=Converts.Bytes2HexString(bytes);
            s= s.split("0d0a")[0]+"0d0a";
            if (s.length()>20){
                s=s.substring(2,s.length());
                return_addr = s.substring(4,12);   //返回数据的开关地址
                System.out.println("Fuck！！！！！！！！！！！返回的命令为s=:"+s);
                byte a[] = Converts.HexString2Bytes(s);
                if (s.substring(12, 14).equals("03"))   //如果是读寄存器状态，解析出开关状态
                {
                    if (s.substring(14, 16).equals("0E")||s.substring(14,16).equals("07"))
                    {
                        String[] states={"0","0","0","0","0","0","0","0","0","0"};   //十个通道的状态，state[0]对应1通道
                        for(int i=0;i<8;i++)   //先获取前八位的开关状态
                        {
                            states[i]=((a[9]&bits[i])==bits[i])?"1":"0";   //1-8通道

                        }
                        for(int i=0;i<2;i++)
                        {
                            states[i+8]=((a[8]&bits[i])==bits[i])?"1":"0";  //9、10通道

                        }

                        for(int i= 0;i<datas.size();i++){
                            datas.get(i).put("state",states[i]);
                        }

                    }
                }
                else if(s.substring(12,14).equals("06"))   //单个通道状态发生改变
                {
                    int k=0;         //k是通道号
                    int state=Integer.valueOf(s.substring(21, 22));  //开关状态，1代表打开，0代表关闭
                    if(s.substring(17,18).equals("A"))
                        k=10;
                    else
                        k=Integer.valueOf(s.substring(17, 18));   //通道号,int型
                    if(k==0)                                          //如果通道号为0，则是总开关
                    {
                        if (state==0){
                            for (int i=0;i<datas.size();i++){
                                datas.get(i).put("state","0");
                            }
                        }
                    }
                    else     //如果通道号不为0，则更改data中的状态，并更新
                    {
//                                    String[] state2={"0","0","0","0","0","0","0","0","0","0"};   //十个通道的状态，state[0]对应1通道
//                                    state2[k-1] = state+"";
                        for (int i=0;i<datas.size();i++){
                            if (datas.get(i).get("position").equals(String.valueOf(k-1))){
                                datas.get(i).put("state",state==1?"1":"0");
                            }
                        }
                    }
                }
                if (adapter!=null){
                    for (int i =0;i<datas.size();i++){
//                        System.out.println(datas.get(i).get("position")+datas.get(i).get("state"));
                    }
                    refreshLayout.setRefreshing(false);
                    UiUtils.showToast(UiUtils.getContext(),"刷新成功！");
                    adapter.notifyDataSetChanged();
                }
            }

        }
    };//广播接收器



}
