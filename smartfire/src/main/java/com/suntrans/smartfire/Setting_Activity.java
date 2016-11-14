package com.suntrans.smartfire;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import convert.Converts;
import services.MainService;

/**
 * Created by pc on 2016/8/20.
 */
public class Setting_Activity extends AppCompatActivity {
    private TextView value_smoke;   //烟雾报警阈值
    private TextView value_tmp;    //温度报警阈值
    private TextView value_shake;    //振动报警阈值
    private TextView value_pm;   //pm报警阈值
    private LinearLayout layout_back;   //返回键
    private LinearLayout layout5;   //开关状态栏
    private String RSAddr = "0000";  //第六感地址
    private int hold_tmp,hold_smoke,hold_shake,hold_pm,hold_arofene;   //5个报警阈值
    private Dialog dialog;  //提示框
    private String which;   //当前正在发送什么类型的指令
    private final String WRITE_VALUE="2";    //写标志
    private long time;
    private byte[] bits={(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};     //从1到8只有一位是1，用于按位与计算，获取某一位的值
    private String warning_state[]=new String[]{"1","1","1","1","1","1"};   //是否报警，依次是振动、PM2.5、甲醛、烟雾、温度，人员
    public MainService.ibinder binder;  //用于Activity与Service通信
    private ServiceConnection con = new ServiceConnection() {
        @Override   //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        public void onServiceConnected(ComponentName name, IBinder service) {
            //  Toast.makeText(getApplication(), "绑定成功！", Toast.LENGTH_SHORT).show();
            binder=(MainService.ibinder)service;   //activity与service通讯的类，调用对象中的方法可以实现通讯
//            binder.sendOrder("aa68"+"0000"+"03 0100 0007",2);   //请求开关状态
//            Log.v("Time", "绑定后时间：" + String.valueOf(System.currentTimeMillis()));
            binder.sendOrder("ab68" + RSAddr + "f003 0700 0005", MainService.SIXSENSOR);   //读取外间报警阈值
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
    private Handler handler1=new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            byte[] a = (byte[]) (msg.obj);    //byte数组a即为客户端发回的数据，aa68 0006是单个开通道，aa68 0003是所有的通道
            // String ipaddr = (String) (map.get("ipaddr"));    //开关的IP地址
            String s = "";                       //保存命令的十六进制字符串
            for (int i = 0; i < msg.what; i++) {
                String s1 = Integer.toHexString((a[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                if (s1.length() == 1)
                    s1 = "0" + s1;
                s = s + s1;
            }
            s = s.replace(" ", ""); //去掉空格
            s=s.toLowerCase();
            int IsEffective = 1;    //指令是否有效，0表示无效，1表示有效；对于和第六感官通讯而言，包头为ab68的数据才有效
            if (msg.what <= 10||!s.substring(0, 8).equals("ab68"+RSAddr)) {
                return;
            }

            if(IsEffective==1) {
                if (s.substring(10, 12).equals("03")||s.substring(10,12).equals("04"))   //如果是读寄存器状态，则判断是读寄存器1（室内参数），还是寄存器0304（报警开关状态），还是寄存器7报警阈值
                {
                    if (s.substring(16, 18).equals("22")&&a.length>32)  //寄存器1，室内参数信息，包含报警开关状态，a[32]是存放开关状态的
                    {
                        String switch_shake = (a[34] & bits[0]) == bits[0] ? "1" : "0";     //振动
                        String switch_pm25 = (a[34] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5
                        String switch_arofene = (a[34] & bits[2]) == bits[2] ? "1" : "0";      //甲醛
                        String switch_smoke = (a[34] & bits[3]) == bits[3] ? "1" : "0";        //烟雾
                        String switch_tmp = (a[34] & bits[4]) == bits[4] ? "1" : "0";          //温度


                    }
                    else if(s.substring(12, 14).equals("02")&&s.substring(14,18).equals("0304")&&a.length>8)    //寄存器0304，报警开关状态
                    {
                        String switch_shake = (a[8] & bits[0]) == bits[0] ? "1" : "0";     //振动
                        String switch_pm25 = (a[8] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5
                        String switch_arofene = (a[8] & bits[2]) == bits[2] ? "1" : "0";      //甲醛
                        String switch_smoke = (a[8] & bits[3]) == bits[3] ? "1" : "0";        //烟雾
                        String switch_tmp = (a[8] & bits[4]) == bits[4] ? "1" : "0";          //温度

                    }//ab 68 00 0f  f0 03 0700 0a 0232021402140203020460ee0d0a
                    else if(s.substring(16,18).equals("0a")&&a.length>16)     //寄存器7，报警阈值
                    {   // ab68 000a f003 0700 0a 0232 0215 0214 0207 0204 31ef 0d0a
                        // ab68 000a f003 0700 0a 0232 0214 0214 0207 0204 212f 0d0a
                        // ab68 000a f003 0700 0a 0232 0215 0214 0207 0204 31ef 0d0a
                        hold_tmp=(a[9]==2?1:-1)*(a[10]&0xff);   //温度报警值
                        hold_smoke=(a[11]==2?1:-1)*(a[12]&0xff);   //烟雾报警值
                        hold_arofene=(a[13]==2?1:-1)*(a[14]&0xff);   //甲醛报警值
                        hold_pm=(a[15]==2?1:-1)*(a[16]&0xff);   //PM2.5报警阈值
                        hold_shake=(a[17]==2?1:-1)*(a[18]&0xff);   //振动报警阈值
                        DecimalFormat df1   = new DecimalFormat("0.0");    //保留一位小数
                        double double_shake = hold_shake * 0.1;    //振动阈值的真实值，乘以0.1
                        value_tmp.setText(String.valueOf(hold_tmp));     //温度阈值的真实值，乘以1
                        value_smoke.setText(String.valueOf(hold_smoke*100));   //烟雾阈值的真实值，乘以100
                        value_pm.setText(String.valueOf(hold_pm*100));    //PM2.5阈值的真实值，乘以100
                        value_shake.setText(df1.format(double_shake));   //只保留一位小数

                    }
                }
                else if(s.substring(10,12).equals("06"))   //如果返回06，则是控制某个寄存器返回，或报警信息主动上报。
                {
                    /*if(s.substring(12,16).equals("0304")&&a.length>9)   //写开关状态的返回
                    {
                        if(which.equals(WRITE_STATE)) {
                            which = "100";
                            Message msg1 = new Message();
                            msg1.what=0;   //关闭dialogprogress
                            handler2.sendMessage(msg1);
                        }
                        String switch_shake = (a[9] & bits[0]) == bits[0] ? "1" : "0";     //振动
                        String switch_pm25 = (a[9] & bits[1]) == bits[1] ? "1" : "0";        //PM2.5
                        String switch_arofene = (a[9] & bits[2]) == bits[2] ? "1" : "0";      //甲醛
                        String switch_smoke = (a[9] & bits[3]) == bits[3] ? "1" : "0";        //烟雾
                        String switch_tmp = (a[9] & bits[4]) == bits[4] ? "1" : "0";          //温度
                        voice_state=(a[9] & bits[5]) == bits[5] ? "1" : "0";     //语音开关状态
                        data.get(0).put("State", switch_shake);
                        data.get(1).put("State", switch_pm25);
                        data.get(2).put("State", switch_arofene);
                        data.get(3).put("State", switch_smoke);
                        data.get(4).put("State", switch_tmp);
                        ((Adapter)list.getAdapter()).notifyDataSetChanged();
                    }
*/
                    if(s.substring(12,15).equals("070")&&a.length>9)      //修改某个报警阈值
                    {
                        if(which.equals(WRITE_VALUE))
                        {
                            which = "100";
                            Message msg1 = new Message();
                            msg1.what=0;   //关闭dialogprogress
                            handler2.sendMessage(msg1);
                        }
                        int item=Integer.valueOf(s.substring(15,16));   //判断是哪个参数，0代表振动，4代表温度。中间依次是烟雾、甲醛、PM2.5
                        switch(item){
                            case 0:
                                hold_shake=(a[8] == 2 ? 1 : -1) * (a[9] & 0xff);
                                value_shake.setText(String.valueOf(hold_shake));
                                break;
                            case 1:
                                hold_smoke=(a[8] == 2 ? 1 : -1) * (a[9] & 0xff);
                                value_smoke.setText(String.valueOf(hold_smoke));
                                break;
                            case 2:
                                break;
                            case 3:
                                hold_pm=(a[8] == 2 ? 1 : -1) * (a[9] & 0xff);
                                value_pm.setText(String.valueOf(hold_pm));
                                break;
                            case 4:
                                hold_tmp=(a[8] == 2 ? 1 : -1) * (a[9] & 0xff);
                                value_tmp.setText(String.valueOf(hold_tmp));
                                break;
                            default:break;
                        }
//                        data.get(item).put("Value", String.valueOf((a[8] == 2 ? 1 : -1) * (a[9] & 0xff)));

                    }
                }
                else if(s.substring(10,12).equals("10")&&a.length>11)   //修改多个寄存器的值
                {
                    if(s.substring(12,20).equals("07000005"))   //是写报警阈值寄存器，表示写成功了
                    {
                        if(which.equals(WRITE_VALUE)) {
                            which = "100";
                            Message msg1 = new Message();
                            msg1.what=0;   //关闭dialogprogress
                            handler2.sendMessage(msg1);
                        }
                        binder.sendOrder("ab68 " +  RSAddr + "f003 0700 0005", MainService.SIXSENSOR);   //读取报警阈值
                    }
                }


            }

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
                Context context = Setting_Activity.this;
                dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);  //dialog去掉标题
//                builder = new AlertDialog.Builder(SmartConfig_Activity.this);
                View view = LayoutInflater.from(context).inflate(R.layout.progressdialog,null);
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
                lp.width = Converts.dip2px(context,  220);//弹出框的宽度 display.getWidth() -
                lp.height= Converts.dip2px(context, 150);   //高度
                lp.alpha=0.9f;
                dialogwindow.setAttributes(lp);
                if(Setting_Activity.this.isFinishing()){}
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
    private TextView tv_title;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(getIntent().getStringExtra("Name"));
        RSAddr = getIntent().getStringExtra("RSAddr");
//        System.out.println("SBBBBBBB="+RSAddr);
        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        value_smoke = (TextView) findViewById(R.id.value_smoke);
        value_tmp = (TextView) findViewById(R.id.value_tmp);
        value_shake = (TextView) findViewById(R.id.value_shake);
        value_pm = (TextView) findViewById(R.id.value_pm);
        layout5 = (LinearLayout) findViewById(R.id.layout5);   //开关状态
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        layout5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Setting_Activity.this, Room_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
        value_smoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(Setting_Activity.this);
                final View view = factory.inflate(R.layout.warning_input, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(Setting_Activity.this);
                builder.setTitle("请输入烟雾报警阈值：");
                final EditText tx1= (EditText) view.findViewById(R.id.tx1);   //整型数值
                tx1.setHint(String.valueOf(hold_smoke*100));

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
                                new_value_int = (int) (new_value * 0.01);
                                String[] values = new String[5];  //阈值数组，依次是温度，烟雾，甲醛，PM2.5，振动
                               //依次将五个值赋值给数组
                                int p =hold_tmp;  //温度
                                int p_abs = Math.abs(p);   //p的绝对值
                                values[0] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =new_value_int;  //烟雾
                                p_abs = Math.abs(p);   //p的绝对值
                                values[1] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_arofene;  //甲醛
                                p_abs = Math.abs(p);   //p的绝对值
                                values[2] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_pm;    //PM2.5
                                p_abs = Math.abs(p);   //p的绝对值
                                values[3] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_shake;  //振动
                                p_abs = Math.abs(p);   //p的绝对值
                                values[4] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                String order_value="";
                                for(int j=0;j<=4;j++)
                                    order_value+=values[j];
                                String order = "ab68" + RSAddr + "f010 0700 0005 0a"+order_value;   //顺序是温度，烟雾，甲醛，PM2.5，振动
                                which = WRITE_VALUE;   //wich=2，代表更改单个报警阈值
                                Timer timer = new Timer(true);     //定义定时器，定时执行关闭progressdialog命令，定时时长为2秒
                                timer.schedule(new TimerTask() {
                                    public void run() {     //在新线程中执行
                                        if (!which.equals("100")) {
                                            Message message = new Message();
                                            message.what = 1;       //1表示要显示
                                            message.obj = "正在发送命令";
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
                                binder.sendOrder(order, MainService.SIXSENSOR);
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
        value_tmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(Setting_Activity.this);
                final View view = factory.inflate(R.layout.warning_input, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(Setting_Activity.this);
                builder.setTitle("请输入温度报警阈值：");
                final EditText tx1= (EditText) view.findViewById(R.id.tx1);   //整型数值
                tx1.setHint(String.valueOf(hold_tmp));

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
                                new_value_int = (int) (new_value);
                                String[] values = new String[5];  //阈值数组，依次是温度，烟雾，甲醛，PM2.5，振动
                                //依次将五个值赋值给数组
                                int p =new_value_int;  //温度
                                int p_abs = Math.abs(p);   //p的绝对值
                                values[0] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_smoke;  //烟雾
                                p_abs = Math.abs(p);   //p的绝对值
                                values[1] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_arofene;  //甲醛
                                p_abs = Math.abs(p);   //p的绝对值
                                values[2] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_pm;    //PM2.5
                                p_abs = Math.abs(p);   //p的绝对值
                                values[3] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_shake;  //振动
                                p_abs = Math.abs(p);   //p的绝对值
                                values[4] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                String order_value="";
                                for(int j=0;j<=4;j++)
                                    order_value+=values[j];
                                String order = "ab68" + RSAddr + "f010 0700 0005 0a"+order_value;   //顺序是温度，烟雾，甲醛，PM2.5，振动
                                which = WRITE_VALUE;   //wich=2，代表更改单个报警阈值
                                Timer timer = new Timer(true);     //定义定时器，定时执行关闭progressdialog命令，定时时长为2秒
                                timer.schedule(new TimerTask() {
                                    public void run() {     //在新线程中执行
                                        if (!which.equals("100")) {
                                            Message message = new Message();
                                            message.what = 1;       //1表示要显示
                                            message.obj = "正在发送命令";
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

                                binder.sendOrder(order, MainService.SIXSENSOR);
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
        value_pm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(Setting_Activity.this);
                final View view = factory.inflate(R.layout.warning_input, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(Setting_Activity.this);
                builder.setTitle("请输入PM2.5报警阈值：");
                final EditText tx1= (EditText) view.findViewById(R.id.tx1);   //整型数值
                tx1.setHint(String.valueOf(hold_pm*100));

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
                                new_value_int = (int) (new_value*0.01);
                                String[] values = new String[5];  //阈值数组，依次是温度，烟雾，甲醛，PM2.5，振动
                                //依次将五个值赋值给数组
                                int p =hold_tmp;  //温度
                                int p_abs = Math.abs(p);   //p的绝对值
                                values[0] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_smoke;  //烟雾
                                p_abs = Math.abs(p);   //p的绝对值
                                values[1] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_arofene;  //甲醛
                                p_abs = Math.abs(p);   //p的绝对值
                                values[2] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =new_value_int;    //PM2.5
                                p_abs = Math.abs(p);   //p的绝对值
                                values[3] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_shake;  //振动
                                p_abs = Math.abs(p);   //p的绝对值
                                values[4] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                String order_value="";
                                for(int j=0;j<=4;j++)
                                    order_value+=values[j];
                                String order = "ab68" + RSAddr + "f010 0700 0005 0a"+order_value;   //顺序是温度，烟雾，甲醛，PM2.5，振动
                                which = WRITE_VALUE;   //wich=2，代表更改单个报警阈值
                                Timer timer = new Timer(true);     //定义定时器，定时执行关闭progressdialog命令，定时时长为2秒
                                timer.schedule(new TimerTask() {
                                    public void run() {     //在新线程中执行
                                        if (!which.equals("100")) {
                                            Message message = new Message();
                                            message.what = 1;       //1表示要显示
                                            message.obj = "正在发送命令";
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

                                binder.sendOrder(order, MainService.SIXSENSOR);
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
        value_shake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(Setting_Activity.this);
                final View view = factory.inflate(R.layout.warning_input, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(Setting_Activity.this);
                builder.setTitle("请输入振动报警阈值：");
                final EditText tx1= (EditText) view.findViewById(R.id.tx1);   //整型数值
                DecimalFormat df=new DecimalFormat("0.0");
                tx1.setHint(df.format(hold_shake*0.1));

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
                                new_value_int = (int) (new_value*10);
                                String[] values = new String[5];  //阈值数组，依次是温度，烟雾，甲醛，PM2.5，振动
                                //依次将五个值赋值给数组
                                int p =hold_tmp;  //温度
                                int p_abs = Math.abs(p);   //p的绝对值
                                values[0] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_smoke;  //烟雾
                                p_abs = Math.abs(p);   //p的绝对值
                                values[1] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_arofene;  //甲醛
                                p_abs = Math.abs(p);   //p的绝对值
                                values[2] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =hold_pm;    //PM2.5
                                p_abs = Math.abs(p);   //p的绝对值
                                values[3] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                p =new_value_int;  //振动
                                p_abs = Math.abs(p);   //p的绝对值
                                values[4] =(p > 0 ? "02" : "00") +Converts.Bytes2HexString(new byte[]{(byte) ((p_abs > 255) ? 255 : p_abs)});

                                String order_value="";
                                for(int j=0;j<=4;j++)
                                    order_value+=values[j];
                                String order = "ab68" + RSAddr + "f010 0700 0005 0a"+order_value;   //顺序是温度，烟雾，甲醛，PM2.5，振动
                                which = WRITE_VALUE;   //wich=2，代表更改单个报警阈值
                                Timer timer = new Timer(true);     //定义定时器，定时执行关闭progressdialog命令，定时时长为2秒
                                timer.schedule(new TimerTask() {
                                    public void run() {     //在新线程中执行
                                        if (!which.equals("100")) {
                                            Message message = new Message();
                                            message.what = 1;       //1表示要显示
                                            message.obj = "正在发送命令";
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

                                binder.sendOrder(order, MainService.SIXSENSOR);
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
        Converts.setTranslucentStatus(Setting_Activity.this, true);   //设置沉浸式状态栏
        //绑定MainService
        Intent intent = new Intent(getApplicationContext(), MainService.class);    //指定要绑定的service
        bindService(intent, con, Context.BIND_AUTO_CREATE);   //绑定主service
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive
        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(con);   //解除Service的绑定
        unregisterReceiver(broadcastreceiver);  //注销广播接收者
    }
}
