package com.suntrans.smartshow.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseActivity1;
import com.suntrans.smartshow.base.BaseApplication;
import com.suntrans.smartshow.fragment.RoomConditionFragment;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.UiUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by pc on 2016/9/15.
 * 智能家居开关控制页面
 */
public class SmartRoonDetails_Activity extends BaseActivity1 {
    private Toolbar toolbar;
    private TextView tv_title;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private Socket client;//连接
    private DataOutputStream out;
    private DataInputStream in;
    private String ipAddress;//第六感ip地址,默认为192.168.235
    int port = 8000;//端口号
    String addr = "0001";
    private  byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private ArrayList<Map<String, String>> data = new ArrayList<>();

    private int area;

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        area = intent.getIntExtra("area", 0);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tv_title = (TextView) findViewById(R.id.tv_title);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        connectToServer();
    }

    @Override
    public int getLayoutId() {
        return R.layout.smartroom_detail_activity;
    }

    @Override
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
        getSwitchStateFromServer();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter adapter = new mAdapter();
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

    }




    @Override
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

            RecyclerView.ViewHolder holder = new SmartRoonDetails_Activity.mAdapter.viewHolder1(LayoutInflater.from(
                    SmartRoonDetails_Activity.this).inflate(R.layout.road_bulb_item, parent, false));
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

            Bitmap bitmap = BitmapFactory.decodeResource(SmartRoonDetails_Activity.this.getResources(), id);
            bitmap = Converts.toRoundCorner(bitmap, UiUtils.dip2px(20));

            ((viewHolder1) holder).image.setImageBitmap(bitmap);
            ((viewHolder1) holder).textView.setText(data.get(position).get("Name"));
            ((viewHolder1) holder).image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (area==0){
                        switch (position){
                            case 0:
                                sendOrder("aa68 00010001 06 0302 0001");
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


    /**
     * 连接到服务器
     */
    private void connectToServer() {
        ipAddress = BaseApplication.getSharedPreferences().getString("ipAddress", "192.168.1.235");
        LogUtil.i("ipAddress=====>" + ipAddress);
        new Thread() {
            @Override
            public void run() {
                if (client == null) {
                    try {
                        client = new Socket(ipAddress, port);
                        LogUtil.i("client=========>>连接成功！");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                new TcpServerThread().start();
            }
        }.start();
    }

    /**
     * 收消息进程
     */
    class TcpServerThread extends Thread {
        public void run() {
            LogUtil.i("正在准备收消息中...");
            try {
                byte[] buf = new byte[100];
                int len = 0;
                out = new DataOutputStream(client.getOutputStream());
                in = new DataInputStream(client.getInputStream());
                while (client != null) {
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
                                    LogUtil.i("Fuck you!收到结果为==>" + result);
                                    parseData(result);
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

    /**
     * 发送命令
     *
     * @param order
     */
    private void sendOrder(final String order) {

        if (client != null)
            if (!client.isOutputShutdown()) {
                new Thread() {
                    @Override
                    public void run() {
                        String order1 = order.replace(" ", "");
                        byte[] bt = null;
                        bt = Converts.HexString2Bytes(order1);
                        String string = order1 + Converts.GetCRC(bt, 2, bt.length) + "0d0a";
                        byte[] bt1 = Converts.HexString2Bytes(string);
                        try {
                            out.write(bt1);
                            out.flush();
                            LogUtil.i("命令已发送==>" + string);/////////////////////
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }


    }

    @Override
    protected void onDestroy() {
        if (client != null) {
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

    private   ArrayList<Map<String,String>> state1;
    private   ArrayList<Map<String,String>> state2;
    private   ArrayList<Map<String,String>> state3;
    private String road_addr1 = "00010001";
    private String road_addr2 = "00010002";
    private String road_addr3 = "00010003";
    /**
     * 获取不同房间的开关状态
     */
    private void getSwitchStateFromServer() {

        switch (area){
            case 0://客厅，只需获取00010001开关2，6，7，9通道的状态
                String order = "aa68"+road_addr1+"03 0300"+"0007";
                sendOrder(order);
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

}
