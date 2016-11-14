package com.suntrans.smartfire;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import Adapter.MainAdapter;
import Utils.DbHelper;
import Utils.LogUtil;
import convert.Converts;
import services.MainService;
import views.TouchListener;

import static android.media.CamcorderProfile.get;

/**
 * Created by Looney on 2016/10/17.
 */

public class Main_Activity extends AppCompatActivity {
    private SwipeRefreshLayout refreshLayout;
    private boolean isShowDialog =true;
    private String addr=null;
    private ArrayList<Map<String,String>> data = new ArrayList<>();
    private ArrayList<Map<String,String>> warningState = new ArrayList<>();
    private LinearLayout layout_record;
    private RecyclerView recyclerView;
    private LinearLayout layout_add;
    private TextView title;
    private TextView textView_empty;
    private SoundPool soundPool;  //声音播放类
    private Map<Integer,Integer> SoundPoolMap;   //载入声音文件所对应的ID
    private GridLayoutManager manager;
    private MainAdapter adapter;
    public MainService.ibinder binder;  //用于Activity与Service通信
    private ServiceConnection con = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder=(MainService.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplication(), "网络错误！", Toast.LENGTH_SHORT).show();

        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setupWindowAnimations();
        initData();
        initView();
        setListener();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        // Re-enter transition is executed when returning to this activity
        Slide slideTransition = new Slide();
        slideTransition.setSlideEdge(Gravity.LEFT);
        slideTransition.setDuration(getResources().getInteger(R.integer.anim_duration_long));
        getWindow().setReenterTransition(slideTransition);
        getWindow().setExitTransition(slideTransition);
    }
    @Override
    protected void onResume() {
        super.onStart();
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE");
        registerReceiver(receiver, filter_dynamic);
        isShowDialog = true;
    }


    @Override
    protected void onStop() {
        isShowDialog = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            unbindService(con);   //解除Service的绑定
            unregisterReceiver(receiver);  //注销广播接收者
        }catch (Exception e){

        }
        soundPool.release();
        super.onDestroy();
    }

    private void initData() {
        data.clear();
        warningState.clear();
        DbHelper dh = new DbHelper(Main_Activity.this, "IBMS", null, 1);
        SQLiteDatabase db=dh.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor=db.query(true,"sixsensor_tb",new String[]{"RSAddr","Name"},null,null,null,null,null,null);
        if(cursor.getCount()>=1) {
            while (cursor.moveToNext()) {
                Map<String, String> map = new HashMap<>();
                map.put("Name", cursor.getString(1));
                map.put("RSAddr",cursor.getString(0));
                data.add(map);
            }
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        for (int i=0;i<data.size();i++){
            Map<String, String> map1 = new HashMap<>();
            map1.put("warningState", "0");
            warningState.add(map1);
        }

    }

    private void initView() {

        //绑定MainService
        Intent intent = new Intent(getApplicationContext(), MainService.class);
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        layout_add = (LinearLayout) findViewById(R.id.layout_add);
        layout_record = (LinearLayout) findViewById(R.id.layout_record);
        title = (TextView) findViewById(R.id.title_name);
        textView_empty = (TextView) findViewById(R.id.text_empty);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        manager= new GridLayoutManager(this,3);
        adapter = new MainAdapter(data,this,warningState);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        if (data.size()==0){
            textView_empty.setVisibility(View.VISIBLE);
        }else {
            textView_empty.setVisibility(View.GONE);
        }
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.bg_action);
        refreshLayout.setSize(SwipeRefreshLayout.LARGE);
        refreshLayout.setColorSchemeResources(R.color.white);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               new GetDataTask().execute();
            }
        });
        //初始化声音播放器，加载声音文件
        soundPool= new SoundPool(1,AudioManager.STREAM_SYSTEM,0);  //支持的声音数量，声音类型，声音品质
        SoundPoolMap = new HashMap<>();
        SoundPoolMap.put(1, soundPool.load(Main_Activity.this,R.raw.voice_fire,1));   //加载振动报警文件

    }

    private void setListener() {
        adapter.setmOnItemClickListener(new MainAdapter.onItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent();
                intent.putExtra("RSAddr",data.get(position).get("RSAddr"));
                intent.putExtra("Name",data.get(position).get("Name"));
                intent.setClass(Main_Activity.this,Parameter_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onLongClick(View v, int position) {
                showModifyDialog(v,position);
            }
        });
        layout_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
        layout_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main_Activity.this,AlertRecordActivity.class));
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });
    }
//修改名称dialog
    private void showModifyDialog(View view, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{"修改名称", "删除设备"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0://修改名称
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Main_Activity.this);
                        View v1 = View.inflate(Main_Activity.this, R.layout.modify_input,null);
                        final EditText text = (EditText) v1.findViewById(R.id.name);
                         TextView textView = (TextView) v1.findViewById(R.id.tv_title);
                        textView.setText("修改名称");
                        builder1.setView(v1);
                        builder1.setPositiveButton("修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbHelper dh1=new DbHelper(Main_Activity.this,"IBMS",null,1);
                                SQLiteDatabase db = dh1.getWritableDatabase();
                                db.beginTransaction();
                                ContentValues cv = new ContentValues();    //内容数组
                                cv.put("Name",text.getText().toString());
                                db.update("sixsensor_tb",cv,"RSAddr=?",new String[]{data.get(position).get("RSAddr")});
                                db.setTransactionSuccessful();
                                db.endTransaction();
                                db.close();
                                initData();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(Main_Activity.this,"修改成功！",Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder1.show();
                        break;
                    case 1:
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(Main_Activity.this);
                        builder2.setTitle("删除设备");
                        builder2.setMessage("你确认要删除吗?");
                        builder2.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbHelper dh1=new DbHelper(Main_Activity.this,"IBMS",null,1);
                                SQLiteDatabase db = dh1.getWritableDatabase();
                                db.beginTransaction();
                                db.delete("sixsensor_tb","RSAddr=?",new String[]{data.get(position).get("RSAddr")});
                                db.setTransactionSuccessful();
                                db.endTransaction();
                                db.close();
                                initData();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(Main_Activity.this,"删除成功！",Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder2.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder2.show();
                        break;
                }
            }
        });
        builder.setTitle(data.get(position).get("Name"));
        builder.create().show();
    }

    private Handler handler = new Handler();
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = View.inflate(this,R.layout.add_inpput,null);
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int flag =0;
                String name = ((EditText) view.findViewById(R.id.name)).getText().toString();
                String addr = ((EditText) view.findViewById(R.id.addr)).getText().toString();
                DbHelper dh = new DbHelper(Main_Activity.this, "IBMS", null, 1);
                if (checkNameAndAddr(name,addr)){
                    try {
                        addr = DecConvert2Hex(addr);
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(Main_Activity.this, "添加失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (addr==null){
                        Toast.makeText(Main_Activity.this, "添加失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SQLiteDatabase db=dh.getWritableDatabase();
                    db.beginTransaction();
                    Cursor cursor = db.query(true, "sixsensor_tb", new String[]{"Name"}, "RSAddr=?", new String[]{addr}, null, null, null, null);
                    if (cursor.getCount() > 0) {
                        flag = 0;
                        Toast.makeText(Main_Activity.this, "该地址已存在，添加失败！", Toast.LENGTH_SHORT).show();
                    }else {

                        ContentValues cv = new ContentValues();
                        cv.put("Name",name);
                        cv.put("RSAddr",addr);
                        db.insert("sixsensor_tb", null, cv);
                        Toast.makeText(getApplicationContext(), "添加成功！", Toast.LENGTH_SHORT).show();
                        flag = 1;
                    }
                    cursor.close();
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.close();
                    if (flag==1)
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                initData();
                                textView_empty.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                            }
                        },1000);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private String DecConvert2Hex(String a) {
            int b = Integer.valueOf(a);
            String c = Integer.toHexString(b);
            System.err.println("a=" + a + ";b=" + b + ";c=" + c);
            StringBuffer sb;
            sb = new StringBuffer();
            if (c.length()>4){
                return  null;
            }
            if (c.length() != 4) {
                for (int i = 0; i < 4 - c.length(); i++) {
                    sb.append(0);
                }
            }
            sb.append(c);

        return sb.toString();
    }

    private boolean checkNameAndAddr(String name, String addr) {
        String expression ="[^\\s]{1,}";
        String expression2 ="[^\\s]{1,}";
        boolean a = addr.matches(expression2);
        boolean b = name.matches(expression);
        if (!a&&b){
            Toast.makeText(this,"地址格式不对",Toast.LENGTH_SHORT).show();
        }
        if (a&&!b){
            Toast.makeText(this,"名称为空或含有空格",Toast.LENGTH_SHORT).show();
        }
        if (!a&&!b){
            Toast.makeText(this,"名称为空,地址格式不对",Toast.LENGTH_SHORT).show();
        }
        return a&&b;
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
                logoutApp();
                return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    private long exitTime = 0;
    private void logoutApp()
    {

        if (System.currentTimeMillis() - exitTime > 2000)
        {
            Toast.makeText(Main_Activity.this,"再按一次退出",Toast.LENGTH_SHORT).show();

            exitTime = System.currentTimeMillis();
        } else
        {
//            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
    private byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private String warning_state[]=new String[]{"1","1","1","1","1","1"};   //是否报警，依次是振动、PM2.5、甲醛、烟雾、温度，人员
    Handler handler1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            byte[] a1 = (byte[]) msg.obj;
            String s = "";                       //保存命令的十六进制字符串
            for (int i = 0; i < msg.what; i++) {
                String s1 = Integer.toHexString((a1[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                if (s1.length() == 1)
                    s1 = "0" + s1;
                s = s + s1;
            }
            int IsEffective = 1;    //指令是否有效，0表示无效，1表示有效；对于和第六感官通讯而言，包头为ab68的数据才有效
            s = s.replace(" ", ""); //去掉空格
            if (msg.what <= 10||!s.substring(0, 4).equals("ab68")) {
                return;
            }
            s = s.toLowerCase();
            byte[] a = Converts.HexString2Bytes(s);
            if (IsEffective == 1)   //如果数据有效，则进行解析，并更新页面
            {
                if (s.substring(10, 12).equals("04") || s.substring(10, 12).equals("03"))   //如果是读寄存器状态，则判断是读寄存器1（室内参数），还是寄存器2（灯光信息）的状态
                {
                    if (s.substring(16, 18).equals("22") && a.length > 40)  //寄存器1，参数信息，长度34个字节
                    {
                        try {
                            String switch_shake = (a[34] & bits[0]) == bits[0] ? "1" : "0";     //振动报警开关状态
                            String switch_pm25 = (a[34] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5报警开关状态
                            String switch_arofene = (a[34] & bits[2]) == bits[2] ? "1" : "0";      //甲醛报警开关状态
                            String switch_smoke = (a[34] & bits[3]) == bits[3] ? "1" : "0";        //烟雾报警开关状态
                            String switch_tmp = (a[34] & bits[4]) == bits[4] ? "1" : "0";          //温度报警开关状态
                            String switch_person = (a[34] & bits[5]) == bits[5] ? "1" : "0";          //人员报警开关状态
                            warning_state[0] = switch_shake;
                            warning_state[1] = switch_pm25;
                            warning_state[2] = switch_arofene;
                            warning_state[3] = switch_smoke;
                            warning_state[4] = switch_tmp;
                            warning_state[5] = switch_person;  //人员报警开关
                        } catch (Exception e) {
                            return;
                        }
                    }
                }
                else if (s.substring(10, 12).equals("06"))   //如果是控制单个寄存器返回命令，则可能是报警信息，也可能是修改语音开关、报警开关返回的数据
                {//ab68 0000 f0 06 0305 0001 4d6e 0d0a
                    if (s.substring(12, 16).equals("0305"))   //报警信息，判断是哪个参数在报警,a[8]是高八位，a[9]是低八位
                    {
                        String str_warning = "";   //要报警的内容
                        int warning_flag = 0;   //哪几个需要进行报警的，个位代表振动，十位代表PM2.5，百位代表烟雾，千位代表温度
                        int If_warning = 0;   //是否要报警

                        if ((a[9] & bits[0]) == bits[0] && warning_state[0].equals("1"))    //振动报警，如果相等则表示该位报警
                        {
//                            If_warning = 1;
//                            str_warning += "振动强度";
//                            warning_flag += 1;
                        }
                        if ((a[9] & bits[1]) == bits[1] && warning_state[1].equals("1"))    //PM2.5报警，如果相等则表示该位报警
                        {
//                            if (If_warning == 1)
//                                str_warning += "、PM2.5";
//                            else
//                                str_warning += "PM2.5";
//                            If_warning = 1;
//                            warning_flag += 10;
                        }

                        if ((a[9] & bits[3]) == bits[3] && warning_state[3].equals("1"))    //烟雾报警，如果相等则表示该位报警
                        {
                            try {
                                if (If_warning == 1)
                                    str_warning += "、烟雾";
                                else
                                    str_warning += "烟雾";
                                If_warning = 1;
                                warning_flag += 100;
                                timer.cancel();
                                timer = new Timer();
                                timer.schedule(new TimeTask1(),15000);//无报警后15更改为无报警状态
                                addr = s.substring(4,8);
                                for (int i =0;i<data.size();i++){
                                    if (data.get(i).get("RSAddr").equals(addr)){
                                        warningState.get(i).put("warningState","1");
                                    }
                                }
                            }catch (Exception e){
                                return;
                            }

                        }
                        if ((a[9] & bits[4]) == bits[4] && warning_state[4].equals("1"))    //温度报警，如果相等则表示该位报警
                        {
//                            if (If_warning == 1)
//                                str_warning += "、温度";
//                            else
//                                str_warning += "温度";
//                            If_warning = 1;
//                            warning_flag += 1000;
                        }
                        if (If_warning >= 1) {
                            addr = s.substring(4,8);
                            for (int i=0;i<data.size();i++){
                                if (data.get(i).get("RSAddr").equals(addr)){
                                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");   //hh为小写是12小时制，为大写HH时时24小时制
                                    String date = sDateFormat.format(new java.util.Date());
                                    if (If_warning == 1)
                                        str_warning = date+" "+data.get(i).get("Name")+"发出火灾报警警告！";
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                            Message msg1 = new Message();
                            upDateDatabase(addr,str_warning, System.currentTimeMillis()+"");
                            msg1.obj = str_warning;
                            msg1.what = warning_flag;  //有哪几个在报警，用于语音提示
                            if (isShowDialog){
                                handler3.sendMessage(msg1);   //通知handler3进行报警
                            }
                        }
                    }
                }
            }
        }
    };

    /**
     * 更新报警数据库内容
     * @param addr
     * @param str_warning
     * @param time
     */
    private void upDateDatabase(String addr, String str_warning,String time) {
        DbHelper dh = new DbHelper(Main_Activity.this, "IBMS", null, 1);
        for (int i=0;i<data.size();i++){
            if (data.get(i).get("RSAddr").equals(addr)){
                SQLiteDatabase db=dh.getWritableDatabase();
                db.beginTransaction();
                String name = data.get(i).get("Name");
                ContentValues cv = new ContentValues();
                cv.put("Content",str_warning);
                cv.put("RSAddr",addr);
                cv.put("Time",time);
                cv.put("Name",name);
                db.insert("alert_tb", null, cv);
                LogUtil.i("报警记录已经添加==>"+"content="+str_warning+";addr="+addr+";时间="+time
                +";name="+name);
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
    }

    //广播接收者
   private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = intent.getIntExtra("ContentNum", 0);   //byte数组的长度
            byte[] data = intent.getByteArrayExtra("Content");  //内容数组
            if(count>13)   //通过handler将数据传过去
            {
                Message msg=new Message();
                msg.obj=data;
                msg.what=data.length;
                handler1.sendMessage(msg);
            }
        }
    };

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    //handler3用于显示报警
    Handler handler3=new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String str_warning=msg.obj.toString();
            if(dialog!=null)
            {
                try {
                    dialog.dismiss();
                }
                catch(Exception ex){ex.printStackTrace();}
            }
            builder = new AlertDialog.Builder(Main_Activity.this);
            LayoutInflater factory = LayoutInflater.from(Main_Activity.this);
            final View view = factory.inflate(R.layout.warning, null);
            TextView tx_warning = (TextView) view.findViewById(R.id.tx_warning);   //报警信息
            tx_warning.setText(str_warning);
            builder.setCancelable(true);
            builder.setView(view);
            dialog=builder.create();
            dialog.show();
            Button button = (Button) view.findViewById(R.id.button);
            button.setOnTouchListener(new TouchListener());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null)
                        dialog.dismiss();
                }
            });

            try {    //播放系统通知声音
                AudioManager mgr=(AudioManager) Main_Activity.this.getSystemService(Context.AUDIO_SERVICE);
                int volume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);   //获取系统音乐声音
                int warning_flag = msg.what;   //是哪项在报警
                if (warning_flag / 1000 > 0) { //温度
//                    soundPool.play(4,1, 1, 4, 0, 1);   //id，左声道，右声道，优先级(0最低)，是否循环（-1表示循环，其他表示循环次数），播放比率（0.5到2，一般为1，表示正常播放）
                }
                if((warning_flag%1000)/100>0) {  //烟雾
//                    soundPool.play(3,1, 1, 3, 0, 1);
                    soundPool.play(1,1, 1, 3, 0, 1);

                }
                if((warning_flag%100)/10>0) {   //PM2.5
//                    soundPool.play(2,1, 1, 2, 0, 1);
                }
                if((warning_flag%10)/1>0) {   //振动
//                    soundPool.play(1,1,1,1,0,1);
                }

            }
            catch (Exception e){
                Log.i("Order", "播放失败！" + e.toString());
            }
            // 震动效果的系统服务
            Vibrator vibrator = (Vibrator) Main_Activity.this.getSystemService(Main_Activity.this.VIBRATOR_SERVICE);
            vibrator.hasVibrator();   //检测当前硬件是否有vibrator
            vibrator.vibrate(1500);//振动1.5秒

        }
    };

    int flag = 0;//0正在播放，1播放完毕;

    ///下拉刷新处理的函数。
    private class GetDataTask extends AsyncTask<Void, Void, String> {
        // 后台处理部分
        @Override
        protected String doInBackground(Void... params) {
            // Simulates a background job.
            String str = "1";
            try {
                Thread.sleep(500);
                if (binder!=null){
                    binder.sendOrder("ab680000f00301000011911b0d0a",4);
                }
                Thread.sleep(400);
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
    int isShowGrey =1;
    private Timer timer = new Timer();
    class TimeTask1 extends TimerTask{
        @Override
        public void run() {
            warningState.clear();
            for (int i=0;i<data.size();i++){
                Map<String, String> map1 = new HashMap<>();
                map1.put("warningState", "0");
                warningState.add(map1);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                    adapter.notifyDataSetChanged();
                    }
                });
        }
    }
    }
}
