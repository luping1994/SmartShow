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
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseActivity1;
import com.suntrans.smartshow.service.SmartHomeService;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by pc on 2016/9/15.
 * 智能家居开关控制页面
 */
public class SmartRoomDetails_Activity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tv_title;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private   ArrayList<Map<String,String>> state1;
    private   ArrayList<Map<String,String>> state2;
    private   ArrayList<Map<String,String>> state3;
    private String road_addr1 = "00010001";
    private String road_addr2 = "00010002";
    private String road_addr3 = "00010003";
    private  byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private ArrayList<Map<String, String>> data = new ArrayList<>();
    private  SmartHomeService.ibinder binder;  //用于Activity与Service通信

    private int area;
    private ServiceConnection con = new ServiceConnection() {
        //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("绑定成功");
            binder=(SmartHomeService.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
            Log.v("Time", "绑定后时间：" + String.valueOf(System.currentTimeMillis()));
        }

        @Override   //service因异常而断开的时候调用此方法
        public void onServiceDisconnected(ComponentName name) {
            Log.v("Time", "绑定失败");
        }
    };   ///用

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent1 = new Intent(getApplicationContext(), SmartHomeService.class);    //指定要绑定的service
        bindService(intent1, con, Context.BIND_AUTO_CREATE);   //绑定主service
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE1");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive
        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver

        StatusBarCompat.compat(this, Color.TRANSPARENT);//设置状态栏为透明颜色
        setContentView(R.layout.smartroom_detail_activity);
        //初始化控件
        initViews(savedInstanceState);
        //初始化ToolBar
        initToolBar();
        initData();
    }

    public void initViews(Bundle savedInstanceState) {

        Intent intent = getIntent();
        area = intent.getIntExtra("area", 0);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tv_title = (TextView) findViewById(R.id.tv_title);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
    }

    public void initData() {
        data.clear();
        switch (area) {
            case 0:
                Map<String, String> map1 = new HashMap<>();
                map1.put("Name", "客厅插座");
                map1.put("Image", String.valueOf(R.drawable.ic_chazuo_off));
                data.add(map1);

                Map<String, String> map2 = new HashMap<>();
                map2.put("Name", "电视机");
                map2.put("Image", String.valueOf(R.drawable.ic_tv1_off));
                data.add(map2);

                Map<String, String> map3 = new HashMap<>();
                map3.put("Name", "客厅灯");
                map3.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                data.add(map3);

                Map<String, String> map4 = new HashMap<>();
                map4.put("Name", "客厅壁灯");
                map4.put("Image", String.valueOf(R.drawable.ic_wall_off));
                data.add(map4);
                break;
            case 1:
                Map<String, String> map5 = new HashMap<>();
                map5.put("Name", "餐厅灯");
                map5.put("Image", String.valueOf(R.drawable.ic_wall_off));
                data.add(map5);

                Map<String, String> map6 = new HashMap<>();
                map6.put("Name", "冰箱");
                map6.put("Image", String.valueOf(R.drawable.ic_binxiang_off));
                data.add(map6);

                Map<String, String> map7 = new HashMap<>();
                map7.put("Name", "餐厅插座");
                map7.put("Image", String.valueOf(R.drawable.ic_chazuo_off));
                data.add(map7);
                break;
            case 2:
                Map<String, String> map8 = new HashMap<>();
                map8.put("Name", "吸油烟机");
                map8.put("Image", String.valueOf(R.drawable.ic_smoke_off));
                data.add(map8);

                Map<String, String> map9 = new HashMap<>();
                map9.put("Name", "微波炉");
                map9.put("Image", String.valueOf(R.drawable.ic_weibolu_off));
                data.add(map9);

                Map<String, String> map10 = new HashMap<>();
                map10.put("Name", "厨房灯");
                map10.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                data.add(map10);
            case 3:
                Map<String, String> map11 = new HashMap<>();
                map11.put("Name", "书房灯");
                map11.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                data.add(map11);

                Map<String, String> map12 = new HashMap<>();
                map12.put("Name", "书房空调");
                map12.put("Image", String.valueOf(R.drawable.ic_weibolu_off));
                data.add(map12);

                Map<String, String> map13 = new HashMap<>();
                map13.put("Name", "电脑");
                map13.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                data.add(map13);
                break;
        }
        //开关一状态
        state1= new ArrayList<Map<String, String>>(10);
        state2= new ArrayList<Map<String, String>>(10);
        state3= new ArrayList<Map<String, String>>(10);
        //默认初始化全部开关为关闭
        for (int i=0;i<10;i++){
            Map<String,String> map = new HashMap<String,String>();
            map.put("state","0");
            state1.add(map);
            state2.add(map);
            state3.add(map);
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter= new mAdapter();
        recyclerView.setAdapter(adapter);

        refreshLayout.setColorSchemeResources(android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_light);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSwitchStateFromServer();
            }
        });
//        getSwitchStateFromServer();

    }


    private mAdapter adapter;

    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            String title = "";//biaoti
            switch (area) {
                case 0:title = "客厅"; break;
                case 1:title = "餐厅";break;
                case 2:title = "厨房";break;
                case 3:title = "书房";break;
                case 4:title = "卫生间";break;
                case 5:title = "主卧";break;
                case 6:title = "次卧";break;
                case 7:title = "走廊";break;
                case 8:title = "阳台";break;
                default:break;
            }
            tv_title.setText(title);
        }
    }



    /**
     * RecyclerView适配器
     * *自定义Recyclerview的适配器,主要的执行顺序：getItemViewType==>onCreateViewHolder==>onBindViewHolder
     */
    class mAdapter extends RecyclerView.Adapter {
        /****
         * 渲染具体的布局，根据viewType选择使用哪种布局
         *
         * @param parent   父容器
         * @param viewType 布局类别，多种布局的情况定义多个viewholder
         * @return
         */
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            RecyclerView.ViewHolder holder = new SmartRoomDetails_Activity.mAdapter.viewHolder1(LayoutInflater.from(
                    SmartRoomDetails_Activity.this).inflate(R.layout.road_bulb_item, parent, false));
            return holder;
        }

        /***
         * 绑定数据
         *
         * @param holder   绑定哪个holder，用if(holder instanceof mViewHolder1)来判断类型，再绑定数据
         * @param position
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            String name = data.get(position).get("name");
            int id = Integer.valueOf(data.get(position).get("Image"));

            Bitmap bitmap = BitmapFactory.decodeResource(SmartRoomDetails_Activity.this.getResources(), id);
            bitmap = Converts.toRoundCorner(bitmap, UiUtils.dip2px(20));

            ((viewHolder1) holder).image.setImageBitmap(bitmap);
            ((viewHolder1) holder).textView.setText(data.get(position).get("Name"));
            ((viewHolder1) holder).image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (area==0){
                        switch (position){
                            case 0:
                                binder.sendOrder("aa68 00010001 06 0304 0001",2);
                        }
                    }
                }
            });

        }


        @Override
        public int getItemCount() {
            return data.size();
        }

        /**
         * 决定元素的布局使用哪种类型
         * 在本activity中，布局1使用R.layout.roomgridview，
         *
         * @param position 数据源的下标
         * @return 一个int型标志，传递给onCreateViewHolder的第二个参数
         */
        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 布局类型1对应的ViewHolder，R.layout.listmain_userinfo
         */
        class viewHolder1 extends RecyclerView.ViewHolder {
            ImageView image;    //图标
            TextView textView;

            public viewHolder1(View view) {
                super(view);
                image = (ImageView) view.findViewById(R.id.iv);
                textView = (TextView) view.findViewById(R.id.name);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {

        unregisterReceiver(broadcastreceiver);  //注销广播接收者
        unbindService(con);   //解除Service的绑定

        super.onDestroy();
    }

    /**
     * 获取不同房间的开关状态
     */
    private void getSwitchStateFromServer() {

        switch (area){
            case 0://客厅，只需获取00010001开关2，6，7，9通道的状态
                String order = "aa68"+road_addr1+"03 0100"+"0007";
                binder.sendOrder(order,2);
                break;
        }
    }
    /**
     * 解析返回的数据：
     */
    private void parseData(String s) {
        String addr;
        if (s.length()>20){
            addr= s.substring(11,12);   //返回数据的开关地址最后一位
            System.out.println("Fuck！！！！！！！！！！！返回的命令为s=:"+s+"地址为"+addr);
            byte[] a = Converts.HexString2Bytes(s);
            if (TextUtils.equals("1",addr)){//若是开关一
                if (s.substring(12, 14).equals("03"))   //如果是读寄存器状态，解析出开关状态
                {
                    if (s.substring(14, 16).equals("0e")||s.substring(14,16).equals("07"))
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

                        for(int i= 0;i<state1.size();i++){
                            if (TextUtils.equals(addr,"0")){
                                state1.get(i).put("state",states[i]);
                            }
                        }

                    }
                }
                else if(s.substring(12,14).equals("06"))   //单个通道状态发生改变
                {
                    int k=0;         //k是通道号
                    int state=Integer.valueOf(s.substring(21, 22));  //开关状态，1代表打开，0代表关闭
                    if(s.substring(17,18).equals("a"))
                        k=10;
                    else
                        k=Integer.valueOf(s.substring(17, 18));   //通道号,int型
                    if(k==0)                                          //如果通道号为0，则是总开关
                    {
                        if (state==0){
                            for (int i=0;i<state1.size();i++){
                                if (TextUtils.equals(addr,"0")){
                                    state1.get(i).put("state","0");
                                }
                            }
                        }
                    }
                    else     //如果通道号不为0，则更改data中的状态，并更新
                    {
//                                    String[] state2={"0","0","0","0","0","0","0","0","0","0"};   //十个通道的状态，state[0]对应1通道
//                                    state2[k-1] = state+"";
                        for (int i=0;i<state1.size();i++){
                            if (TextUtils.equals(addr,"0")){
                                if (state1.get(i).get("position").equals(String.valueOf(k-1))){
                                    state1.get(i).put("state",state==1?"1":"0");
                                }
                            }

                        }
                    }
                }
                for (int i=0;i<state1.size();i++){
                    LogUtil.i(state1.get(i).get("state"));
                }
            }
        }


    }//parsedata结束


    private String return_addr;//开关地址
    private String s;//收到的命令
    //新建广播接收器，接收服务器的数据并解析，
    protected BroadcastReceiver broadcastreceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes = intent.getByteArrayExtra("Content");
            if (bytes.length < 10) {
                UiUtils.showToast(UiUtils.getContext(), "刷新失败");
                refreshLayout.setRefreshing(false);
                return;
            }
            s = Converts.Bytes2HexString(bytes);
            s = s.split("0d0a")[0] + "0d0a";
            if (s.length() > 20) {
//                s = s.substring(2, s.length());
                return_addr = s.substring(4, 12);   //返回数据的开关地址
                System.out.println("Fuck！！！！！！！！！！！返回的命令为s=:" + s);
                byte a[] = Converts.HexString2Bytes(s);
                if (s.substring(12, 14).equals("03"))   //如果是读寄存器状态，解析出开关状态
                {
                    if (s.substring(14, 16).equals("0E") || s.substring(14, 16).equals("07")) {
                        String[] states = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};   //十个通道的状态，state[0]对应1通道
                        for (int i = 0; i < 8; i++)   //先获取前八位的开关状态
                        {
                            states[i] = ((a[9] & bits[i]) == bits[i]) ? "1" : "0";   //1-8通道

                        }
                        for (int i = 0; i < 2; i++) {
                            states[i + 8] = ((a[8] & bits[i]) == bits[i]) ? "1" : "0";  //9、10通道

                        }

                        for (int i = 0; i < state1.size(); i++) {
                            state1.get(i).put("state", states[i]);
                        }

                    }
                } else if (s.substring(12, 14).equals("06"))   //单个通道状态发生改变
                {
                    int k = 0;         //k是通道号
                    int state = Integer.valueOf(s.substring(21, 22));  //开关状态，1代表打开，0代表关闭
                    if (s.substring(17, 18).equals("A"))
                        k = 10;
                    else
                        k = Integer.valueOf(s.substring(17, 18));   //通道号,int型
                    if (k == 0)                                          //如果通道号为0，则是总开关
                    {
                        if (state == 0) {
                            for (int i = 0; i < state1.size(); i++) {
                                state1.get(i).put("state", "0");
                            }
                        }
                    } else     //如果通道号不为0，则更改data中的状态，并更新
                    {
//                                    String[] state2={"0","0","0","0","0","0","0","0","0","0"};   //十个通道的状态，state[0]对应1通道
//                                    state2[k-1] = state+"";
                        for (int i = 0; i < state1.size(); i++) {
                            if (state1.get(i).get("position").equals(String.valueOf(k - 1))) {
                                state1.get(i).put("state", state == 1 ? "1" : "0");
                            }
                        }
                    }
                }
                if (adapter != null) {
                    for (int i = 0; i < state1.size(); i++) {
                        System.out.println(state1.get(i).get("state"));
                    }
                    refreshLayout.setRefreshing(false);
                    UiUtils.showToast(UiUtils.getContext(), "刷新成功！");
                    adapter.notifyDataSetChanged();
//                }
                }

            }
        }

    } ;//广播接收器
}

