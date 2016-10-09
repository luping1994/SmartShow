package com.suntrans.smartshow.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.base.BaseApplication;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.utils.ThreadManager;
import com.suntrans.smartshow.utils.UiUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by pc on 2016/9/16.
 * 三相电表参数页面
 */
public class IndustryState_Activity extends AppCompatActivity {

    public MainService1.ibinder binder;  //用于Activity与Service通信
    private LinearLayout layout_back;    //返回键
    private TextView tx_title;   //标题
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private String title;//标题值
    private ArrayList<Map<String, String>> data = new ArrayList<>();
    private TextView textView;
    private String date = "null";//刷新的时间
    private  boolean refresh = true;
    Handler handler = new Handler();
    private mAdapter adapter;
    /**
     * 服务连接
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder=(MainService1.ibinder)service;
            String order = "FE 68 09 01 00 12 14 20 68 1F 00";
            binder.sendOrder(order,8);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            Toast.makeText(getApplication(), "网络错误！", Toast.LENGTH_SHORT).show();

        }
    };
    private String sanxiangAddr="09 01 00 12 14 20";//反向三相电表地址

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent1 = new Intent(BaseApplication.getApplication(), MainService1.class);    //指定要绑定的service
        bindService(intent1, connection, Context.BIND_AUTO_CREATE);   //绑定主service
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive
        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver
        StatusBarCompat.compat(this, Color.TRANSPARENT);
        setContentView(R.layout.meter);
        initViews();
        initData();
    }

    public void initViews(){

        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        tx_title = (TextView) findViewById(R.id.tx_title);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        textView = (TextView) findViewById(R.id.tv_back);
        textView.setText("工业用电");
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title = "";
        tx_title.setText(title);
        recyclerView.setLayoutManager(new LinearLayoutManager(IndustryState_Activity.this));   //设置布局方式
        recyclerView.addItemDecoration(new RecyclerViewDivider(IndustryState_Activity.this, LinearLayoutManager.VERTICAL));  //设置分割线
        adapter= new mAdapter();
        recyclerView.setAdapter(adapter);
        //下拉刷新数据
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ThreadManager.getInstance().createLongPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        boolean refresh = true;
                        while (refresh){
                            if (binder!=null){
                                String order = "FE 68"+sanxiangAddr+"68 1F 00";
                                binder.sendOrder(order,8);
                                refresh=false;
                            }
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
            }
        });
        //保证一进页面就刷新获得数据
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                    if (binder!=null){
//
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (refreshLayout.isRefreshing()){
                                    refreshLayout.setRefreshing(false);
//                                    UiUtils.showToast(UiUtils.getContext(),"连接失败,请重试！");
                                }
                            }
                        },2000);
                    }
            }
        });
//        新进线程刷新数据
        ThreadManager.getInstance().createLongPool().execute(new Runnable() {
            @Override
            public void run() {
                while (refresh){
                    if (binder!=null){
                        String order = "FE 68 09 01 00 12 14 20 68 1F 00";
                        binder.sendOrder(order,8);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);   //解除Service的绑定
        unregisterReceiver(broadcastreceiver);  //注销广播接收者
        refresh = false;
        super.onDestroy();
    }



    public int getLayoutId() {
        return R.layout.meter;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void initData(){
        data.clear();   //先清空数据

                Map<String, String> map1 = new HashMap<>();
                map1.put("Name","用电量");
                map1.put("Value","null");
                map1.put("Image", String.valueOf(R.drawable.ic_elec));
                data.add(map1);
                Map<String, String> map2 = new HashMap<>();
                map2.put("Name","有功功率");
                map2.put("Value","null");
                map2.put("Image", String.valueOf(R.drawable.ic_power));
                data.add(map2);
                Map<String, String> map3 = new HashMap<>();
                map3.put("Name","无功功率");
                map3.put("Value","null");
                map3.put("Image", String.valueOf(R.drawable.ic_power));
                data.add(map3);
                Map<String, String> map4 = new HashMap<>();
                map4.put("Name","功率因素");
                map4.put("Value","null");
                map4.put("Image", String.valueOf(R.drawable.ic_powerrate));
                data.add(map4);
                Map<String, String> map5 = new HashMap<>();
                map5.put("Name","A相电压");
                map5.put("Value","null");
                map5.put("Image", String.valueOf(R.drawable.ic_voltage));
                data.add(map5);
        Map<String, String> map6 = new HashMap<>();
        map6.put("Name","B相电压");
        map6.put("Value","null");
        map6.put("Image", String.valueOf(R.drawable.ic_voltage));
        data.add(map6);

        Map<String, String> map7 = new HashMap<>();
        map7.put("Name","C相电压");
        map7.put("Value","null");
        map7.put("Image", String.valueOf(R.drawable.ic_voltage));
        data.add(map7);

        Map<String, String> map8 = new HashMap<>();
        map8.put("Name","A相电流");
        map8.put("Value","null");
        map8.put("Image", String.valueOf(R.drawable.ic_current));
        data.add(map8);

        Map<String, String> map9 = new HashMap<>();
        map9.put("Name","B相电流");
        map9.put("Value","null");
        map9.put("Image", String.valueOf(R.drawable.ic_current));
        data.add(map9);

        Map<String, String> map10= new HashMap<>();
        map10.put("Name","C相电流");
        map10.put("Value","null");
        map10.put("Image", String.valueOf(R.drawable.ic_current));
        data.add(map10);

    }
    /**
     * RecyclerView适配器
     **自定义Recyclerview的适配器,主要的执行顺序：getItemViewType==>onCreateViewHolder==>onBindViewHolder
     */
    class mAdapter extends RecyclerView.Adapter{
        /****
         * 渲染具体的布局，根据viewType选择使用哪种布局
         * @param parent   父容器
         * @param viewType    布局类别，多种布局的情况定义多个viewholder
         * @return
         */
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            if (viewType==1){
                RecyclerView.ViewHolder holder= new viewHolder1(LayoutInflater.from(
                        IndustryState_Activity.this).inflate(R.layout.meter_listview, parent,false));
                return holder;
            }else {
                RecyclerView.ViewHolder holder= new viewHolder2(LayoutInflater.from(
                        IndustryState_Activity.this).inflate(R.layout.meter_listview_time, parent,false));
                return holder;
            }

        }

        /***
         * 绑定数据
         * @param holder   绑定哪个holder，用if(holder instanceof mViewHolder1)来判断类型，再绑定数据
         * @param position
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
        {
            //判断holder是哪个类，从而确定是哪种布局
            /////布局1
            if(holder instanceof viewHolder1) {
                viewHolder1 viewholder = (viewHolder1) holder;
                Map<String, String> map = data.get(position-1);
                final String Name = map.get("Name");
                final String Value = map.get("Value");
                Bitmap bitmap = BitmapFactory.decodeResource(IndustryState_Activity.this.getResources(), Integer.valueOf(map.get("Image")));
                viewholder.image.setImageBitmap(bitmap);
                viewholder.name.setText(Name);
                viewholder.value.setText(Value);
            }else {
                viewHolder2 viewholder = (viewHolder2) holder;
                viewholder.value.setText(date);
            }
        }



        @Override
        public int getItemCount()
        {
            return data.size()+1;
        }
        /**
         * 决定元素的布局使用哪种类型
         *在本activity中，布局1使用R.layout.roomgridview，
         * @param position 数据源的下标
         * @return 一个int型标志，传递给onCreateViewHolder的第二个参数 */
        @Override
        public int getItemViewType(int position) {
            if (position==0){
                return 0;
            }else
                return 1;
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 布局类型1对应的ViewHolder，R.layout.listmain_userinfo
         */
        class viewHolder1 extends RecyclerView.ViewHolder
        {
            LinearLayout layout;   //整体布局
            ImageView image;    //图标
            TextView name;    //名称
            TextView value;    //参数值
            public viewHolder1(View view)
            {
                super(view);
                layout=(LinearLayout)view.findViewById(R.id.layout);
                image=(ImageView)view.findViewById(R.id.image);
                name = (TextView) view.findViewById(R.id.name);
                value = (TextView) view.findViewById(R.id.value);
            }
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 布局类型1对应的ViewHolder，R.layout.listmain_userinfo
         */
        class viewHolder2 extends RecyclerView.ViewHolder
        {
            LinearLayout layout;   //整体布局
            TextView value;    //参数值
            public viewHolder2(View view)
            {
                super(view);
                layout=(LinearLayout)view.findViewById(R.id.layout);
                value = (TextView) view.findViewById(R.id.value);
            }
        }
    }

    byte[] a ;
    //新建广播接收器，接收服务器的数据并解析，
    protected BroadcastReceiver broadcastreceiver=new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent){
            byte[] bytes = intent.getByteArrayExtra("Content");
            String s = Converts.Bytes2HexString(bytes);
            s=s.toLowerCase();
            if (MainService1.IsInnerNet){
                if (!s.substring(0,6).equals("f8fe68"))
                    return;
                    s = s.substring(2,s.length());
            }else {
                if (!s.substring(0,20).equals("020000ff00571f98fe68"))
                    return;
                s = s.substring(16,s.length());
            }
                a=Converts.HexString2Bytes(s);
                final String return_addr=s.substring(4,16);  //反向电表表号
                double VA = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[12] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[11] & 0xff) - 51)}))) / 10.0;  //A相电压。单位是V
                double VB = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[14] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[13] & 0xff) - 51)}))) / 10.0;  //B相电压
                double VC = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[16] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[15] & 0xff) - 51)}))) / 10.0;  //C相电压
                double IA = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[19] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[18] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[17] & 0xff) - 51)}))) / 1000.0;  //A相电流，单位是A
                double IB = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[22] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[21] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[20] & 0xff) - 51)}))) / 1000.0;  //B相电流
                double IC = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[25] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[24] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[23] & 0xff) - 51)}))) / 1000.0;  //C相电流
                double Active_Power = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[28] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[27] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[26] & 0xff) - 51)}))) / 10000.0;  //总有功功率，单位是kW
                double Reactive_Power = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[40] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[39] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[38] & 0xff) - 51)}))) / 10000.0;  //总无功功率，单位是kW
                double Powerrate = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[51] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[50] & 0xff) - 51)}))) / 10.0;  //总功率因数
                double Electricity = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[61] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[60] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[59] & 0xff) - 51)}))) + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[58] & 0xff) - 51)})) / 100.0;  //总用电量，单位是kWh
                data.get(0).put("Value", String.valueOf(Electricity)+"kWh");
                data.get(1).put("Value", String.valueOf(Active_Power)+"kW");
                data.get(2).put("Value", String.valueOf(Reactive_Power)+"kvar");
                data.get(3).put("Value", String.valueOf(Powerrate)+"");
                data.get(4).put("Value", String.valueOf(VA)+"V");
                data.get(5).put("Value", String.valueOf(VB)+"V");
                data.get(6).put("Value", String.valueOf(VC)+"V");
                data.get(7).put("Value", String.valueOf(IA)+"I");
                data.get(8).put("Value", String.valueOf(IB)+"I");
                data.get(9).put("Value", String.valueOf(IC)+"I");

                SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                date=sdf.format(new java.util.Date());
                UiUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }
                });
        }
    };//广播接收器

}
