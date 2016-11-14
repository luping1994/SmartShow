package com.suntrans.smartshow.service;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.activity.Meter_Activity;
import com.suntrans.smartshow.base.BaseApplication;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.MyObserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import static android.R.attr.x;


/**
 * Create by 1111b on 2015/12/12.
 *  主串口服务器service，与服务器连接，监听服务器发送的消息，并广播。在用户登录成功时启动
 */
public class MainService1 extends Service {

    public Socket client=null;    //保持TCP连接的串口服务器ip socket
    public String serverip;     //串口服务器IP
    public int port;    //串口服务器端口
    private IBinder binder;
    private String SerialNumber;   //手机唯一标识
    private ConnectivityManager mConnectivityManager;
    private NetworkInfo netInfo;
    public static int SWITCH=2;      //开关代号
    public static int SIXSENSOR=4;   //第六感代号
    public static int AMMETER=3;   //电表
    public static int WATERMETER=6;   //水表

    public Socket sixClient=null;    //保持TCP连接的第六感socket
    public String SixSensorip;     //第六感IP
    public int sixPort;    //第六感端口
    private String Addr="0001";    //第六感官的地址，从0000到ffff。跟用户名相对应

    public static boolean IsInnerNet ;//是否是内网

    @Override   //当activity与service绑定的时候会调用此方法
    public IBinder onBind(Intent intent) {
        Log.v("Service", "ServiceDemo onBind");

        Log.i("Intenet", "串口ip==>" + serverip);
        LogUtil.i("Intenet", "第六感ip==>" + SixSensorip);
        LogUtil.i("Intenet", "串口port==>" + port);
        if(client==null)   //如果client为空，则建立连接
            new Thread(){      //不能在主线程中访问网络，所以要新建线程
                public void run(){   //新建线程连接服务器，不占用主线程
                    try
                    {
                        //获取服务器ip
//                        final InetAddress serverAddr = InetAddress.getByName(sixSensorIp);// TCPServer.SERVERIP
                        //定义socketaddress
                        //final SocketAddress my_sockaddr = new InetSocketAddress(serverAddr, sixSensorPort);
                        client = new Socket(serverip, port);   //新建TCP连接
                        new TCPServerThread().start();    //开启新的线程接收数据
                        //client.connect(my_sockaddr,5000);	  //第二个参数是timeout
                        Thread.sleep(100);
                        DataOutputStream out=new DataOutputStream(client.getOutputStream());
                        // 把用户输入的内容发送给server
                        //String toServer = "ab68 F006 0500 000a 14 0001 0002 0003 0004 0005 0006 0007 0008 0009 000a";    //查询开关所有通道的状态
                        String toServer="ab70 "+SerialNumber;  //发送手机地址
                        toServer = toServer.replace(" ","");    //去掉空格
                        byte[] bt=null;
                        bt= Converts.HexString2Bytes(toServer);
                        String str=toServer+Converts.GetCRC(bt, 0, bt.length);   //添加校验码
                        Log.i("外网校验码",str);
                        byte[] bt1=Converts.HexString2Bytes(str);      //将完整的命令转换成十六进制
                        if(!client.isClosed())
                        {
                            out.write(bt1);
                            out.flush();
                            //out.close();   //关闭输出流
                        }
                    }
                    catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.i("Order", "出错：" + e.toString());
                    }

                }
            }.start();

        if(sixClient==null&&IsInnerNet==true)   //如果client为空，并且为内网模式，外网模式用一个client就够了则建立第六感客户端连接
            new Thread(){      //不能在主线程中访问网络，所以要新建线程
                public void run(){   //新建线程连接服务器，不占用主线程
                    try
                    {
                        //获取服务器ip
//                        final InetAddress serverAddr = InetAddress.getByName(sixSensorIp);// TCPServer.SERVERIP
                        //定义socketaddress
                        //final SocketAddress my_sockaddr = new InetSocketAddress(serverAddr, sixSensorPort);
                        sixClient = new Socket(SixSensorip, sixPort);   //新建TCP连接
                        if (IsInnerNet){
                            new TCPServerThread1().start();    //开启新的线程接收数据
                        }
                        //client.connect(my_sockaddr,5000);	  //第二个参数是timeout
                        Thread.sleep(100);
                        DataOutputStream out1=new DataOutputStream(client.getOutputStream());
                        // 把用户输入的内容发送给server
                        //String toServer = "ab68 F006 0500 000a 14 0001 0002 0003 0004 0005 0006 0007 0008 0009 000a";    //查询开关所有通道的状态
                        String toServer="ab70 "+SerialNumber;  //发送手机地址
                        toServer.replace(" ","");    //去掉空格
                        byte[] bt=null;
                        bt= Converts.HexString2Bytes(toServer);
                        String str=toServer+Converts.GetCRC(bt, 2, bt.length);   //添加校验码
                        Log.i("外网校验码2",str);
                        byte[] bt1=Converts.HexString2Bytes(str);      //将完整的命令转换成十六进制
                        if(!sixClient.isClosed())
                        {
                            out1.write(bt1);
                            out1.flush();
                            //out.close();   //关闭输出流
                        }
                    }
                    catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.i("Order", "出错：" + e.toString());
                    }

                }
            }.start();

        binder=new ibinder(){
            @Override
            public boolean sendOrder(final String order, final int dev){   //重写发送命令的方法

                new Thread()   //新建子线程，发送命令
                {

                    public void run(){
                        DataOutputStream out;
                        try
                        {

                            out = new DataOutputStream(client.getOutputStream());
                            String toServer = order;    //要发送到服务器的指令，添加包头和第六关关地址(这两项不进行校验)
                            toServer=toServer.replace(" ","");    //去掉空格
                            byte[] bt=null;
                            bt=Converts.HexString2Bytes(toServer);
                            String str="";
                            if (dev==SIXSENSOR)//第六感
                            {
                                if (IsInnerNet)
                                    str= toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";   //添加校验码和包尾
                                else
                                    str= "ab68"+toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";//外网添加前缀ab68协议
                            }
                            else if (dev==SWITCH)//路灯
                            {
                                if (IsInnerNet)
                                    str="f7"+toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";   //添加校验码和包尾
                                else
                                    str ="02 00 00 ff 00 57 1f 97"+toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";   //外网添加前缀协议
                            }
                            else if (dev==3)//电表
                                if (IsInnerNet){
                                    str ="f1"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //电表，添加校验和结束符
                                }else {
                                    str ="02 00 00 ff 00 57 1f 91"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //电表外网添加前面的协议
                                }
                            else if (dev==9)//智能家居这边的电表
                                if (IsInnerNet){
                                    str ="f2"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //电表，添加校验和结束符
                                }else {
                                    str ="02 00 00 ff 00 57 1f 92"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //电表，添加校验和结束符
                                }
                            else if (dev==6)//水表,热量表，气表 //水表 气表 热量表 校验码已经在activity算好
                            {   if (IsInnerNet)
                                    str = "f3"+toServer+"16";
                                else
                                    str = "02 00 00 ff 00 57 1f 93"+toServer+"16";
                            }
                            else if (dev==5)//氙气灯，三相电动机
                            {   if (IsInnerNet)
                                    str ="f5"+toServer;
                                else
                                   str ="02 00 00 ff 00 57 1f 95"+toServer;
                            }
                            else if (dev==7)
                                str = ""+toServer+Converts.getMeterCS(bt,2,bt.length)+"0d0a";
                            else if (dev==8)//三相电表
                            {
                                if (IsInnerNet)
                                    str ="f8"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //
                                else
                                    str ="02 00 00 ff 00 57 1f 98"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //
                            }

                            str = str.replace(" ","");
                            Log.i("Order","发送数据："+str);
                            str=str.replace(" ","");
                            byte[] bt1=Converts.HexString2Bytes(str);      //将完整的命令转换成十六进制
                            if(!client.isClosed())
                                if(client.isConnected())
                                    if(!client.isOutputShutdown())
                                    {
                                        out.write(bt1);
                                        out.flush();
                                    }
                        }
                        catch (Exception e) {			// 发送出错，证明TCP断开了连接，重新建立连接
                            try
                            {

                                if (client != null)
                                {
                                    Log.i("Info", "isConnected==>"+ String.valueOf(client.isConnected()));
                                    Log.i("Info", "isoutputShutdown==>" + String.valueOf(client.isOutputShutdown()));
                                    Log.i("Info", "isinputshutdowm==>" + String.valueOf(client.isInputShutdown()));
                                    try {
                                        client.shutdownInput();
                                        client.shutdownOutput();
                                        client.close();
                                    }
                                    catch(Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                client = null;
                                InetAddress serverAddr = InetAddress.getByName(serverip);// TCPServer.SERVERIP
                                client = new Socket(serverAddr, port);   //新建TCP连接
                                new TCPServerThread().start();
                                out = new DataOutputStream(client.getOutputStream());
                                Thread.sleep(100);
                                String toServer = "ab70 " + SerialNumber;  //发送手机地址，发送后手机才能接收到服务器的数据
                                toServer.replace(" ", "");    //去掉空格
                                byte[] bt = null;
                                bt = Converts.HexString2Bytes(toServer);
                                String str = toServer + Converts.GetCRC(bt, 0, bt.length);   //添加校验码
                                byte[] bt1 = Converts.HexString2Bytes(str);      //将完整的命令转换成十六进制
                                if (!client.isClosed())
                                {
                                    out.write(bt1);
                                    out.flush();
                                }
                                Thread.sleep(100);
                                toServer =  order;    //指令，添加包头和第六感官地址
                                toServer=toServer.replace(" ", "");    //去掉空格
                                bt = null;
                                bt = Converts.HexString2Bytes(toServer);
                                if (dev==SIXSENSOR){
                                    if (IsInnerNet)
                                        str= toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";   //添加校验码和包尾
                                    else
                                        str= "ab68"+toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";//外网添加前缀ab68协议
                                }
                                else if(dev==SWITCH){//路灯
                                    if (IsInnerNet)
                                        str="f7"+toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";   //添加校验码和包尾
                                    else
                                        str ="02 00 00 ff 00 57 1f 97"+toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";   //外网添加前缀协议
                                } else if (dev==3)//电表
                                    if (IsInnerNet){
                                        str ="f1"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //电表，添加校验和结束符
                                    }else {
                                        str ="02 00 00 ff 00 57 1f 91"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //电表外网添加前面的协议
                                    }
                                else if (dev==9)//智能家居这边的电表
                                    if (IsInnerNet){
                                        str ="f2"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //电表，添加校验和结束符
                                    }else {
                                        str ="02 00 00 ff 00 57 1f 92"+toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //电表，添加校验和结束符
                                    }
                                else if (dev==6)//水表 气表 热量表 校验码已经在activity算好
                                    if (IsInnerNet){
                                        str = "f3"+toServer+"16";
                                    }else {
                                        str = "02 00 00 ff 00 57 1f 93"+toServer+"16";
                                    }
                                else if (dev==5)//氙气灯
                                {   if (IsInnerNet)
                                        str ="f5"+toServer;
                                    else
                                        str ="02 00 00 ff 00 57 1f 95"+toServer;
                                }
                                else if (dev==7)
                                    str = toServer+Converts.getMeterCS(bt,2,bt.length)+"0d0a";
                                else if (dev==8)//三相电表
                                {
                                    if (IsInnerNet)
                                        str = "f8" + toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";  //
                                    else
                                        str = "02 00 00 ff 00 57 1f 98" + toServer + Converts.getMeterCS(bt, 1, bt.length) + "16";
                                }
                                str = str.replace(" ","");
                                LogUtil.i("service发送命令：="+str);
                                bt1 = Converts.HexString2Bytes(str);      //将完整的命令转换成十六进制
                                if (!client.isClosed()) {
                                    out.write(bt1);
                                    out.flush();
                                }
                            }
                            catch (Exception ee) {
                                Log.i("Info", "client重启出错" + ee.toString());
                            }

                        }
                    }
                }.start();
                return true;
            }

            @Override
            public boolean sendOrder2Sixsenor(final String order, final int dev) {
                if (IsInnerNet==false){//外网的话就直接共用一个发命令进程就行了
                    sendOrder(order,4);
                }else
                new Thread()   //新建子线程，发送命令
                {

                    public void run(){
                        DataOutputStream out;
                        try
                        {

                            out = new DataOutputStream(sixClient.getOutputStream());
                            String toServer = order;    //要发送到服务器的指令，添加包头和第六关关地址(这两项不进行校验)
                            toServer=toServer.replace(" ","");    //去掉空格
                            byte[] bt=null;
                            bt=Converts.HexString2Bytes(toServer);
                            String str="";
                            if(dev==SIXSENSOR)//第六感
                                str= toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";   //添加校验码和包尾
                            Log.i("Order","发送数据："+str);
                            byte[] bt1=Converts.HexString2Bytes(str);      //将完整的命令转换成十六进制
                            if(!sixClient.isClosed())
                                if(sixClient.isConnected())
                                    if(!sixClient.isOutputShutdown())
                                    {
                                        out.write(bt1);
                                        out.flush();
                                    }
                        }
                        catch (Exception e) {			// 发送出错，证明TCP断开了连接，重新建立连接
                            try
                            {

                                if (sixClient != null)
                                {
                                    Log.i("Info", "isConnected==>"+ String.valueOf(sixClient.isConnected()));
                                    Log.i("Info", "isoutputShutdown==>" + String.valueOf(sixClient.isOutputShutdown()));
                                    Log.i("Info", "isinputshutdowm==>" + String.valueOf(sixClient.isInputShutdown()));
                                    try {
                                        sixClient.shutdownInput();
                                        sixClient.shutdownOutput();
                                        sixClient.close();
                                    }
                                    catch(Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                sixClient = null;
                                InetAddress serverAddr = InetAddress.getByName(SixSensorip);// TCPServer.SERVERIP
                                sixClient = new Socket(serverAddr, sixPort);   //新建TCP连接
                                new TCPServerThread().start();
                                out = new DataOutputStream(sixClient.getOutputStream());
                                Thread.sleep(100);
                                String toServer = "ab70 " + SerialNumber;  //发送手机地址，发送后手机才能接收到服务器的数据
                                toServer.replace(" ", "");    //去掉空格
                                byte[] bt = null;
                                bt = Converts.HexString2Bytes(toServer);
                                String str = toServer + Converts.GetCRC(bt, 2, bt.length);   //添加校验码
                                byte[] bt1 = Converts.HexString2Bytes(str);      //将完整的命令转换成十六进制
                                if (!sixClient.isClosed())
                                {
                                    out.write(bt1);
                                    out.flush();
                                }
                                Thread.sleep(100);
                                toServer =  order;    //指令，添加包头和第六感官地址
                                toServer=toServer.replace(" ", "");    //去掉空格
                                bt = null;
                                bt = Converts.HexString2Bytes(toServer);
                                if(dev==SIXSENSOR){
                                    str= toServer+Converts.GetCRC(bt, dev, bt.length)+"0d0a";   //添加校验码和包尾
                                    LogUtil.i("service发送命令：="+str);
                                }
                                LogUtil.i("service发送命令：="+str);
                                bt1 = Converts.HexString2Bytes(str);      //将完整的命令转换成十六进制
                                if (!sixClient.isClosed()) {
                                    out.write(bt1);
                                    out.flush();
                                }
                            }
                            catch (Exception ee) {
                                Log.i("Info", "client重启出错" + ee.toString());
                            }

                        }
                    }
                }.start();
                return true;
            }
        };   //重写发送命令方法

        return binder;   //如果这边不返回一个IBinder的接口实例，那么ServiceConnection中的onServiceConnected就不会被调用
              //那么bind所具有的传递数据的功能也就体现不出来。返回值在onServiceConnected中的第二个参数

    }
    @Override   //只调用一次
    public void onCreate() {
        Log.v("Service", "ServiceDemo onCreate");

        SixSensorip = BaseApplication.getSharedPreferences().getString("sixIpAddress","null");
        sixPort = BaseApplication.getSharedPreferences().getInt("sixPort",-1);

        serverip = BaseApplication.getSharedPreferences().getString("chunkouIpAddress","null");
        port = BaseApplication.getSharedPreferences().getInt("chunkouPort",8000);

        if (serverip.equals("192.168.1.213")){
            IsInnerNet =  true;
        }else {
            IsInnerNet = false;
        }
        SerialNumber = "0001";
        Log.i("Internet", "网络状态：" + (IsNetWork() ? "可用" : "不可用"));   //判断网络状态,打开wifi显示可用，关闭wifi显示不可用。但是不确定能不能联网
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(myNetReceiver, mFilter);   //注册接收网络连接状态改变广播接收器
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        LogUtil.v("Service", "ServiceDemo onStart");
        super.onStart(intent, startId);
    }

    @Override   //会调用多次
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.v("Service", "ServiceDemo onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.v("Service", "ServiceDemo OnUnbind");
        return super.onUnbind(intent);

    }

    @Override
    public void onDestroy() {
        LogUtil.v("Service", "ServiceDemo OnDestroy");
        if(myNetReceiver!=null){
            unregisterReceiver(myNetReceiver);   //注销接收网络变化的广播通知的广播接收器
        }
        if(client!=null) {
            try {
                client.shutdownInput();
                client.shutdownOutput();
                client.close();     //关闭与服务器的tcp连接
                client = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if(sixClient!=null) {
            try {
                sixClient.shutdownInput();
                sixClient.shutdownOutput();
                sixClient.close();     //关闭与服务器的tcp连接
                sixClient = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onDestroy();   //调用父类onDestroy方法释放资源
    }



    public class TCPServerThread extends Thread   //监听服务器发回的信息
     {

        public void run()
        {
            byte[] buffer = new byte[1024];
            final StringBuilder sb = new StringBuilder();
            try {
                // 接收服务器信息       定义输入流
                InputStream in=client.getInputStream();
                DataInputStream ins = new DataInputStream(in);
                while (client!=null) {
                    //content=new byte[1024];
                    if (!client.isClosed()) {
                        if (client.isConnected()) {
                            if (!client.isInputShutdown()) {
                                byte[] content=new byte[1024];
                                int count=0;   //记录接收数据数组的长度
                                try {
                                    while((count=ins.read(content)) !=-1) {     //读取数据 ，存放到缓存区content中

//                                    Map<String,Object> map=new HashMap<String,Object>();   //新建map存放要传递给主线程的数据
//                                    map.put("data",content);    //客户端发回的数据
//                                    Message msg=new Message();
//                                    msg.what=count;   //数组有效数据长度
//                                    msg.obj=map;  //接收到的数据数组
//                                    handler1.sendMessage(msg);
                                        String s = "";                       //保存命令的十六进制字符串
                                        for (int i = 0; i < count; i++) {
                                            String s1 = Integer.toHexString((content[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                                            if (s1.length() == 1)
                                                s1 = "0" + s1;
                                            s = s + s1;
                                        }
                                        //   String crc=Converts.GetCRC(a, 2, msg.what-2-2);    //获取返回数据的校验码，倒数第3、4位是验证码，倒数第1、2位是包尾0d0a
                                        s = s.replace(" ", ""); //去掉空格
                                        String[] single_str=s.split("0d0a");   //防止多条命令重叠，对命令按照包尾0d0a进行分解，逐条广播
                                        for(String str:single_str)
                                        {
                                            //电表通讯协议中包尾是16，不是0d0a，此处加上0d0a不影响电表数据的解析，因为解析的时候没有计算校验
                                            byte[] tem=Converts.HexString2Bytes(str+"0d0a");   //转换成byte数组
                                            if(tem.length>8) {
                                                Intent intent = new Intent();
                                                intent.setAction("com.suntrans.beijing.RECEIVE");
                                                intent.putExtra("ContentNum", tem.length);   //数组长度
                                                intent.putExtra("Content", tem);   //命令内容数组
                                                sendBroadcast(intent);   //发送广播，通知各个activity
                                            }
                                            LogUtil.i("Order", "收到数据：" + str+"0d0a");
                                        }


                                    }
                                }catch (Exception e){

                                }

                            }
                        }
                    }
                }
                LogUtil.i("Info", "TCP接收监听退出");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //第六感接收进程，外网模式不应启动
    public class TCPServerThread1 extends Thread   //监听服务器发回的信息
    {

        public void run()
        {
            byte[] buffer = new byte[1024];
            final StringBuilder sb = new StringBuilder();
            try {
                // 接收服务器信息       定义输入流
                InputStream in=sixClient.getInputStream();
                DataInputStream ins = new DataInputStream(in);
                while (sixClient!=null) {
                    //content=new byte[1024];
                    if (!sixClient.isClosed()) {
                        if (sixClient.isConnected()) {
                            if (!sixClient.isInputShutdown()) {
                                byte[] content=new byte[1024];
                                int count=0;   //记录接收数据数组的长度
                                while((count=ins.read(content)) !=-1) {     //读取数据 ，存放到缓存区content中
//                                    Map<String,Object> map=new HashMap<String,Object>();   //新建map存放要传递给主线程的数据
//                                    map.put("data",content);    //客户端发回的数据
//                                    Message msg=new Message();
//                                    msg.what=count;   //数组有效数据长度
//                                    msg.obj=map;  //接收到的数据数组
//                                    handler1.sendMessage(msg);
                                    String s = "";                       //保存命令的十六进制字符串
                                    for (int i = 0; i < count; i++) {
                                        String s1 = Integer.toHexString((content[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                                        if (s1.length() == 1)
                                            s1 = "0" + s1;
                                        s = s + s1;
                                    }
                                    //   String crc=Converts.GetCRC(a, 2, msg.what-2-2);    //获取返回数据的校验码，倒数第3、4位是验证码，倒数第1、2位是包尾0d0a
                                    s = s.replace(" ", ""); //去掉空格
                                    String[] single_str=s.split("0d0a");   //防止多条命令重叠，对命令按照包尾0d0a进行分解，逐条广播
                                    for(String str:single_str)
                                    {
                                        //电表通讯协议中包尾是16，不是0d0a，此处加上0d0a不影响电表数据的解析，因为解析的时候没有计算校验
                                        byte[] tem=Converts.HexString2Bytes(str+"0d0a");   //转换成byte数组
                                        if(tem.length>=14) {
                                            Intent intent = new Intent();
                                            intent.setAction("com.suntrans.beijing.RECEIVE");
                                            intent.putExtra("ContentNum", tem.length);   //数组长度
                                            intent.putExtra("Content", tem);   //命令内容数组
                                            sendBroadcast(intent);   //发送广播，通知各个activity
                                        }
//                                        LogUtil.i("Order", "收到数据：" + str+"0d0a");
                                    }


                                }
                            }
                        }
                    }
                }
                LogUtil.i("Info", "TCP接收监听退出");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class ibinder extends Binder {
        /****
         * 发送命令
         * @param order    控制命令内容，从控制子地址开始，到校验码之前
         * @param dev 发送命令的设备号，第六感与开关不同，开关是2，第六感是4.，三相电表是3
         * @return  发送成功返回true，失败返回false
         */
        public boolean sendOrder(String order, int dev){
            return true;
        }

        /**
         * 发送命令到第六感服务器
         * @param order
         * @param dev
         * @return
         */
        public boolean sendOrder2Sixsenor(String order, int dev){
            return true;
        }
    }

    /////////////监听网络状态变化的广播接收器

    private BroadcastReceiver myNetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                netInfo = mConnectivityManager.getActiveNetworkInfo();
                if(netInfo != null && netInfo.isAvailable()) {

                    /////////////网络连接
                    String name = netInfo.getTypeName();

                    if(netInfo.getType()== ConnectivityManager.TYPE_WIFI){
                        LogUtil.i("Internet","网络改变==>网络变成了wifi");
                     //   Log.i("Internet", "状态：isClosed?" + String.valueOf(client.isClosed()) + "; isoutputshutdown?" + String.valueOf(client.isOutputShutdown()) +
                     //           "; isInputshutdown?" + String.valueOf(client.isInputShutdown()) + ";  有网？" + String.valueOf(ping()));
                        /////WiFi网络
                        if(client!=null) {
                            LogUtil.i("Internet", "isConnected==>" + String.valueOf(client.isConnected()));
                            LogUtil.i("Internet", "isoutputShutdown==>" + String.valueOf(client.isOutputShutdown()));
                            LogUtil.i("Internet", "isinputshutdowm==>" + String.valueOf(client.isInputShutdown()));
                        }
                    }else if(netInfo.getType()== ConnectivityManager.TYPE_ETHERNET) {
                        /////有线网络
                        LogUtil.i("Internet", "网络改变==>网络变成了有线");

                    }else if(netInfo.getType()== ConnectivityManager.TYPE_MOBILE) {
                        /////////3g网络
                        LogUtil.i("Internet", "网络改变==>网络变成了移动网");
                    }
                } else {
                    ////////网络断开
                        LogUtil.i("Internet","网络改变==>网络断开");
                    if(client!=null) {
                        LogUtil.i("Internet", "isConnected==>" + String.valueOf(client.isConnected()));
                        LogUtil.i("Internet", "isoutputShutdown==>" + String.valueOf(client.isOutputShutdown()));
                        LogUtil.i("Internet", "isinputshutdowm==>" + String.valueOf(client.isInputShutdown()));

                    } //   Log.i("Internet", "状态：isClosed?" + String.valueOf(client.isClosed()) + "; isoutputshutdown?" + String.valueOf(client.isOutputShutdown()) +
                       //     "; isInputshutdown?" + String.valueOf(client.isInputShutdown()) + ";  有网？" + String.valueOf(ping()));

                }
            }

        }
    };

    ///判断网络是否可用，如果连接到了网络就返回true（无论是否真正可以上网），如果没有连接，则返回false。表示无可用网络
    public boolean IsNetWork()
    {
        Context context = this.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    //System.out.println(i + "===状态===" + networkInfo[i].getState());
                    //System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /***判断是否真正可以上网。如果可以返回true
    * @author sichard
    * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
            * @return
            */
    public static boolean ping() {

        String result = null;
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            LogUtil.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            LogUtil.d("----result---", "result = " + result);
        }
        return false;
    }

}
