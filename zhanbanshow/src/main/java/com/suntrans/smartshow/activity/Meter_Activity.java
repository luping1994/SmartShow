package com.suntrans.smartshow.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.MyObserver;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.utils.ThreadManager;
import com.suntrans.smartshow.utils.UiUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.order;
import static android.os.Build.VERSION_CODES.M;
import static com.suntrans.smartshow.base.BaseApplication.getApplication;


/**
 * Created by pc on 2016/9/16.
 * 四个表信息详细页面，通过传入不同的参数显示
 */
public class Meter_Activity extends AppCompatActivity {
    Handler handler = new Handler();
    boolean isRefresh;//是否自动刷新标志位
    private LinearLayout layout_back;    //返回键
    private TextView tx_title;   //标题
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private int Meter_Type=0;  //智能电表对应1，数字水表对应2，数字热量表对应3，数字气表对应4
    private String title;
    private ArrayList<Map<String, String>> data = new ArrayList<>();
    private mAdapter adapter;
    private int flag;//1表示正在发送命令,2表示发送完毕
    private  String  date;//日期
    public MainService1.ibinder binder;  //用于Activity与Service通信
    private String waterMeterAddr = "04 04 00 11 14 38 00";//水表反向地址
    private String amMeterAddr = "14 29 00 07 14 20";//电表反向地址
    private String gasMeterAddr = "01 00 72 32 20 05 00";//气表反向地址
    private String heatMeterAddr = "85 05 55 45 10 05 02";//热量表反向地址
    //020000ff00571f9 36830010072322005000103901F002416
    private ServiceConnection con = new ServiceConnection() {
        //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("绑定成功");
            binder=(MainService1.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
//            getDataFromServer();
        }

        @Override   //service因异常而断开的时候调用此方法
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplication(), "网络错误！", Toast.LENGTH_SHORT).show();

        }
    };   ///用于绑定activity与service

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent1 = new Intent(getApplicationContext(), MainService1.class);    //指定要绑定的service
        bindService(intent1, con, Context.BIND_AUTO_CREATE);   //绑定主service

        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive

        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver
        StatusBarCompat.compat(this, Color.TRANSPARENT);//设置状态栏为透明颜色
        setContentView(getLayoutId());
        //初始化控件
        initViews();
        initData();
    }

    public void initViews() {
      //检查SD卡写权限授予情况
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            //android6.0申请WRITE_EXTERNAL_STORAGE权限
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    1000);
//        }
        Intent intent = getIntent();
        Meter_Type = intent.getIntExtra("Meter_Type", 0);//表的类型
        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        tx_title = (TextView) findViewById(R.id.tx_title);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);


        switch (Meter_Type){
            case 1:title="智能电表";break;
            case 2:title="智能水表";break;
            case 3:title="智能热量表";break;
            case 4:title="智能气表";break;
            default:break;
        }
        tx_title.setText(title);
        recyclerView.setLayoutManager(new LinearLayoutManager(Meter_Activity.this));   //设置布局方式
        recyclerView.addItemDecoration(new RecyclerViewDivider(Meter_Activity.this, LinearLayoutManager.VERTICAL));  //设置分割线
        adapter = new mAdapter();
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.bg_action);
        refreshLayout.setColorSchemeResources(R.color.white);
        refreshLayout.setSize(SwipeRefreshLayout.LARGE);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetDataTask().execute();
            }
        });

        recyclerView.setAdapter(adapter);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                new GetDataTask().execute();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1000){
//            initViews();
        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public int getLayoutId() {
        return R.layout.meter;
    }
    @Override
    protected void onDestroy() {
        isRefresh = false;
        if(con!=null){
            unbindService(con);   //解除Service的绑定
        }
        unregisterReceiver(broadcastreceiver);  //注销广播接收者
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        isRefresh=false;
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.v("OnResume 被执行了");
        try{
            isRefresh = true;
//            new RefreshThread().start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initData(){
        data.clear();   //先清空数据
        switch (Meter_Type){

            case 1:{   //智能电表
                Map<String, String> map1 = new HashMap<>();
                map1.put("Name","用电量");
                map1.put("Value","null");
                map1.put("Image", String.valueOf(R.drawable.ic_elec));
                data.add(map1);
                Map<String, String> map2 = new HashMap<>();
                map2.put("Name","电压");
                map2.put("Value","null");
                map2.put("Image", String.valueOf(R.drawable.ic_voltage));
                data.add(map2);
                Map<String, String> map3 = new HashMap<>();
                map3.put("Name","电流");
                map3.put("Value","null");
                map3.put("Image", String.valueOf(R.drawable.ic_current));
                data.add(map3);
                Map<String, String> map4 = new HashMap<>();
                map4.put("Name","有功功率");
                map4.put("Value","null");
                map4.put("Image", String.valueOf(R.drawable.ic_power));
                data.add(map4);
                Map<String, String> map5 = new HashMap<>();
                map5.put("Name","功率因数");
                map5.put("Value","null");
                map5.put("Image", String.valueOf(R.drawable.ic_powerrate));
                data.add(map5);
                break;
            }
            case 2:{    //智能水表
                Map<String, String> map5 = new HashMap<>();
                map5.put("Name","当前累计流量");
                map5.put("Value","null");
                map5.put("Image", String.valueOf(R.mipmap.ic_flow));
                data.add(map5);
                Map<String, String> map6 = new HashMap<>();
                map6.put("Name","结算日累计流量");
                map6.put("Value","null");
                map6.put("Image", String.valueOf(R.mipmap.ic_flow));
                data.add(map6);
                break;
            }
            case 3:{    //智能热量表
                Map<String, String> map1 = new HashMap<>();
                map1.put("Name","当前累计冷量");
                map1.put("Value","null");
                map1.put("Image", String.valueOf(R.mipmap.ic_flow));
                data.add(map1);
                Map<String, String> map2 = new HashMap<>();
                map2.put("Name","当前累计热量");
                map2.put("Value","null");
                map2.put("Image", String.valueOf(R.mipmap.ic_flow));
                data.add(map2);
                Map<String, String> map3 = new HashMap<>();
                map3.put("Name","热功率");
                map3.put("Value","null");
                map3.put("Image", String.valueOf(R.mipmap.ic_power));
                data.add(map3);
                Map<String, String> map4 = new HashMap<>();
                map4.put("Name","瞬时流量");
                map4.put("Value","null");
                map4.put("Image", String.valueOf(R.mipmap.ic_flow));
                data.add(map4);
                Map<String, String> map5 = new HashMap<>();
                map5.put("Name","累计流量");
                map5.put("Value","null");
                map5.put("Image", String.valueOf(R.mipmap.ic_flow));
                data.add(map5);
                Map<String, String> map6 = new HashMap<>();
                map6.put("Name","进水温度");
                map6.put("Value","null");
                map6.put("Image", String.valueOf(R.mipmap.ic_temp));
                data.add(map6);
                Map<String, String> map7 = new HashMap<>();
                map7.put("Name","回水温度");
                map7.put("Value","null");
                map7.put("Image", String.valueOf(R.mipmap.ic_temp));
                data.add(map7);
                break;
            }
            case 4:{    //智能气表
                Map<String, String> map5 = new HashMap<>();
                map5.put("Name","当前使用量");
                map5.put("Value","null");
                map5.put("Image", String.valueOf(R.mipmap.meter_gas));
                data.add(map5);
                break;
            }
            default:break;
        }
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
                        Meter_Activity.this).inflate(R.layout.meter_listview, parent,false));
                return holder;
            }else {
                RecyclerView.ViewHolder holder= new viewHolder2(LayoutInflater.from(
                        Meter_Activity.this).inflate(R.layout.meter_listview_time, parent,false));
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
                Bitmap bitmap = BitmapFactory.decodeResource(Meter_Activity.this.getResources(), Integer.valueOf(map.get("Image")));
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

    private void getDataFromServer(){
        if (binder==null){
            return;
        }

        String order="";
        if (Meter_Type==1){
            order = "FE 68 "+amMeterAddr+" 68 1F 00";
            binder.sendOrder(order,3);
        }else if (Meter_Type==2){//水表
            order ="68 10 "+waterMeterAddr+"01 03 90 1F 00 90";
            binder.sendOrder(order,6);
        }else if (Meter_Type==3){
            order ="68 20 "+ heatMeterAddr +"01 03 90 1F 00 76";
            binder.sendOrder(order,6);
        }else{
            order ="68 30 "+ gasMeterAddr +" 01 03 90 1F 00 15";
            binder.sendOrder(order,6);
        }


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (refreshLayout.isRefreshing()){
                    refreshLayout.setRefreshing(false);
                }
            }
        }, 2000);
    }

    /**
     * 解析电表数据
     * @param datas
     */
    private void parseSingleMeter(byte[] datas) {

        // String ipaddr = (String) (map.get("ipaddr"));    //开关的IP地址
        String s = "";                       //保存命令的十六进制字符串
        for (int i = 0; i < datas.length; i++) {
            String s1 = Integer.toHexString((datas[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
            if (s1.length() == 1)
                s1 = "0" + s1;
            s = s + s1;
        }
        s = s.replace(" ", ""); //去掉空格
        s=s.toLowerCase();
        if(MainService1.IsInnerNet){
            //02 00 00 ff 00 57 1f 91
            if (!s.substring(0,6).equals("f1fefe"))
                return;
                s=s.substring(2,s.length());
        }else {
            //02 00 00 ff 00 57 1f 91
            if (!s.substring(0,20).equals("020000ff00571f91fefe")){
                return;
            }
                s=s.substring(16,s.length());
        }
        if (s.length()<50)
            return;//防止无效数据
        byte[] a = Converts.HexString2Bytes(s);
        try {
            double VA = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[13] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[12] & 0xff) - 51)}))) / 10.0;  //A相电压。单位是V
            double IA = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[16] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[15] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[14] & 0xff) - 51)}))) / 1000.0;  //A相电流，单位是A
            double Active_Power = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[19] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[18] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[17] & 0xff) - 51)}))) / 10000.0;  //有功功率，单位是kW
            double Powerrate = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[21] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[20] & 0xff) - 51)}))) / 1000.0;  //功率因数
            double Electricity = (Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[25] & 0xff) - 51)})) * 10000 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[24] & 0xff) - 51)})) * 100 + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[23] & 0xff) - 51)}))) + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) ((a[22] & 0xff) - 51)})) / 100.0;  //总用电量，单位是kWh
            LogUtil.i("电压为:"+VA);
            data.get(0).put("Value",Electricity+" 度");
            data.get(1).put("Value",VA+" V");
            data.get(2).put("Value",IA+" A");
            data.get(3).put("Value",Active_Power+" W");
            data.get(4).put("Value",Powerrate+"");
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            date=sdf.format(new java.util.Date());
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
        catch(Exception e){
            return;
        }
    }

    /**
     * 解析水表数据
     * @param datas
     */
    private void parseSmartWater(byte[] datas) {

        // String ipaddr = (String) (map.get("ipaddr"));    //开关的IP地址
        String s = "";                       //保存命令的十六进制字符串
        for (int i = 0; i < datas.length; i++) {
            String s1 = Integer.toHexString((datas[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
            if (s1.length() == 1)
                s1 = "0" + s1;
            s = s + s1;
        }
        s = s.replace(" ", ""); //去掉空格
        s=s.toLowerCase();
        if (MainService1.IsInnerNet){
            if (!s.substring(0,14).equals("f3fefefefe6810")){
                return;
            }
            s=s.substring(10,s.length());
        }else {
            if (!TextUtils.equals(s.substring(0,28),"020000ff00571f93fefefefe6810")){
                return;
            }
            s=s.substring(24,s.length());
        }
        if (s.length()<70)
        return;
        LogUtil.i(s);
        byte[] a = Converts.HexString2Bytes(s);
        try {

            double current = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[17] & 0xff)})) * 10000
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[16] & 0xff)})) * 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[15] & 0xff)})) * 1
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[14] & 0xff)})) / 100;

            double jiesuanri = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[22] & 0xff)})) * 10000
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[21] & 0xff)})) * 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[20] & 0xff)})) * 1
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[19] & 0xff)})) / 100;
            data.get(0).put("Value", current + " 吨");
            data.get(1).put("Value", jiesuanri + " 吨");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            date = sdf.format(new java.util.Date());
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }catch (Exception e){
            return;
        }
    }

    /**
     * 解析热量表数据
     * @param datas
     */
    private void parseHot(byte[] datas) {
        // String ipaddr = (String) (map.get("ipaddr"));    //开关的IP地址
        String s = Converts.Bytes2HexString(datas);         //保存命令的十六进制字符串
        Converts.Bytes2HexString(datas);
        s = s.replace(" ", ""); //去掉空格
        s=s.toLowerCase();
        if (MainService1.IsInnerNet){
            if (!TextUtils.equals(s.substring(0,6),"f36820")){
                return;
            }
            s=s.substring(2,s.length());
        }else {//                          020000ff00571f936820
            if (!s.substring(0,20).equals("020000ff00571f936820")){
                return;
            }
            s=s.substring(16,s.length());
        }
        if (s.length()<70)
            return;
        byte[] a = Converts.HexString2Bytes(s);
        try {

            double coldSum = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[14] & 0xff)})) / 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[15] & 0xff)}))
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[16] & 0xff)})) * 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[17] & 0xff)})) * 10000;
            double hotSum = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[19] & 0xff)})) / 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[20] & 0xff)}))
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[21] & 0xff)})) * 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[22] & 0xff)})) * 10000;
            double hotRate = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[24] & 0xff)})) / 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[25] & 0xff)}))
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[26] & 0xff)})) * 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[27] & 0xff)})) * 10000;
            double perFlow = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[29] & 0xff)})) / 10000
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[30] & 0xff)})) / 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[31] & 0xff)}))
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[32] & 0xff)})) * 100;
            double flowSum = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[34] & 0xff)})) / 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[35] & 0xff)}))
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[36] & 0xff)})) * 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[37] & 0xff)})) * 10000;
            double temIn = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[39] & 0xff)})) / 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[40] & 0xff)}))
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[41] & 0xff)})) * 100;
            double temOut = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[42] & 0xff)})) / 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[43] & 0xff)}))
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[44] & 0xff)})) * 100;

//        LogUtil.i("======>>"+coldSum+" kwh "+hotSum+"kwh");
        data.get(0).put("Value",coldSum+"kWh");
        data.get(1).put("Value",hotSum+"kWh");
        data.get(2).put("Value",hotRate+" ");
        data.get(3).put("Value",perFlow+"L/h");
        data.get(4).put("Value",flowSum+" L");
        data.get(5).put("Value",temIn+" ℃");
        data.get(6).put("Value",temOut+" ℃");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        date=sdf.format(new java.util.Date());
        UiUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        }catch (Exception e){
            return;//解析数据异常返回不解析
        }
    }
    /**
     * 解析气表数据
     * @param datas
     */
    private void parseAir(byte[] datas) {
        // String ipaddr = (String) (map.get("ipaddr"));    //开关的IP地址
        String s = Converts.Bytes2HexString(datas);         //保存命令的十六进制字符串
        Converts.Bytes2HexString(datas);
        s = s.replace(" ", ""); //去掉空格
        s=s.toLowerCase();
        System.out.println("气表数据sb="+s);
        if (MainService1.IsInnerNet){
            if (!s.substring(0,14).equals("f3fefefefe6830")){
                return;
            }
            s=s.substring(10,s.length());
        }else {
            if (!TextUtils.equals(s.substring(0,28),"020000ff00571f93fefefefe6830")){
                return;
            }
            s=s.substring(24,s.length());
        }
        byte[] a = Converts.HexString2Bytes(s);
//        LogUtil.i(s);
        try {

            double currentSum = Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[14] & 0xff)})) / 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[15] & 0xff)}))
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[16] & 0xff)})) * 100
                    + Integer.valueOf(Converts.Bytes2HexString(new byte[]{(byte) (a[17] & 0xff)})) * 10000;
            data.get(0).put("Value", currentSum + "m³");
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }catch (Exception e){
            return;
        }
    }


    //新建广播接收器，接收服务器的数据并解析，
    protected BroadcastReceiver broadcastreceiver=new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent){
            int count = intent.getIntExtra("ContentNum", 0);   //byte数组的长度
            byte[] data = intent.getByteArrayExtra("Content");  //内容数组
            String s = Converts.Bytes2HexString(data);

            if (Meter_Type==1){
                refreshLayout.setRefreshing(false);
                parseSingleMeter(data);
            }else if (Meter_Type==2){
                refreshLayout.setRefreshing(false);
                parseSmartWater(data);
            }else if (Meter_Type==3){
                refreshLayout.setRefreshing(false);
                parseHot(data);
            }else {
                refreshLayout.setRefreshing(false);
                parseAir(data);
            }

        }
    };//广播接收器


    private class RefreshThread extends Thread{
        @Override
        public void run() {
            LogUtil.i("自动刷新程序开始执行...");
            while(isRefresh){
                try {
                    int i =1;
                    while (i==1){
                        if (binder!=null){
                            getDataFromServer();
                            i=2;
                        }
                    }

                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    ///下拉刷新处理的函数。
    private class GetDataTask extends AsyncTask<Void, Void, String> {
        // 后台处理部分
        @Override
        protected String doInBackground(Void... params) {
            // Simulates a background job.
            String str = "1";
            try {
                Thread.sleep(500);
                getDataFromServer();
                str = "1"; // 表示请求成功
                Thread.sleep(1000);
            } catch (Exception e1) {

                e1.printStackTrace();
                str = "0"; // 表示请求失败
            }
            return str;
        }

        //这里是对刷新的响应，可以利用addFirst（）和addLast()函数将新加的内容加到LISTView中
        //根据AsyncTask的原理，onPostExecute里的result的值就是doInBackground()的返回值
        @Override
        protected void onPostExecute(String result) {

            if(result.equals("1"))  //请求数据成功，根据显示的页面重新初始化listview
            {

            }
            else            //请求数据失败
            {
                Toast.makeText(getApplicationContext(), "刷新失败！", Toast.LENGTH_SHORT).show();
            }
            // Call onRefreshComplete when the list has been refreshed.
            if (refreshLayout.isRefreshing())
                refreshLayout.setRefreshing(false);   //结束加载动作
            super.onPostExecute(result);//这句是必有的，AsyncTask规定的格式
        }
    }
}
