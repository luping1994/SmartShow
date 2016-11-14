package com.suntrans.smartfire;


//import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.viewbadger.BadgeView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import convert.Converts;
import services.MainService;

/**
 * Created by 石奋斗 on 2016/7/16.
 */
public class Room_Activity extends AppCompatActivity {
    private byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private int result_code=0;
    private String which = "100";    //是对那个通道进行控制
    private long time;    //触发progressdialog显示的时间
    private int IsFinish=0 ;   //用来判断下拉刷新是否完成，0表示刷新未完成，1表示刷新完成
    private String MACAddr;   //开关的MAC地址
    private String IPAddr; //开关的IP地址
    private String SName;   //开关名称
    private String RSAddr="0000";    //开关地址
    private LinearLayout layout_back;   //返回键
    private TextView title;  //标题
    private DisplayMetrics displayMetrics = new DisplayMetrics();   //屏幕信息
    private SwipeRefreshLayout refreshlayout;   //下拉刷新控件
    private RecyclerView recyclerview;
    private Dialog dialog;      //定义”加载中。。。“的圆形滚动条弹出框
    private mAdapter adapter;
    private GridLayoutManager mgr;
    private ArrayList<Map<String,Object>> data=new ArrayList<Map<String,Object>>();
    private Bitmap bitmap_on;
    private Bitmap bitmap_off;
    public MainService.ibinder binder;  //用于Activity与Service通信
    private ServiceConnection con = new ServiceConnection() {
        @Override   //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        public void onServiceConnected(ComponentName name, IBinder service) {
//            Toast.makeText(getApplication(), "绑定成功！", Toast.LENGTH_SHORT).show();
            binder=(MainService.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
            binder.sendOrder("aa68"+RSAddr+"03 0100 0007",2);   //请求开关状态
//            Log.v("Time", "绑定后时间：" + String.valueOf(System.currentTimeMillis()));
        }

        @Override   //service因异常而断开的时候调用此方法
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplication(), "网络错误！", Toast.LENGTH_SHORT).show();

        }
    };;   ///用于绑定activity与service
    //新建广播接收器，接收服务器的数据并解析，根据第六感官的地址和开关的地址将数据转发到相应的Fragment
    private BroadcastReceiver broadcastreceiver=new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent){
            int count = intent.getIntExtra("ContentNum", 0);   //byte数组的长度
            byte[] data = intent.getByteArrayExtra("Content");  //内容数组
           /* String content = "";   //接收的字符串
            for (int i = 0; i < count; i++) {
                String s1 = Integer.toHexString((data[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                if (s1.length() == 1)
                    s1 = "0" + s1;
                content = content + s1;
            }*/
            if(count>13)   //通过handler将数据传过去
            {
                Message msg=new Message();
                msg.obj=data;
                msg.what=data.length;
                handler1.sendMessage(msg);
            }

        }
    };//广播接收器
    //用来解析数据，刷新页面
    public Handler handler1=new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
           // Map<String, Object> map = (Map<String, Object>) msg.obj;
            byte[] a = (byte[]) (msg.obj);    //byte数组a即为客户端发回的数据，aa68 地址 06是单个开通道，aa68 地址 03是所有的通道
            // String ipaddr = (String) (map.get("ipaddr"));    //开关的IP地址
            String s = "";                       //保存命令的十六进制字符串
            for (int i = 0; i < msg.what; i++) {
                String s1 = Integer.toHexString((a[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                if (s1.length() == 1)
                    s1 = "0" + s1;
                s = s + s1;
            }
            //   String crc=Converts.GetCRC(a, 2, msg.what-2-2);    //获取返回数据的校验码，倒数第3、4位是验证码，倒数第1、2位是包尾0d0a
            s = s.replace(" ", ""); //去掉空格
            //  Log.i("Order", "收到数据：" + s);
            int IsEffective = 0;    //指令是否有效，0表示无效，1表示有效；对于和第六感官通讯而言，包头为ab68的数据才有效
            if (msg.what > 13) {
                if (s.substring(0, 4).equals("aa69"))
                    IsEffective = 1;    //数据有效
            }
            if(IsEffective==1)   //数据有效
            {
               // final String return_addr=s.substring(4,12);   //返回数据的开关地址
                if (s.substring(12, 14).equals("03"))   //如果是读寄存器状态，解析出开关状态
                {
                    if (s.substring(14, 16).equals("0e"))
                    {
                        String[] states={"0","0","0","0","0","0","0","0","0","0","0"};   //十个通道的状态，state[0]对应1通道,state[10]对应总开关
                        for(int i=0;i<8;i++)   //先获取前八位的开关状态
                        {
                            states[i]=((a[9]&bits[i])==bits[i])?"1":"0";   //1-8通道
                        }
                        for(int i=0;i<3;i++)
                        {
                            states[i+8]=((a[8]&bits[i])==bits[i])?"1":"0";  //9、10通道,总开关
                        }
                        for(int i=0;i<data.size();i++)
                        {
                            data.get(i).put("State",states[Integer.valueOf(data.get(i).get("Channel").toString().equals("0")?"11":data.get(i).get("Channel").toString())-1]);
                        }

                        adapter.notifyDataSetChanged();
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
                        if(state==0)   //如果总开关关了，那肯定所有通道都关了
                        {
                            for(int i=0;i<data.size();i++)
                            {
                                 data.get(i).put("State","0");
                            }
                        }
                        else{   //如果是总开关打开，那么只更新总开关的状态
                            data.get(0).put("State", "1");
                        }

                    }
                    else     //如果通道号不为0，则更改data中的状态，并更新
                    {


                        for(int i=0;i<data.size();i++)
                        {
                            Map<String, Object> map = data.get(i);
                            if(map.get("Channel").toString().equals(String.valueOf(k)))
                                  map.put("State",state==1?"1":"0");

                        }
                     /* int ison=0;  //是否有通道状态为开
                       for(int i=1;i<data.size();i++)
                        {
                            Map<String, Object> map = data.get(i);
                            if(map.get("State").toString().equals("1"))
                                ison++;
                        }
                        if(ison>=1)
                            data.get(0).put("State", "1");
                        else
                            data.get(0).put("State", "0");*/
                    }
                    if(String.valueOf(k).equals(which))
                    {
                        which="100";
                        Message message = new Message();
                        message.what =0;       //0表示要隐藏
                        handler2.sendMessage(message);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            //  Log.i("Order", "外间总开关：" + String.valueOf(Mainstate) + "里间总开关" + String.valueOf(Mainstate1));
        }
    };
    private Handler handler2=new Handler()   //用来控制progressdialog的显示和销毁
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            if(msg.what==0)   //如果是要关闭progresedialog的显示（收到相应反馈，则进行此操作）
            {
                if(dialog!= null)
                {
                    dialog.dismiss();
                    dialog=null;
                }
                //which="100";
            }
            else if(msg.what==1)   //是要显示progressdialog
            {
                String loading_text = msg.obj.toString();
                if(loading_text.equals("")||loading_text==null){
                    loading_text = "正在加载...";
                }

                if(dialog!= null)
                {
                    dialog.dismiss();
                    dialog=null;
                }
                dialog = new Dialog(Room_Activity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);  //dialog去掉标题
//                builder = new AlertDialog.Builder(SmartConfig_Activity.this);
                View view = LayoutInflater.from(Room_Activity.this).inflate(R.layout.progressdialog,null);
                view.setBackgroundResource(R.color.white);   //背景颜色
                ProgressBar progressbar = (ProgressBar) view.findViewById(R.id.loading__progressBar);   //圆形加载条
                TextView tx_loading = (TextView)view.findViewById(R.id.tx_loading);  //加载的文字
                tx_loading.setText(loading_text);
                dialog.setContentView(view);
                dialog.setCancelable(false);
                Window dialogwindow=dialog.getWindow();
                WindowManager.LayoutParams lp=dialogwindow.getAttributes();
                dialogwindow.setGravity(Gravity.CENTER);
                lp.x=0;
                lp.y=0;
                lp.width = Converts.dip2px(Room_Activity.this,  220);//弹出框的宽度 display.getWidth() -
                lp.height= Converts.dip2px(Room_Activity.this, 150);   //高度
                lp.alpha=0.9f;
                dialogwindow.setAttributes(lp);

                dialog.show();
            }
            else if(msg.what==2)   //如果是要根据时间判断是否关闭progressdialog的显示，用于通讯条件不好，收不到反馈时
            {
                if(new Date().getTime()-time>=1900)
                {
                    if(dialog!= null)
                    {
                        dialog.dismiss();
                        dialog=null;
                    }
                    if(!which.equals("100"))
                    {
                        which="100";
                        // Toast.makeText(getActivity(), "网络错误！", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room);

        //绑定MainService
        Intent intent = new Intent(getApplicationContext(), MainService.class);    //指定要绑定的service
        bindService(intent, con, Context.BIND_AUTO_CREATE);   //绑定主service
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive
        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver

        //数据初始化
        DataInit();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);//获取屏幕大小的信息
        int width= Converts.px2dip(Room_Activity.this,displayMetrics.widthPixels);   //屏幕宽度，单位转化为dip
        int spancount=(width-22)/85;   //gridview的列宽
//        Log.i("Order", "width==>" + width + "dip;spancount==>" + spancount + "个");

        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

            });
        title = (TextView) findViewById(R.id.tx_title);
        title.setText(SName);
        refreshlayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);  //下拉刷新控件
        refreshlayout.setSize(SwipeRefreshLayout.LARGE);  //设置大小
        refreshlayout.setColorSchemeResources(R.color.white);   //设置滚动条颜色，可以设置多个
        refreshlayout.setProgressBackgroundColorSchemeResource(R.color.bg_action);   //设置背景颜色，可以设置多个

        refreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                IsFinish=0;   //表示开始刷新
                new GetDataTask().execute();   //执行刷新任务
            }
        });
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        //设置布局管理器GridLayout spancount列
        mgr=new GridLayoutManager(this,spancount);
        recyclerview.setLayoutManager(mgr);
        //添加分割线，自定义RecyclerViewDivider继承RecyclerView.ItemDecoration
//        Context context=getApplicationContext();
//        recyclerview.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL,
//                Converts.dip2px(context,1f),R.color.gray));  //不知道为什么，此处修改分割线的颜色无效
//        DividerGridItemDecoration divider = new DividerGridItemDecoration(Room_Activity.this);
//        recyclerview.addItemDecoration(divider);   //设置grid间隔
        adapter=new mAdapter();
        recyclerview.setAdapter(adapter);

        mgr.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {   //设置每项的宽度，占用几个列宽
                return  1;
            }
        });
        Converts.setTranslucentStatus(Room_Activity.this,true);   //设置标题栏透明
        bitmap_on = BitmapFactory.decodeResource(Room_Activity.this.getResources(), R.mipmap.ic_bulb_on);
        bitmap_off = BitmapFactory.decodeResource(Room_Activity.this.getResources(), R.mipmap.ic_bulb_off);
        bitmap_on = Converts.toRoundCorner(bitmap_on, Converts.dip2px(Room_Activity.this,10));  //实现图片圆角
        bitmap_off = Converts.toRoundCorner(bitmap_off, Converts.dip2px(Room_Activity.this,10));   //实现图片圆角

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(con);   //解除Service的绑定
        unregisterReceiver(broadcastreceiver);  //注销广播接收者
    }

    //数据初始化
    private void DataInit(){


        for(int i=1;i<=10;i++){
            Map<String, Object> map = new HashMap<>();
            map.put("Name", "通道"+i);
            map.put("Channel", i);
            map.put("State", "0");
            data.add(map);
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
                    binder.sendOrder("aa68"+RSAddr+"03 0100 0007",2);   //请求开关状态
                    Thread.sleep(1000);
                    str = "1"; // 表示请求成功
                } catch (InterruptedException e1) {

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
            refreshlayout.setRefreshing(false);   //结束加载动作
            super.onPostExecute(result);//这句是必有的，AsyncTask规定的格式
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
            RecyclerView.ViewHolder holder= new viewHolder1(LayoutInflater.from(
                    Room_Activity.this).inflate(R.layout.roomgridview, parent,false));

            return holder;
        }

        /***
         * 绑定数据
         * @param holder   绑定哪个holder，用if(holder instanceof mViewHolder1)来判断类型，再绑定数据
         * @param position
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position)
        {
            //判断holder是哪个类，从而确定是哪种布局
            /////布局1
            if(holder instanceof viewHolder1) {
                viewHolder1 viewholder = (viewHolder1) holder;
                Map<String, Object> map = data.get(position);
                final String Name = map.get("Name").toString();
                final String Channel = map.get("Channel").toString();
                final String State = map.get("State").toString();

                viewholder.name.setText(Name);
                if(State.equals("0") ){    //如果开关状态是关的
                    viewholder.image.setImageBitmap(bitmap_off);
                    viewholder. badgeview.setBackgroundResource(R.drawable.offdot);    //设置图标
                }
                else{
                    viewholder.image.setImageBitmap(bitmap_on);
                    viewholder. badgeview.setBackgroundResource(R.drawable.ondot);    //设置图标
                }
                viewholder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {   //根据点击的通道发送相应指令
                        if(which.equals("100")) {
                            which = Channel;    //设置which的值，表明是第channel个通道发生了改变
                            Timer timer = new Timer(true);     //定义定时器，定时执行关闭progressdialog命令，定时时长为2秒
                            timer.schedule(new TimerTask() {
                                public void run() {     //在新线程中执行
                                    if (!which.equals("100")) {
                                        Message message = new Message();
                                        message.what = 1;       //1表示要显示
                                        message.obj="正在发送命令....";
                                        handler2.sendMessage(message);
                                    }
                                    Timer timer1 = new Timer(true);     //定义定时器，定时执行关闭progressdialog命令，定时时长为2秒
                                    timer1.schedule(new TimerTask() {
                                        public void run() {     //在新线程中执行
                                            if (!which.equals(100)) {
                                                Message message = new Message();
                                                message.what = 2;       //2表示要隐藏
                                                handler2.sendMessage(message);
                                            }
                                        }
                                    }, 2500); //2.5s后判断是否关闭progressdialog，若没关闭，则进行关闭
                                }
                            }, 250); //0.25s后判断是否关闭progressdialog，若没关闭，则进行关闭

                            binder.sendOrder("aa68"+RSAddr + "06 030" + (Channel.equals("10") ? "a" : Channel) + " 000" + (State.equals("0") ? "1" : "0"),2);

                        }
                    }
                });
                // 设置长点击监听
                viewholder.layout.setOnLongClickListener(new View.OnLongClickListener(){    //设置长点击事件

                    @Override
                    public boolean onLongClick(View v) {
                        // TODO Auto-generated method stub
                        return false;
                    }});
            }
        }



        @Override
        public int getItemCount()
        {
            return data.size();
        }
        /**
         * 决定元素的布局使用哪种类型
         *在本activity中，布局1使用R.layout.roomgridview，
         * @param position 数据源的下标
         * @return 一个int型标志，传递给onCreateViewHolder的第二个参数 */
        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 布局类型1对应的ViewHolder，R.layout.listmain_userinfo
         */
        class viewHolder1 extends RecyclerView.ViewHolder
        {
            LinearLayout layout;   //整体布局
            LinearLayout layout1;    //图片布局
            ImageView image;    //图标
            TextView name;    //名称
            BadgeView badgeview;    //角标
            public viewHolder1(View view)
            {
                super(view);
                layout=(LinearLayout)view.findViewById(R.id.layout);
                layout1=(LinearLayout)view.findViewById(R.id.layout1);
                image=(ImageView)view.findViewById(R.id.image);
                name = (TextView) view.findViewById(R.id.name);
                badgeview = new BadgeView(getApplicationContext(), layout1);
                badgeview.setWidth(convert.Converts.dip2px(getApplicationContext(), 7));  //设置宽度为7dip
                badgeview.setHeight(convert.Converts.dip2px(getApplicationContext(), 7)); //设置高度为7dip
                badgeview.setBackgroundResource(R.drawable.offdot);    //设置图标
                badgeview.setBadgePosition(BadgeView.POSITION_TOP_RIGHT); //设置显示的位置，右上角
                badgeview.show();
            }
        }


    }


}
