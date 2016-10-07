package com.suntrans.smartshow.activity;

import android.app.ProgressDialog;
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
import android.os.Message;
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
import android.widget.Toast;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.service.MainService2;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.utils.ThreadManager;
import com.suntrans.smartshow.utils.UiUtils;
import com.suntrans.smartshow.views.LoadingDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.media.CamcorderProfile.get;
import static com.suntrans.smartshow.R.layout.smartroom_detail_activity;


/**
 * Created by pc on 2016/9/15.
 * 智能家居开关控制页面
 */
public class SmartRoomDetails_Activity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tv_title;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private   ArrayList<Map<String,String>> state1;//00010001开关状态
    private   ArrayList<Map<String,String>> state2;//00010002开关状态
    private   ArrayList<Map<String,String>> state3;//00010003开关状态
    private String road_addr1 = "00010001";
    private String road_addr2 = "00010002";
    private String road_addr3 = "00010003";
    private  byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private ArrayList<Map<String, String>> data = new ArrayList<>();//存储各个通道的名称和当前状态
    private  MainService2.ibinder binder;  //用于Activity与Service通信
    private ProgressDialog progressdialog;
    private int area;
    boolean isrun = true;
    Handler handler = new Handler();
    private mAdapter adapter;
    private ServiceConnection con = new ServiceConnection() {
        //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("绑定成功");
            binder=(MainService2.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
            Log.v("Time", "绑定后时间：" + String.valueOf(System.currentTimeMillis()));
        }

        @Override   //service因异常而断开的时候调用此方法
        public void onServiceDisconnected(ComponentName name) {
            Log.v("Time", "绑定失败");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent1 = new Intent(getApplicationContext(), MainService2.class);    //指定要绑定的service
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
        dialog = new LoadingDialog(this);
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
                map1.put("state","0");
                map1.put("dot",String.valueOf(R.drawable.ic_dot_off));
                data.add(map1);

                Map<String, String> map2 = new HashMap<>();
                map2.put("Name", "电视机");
                map2.put("Image", String.valueOf(R.drawable.ic_tv1_off));
                map2.put("state","0");
                map2.put("dot",String.valueOf(R.drawable.ic_dot_off));
                data.add(map2);

                Map<String, String> map3 = new HashMap<>();
                map3.put("Name", "客厅灯");
                map3.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                map3.put("state","0");
                map3.put("dot",String.valueOf(R.drawable.ic_dot_off));
                data.add(map3);

                Map<String, String> map4 = new HashMap<>();
                map4.put("Name", "客厅壁灯");
                map4.put("Image", String.valueOf(R.drawable.ic_wall_off));
                map4.put("state","0");
                map4.put("dot",String.valueOf(R.drawable.ic_dot_off));
                data.add(map4);
                break;
            case 1:
                Map<String, String> map5 = new HashMap<>();
                map5.put("Name", "餐厅灯");
                map5.put("state","0");
                map5.put("Image", String.valueOf(R.drawable.ic_wall_off));
                map5.put("dot",String.valueOf(R.drawable.ic_dot_off));
                data.add(map5);

                Map<String, String> map6 = new HashMap<>();
                map6.put("Name", "冰箱");
                map6.put("state","0");
                map6.put("Image", String.valueOf(R.drawable.ic_binxiang_off));
                map6.put("dot",String.valueOf(R.drawable.ic_dot_off));
                data.add(map6);

                Map<String, String> map7 = new HashMap<>();
                map7.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map7.put("state","0");
                map7.put("Name", "餐厅插座");
                map7.put("Image", String.valueOf(R.drawable.ic_chazuo_off));
                data.add(map7);
                break;
            case 2:
                Map<String, String> map8 = new HashMap<>();
                map8.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map8.put("state","0");
                map8.put("Name", "吸油烟机");
                map8.put("Image", String.valueOf(R.drawable.ic_smoke_off));
                data.add(map8);

                Map<String, String> map9 = new HashMap<>();
                map9.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map9.put("state","0");
                map9.put("Name", "微波炉");
                map9.put("Image", String.valueOf(R.drawable.ic_weibolu_off));
                data.add(map9);

                Map<String, String> map10 = new HashMap<>();
                map10.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map10.put("state","0");
                map10.put("Name", "厨房灯");
                map10.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                data.add(map10);
                break;
            case 3:
                Map<String, String> map11 = new HashMap<>();
                map11.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map11.put("state","0");
                map11.put("Name", "书房灯");
                map11.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                data.add(map11);

                Map<String, String> map12 = new HashMap<>();
                map12.put("Name", "书房空调");
                map12.put("Image", String.valueOf(R.drawable.ic_kongtiao_off));
                map12.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map12.put("state","0");
                data.add(map12);

                Map<String, String> map13 = new HashMap<>();
                map13.put("Name", "电脑");
                map13.put("Image", String.valueOf(R.drawable.ic_computer_off));
                map13.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map13.put("state","0");
                data.add(map13);
                break;
            case 4:
                Map<String, String> map14 = new HashMap<>();
                map14.put("Name", "卫生间灯");
                map14.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                map14.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map14.put("state","0");
                data.add(map14);

                Map<String, String> map15 = new HashMap<>();
                map15.put("Name", "热水器");
                map15.put("Image", String.valueOf(R.drawable.ic_hotwater_off));
                map15.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map15.put("state","0");
                data.add(map15);

                Map<String, String> map16 = new HashMap<>();
                map16.put("Name", "卫生间插座");
                map16.put("Image", String.valueOf(R.drawable.ic_chazuo_off));
                map16.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map16.put("state","0");
                data.add(map16);
                break;
            case 5:
                Map<String, String> map17 = new HashMap<>();
                map17.put("Name", "主卧壁灯");
                map17.put("Image", String.valueOf(R.drawable.ic_wall_off));
                map17.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map17.put("state","0");
                data.add(map17);

                Map<String, String> map18 = new HashMap<>();
                map18.put("Name", "主卧阳台灯");
                map18.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                map18.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map18.put("state","0");
                data.add(map18);

                Map<String, String> map19 = new HashMap<>();
                map19.put("Name", "主卧灯");
                map19.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                map19.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map19.put("state","0");
                data.add(map19);

                Map<String, String> map20 = new HashMap<>();
                map20.put("Name", "主卧空调");
                map20.put("Image", String.valueOf(R.drawable.ic_kongtiao_off));
                map20.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map20.put("state","0");
                data.add(map20);

                Map<String, String> map21 = new HashMap<>();
                map21.put("Name", "主卧插座");
                map21.put("Image", String.valueOf(R.drawable.ic_chazuo_off));
                map21.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map21.put("state","0");
                data.add(map21);
                break;
            case 6:
                Map<String, String> map22 = new HashMap<>();
                map22.put("Name", "次卧插座");
                map22.put("Image", String.valueOf(R.drawable.ic_chazuo_off));
                map22.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map22.put("state","0");
                data.add(map22);

                Map<String, String> map23 = new HashMap<>();
                map23.put("Name", "次卧空调");
                map23.put("Image", String.valueOf(R.drawable.ic_kongtiao_off));
                map23.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map23.put("state","0");
                data.add(map23);

                Map<String, String> map24 = new HashMap<>();
                map24.put("Name", "次卧灯");
                map24.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                map24.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map24.put("state","0");
                data.add(map24);

                Map<String, String> map25 = new HashMap<>();
                map25.put("Name", "次卧壁灯");
                map25.put("Image", String.valueOf(R.drawable.ic_wall_off));
                map25.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map25.put("state","0");
                data.add(map25);
                break;
            case 7:
                Map<String, String> map26 = new HashMap<>();
                map26.put("Name", "走廊灯");
                map26.put("Image", String.valueOf(R.drawable.ic_bulb_off));
                map26.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map26.put("state","0");
                data.add(map26);
                break;
            case 8:
                Map<String, String> map27 = new HashMap<>();
                map27.put("Name", "洗衣机");
                map27.put("Image", String.valueOf(R.drawable.ic_xiyiji_off));
                map27.put("dot",String.valueOf(R.drawable.ic_dot_off));
                map27.put("state","0");
                data.add(map27);
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
            map.put("position", String.valueOf(i));
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
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                if (binder!=null){
                    getSwitchStateFromServer();
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
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSwitchStateFromServer();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                            UiUtils.showToast(UiUtils.getContext(),"请求服务器失败，请稍后再试");
                        }
                    }
                }, 2000);
            }
        });
        ThreadManager.getInstance().createLongPool().execute(new Runnable() {
            @Override
            public void run() {
                while (isrun){
                    if (binder!=null){
                        getSwitchStateFromServer();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    public void initToolBar() {

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
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
//        private int[] ketingBitmapId_off={R.drawable.ic_chazuo_off,R.drawable.ic_tv1_off,R.drawable.ic_bulb_off,R.drawable.ic_wall_off};
//        private int[] ketingBitmapId_on={R.drawable.ic_chazuo_off,R.drawable.ic_tv1_off,R.drawable.ic_bulb_off,R.drawable.ic_wall_off};

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
            int idDot = Integer.valueOf(data.get(position).get("dot"));
            Bitmap bitmap = BitmapFactory.decodeResource(SmartRoomDetails_Activity.this.getResources(), id);
            Bitmap bitmapDot = BitmapFactory.decodeResource(SmartRoomDetails_Activity.this.getResources(), idDot);

            bitmap = Converts.toRoundCorner(bitmap, UiUtils.dip2px(20));
            ((viewHolder1) holder).image.setImageBitmap(bitmap);
            ((viewHolder1) holder).dot.setImageBitmap(bitmapDot);
            ((viewHolder1) holder).textView.setText(data.get(position).get("Name"));
            ((viewHolder1) holder).image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parseClick(v,position);
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
            ImageView dot;    //图标

            public viewHolder1(View view) {
                super(view);
                image = (ImageView) view.findViewById(R.id.iv);
                textView = (TextView) view.findViewById(R.id.name);
                dot = (ImageView)view.findViewById(R.id.dot);
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
        isrun =false;
        unregisterReceiver(broadcastreceiver);  //注销广播接收者
        unbindService(con);   //解除Service的绑定
        super.onDestroy();
    }

    /**
     * 获取不同房间的开关状态
     */
    private void getSwitchStateFromServer() {
        String order;
        switch (area){
            case 0://客厅，只需获取00010001开关2，6，7，9通道的状态
            case 2://餐厅
            case 6://次卧室
            case 8://阳台洗衣机
                order = "aa68"+road_addr1+"03 0100"+"0007";
                binder.sendOrder(order,2);
                break;
            case 1://餐厅
            case 4://卫生间
                order = "aa68"+road_addr2+"03 0100"+"0007";
                binder.sendOrder(order,2);
                break;
            case 3://书房
            case 5://主卧室
            case 7://走廊
                order = "aa68"+road_addr3+"03 0100"+"0007";
                binder.sendOrder(order,2);
                break;
            //aa68000100030301000007cf310d0a
            // ab68aa68000100030301000007cf310d0a

        }
    }


    private String return_addr;//命令返回的开关地址末位
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
            s=s.toLowerCase();
            if (MainService2.isInnerNet1){
//                if (!s.substring(0,8).equals("aa690001"))
//                    return;
            }else {
//                if (!s.substring(0,12).equals("ab68aa690001"))
//                    return;
                s=s.substring(4,s.length());
            }
            if (s.length() > 20) {
                return_addr = s.substring(4, 12);   //返回数据的开关地址
                System.out.println("Fuck！！！！！！！！！！！返回的命令为s=:" + s);
                byte a[] = Converts.HexString2Bytes(s);
                if (s.substring(12, 14).equals("03"))   //如果是读寄存器状态，解析出开关状态
                {
                    if (s.substring(14, 16).equals("0e") || s.substring(14, 16).equals("07")) {
                        String[] states = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};   //十个通道的状态，state[0]对应1通道
                        for (int i = 0; i < 8; i++)   //先获取前八位的开关状态
                        {
                            states[i] = ((a[9] & bits[i]) == bits[i]) ? "1" : "0";   //1-8通道

                        }
                        for (int i = 0; i < 2; i++) {
                            states[i + 8] = ((a[8] & bits[i]) == bits[i]) ? "1" : "0";  //9、10通道

                        }

                        for (int i = 0; i < state1.size(); i++) {//更新状态到集合中
                            if (return_addr.equals("00010001")){
                                state1.get(i).put("state", states[i]);
                            }else if (return_addr.equals("00010002")){
                                state2.get(i).put("state", states[i]);
                            }else if (return_addr.equals("00010003")){
                                state3.get(i).put("state",states[i]);
                            }
                        }

                    }
                } else if (s.substring(12, 14).equals("06"))   //单个通道状态发生改变
                {
                    int k = 0;         //k是通道号
                    int state = Integer.valueOf(s.substring(21, 22));  //开关状态，1代表打开，0代表关闭
                    if (s.substring(17, 18).equals("a"))
                        k = 10;
                    else
                        k = Integer.valueOf(s.substring(17, 18));   //通道号,int型
                    if (k == 0)                                          //如果通道号为0，则是总开关
                    {
                        if (state == 0) {
                            for (int i = 0; i < state1.size(); i++) {//更新状态到集合中
                                if (return_addr.equals("00010001")){
                                    state1.get(i).put("state", "0");
                                }else if (return_addr.equals("00010002")){
                                    state2.get(i).put("state", "0");
                                }else if (TextUtils.equals(return_addr,road_addr3)){
                                    state3.get(i).put("state", "0");
                                }
                            }
                        }
                    } else     //如果通道号不为0，则更改data中的状态，并更新
                    {
//                                    String[] state2={"0","0","0","0","0","0","0","0","0","0"};   //十个通道的状态，state[0]对应1通道
//                                    state2[k-1] = state+"";
                        for (int i = 0; i < state1.size(); i++) {
                            if (state1.get(i).get("position").equals(String.valueOf(k - 1))) {
                                if (return_addr.equals("00010001")){
                                    state1.get(i).put("state", state == 1 ? "1" : "0");
//                                    System.out.println("当前通道"+state1.get(i).get("state"));
                                }else if (return_addr.equals("00010002")){
                                    state2.get(i).put("state", state == 1 ? "1" : "0");
//                                    LogUtil.i("当前通道"+state1.get(i).get("state"));
                                }else if (TextUtils.equals(return_addr,road_addr3)){
                                    state3.get(i).put("state", state == 1 ? "1" : "0");
                                }
                            }
                        }
                    }
                }

                    if (area==0){
                        //更新保存的状态
                         data.get(0).put("state",state1.get(1).get("state"));
                         data.get(1).put("state",state1.get(5).get("state"));
                         data.get(2).put("state",state1.get(6).get("state"));
                         data.get(3).put("state",state1.get(8).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_chazuo_off:R.drawable.ic_chazuo_on));
                        data.get(1).put("Image",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_tv1_off:R.drawable.ic_tv1_on));
                        data.get(2).put("Image",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_bulb_off:R.drawable.ic_bulb_on));
                        data.get(3).put("Image",String.valueOf((data.get(3).get("state").equals("0"))?R.drawable.ic_wall_off:R.drawable.ic_wall_on));

                        data.get(0).put("dot",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(1).put("dot",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(2).put("dot",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(3).put("dot",String.valueOf((data.get(3).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));

                    }else if (area==1){
                        data.get(0).put("state",state2.get(1).get("state"));
                        data.get(1).put("state",state2.get(2).get("state"));
                        data.get(2).put("state",state2.get(7).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_wall_off:R.drawable.ic_wall_on));
                        data.get(1).put("Image",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_binxiang_off:R.drawable.ic_bingxiang_on));
                        data.get(2).put("Image",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_chazuo_off:R.drawable.ic_chazuo_on));

                        data.get(0).put("dot",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(1).put("dot",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(2).put("dot",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                    }else if (area==2){
                        data.get(0).put("state",state2.get(0).get("state"));
                        data.get(1).put("state",state2.get(3).get("state"));
                        data.get(2).put("state",state2.get(4).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_smoke_off:R.drawable.ic_smoke_on));
                        data.get(1).put("Image",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_weibolu_off:R.drawable.ic_wobolu_on));
                        data.get(2).put("Image",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_bulb_off:R.drawable.ic_bulb_on));

                        data.get(0).put("dot",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(1).put("dot",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(2).put("dot",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                    }else if (area==3){
                        data.get(0).put("state",state3.get(5).get("state"));
                        data.get(1).put("state",state3.get(6).get("state"));
                        data.get(2).put("state",state3.get(8).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_bulb_off:R.drawable.ic_bulb_on));
                        data.get(1).put("Image",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_kongtiao_off:R.drawable.ic_kongtiao_on));
                        data.get(2).put("Image",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_computer_off:R.drawable.ic_computer_on));

                        data.get(0).put("dot",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(1).put("dot",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(2).put("dot",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                    }else if (area==4){
                        //更新状态
                        data.get(0).put("state",state2.get(5).get("state"));
                        data.get(1).put("state",state2.get(6).get("state"));
                        data.get(2).put("state",state2.get(8).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_bulb_off:R.drawable.ic_bulb_on));
                        data.get(1).put("Image",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_hotwater_off:R.drawable.ic_hotwater_on));
                        data.get(2).put("Image",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_chazuo_off:R.drawable.ic_chazuo_on));

                        data.get(0).put("dot",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(1).put("dot",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(2).put("dot",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                    }else if(area==5){
                        //更新状态
                        data.get(0).put("state",state3.get(0).get("state"));
                        data.get(1).put("state",state3.get(1).get("state"));
                        data.get(2).put("state",state3.get(2).get("state"));
                        data.get(3).put("state",state3.get(3).get("state"));
                        data.get(4).put("state",state3.get(7).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_wall_off:R.drawable.ic_wall_on));
                        data.get(1).put("Image",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_bulb_off:R.drawable.ic_bulb_on));
                        data.get(2).put("Image",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_bulb_off:R.drawable.ic_bulb_on));
                        data.get(3).put("Image",String.valueOf((data.get(3).get("state").equals("0"))?R.drawable.ic_kongtiao_off:R.drawable.ic_kongtiao_on));
                        data.get(4).put("Image",String.valueOf((data.get(4).get("state").equals("0"))?R.drawable.ic_chazuo_off:R.drawable.ic_chazuo_on));

                        data.get(0).put("dot",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(1).put("dot",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(2).put("dot",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(3).put("dot",String.valueOf((data.get(3).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(4).put("dot",String.valueOf((data.get(4).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));

                    }else if (area==6){//侧卧室
                        //更新状态
                        data.get(0).put("state",state1.get(0).get("state"));
                        data.get(1).put("state",state1.get(2).get("state"));
                        data.get(2).put("state",state1.get(3).get("state"));
                        data.get(3).put("state",state1.get(4).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_chazuo_off:R.drawable.ic_chazuo_on));
                        data.get(1).put("Image",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_kongtiao_off:R.drawable.ic_kongtiao_on));
                        data.get(2).put("Image",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_bulb_off:R.drawable.ic_bulb_on));
                        data.get(3).put("Image",String.valueOf((data.get(3).get("state").equals("0"))?R.drawable.ic_wall_off:R.drawable.ic_wall_on));

                        data.get(0).put("dot",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(1).put("dot",String.valueOf((data.get(1).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(2).put("dot",String.valueOf((data.get(2).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                        data.get(3).put("dot",String.valueOf((data.get(3).get("state").equals("0"))?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                    }else if (area==7){
                        data.get(0).put("state",state3.get(4).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_bulb_off:R.drawable.ic_bulb_on));

                        data.get(0).put("dot",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                    }else if (area==8){
                        data.get(0).put("state",state1.get(7).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_xiyiji_off:R.drawable.ic_xiyiji_on));

                        data.get(0).put("dot",String.valueOf(TextUtils.equals(data.get(0).get("state"),"0")?R.drawable.ic_dot_off:R.drawable.ic_dot_on));
                    }
                if (adapter != null) {
                    showSuccessDialog();
                    UiUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (refreshLayout.isRefreshing())
                            refreshLayout.setRefreshing(false);
//                            UiUtils.showToast(UiUtils.getContext(), "success！");
                            adapter.notifyDataSetChanged();
                        }
                    });

                }

            }
        }

    } ;//广播接收器


    private void parseClick(View v, int position) {
       showFailedDialog();
        if (area==0){
            switch (position){//客厅对应1号开关2，6，7，8通道
                case 0:
                    if(data.get(0).get("state").equals("0")){
                        binder.sendOrder("aa68"+road_addr1+"06 0302 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr1+"06 0302 0000",2);
                    }
                    break;
                case 1:
                    if(data.get(1).get("state").equals("0")){
                        binder.sendOrder("aa68"+road_addr1+"06 0306 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr1+"06 0306 0000",2);
                    }
                    break;
                case 2:
                    if(data.get(2).get("state").equals("0")){
                        binder.sendOrder("aa68 "+road_addr1+"06 0307 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr1+"06 0307 0000",2);
                    }
                    break;
                case 3:
                    if(data.get(3).get("state").equals("0")){
                        binder.sendOrder("aa68"+road_addr1+"06 0309 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr1+"06 0309 0000",2);
                    }
                    break;
            }
        }else if (area==1){//餐厅
            switch (position){
                case 0:
                    if(data.get(0).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr2+"06 0302 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr2 +"06 0302 0000",2);
                    }
                    break;
                case 1:
                    if(data.get(1).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr2+"06 0303 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr2+"06 0303 0000",2);
                    }
                    break;
                case 2:
                    if(data.get(2).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr2+"06 0308 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr2 +"06 0308 0000",2);
                    }
                    break;
            }
        }else if (area==2){//厨房
            switch (position){
                case 0:
                    if(data.get(0).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr2+"06 0301 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr2 +"06 0301 0000",2);
                    }
                    break;
                case 1:
                    if(data.get(1).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr2+"06 0304 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr2+"06 0304 0000",2);
                    }
                    break;
                case 2:
                    if(data.get(2).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr2+"06 0305 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr2 +"06 0305 0000",2);
                    }
                    break;
            }
        }else if (area==3){//书房
            switch (position){
                case 0:
                    if(data.get(0).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr3+"06 0306 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr3+"06 0306 0000",2);
                    }
                    break;
                case 1:
                    if(data.get(1).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr3+"06 0307 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr3+"06 0307 0000",2);
                    }
                    break;
                case 2:
                    if(data.get(2).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr3+"06 0309 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr3+"06 0309 0000",2);
                    }
                    break;
            }
        }else if (area==4){//卫生间
            switch (position){
                case 0:
                    if(data.get(0).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr2+"06 0306 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr2+"06 0306 0000",2);
                    }
                    break;
                case 1:
                    if(data.get(1).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr2+"06 0307 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr2+"06 0307 0000",2);
                    }
                    break;
                case 2:
                    if(data.get(2).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr2+"06 0309 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr2+"06 0309 0000",2);
                    }
                    break;
            }
        }else if (area==5){//主卧室
            switch (position){
                case 0:
                    if(data.get(0).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr3+"06 0301 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr3+"06 0301 0000",2);
                    }
                    break;
                case 1:
                    if(data.get(1).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr3+"06 0302 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr3+"06 0302 0000",2);
                    }
                    break;
                case 2:
                    if(data.get(2).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr3+"06 0303 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr3+"06 0303 0000",2);
                    }
                    break;
                case 3:
                    if(data.get(3).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr3+"06 0304 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr3+"06 0304 0000",2);
                    }
                    break;
                case 4:
                    if(data.get(4).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr3+"06 0308 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr3+"06 0308 0000",2);
                    }
                    break;
            }
        }else if (area==6){//次卧室
            switch (position){
                case 0:
                    if(data.get(0).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr1+"06 0301 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr1+"06 0301 0000",2);
                    }
                    break;
                case 1:
                    if(data.get(1).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr1+"06 0303 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr1+"06 0303 0000",2);
                    }
                    break;
                case 2:
                    if(data.get(2).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr1+"06 0304 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr1+"06 0304 0000",2);
                    }
                    break;
                case 3:
                    if(data.get(3).get("state").equals("0")){
                        binder.sendOrder("aa68" +road_addr1+"06 0305 0001",2);
                    }else {
                        binder.sendOrder("aa68"+road_addr1+"06 0305 0000",2);
                    }
                    break;
            }
        }else if (area==7){//走廊
            if (data.get(0).get("state").equals("0")){
                binder.sendOrder("aa68"+road_addr3+"06 0305 0001",2);
            }else {
                binder.sendOrder("aa68"+road_addr3+"06 0305 0000",2);
            }
        }else if (area==8){//阳台
            if (data.get(0).get("state").equals("0")){
                binder.sendOrder("aa68"+road_addr1+"06 0308 0001",2);
            }else {
                binder.sendOrder("aa68"+road_addr1+"06 0308 0000",2);
            }
        }
    }

    private LoadingDialog dialog;
    private int which = 100;//1表示成功 100表示成功界面显示完毕
    // 显示成功发送命令时候的dialog
    private void showSuccessDialog() {
        which=1;
        dialog.setTipTextView("成功");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing())
                    dialog.dismiss();
                which=100;
            }
        }, 500);
    }

    // 显示点击按钮发送命令时候的dialog，2s后无回应则认为执行失败
    private void showFailedDialog() {
        dialog.show();
        dialog.setTipTextView("执行中...");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (which==100){
                    dialog.setTipTextView("执行失败");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            which=100;
                        }
                    }, 500);
                }
            }
        }, 3000);
    }
}

