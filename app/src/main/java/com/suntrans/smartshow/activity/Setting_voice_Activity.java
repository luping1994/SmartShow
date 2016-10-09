package com.suntrans.smartshow.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseApplication;
import com.suntrans.smartshow.service.MainService2;
import com.suntrans.smartshow.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.makeText;

/**
 * Created by Looney on 2016/10/5.
 */
public class Setting_voice_Activity extends AppCompatActivity{
    private TextView textView;//标题
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private String addr = "0001";//第六感地址
    private ArrayList<Map<String, String>> data1 = new ArrayList<>();
    private ArrayList<Map<String, String>> data2 = new ArrayList<>();
    private ArrayList<Map<String, String>> data3 = new ArrayList<>();
    private LinearLayoutManager manager;
    private Setting_voice_Activity.MyAdapter adapter;
    private int flag=0;     //是否正在更改语音，如果flag=1，则表示正在更改语音
    private String sub_voice="";   //更改的语音内容
    private String sub_channel="";   //更改的通道号
    private  MainService2.ibinder binder;  //用于Activity与Service通信
    private ServiceConnection con = new ServiceConnection() {
        //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("绑定成功");
            binder=(MainService2.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯

        }

        @Override   //service因异常而断开的时候调用此方法
        public void onServiceDisconnected(ComponentName name) {
            Log.v("Time", "绑定失败");
        }
    };   ///用
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_details);
        initData();
        initViews();
        initToolBar();
    }


    //设备数据初始化,从数据库中读取设备信息，存到data中,顺序依次是开关，第六感，单相电表，三相电表
    private void initData() {

        data1.clear();
        data2.clear();
        data3.clear();

//        DbHelper dh = new DbHelper(Setting_voice_Activity.this, "IBMS", null, 1);
        String sqlPath = "data/data/" + BaseApplication.getApplication().getPackageName() + "/databases/IBMS";
        SQLiteDatabase db=SQLiteDatabase.openDatabase(sqlPath,null,SQLiteDatabase.OPEN_READWRITE);
        db.beginTransaction();   //事务开始
        //查找开关1的所有通道名
        Cursor cursor = db.rawQuery("select Name,Channel,Area,VoiceName from switchs_tb where RSAddr=?",new String[]{"00010001"});
        if(cursor.getCount()>=1) {
            while (cursor.moveToNext()) {
//                smartSwitch.setChannelName(Integer.valueOf(cursor.getString(1))-1, cursor.getString(0));
//                smartSwitch.setAreaName(Integer.valueOf(cursor.getString(1))-1,cursor.getString(2));
//                smartSwitch.setName("00010001");
//                data1.add(smartSwitch);
                Map<String, String> map = new HashMap<>();
                map.put("Name","智能开关");
                map.put("Type","0");
                map.put("RSAddr","00010001");
                map.put("ChannelName", cursor.getString(0));
                map.put("Channel",cursor.getString(1));
                map.put("Area",cursor.getString(2));
                map.put("VoiceName",cursor.getString(3));
                data1.add(map);

            }
        }

        cursor = db.rawQuery("select Name,Channel,Area,VoiceName from switchs_tb where RSAddr=?",new String[]{"00010002"});
        if(cursor.getCount()>=1) {
            while (cursor.moveToNext()) {

                Map<String, String> map = new HashMap<>();
                map.put("Name","智能开关");
                map.put("Type","0");
                map.put("RSAddr","00010002");
                map.put("ChannelName", cursor.getString(0));
                map.put("Channel",cursor.getString(1));
                map.put("Area",cursor.getString(2));
                map.put("VoiceName",cursor.getString(3));
                data2.add(map);

            }
        }

        cursor = db.rawQuery("select Name,Channel,Area,VoiceName from switchs_tb where RSAddr=?",new String[]{"00010003"});
        if(cursor.getCount()>=1) {
            while (cursor.moveToNext()) {

                Map<String, String> map = new HashMap<>();
                map.put("Name","智能开关");
                map.put("Type","0");
                map.put("RSAddr","00010003");
                map.put("ChannelName", cursor.getString(0));
                map.put("Channel",cursor.getString(1));
                map.put("Area",cursor.getString(2));
                map.put("VoiceName",cursor.getString(3));
                data3.add(map);

            }
        }


        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        ComparDatas(data1);//由于数据库查找出来的数据是无序的所以需要排序更美观
        ComparDatas(data2);
        ComparDatas(data3);


    }


    private void initViews() {
        Intent intent1 = new Intent(getApplicationContext(), MainService2.class);    //指定要绑定的service
        bindService(intent1, con, Context.BIND_AUTO_CREATE);   //绑定主service
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE1");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive
        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver

        textView = (TextView) findViewById(R.id.tv_title);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        manager = new LinearLayoutManager(this);
        adapter = new Setting_voice_Activity.MyAdapter();
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
            textView.setText("语音配置");

        }
    }

    /**
     *     根据通道号对查找出来的开关集合排序
     */
    private void ComparDatas(ArrayList<Map<String, String>> data) {
        Collections.sort(data, new Comparator<Map<String, String>>() {
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                int map1value = (Integer.valueOf(o1.get("Channel")));
                int map2value = (Integer.valueOf(o2.get("Channel")));
                return map1value - map2value;
            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastreceiver);  //注销广播接收者
        unbindService(con);   //解除Service的绑定
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    private class MyAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder= new Setting_voice_Activity.MyAdapter.viewHolder1(LayoutInflater.from(
                    Setting_voice_Activity.this).inflate(R.layout.voice_setting_item, parent,false));
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((Setting_voice_Activity.MyAdapter.viewHolder1)holder).setData(position);
            ((viewHolder1)holder).setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSelectDialog(position,v);
                }
            });
        }

        @Override
        public int getItemCount() {
            return 30;
        }

        class viewHolder1 extends RecyclerView.ViewHolder
        {
            TextView name;
            TextView title;
            TextView setting;
            public viewHolder1(View view)
            {
                super(view);
                name = (TextView) view.findViewById(R.id.name);
                setting = (TextView) view.findViewById(R.id.peizhi);
                title = (TextView) view.findViewById(R.id.tv_title);
            }

            public void setData(final int position) {

                if (position==0||position==10||position==20){
                    title.setVisibility(View.VISIBLE);
                    title.setText("开关"+(position/10+1));
                }else {
                    title.setVisibility(View.GONE);
                }
                if (position<10){
                    setting.setTextColor(data1.get(position).get("VoiceName").equals("1")?Color.BLUE:Color.GRAY);
                    setting.setText(data1.get(position).get("VoiceName").equals("1")?"已配置":"未配置");
                    name.setText("通道"+data1.get(position).get("Channel")+"("+data1.get(position).get("ChannelName")+")");
                }else if (position>=10&&position<20){
                    setting.setTextColor(data2.get(position-10).get("VoiceName").equals("1")?Color.BLUE:Color.GRAY);
                    setting.setText(data2.get(position-10).get("VoiceName").equals("1")?"已配置":"未配置");
                    name.setText("通道"+data2.get(position-10).get("Channel")+"("+data2.get(position-10).get("ChannelName")+")");
                }else if (position>=20){
                    setting.setTextColor(data3.get(position-20).get("VoiceName").equals("1")?Color.BLUE:Color.GRAY);
                    setting.setText(data3.get(position-20).get("VoiceName").equals("1")?"已配置":"未配置");
                    name.setText("通道"+data3.get(position-20).get("Channel")+"("+data3.get(position-20).get("ChannelName")+")");
                }

            }
        }
    }

    /**
     * 显示配置语音的dialog
     * @param position
     * @param v
     */
    private void showSelectDialog(final int position, View v) {

        final AlertDialog.Builder builder  = new AlertDialog.Builder(Setting_voice_Activity.this);
        builder.setTitle("请选择语音指令");
        final String[] list_name = new String[]{"客厅", "餐厅", "厨房", "洗手间",
                "卫生间","阳台", "房间", "书房", "厕所", "门灯",
                "车库", "路灯", "走廊", "壁灯", "客房",
                "花园", "台灯", "储物室", "仓库", "阁楼", "地下室",
                "楼梯", "水池", "泳池", "彩灯", "红灯", "绿灯", "蓝灯",
                "电视", "空调", "会议室", "顶灯", "灯",
                "开关", "前灯", "后灯", "办公室", "吊灯", "筒灯",
                "射灯", "画面灯", "灯带"

        };
        //语音拼音
        final String[] list_pinyin = new String[]{"ke ting", "can ting", "chu fang", "xi shou jian",
                "wei sheng jian",   "yang tai", "fang jian", "shu fang", "ce suo", "men deng",
                "che ku", "lu deng", "zou lang", "bi deng", "ke fang",
                "hua yuan", "tai deng", "chu wu shi", "cang ku", "ge lou", "di xia shi",
                "lou ti", "shui chi","yong chi", "cai deng", "hong deng", "lv deng", "lan deng",
                "dian shi", "kong tiao", "hui yi shi", "ding deng", "deng",
                "kai guan", "qian deng", "hou deng", "ban gong shi", "diao deng", "tong deng",
                "she deng", "hua mian deng", "deng dai"

        };
        //语音序列号和语音长度
        final String[] list_serial=new String[]{"0107","0208","0308","040c",
                "050e","0608","0709","0808","0906","0a08",
                "0b06","0c07","0d08","0e07","0f07",
                "1008","1a08","120a","1307","1406","150a",
                "1606","1708","1808","1908","1a09","1b07","1c08",
                "1d08","1e09","1f0a","2009","2104",
                "2208","2309","2408","250c","2609","2709",
                "2808","290d","2a08"

        };
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        builder.setItems(list_name, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String Channel = "";
                String RSAddr = "";
                if (position<10){
                    Channel= data1.get(position).get("Channel");
                    RSAddr = data1.get(position).get("RSAddr");
                }else if (position>=10&&position<20){
                    Channel=data2.get(position-10).get("Channel");
                    RSAddr = data2.get(position-10).get("RSAddr");
                }else if (position>=20){
                    Channel=data3.get(position-20).get("Channel");
                    RSAddr = data3.get(position-20).get("RSAddr");
                }
                int num_channel = Integer.valueOf(Channel);   //通道号，int型
                //点击后弹出窗口选择了第几项,which从0开始
                //  Toast.makeText(getApplication(), String.valueOf(which), Toast.LENGTH_LONG).show();
                flag=1;
                RSAddr = RSAddr.substring(4,RSAddr.length());
                if (RSAddr.equals("0001"))
                    binder.sendOrder("ab68" + addr + "f006 050" + (Channel.equals("10") ? "a" : Channel) + RSAddr.substring(2, 4) + "0" + (Channel.equals("10") ? "a" : Channel) + list_serial[which] + Converts.Bytes2HexString(list_pinyin[which].getBytes()),4);
                else {
                    byte x=(byte)(num_channel+10);
                    String addr1=Converts.Bytes2HexString(new byte[]{x});
                    if(addr1.length()==1)
                        addr1="0"+addr1;
                    binder.sendOrder("ab68" + addr + "f006 05" + addr1 + RSAddr.substring(2, 4) + "0" + (Channel.equals("10") ? "a" : Channel) + list_serial[which] + Converts.Bytes2HexString(list_pinyin[which].getBytes()),4);

                }
//                ab680001 f0 06 0508 00 08 09 06 63652073756F
                LogUtil.i("命令为:ab68" + addr + "f006 050" + (Channel.equals("10") ? "a" : Channel) + RSAddr.substring(2, 4) + "0" + (Channel.equals("10") ? "a" : Channel) + list_serial[which] + Converts.Bytes2HexString(list_pinyin[which].getBytes()));
                sub_voice=list_name[which];
                sub_channel=Channel;
                //  Converts.Bytes2HexString(list_pinyin[2].getBytes())
            }
        });
        builder.create().show();
    }
    private ArrayList<Map<String,String>> data=new ArrayList<Map<String,String>>();
    private byte warning_state=0;     //外间的报警开关的状态，因为修改语音开关时会涉及到报警开关的状态（存放在同一个寄存器中）
    private String s;//收到的命令
    private String state="1";   //外间的语音开关状态
    private byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    //广播接收者
    protected BroadcastReceiver broadcastreceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes = intent.getByteArrayExtra("Content");
            if (bytes.length < 10) {
                return;
            }
            s = Converts.Bytes2HexString(bytes);
            s = s.split("0d0a")[0] + "0d0a";
            s = s.toLowerCase();
            if (!MainService2.isInnerNet1){//外网截掉协议前部
                s=s.substring(4,s.length());
            }
            byte[] a = Converts.HexString2Bytes(s);
            if(s.substring(10,12).equals("03")||s.substring(10,12).equals("04"))  //读寄存器数据
            {
                if (s.substring(12, 14).equals("22")&&a.length>32)  //寄存器1，室内参数信息，包含报警开关状态，a[32]是存放开关状态的
                {
                    warning_state=(byte)(a[32]&0x1f);                                   //温度
                    state=(a[32] & bits[5]) == bits[5] ? "1" : "0";     //外间语音开关状态

                }
                else if(s.substring(12, 14).equals("02")&&s.substring(14,18).equals("0304")&&a.length>8)    //寄存器0304，报警开关状态
                {
                    warning_state=(byte)(a[8]&0x1f);
                    state=(a[8] & bits[5]) == bits[5] ? "1" : "0";     //语音开关状态

                }
            }
            else if(s.substring(10,12).equals("06"))   //控制命令返回
            {
                if(s.substring(12,14).equals("03"))   //更改语音、报警开关状态
                {
                    warning_state=(byte)(a[9]&0x1f);
                    state=(a[9] & bits[5]) == bits[5] ? "1" : "0";     //语音开关状态

                }
                else if(s.substring(12,14).equals("05"))   //更改语音内容
                {
                    if(!sub_voice.equals("")&&!sub_channel.equals(""))
                    {
                        System.out.println("语音配置成功");
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };
}
