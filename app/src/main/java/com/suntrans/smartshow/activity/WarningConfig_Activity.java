package com.suntrans.smartshow.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.service.MainService2;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.ThreadManager;
import com.suntrans.smartshow.views.LoadingDialog;
import com.suntrans.smartshow.views.Switch;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Looney on 2016/10/5.
 */

public class WarningConfig_Activity extends AppCompatActivity {
    private TextView textView;//标题
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private int type;
    private String voice_state="0";   //外间语音开关状态，1代表开，0代表关
    private ArrayList<Map<String, String>> datas = new ArrayList<>();
    private LinearLayoutManager manager;
    private MyAdapter adapter;
    String addr = "0001";
    private LoadingDialog dialog1;//进度条
    boolean isRefresh =true;
    private byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    DecimalFormat df1   = new DecimalFormat("0.0");    //保留一位小数
    DecimalFormat df3   = new DecimalFormat("0.000");   //保留三位小数
    private ProgressDialog progressdialog;    //进度条
    private String which = "100";
    private static String WRITE_STATE="1";   //写开关状态时的标志位
    private static String WRITE_VALUE = "2";   //写报警阈值时的标志位
    private static String INIT = "3";    //恢复默认时的标志位
    private  MainService2.ibinder binder;  //用于Activity与Service通信
    private ServiceConnection con = new ServiceConnection() {
        //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("绑定成功");
            binder=(MainService2.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
            binder.sendOrder("ab68" + addr + "f003 0100 0011", 4);   //读取报警开关状态
            try{Thread.sleep(310);}
            catch(Exception ex) {
                ex.printStackTrace();
            }
            binder.sendOrder("ab68" + addr + "f003 0700 0005", 4);   //读取报警阈值
            try{Thread.sleep(310);}
            catch(Exception ex) {
                ex.printStackTrace();
            }
            new RefreshThread().start();
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

    private void initData() {
        datas.clear();

        Map<String, String> map1 = new HashMap<>();
        map1.put("Name", "温度");
        map1.put("Value", "0");
        map1.put("State", "0");
        datas.add(map1);
        Map<String, String> map2 = new HashMap<>();
        map2.put("Name", "烟雾");
        map2.put("Value", "0");
        map2.put("State", "0");
        datas.add(map2);
        Map<String, String> map3 = new HashMap<>();
        map3.put("Name", "甲醛");
        map3.put("Value", "0");
        map3.put("State", "0");
        datas.add(map3);
        Map<String, String> map4 = new HashMap<>();
        map4.put("Name", "PM2.5");
        map4.put("Value", "0");
        map4.put("State", "0");
        datas.add(map4);
        Map<String, String> map5 = new HashMap<>();
        map5.put("Name", "振动");
        map5.put("Value", "0");
        map5.put("State", "0");
        datas.add(map5);
    }

    private void initViews() {
        dialog = new LoadingDialog(this);
        Intent intent1 = new Intent(getApplicationContext(), MainService2.class);    //指定要绑定的service
        bindService(intent1, con, Context.BIND_AUTO_CREATE);   //绑定主service
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE1");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive
        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver
        dialog1 = new LoadingDialog(this);
        refreshLayout= (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        textView = (TextView) findViewById(R.id.tv_title);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        manager = new LinearLayoutManager(this);
        adapter = new MyAdapter();
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String order = "ab 68 00 01 f0 03 07 00 00 05";//获得阈值
                binder.sendOrder(order, MainService2.SIXSENSOR);
            }
        });
        ThreadManager.getInstance().createLongPool().execute(new Runnable() {
            String order="ab68"+addr+"f003 0100 0011";
            @Override
            public void run() {
                while (isRefresh){
                    boolean run = true;
                    while (run){
                        if (binder!=null){
                            binder.sendOrder(order,4);
                        }
                        run=false;
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

    private void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
                textView.setText("报警配置");

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.recover:
                showMyDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("确定要恢复默认设置吗?");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showFailedDialog();
                binder.sendOrder("ab68" + addr + "f010 0700 0005 0a 0232 0208 0214 0207 0205", 4);
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_warning_config,menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        isRefresh=false;
        unregisterReceiver(broadcastreceiver);  //注销广播接收者
        unbindService(con);   //解除Service的绑定
        super.onDestroy();
    }

    private class MyAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder= new WarningConfig_Activity.MyAdapter.viewHolder1(LayoutInflater.from(
                    WarningConfig_Activity.this).inflate(R.layout.alarm_setting_item, parent,false));
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((viewHolder1)holder).setData(position);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        class viewHolder1 extends RecyclerView.ViewHolder  {
            TextView name;
            TextView value;
            Switch aSwitch;
            public viewHolder1(View view)
            {
                super(view);
                name = (TextView) view.findViewById(R.id.name);
                value = (TextView) view.findViewById(R.id.value);
                aSwitch = (Switch) view.findViewById(R.id.switch1);
            }

            public void setData(final int position) {
                int int_value = Integer.valueOf(datas.get(position).get("Value"));   //int型阈值
                double double_value=int_value;  //乘以系数以后的实际阈值
                String str_double_value="";  //double型数据转成String型数据
                if(position==4)
                {
                    double_value = int_value * 0.1;
                    str_double_value=df1.format(double_value);
                }
                else if(position==2) {
                    double_value = int_value * 0.01;
                    str_double_value=df3.format(double_value);
                }
                else if(position==0) {
                    double_value = int_value * 1;
                    str_double_value=String.valueOf((int)double_value);
                }
                else {
                    double_value = int_value * 100;
                    str_double_value=String.valueOf((int)double_value);
                }
                final String str_value = str_double_value;
                name.setText(datas.get(position).get("Name"));
                value.setText(str_double_value);
                aSwitch.setState(datas.get(position).get("State").equals("0")?false:true);
                value.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutInflater factory = LayoutInflater.from(WarningConfig_Activity.this);
                        final View view = factory.inflate(R.layout.warning_input, null);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(WarningConfig_Activity.this);
                        builder.setTitle("请输入"+datas.get(position).get("Name")+"报警阈值：");
                        final EditText  tx1= (EditText) view.findViewById(R.id.tx1);   //整型数值
                        tx1.setHint(str_value);
                        builder.setView(view);
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(!(tx1.getText().toString().length()==0||tx1.getText().toString()==null)) {   //判断输入不为空
                                    String str=tx1.getText().toString();   //获取输入字符串的内容
                                    Pattern pattern = Pattern.compile("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
                                    Matcher isNum = pattern.matcher(str);
                                    if( isNum.matches() ) {    //判断是否为数字
                                        double new_value =Double.valueOf(tx1.getText().toString());   //用户输入的新的报警阈值,position正好对应寄存器070x中的x
                                        int new_value_int=0;
                                        if(position==4)
                                            new_value_int=(int)(new_value*10);
                                        else if(position==2)
                                            new_value_int = (int) (new_value * 100);
                                        else if(position==0)
                                            new_value_int=(int)new_value;
                                        else
                                            new_value_int = (int) (new_value * 0.01);
                                        String[] values = new String[5];  //阈值数组
                                        Log.e("调试中","新输入的位置为:"+position+",值为:"+new_value);
                                        for (int k = 0; k < 5; k++) {
                                            if(k==position)   //要更改的值
                                            {
                                                int new_value_abs=Math.abs(new_value_int);
                                                values[k]= (new_value_int > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((new_value_abs> 255) ? 255 : new_value_abs)});
                                            }
                                            else
                                            {
                                                int p =Integer.valueOf( datas.get(k).get("Value"));
                                                Log.e("调试中","其它的位置:"+k+",值为:"+p);
                                                int p_abs = Math.abs(p);   //p的绝对值
                                                values[k] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                            }
                                        }
                                        String order_value="";
                                        for(int j=0;j<5;j++){
                                            order_value+=values[j];
                                        }
                                        String order = "ab68" + addr + "f010 0700 0005 0a"+order_value;
                                        showFailedDialog();
                                        binder.sendOrder(order,4);
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(),"输入非法字符！",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        });
                        builder.create().show();
                    }
                });

                aSwitch.setOnChangeListener(new Switch.OnSwitchChangedListener() {
                    @Override
                    public void onSwitchChange(Switch switchView, boolean isChecked) {
                        showFailedDialog();
                        //因为0304寄存器存放的是语音、温度、烟雾、甲醛、PM2.5、振动的开关状态，所以值要把六个开关的状态都写进去
                        byte byt= (byte) ((voice_state.equals("1")?1:0)*32);   //语音开关状态
                        for(int i=0;i<=4;i++)
                        {
                            if(i==position)    //如果是需要改变的开关，数值写入与原状态相反的值
                                byt+=(datas.get(i).get("State").equals("1")?0:1)*Math.pow(2,4-i);
                            else
                                byt+=(datas.get(i).get("State").equals("1")?1:0)*Math.pow(2,4-i);
                        }

                        String order = "ab68" + addr + "f006 0304" + "00" + Converts.Bytes2HexString(new byte[]{byt});
                        binder.sendOrder(order, 4);
                    }
                });
            }


        }
    }
    private long time=0;
    private String s;//收到的命令
    //广播接收者
    protected BroadcastReceiver broadcastreceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes = intent.getByteArrayExtra("Content");
            if (bytes.length < 10) {
                return;
            }
            //AB 68 00 01 F0 030A02320208021402070205C28F0D0A0d0a
            s = Converts.Bytes2HexString(bytes);
            s = s.split("0d0a")[0] + "0d0a";
            s = s.toLowerCase();
            if (MainService2.isInnerNet1){
                if (!s.substring(0,4).equals("ab68"));
                return;
            }else {
                if (!s.substring(0,8).equals("ab68ab68"))
                    return;
                s=s.substring(4,s.length());
            }
            byte[] a = Converts.HexString2Bytes(s);
            if (s.substring(10, 12).equals("03") || s.substring(10, 12).equals("04"))   //如果是读寄存器状态，则判断是读寄存器1（室内参数），还是寄存器0304（报警开关状态），还是寄存器7报警阈值
            {
                if (s.substring(12, 14).equals("22") && a.length > 32)  //寄存器1，室内参数信息，包含报警开关状态，a[32]是存放开关状态的
                {
                    String switch_shake = (a[32] & bits[0]) == bits[0] ? "1" : "0";     //振动
                    String switch_pm25 = (a[32] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5
                    String switch_arofene = (a[32] & bits[2]) == bits[2] ? "1" : "0";      //甲醛
                    String switch_smoke = (a[32] & bits[3]) == bits[3] ? "1" : "0";        //烟雾
                    String switch_tmp = (a[32] & bits[4]) == bits[4] ? "1" : "0";          //温度
                    voice_state = (a[32] & bits[5]) == bits[5] ? "1" : "0";     //语音开关状态
                    datas.get(0).put("State", switch_tmp);
                    datas.get(1).put("State", switch_smoke);
                    datas.get(2).put("State", switch_arofene);
                    datas.get(3).put("State", switch_pm25);
                    datas.get(4).put("State", switch_shake);
//                    if(!which.equals(WRITE_STATE))   //如果正在更改状态，则先不更新页面
                    if (adapter != null) {
                       showSuccessDialog();
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter = new MyAdapter();
                        recyclerView.setAdapter(adapter);
                    }
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                } else if (s.substring(12, 14).equals("02") && s.substring(14, 18).equals("0304") && a.length > 8)    //寄存器0304，报警开关状态
                {
                    String switch_shake = (a[8] & bits[0]) == bits[0] ? "1" : "0";     //振动
                    String switch_pm25 = (a[8] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5
                    String switch_arofene = (a[8] & bits[2]) == bits[2] ? "1" : "0";      //甲醛
                    String switch_smoke = (a[8] & bits[3]) == bits[3] ? "1" : "0";        //烟雾
                    String switch_tmp = (a[8] & bits[4]) == bits[4] ? "1" : "0";          //温度
                    voice_state = (a[8] & bits[5]) == bits[5] ? "1" : "0";     //语音开关状态
                    datas.get(0).put("State", switch_tmp);
                    datas.get(1).put("State", switch_smoke);
                    datas.get(2).put("State", switch_arofene);
                    datas.get(3).put("State", switch_pm25);
                    datas.get(4).put("State", switch_shake);
                    if (adapter != null) {
                       showSuccessDialog();
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter = new MyAdapter();
                        recyclerView.setAdapter(adapter);
                    }
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                } else if (s.substring(12, 14).equals("0a") && a.length > 16)     //寄存器7，报警阈值
                {
                    int tmp = (a[7] == 2 ? 1 : -1) * (a[8] & 0xff);   //温度报警值
                    int smoke = (a[9] == 2 ? 1 : -1) * (a[10] & 0xff);   //烟雾报警值
                    int arofene = (a[11] == 2 ? 1 : -1) * (a[12] & 0xff);   //甲醛报警值
                    int pm25 = (a[13] == 2 ? 1 : -1) * (a[14] & 0xff);   //PM2.5报警阈值
                    int shake = (a[15] == 2 ? 1 : -1) * (a[16] & 0xff);   //振动报警阈值

                    datas.get(0).put("Value", String.valueOf(tmp));   //振动报警阈值
                    datas.get(1).put("Value", String.valueOf(smoke));
                    datas.get(2).put("Value", String.valueOf(arofene));
                    datas.get(3).put("Value", String.valueOf(pm25));
                    datas.get(4).put("Value", String.valueOf(shake));
                    if (adapter != null) {
                       showSuccessDialog();
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter = new MyAdapter();
                        recyclerView.setAdapter(adapter);
                    }
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                }
            } else if (s.substring(10, 12).equals("06"))   //如果返回06，则是控制某个寄存器返回，或报警信息主动上报。
            {
                if (s.substring(12, 16).equals("0304") && a.length > 9)   //写开关状态的返回
                {
                    if (which.equals(WRITE_STATE)) {
                        which = "100";
                        Message msg1 = new Message();
                        msg1.what = 0;   //关闭dialogprogress
                        handler2.sendMessage(msg1);
                    }
                    String switch_shake = (a[9] & bits[0]) == bits[0] ? "1" : "0";     //振动
                    String switch_pm25 = (a[9] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5
                    String switch_arofene = (a[9] & bits[2]) == bits[2] ? "1" : "0";      //甲醛
                    String switch_smoke = (a[9] & bits[3]) == bits[3] ? "1" : "0";        //烟雾
                    String switch_tmp = (a[9] & bits[4]) == bits[4] ? "1" : "0";          //温度
                    voice_state = (a[9] & bits[5]) == bits[5] ? "1" : "0";     //语音开关状态
                    datas.get(0).put("State", switch_tmp);
                    datas.get(1).put("State", switch_smoke);
                    datas.get(2).put("State", switch_arofene);
                    datas.get(3).put("State", switch_pm25);
                    datas.get(4).put("State", switch_shake);
                    if (adapter != null) {
                       showSuccessDialog();
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter = new MyAdapter();
                        recyclerView.setAdapter(adapter);
                    }
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                } else if (s.substring(12, 15).equals("070") && a.length > 9)      //修改某个报警阈值
                {
                    showSuccessDialog();
                    int item = Integer.valueOf(s.substring(15, 16));   //判断是哪个参数，0代表振动，4代表温度。中间依次是烟雾、甲醛、PM2.5
                    datas.get(item).put("Value", String.valueOf((a[8] == 2 ? 1 : -1) * (a[9] & 0xff)));
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter = new MyAdapter();
                        recyclerView.setAdapter(adapter);
                    }
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                }
            } else if (s.substring(10, 12).equals("10") && a.length > 11)   //修改多个寄存器的值
            {
                if (s.substring(12, 20).equals("07000005"))   //是写报警阈值寄存器，表示写成功了
                {
//                    if (which.equals(INIT) || which.equals(WRITE_VALUE)) {
//                        which = "100";
//                        Message msg1 = new Message();
//                        msg1.what = 0;   //关闭dialogprogress
//                        handler2.sendMessage(msg1);
//                    }
                    binder.sendOrder("ab68 " + addr + "f003 0700 0005", 4);   //读取报警阈值
//                    if (adapter != null) {
//                        dialog1.setTipTextView("成功!");
//                        handler2.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                dialog1.dismiss();
//                            }
//                        }, 500);
//                        adapter.notifyDataSetChanged();
//                    } else {
//                        adapter = new MyAdapter();
//                        recyclerView.setAdapter(adapter);
//                    }
//                    if (refreshLayout.isRefreshing()) {
//                        refreshLayout.setRefreshing(false);
//                    }
                }
            }
        }
    };
    private Handler handler2 = new Handler();


    //新建刷新线程，刷新当前页面显示数据
    class RefreshThread extends Thread{
        @Override
        public void run(){

            while(isRefresh==true) {
                if(which.equals("100"))
                    binder.sendOrder("ab68" + addr + "f003 0700 0005", 4);   //读取报警阈值
                try {
                    Thread.sleep(5000);
                }   //每隔5s读取一次报警阈值
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    private LoadingDialog dialog;
    private int which1 = 100;//1表示成功 100表示成功界面显示完毕
    // 显示成功发送命令时候的dialog
    private void showSuccessDialog() {
        which1=1;
        dialog.setTipTextView("成功");
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing())
                    dialog.dismiss();
                which1=100;
            }
        }, 500);
    }

    // 显示点击按钮发送命令时候的dialog，2s后无回应则认为执行失败
    private void showFailedDialog() {
        dialog.show();
        dialog.setTipTextView("执行中...");
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (which1==100){
                    dialog.setTipTextView("执行失败");
                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            which1=100;
                            adapter.notifyDataSetChanged();
                        }
                    }, 500);
                }
            }
        }, 2000);
    }
}
