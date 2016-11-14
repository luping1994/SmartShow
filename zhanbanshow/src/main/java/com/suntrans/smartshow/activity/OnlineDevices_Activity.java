package com.suntrans.smartshow.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.utils.UiUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Looney on 2016/10/4.
 */

public class OnlineDevices_Activity extends AppCompatActivity {
    private DatagramSocket UDPclient;
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private TextView textView;//标题
    private Toolbar toolbar;
    private boolean isRefresh=true;
    private ArrayList<Map<String,String>> datas = new ArrayList<Map<String,String>>();
    private Handler handler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_devices);
        StatusBarCompat.compat(this, Color.TRANSPARENT);
        initViews();
        initToolBar();
        initData();
    }



    private void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
            textView.setText("在线设备");
        }
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        refreshLayout.setColorSchemeResources(R.color.white);
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.bg_action);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        textView = (TextView) findViewById(R.id.tv_title);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new  Runnable() {
                    public void run() {
                        try {
                            if (UDPclient!=null){
                                //创建一个InetAddree
                                InetAddress serverAddress = InetAddress.getByName("255.255.255.255");
                                byte[] arrayOfByte = "123456AT+QMAC".getBytes();
                                DatagramPacket packet = new DatagramPacket(arrayOfByte, arrayOfByte.length, serverAddress, 988);
                                //调用socket对象的send方法，发送数据
                                UDPclient.send(packet);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (refreshLayout.isRefreshing())
                                    refreshLayout.setRefreshing(false);
                            }
                        },2000);
                    }
                }).start();
            }
        });

        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
    }
    private MyAdapter adapter;
    private InetAddress serverAddress ;

    private void initData() {
        new Thread(new  Runnable() {
            public void run() {
                try {
                    UDPclient = new DatagramSocket();
                    new UDPThread().start();
                    //创建一个InetAddree
                    serverAddress= InetAddress.getByName("255.255.255.255");
                    byte[] arrayOfByte = "123456AT+QMAC".getBytes();
                    DatagramPacket packet = new DatagramPacket(arrayOfByte, arrayOfByte.length, serverAddress, 988);
                    //调用socket对象的send方法，发送数据
                    UDPclient.send(packet);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
//        Map<String,String> map = new HashMap<String,String>();
//        map.put("ipAddr","192.168.191.1");
//        map.put("macAddr","212321321");
//        datas.add(map);
    }
    private class MyAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder = new ViewHolder(LayoutInflater.from(OnlineDevices_Activity.this)
                    .inflate(R.layout.devices_listview, parent,false));
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ViewHolder)holder).setdata(datas.get(position).get("macAddr"),datas.get(position).get("ipAddr"));
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder{
            TextView mac_addr;
            TextView ip_addr;
            public ViewHolder(View itemView) {
                super(itemView);
                mac_addr = (TextView) itemView.findViewById(R.id.mac_addr);
                ip_addr = (TextView) itemView.findViewById(R.id.ip_addr);
            }
            public void setdata(String macAddr,String ipAddr){
                mac_addr.setText(macAddr);
                ip_addr.setText(ipAddr);
            }
        }
    }

    public  class UDPThread extends Thread    //新建线程接收UDP回应
    {

        public UDPThread() {
        }

        public void run() {
            //tvRecv.setText("start");
            byte[] buffer = new byte[1024];
            final StringBuilder sb = new StringBuilder();
            try {

                while (UDPclient != null&&isRefresh) {
                    // 接收服务器信息       定义输入流
                    byte data[] = new byte[1024];
                    //创建一个空的DatagramPacket对象
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    //使用receive方法接收客户端所发送的数据
                    UDPclient.receive(packet);
                    String clientip;
                    clientip = packet.getAddress().toString().replace("/", "");    //ip地址
                    String clientmac = new String(packet.getData()).replace("+OK=", "");  //MAC地址
                    clientmac = clientmac.replaceAll("\r|\n", "");    //去掉换行符
                    clientmac = clientmac.replace(" ", "");   //去掉空格
                    System.out.println("开关IP地址为"+clientip);
                    System.out.println("MAC地址位"+clientmac);
                    boolean j = false;
                    for (int i = 0;i<datas.size();i++){
                        if (datas.get(i).get("macAddr").equals(clientmac)){
                            j=true;
                        }
                    }
                    if (!j){
                        Map<String,String> map = new HashMap<>();
                        map.put("ipAddr",clientip);
                        map.put("macAddr",clientmac);
                        datas.add(map);
                        UiUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(adapter==null){
                                    adapter = new MyAdapter();
                                    recyclerView.setAdapter(adapter);
                                }else {
                                    if (refreshLayout.isRefreshing())
                                        refreshLayout.setRefreshing(false);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    @Override
    protected void onDestroy() {
        if (UDPclient!=null)
        UDPclient.close();
        isRefresh = false;
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
