package com.suntrans.smartshow.activity;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.os.EnvironmentCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseApplication;
import com.suntrans.smartshow.service.MainService2;
import com.suntrans.smartshow.utils.AudioPlayUtil;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.ScreenOrientationHelper;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.utils.UiUtils;
import com.suntrans.smartshow.views.LoadingDialog;
import com.suntrans.smartshow.views.WaitDialog;
import com.videogo.constant.Constant;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.realplay.RealPlayStatus;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.R.attr.order;
import static com.tencent.bugly.crashreport.inner.InnerAPI.context;
import static com.videogo.realplay.RealPlayStatus.STATUS_PLAY;
import static com.videogo.smack.packet.RosterPacket.ItemType.to;


/**
 * Created by pc on 2016/9/15.
 * 智能家居开关控制页面
 */
public class SwitchControl_Activity extends AppCompatActivity implements SurfaceHolder.Callback,Handler.Callback, View.OnClickListener {
    private static final String TAG = "SwitchControl_Activity";
    private int isShowPlay = 0;//是否展示视频预览页面
    private Handler mHandler = new Handler(this);
    private Button stopPlay;//退出视频
    private EZPlayer mEZPlayer = null;
    private EZOpenSDK mEZOpenSDK = EZOpenSDK.getInstance();
    private SurfaceHolder mRealPlaySh = null;
    private SurfaceView mRealPlaySv = null;
    private EZCameraInfo mCameraInfo = null;
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;//屏幕当前方向
    private int mStatus = RealPlayStatus.STATUS_INIT;//播放视频的状态
    private boolean mIsOnStop = false;
    private EZDeviceInfo mDeviceInfo = null;
    private TextView loading;//加载中..页面
    private RelativeLayout relativeLayout;
    private String mRtspUrl = null;    // 视频广场URL
    private CustomTouchListener mRealPlayTouchListener = null;
    private Button mRealPlayQualityBtn = null;//清晰度button
    private CheckTextButton mFullScreenButton;//全屏按钮
    private ScreenOrientationHelper mScreenOrientationHelper;//设置全屏辅助工具
    private Button cutButton =null;
    /******************************分割线*************************/
    private String oldRessult = "";
    private LinearLayout root = null;
    private Toolbar toolbar;//标题栏
    private TextView tv_title;//标题
    private RecyclerView recyclerView;//
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
    private int area;//区域(客厅，厨房灯)
    boolean isrun = true;//是否刷新数据
    private Handler handler = new Handler();//handler
    private mAdapter adapter;//recycle适配器
    private LinearLayout layout = null;//控制部分的layout
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
        // 注册自定义动态广播消息。根据Action识别广播

        StatusBarCompat.compat(this, Color.TRANSPARENT);//设置状态栏为透明颜色
        setContentView(R.layout.smartroom_detail_activity);
        initData();
        //初始化控件
        initViews(savedInstanceState);
        //初始化ToolBar
        initToolBar();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mScreenOrientationHelper.postOnStart();
    }

    @Override
    protected void onStop() {
        isrun = false;
        mScreenOrientationHelper.postOnStop();
        stopRealPlay();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if ( mStatus== RealPlayStatus.STATUS_PLAY){
//            startRealPlay();
//        }
        try {
            isrun = true;
            new RefreshThread().start();
        } catch (Exception e) {

        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mRealPlaySv != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mRealPlaySv.getWindowToken(), 0);
                }
            }
        }, 200);

    }


    @Override
    protected void onDestroy() {
        mHandler = null;
        mScreenOrientationHelper = null;
        isrun =false;
        if (broadcastreceiver!=null)
            unregisterReceiver(broadcastreceiver);  //注销广播接收者
        unbindService(con);   //解除Service的绑定
        mScreenOrientationHelper = null;
        super.onDestroy();
    }

    public void initViews(Bundle savedInstanceState) {
        mLocalInfo = LocalInfo.getInstance();
        dialog = new LoadingDialog(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tv_title = (TextView) findViewById(R.id.tv_title);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        refreshLayout.setSize(SwipeRefreshLayout.LARGE);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter= new mAdapter();
        recyclerView.setAdapter(adapter);
        refreshLayout.setColorSchemeResources(R.color.white);
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.bg_action);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                new GetDataTask().execute();
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               new GetDataTask().execute();
            }
        });
        /*************************视频控制组件******************************/
        root = (LinearLayout) findViewById(R.id.ll_root);
        cutButton = (Button) findViewById(R.id.cut);
        mRealPlaySv = (SurfaceView) findViewById(R.id.surface_view);
        loading= (TextView) findViewById(R.id.loading);
        relativeLayout = (RelativeLayout) findViewById(R.id.realplay_loading_rl);
        mRealPlaySv.getHolder().addCallback(this);
        mRealPlayPlayRl = (RelativeLayout) findViewById(R.id.realplay_play_rl);
        mRealPlayPlayRl.setVisibility(View.GONE);
        mRealPlayQualityBtn = (Button) findViewById(R.id.realplay_quality_btn);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDialog.setCancelable(false);
        layout = (LinearLayout) findViewById(R.id.realplay_control_rl);
        layout.setVisibility(View.GONE);
        cutButton.setOnClickListener(this);
        mFullScreenButton = (CheckTextButton) findViewById(R.id.fullscreen_button);
        mScreenOrientationHelper = new ScreenOrientationHelper(SwitchControl_Activity.this, mFullScreenButton,toolbar);

        mRealPlayTouchListener = new CustomTouchListener() {
            @Override
            public boolean canZoom(float scale) {
                if (mStatus == STATUS_PLAY) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean canDrag(int i) {
                return false;
            }

            @Override
            public void onSingleClick() {

            }

            @Override
            public void onDoubleClick(MotionEvent motionEvent) {

            }

            @Override
            public void onZoom(float scale) {
                LogUtil.e(TAG, "onZoom:" + scale);
                if (mEZPlayer != null && mDeviceInfo.isSupportZoom()) {
                    startZoom(scale);
                }
            }

            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.e(oRect.getLeft()+" "+oRect.getRight()+" "+ oRect.getTop()+" "+oRect.getBottom());
                LogUtil.e(curRect.getLeft()+" "+curRect.getRight()+" "+ curRect.getTop()+" "+curRect.getBottom());
                if (mEZPlayer != null && mDeviceInfo.isSupportZoom()) {
                    //采用云台调焦
                    return;
                }
                if (mStatus == STATUS_PLAY) {
                    if (scale > 1.0f && scale < 1.1f) {
                        scale = 1.1f;
                    }
                    setPlayScaleUI(scale, oRect, curRect);
                }
            }

            @Override
            public void onDrag(int i, float v, float v1) {

            }

            @Override
            public void onEnd(int mode) {
               LogUtil.e(TAG, "onEnd:" + mode);
                if (mEZPlayer != null) {
//                    stopDrag(false);
                }
                if (mEZPlayer != null && mDeviceInfo.isSupportZoom()) {
                    LogUtil.e(TAG, "isSupportZoom:" + mDeviceInfo.isSupportZoom());

                    stopZoom();
                }
            }
        };

        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (mLocalInfo.getScreenHeight() - mLocalInfo
                .getNavigationBarHeight()) : mLocalInfo.getScreenHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);
        mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
        mRealPlaySv.setOnTouchListener(mRealPlayTouchListener);
        setRealPlaySvLayout();
        /*************************视频控制组件******************************/

    }
//初始化数据
    public void initData() {
        Intent intent = getIntent();
        area = intent.getIntExtra("area", 0);
//        if (mEZOpenSDK.getEZAccessToken()!=null)
        // 获取本地信息
        Application application = (Application) getApplication();
        mAudioPlayUtil = AudioPlayUtil.getInstance(application);
        // 获取配置信息操作对象
        mLocalInfo = LocalInfo.getInstance();
        // 获取屏幕参数
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mLocalInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        mLocalInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));


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
        state1= new ArrayList<Map<String, String>>();
        state2= new ArrayList<Map<String, String>>();
        state3= new ArrayList<Map<String, String>>();
        //默认初始化全部开关为关闭
        for (int i=0;i<10;i++){
            Map<String,String> map = new HashMap<String,String>();
            map.put("state","0");
            map.put("position", String.valueOf(i));
            state1.add(map);
            state2.add(map);
            state3.add(map);
        }
    }
//刷新数据进程
    class RefreshThread extends Thread{
        @Override
        public void run() {
//            while (isrun){
                if (binder!=null){
                    getSwitchStateFromServer();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//            }
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
                getSwitchStateFromServer();
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
            refreshLayout.setRefreshing(false);   //结束加载动作
            super.onPostExecute(result);//这句是必有的，AsyncTask规定的格式
        }
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
            if (viewType==0){
                RecyclerView.ViewHolder holder = new SwitchControl_Activity.mAdapter.viewHolder1(LayoutInflater.from(
                        SwitchControl_Activity.this).inflate(R.layout.road_bulb_item, parent, false));
                return holder;
            }else {
                RecyclerView.ViewHolder holder = new SwitchControl_Activity.mAdapter.viewHolder2(LayoutInflater.from(
                        SwitchControl_Activity.this).inflate(R.layout.control_play, parent, false));
                return holder;
            }

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
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (position==data.size()){
                ((viewHolder2) holder).open_play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /****判断是否验证账号,超时重先登录****/
                        String timeout = BaseApplication.getSharedPreferences().getString("timeout","0");
                        String fristTime = BaseApplication.getSharedPreferences().getString("fristTime","0");
                        long value= System.currentTimeMillis()-Long.valueOf(fristTime) ;
                        if (value>(Long.valueOf(timeout)*1000)){
                            /****判断是否验证账号,超时重先登录****/
                            AlertDialog.Builder builder = new AlertDialog.Builder(SwitchControl_Activity.this);
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mEZOpenSDK.openLoginPage();
                                }
                            });
                            builder.setTitle("警告");
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.setMessage("第一次使用实景预览功能请先登录！");
                            builder.create().show();
                            return;
                        }
                        AlphaAnimation animation = new AlphaAnimation(0.0f,1.0f);
                        animation.setDuration(500);
                        if (isShowPlay==0){
//                            if (null!=mCameraInfo){
                                new GetCamersInfoListTask(true).execute();
//                            }
                            ((viewHolder2) holder).open_play.setBackgroundResource(R.drawable.pause);
                            mRealPlayPlayRl.setVisibility(View.VISIBLE);
                            layout.setVisibility(View.VISIBLE);
                            isShowPlay =1;
                            mRealPlayPlayRl.startAnimation(animation);
                        } else{
                            mRealPlayPlayRl.setVisibility(View.GONE);
                            ((viewHolder2) holder).open_play.setBackgroundResource(R.drawable.play_2);
                            layout.setVisibility(View.GONE);
                            stopRealPlay();
                            isShowPlay =0;
                        }

                    }
                });
            }else {
                String name = data.get(position).get("name");
                int id = Integer.valueOf(data.get(position).get("Image"));
                int idDot = Integer.valueOf(data.get(position).get("dot"));
                Bitmap bitmap = BitmapFactory.decodeResource(SwitchControl_Activity.this.getResources(), id);
                Bitmap bitmapDot = BitmapFactory.decodeResource(SwitchControl_Activity.this.getResources(), idDot);

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


        }



        @Override
        public int getItemCount() {
            return data.size()+1;
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
            if(position==data.size()){
             return 1;
            }else {
                return 0;
            }
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

        class viewHolder2 extends RecyclerView.ViewHolder {
            Button open_play;
            public viewHolder2(View view) {
                super(view);
                open_play = (Button) view.findViewById(R.id.open_play);
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
     * 获取不同房间的开关状态
     */
    private void getSwitchStateFromServer() {
        if (binder==null){
            return;
        }
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing())
                        refreshLayout.setRefreshing(false);
                    }
                });
                return;
            }
            s = Converts.Bytes2HexString(bytes);
            s = s.split("0d0a")[0] + "0d0a";
            s=s.toLowerCase();
            if (MainService2.isInnerNet1){
                if (!s.substring(0,8).equals("aa690001"))
                    return;
            }else {
                if (!s.substring(0,12).equals("ab68aa690001"))
                    return;
                s=s.substring(4,s.length());
            }
//            if (oldRessult.equals(s)){
//                return;
//            }else {
//                oldRessult = s;
//            }
            try {
                if (s.length() > 20) {
                    return_addr = s.substring(4, 12);   //返回数据的开关地址
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
                                if (return_addr.equals("00010001")) {
                                    state1.get(i).put("state", states[i]);
                                } else if (return_addr.equals("00010002")) {
                                    state2.get(i).put("state", states[i]);
                                } else if (return_addr.equals("00010003")) {
                                    state3.get(i).put("state", states[i]);
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
                                    if (return_addr.equals("00010001")) {
                                        state1.get(i).put("state", "0");
                                    } else if (return_addr.equals("00010002")) {
                                        state2.get(i).put("state", "0");
                                    } else if (TextUtils.equals(return_addr, road_addr3)) {
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
                                    if (return_addr.equals("00010001")) {
                                        state1.get(i).put("state", state == 1 ? "1" : "0");
//                                    System.out.println("当前通道"+state1.get(i).get("state"));
                                    } else if (return_addr.equals("00010002")) {
                                        state2.get(i).put("state", state == 1 ? "1" : "0");
//                                    LogUtil.i("当前通道"+state1.get(i).get("state"));
                                    } else if (TextUtils.equals(return_addr, road_addr3)) {
                                        state3.get(i).put("state", state == 1 ? "1" : "0");
                                    }
                                }
                            }
                        }
                        showSuccessDialog();
                    }
                    if (area == 0) {
                        //更新保存的状态
                        data.get(0).put("state", state1.get(1).get("state"));
                        data.get(1).put("state", state1.get(5).get("state"));
                        data.get(2).put("state", state1.get(6).get("state"));
                        data.get(3).put("state", state1.get(8).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_chazuo_off : R.drawable.ic_chazuo_on));
                        data.get(1).put("Image", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_tv1_off : R.drawable.ic_tv1_on));
                        data.get(2).put("Image", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_bulb_off : R.drawable.ic_bulb_on));
                        data.get(3).put("Image", String.valueOf((data.get(3).get("state").equals("0")) ? R.drawable.ic_wall_off : R.drawable.ic_wall_on));

                        data.get(0).put("dot", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(1).put("dot", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(2).put("dot", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(3).put("dot", String.valueOf((data.get(3).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));

                    } else if (area == 1) {
                        data.get(0).put("state", state2.get(1).get("state"));
                        data.get(1).put("state", state2.get(2).get("state"));
                        data.get(2).put("state", state2.get(7).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_wall_off : R.drawable.ic_wall_on));
                        data.get(1).put("Image", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_binxiang_off : R.drawable.ic_bingxiang_on));
                        data.get(2).put("Image", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_chazuo_off : R.drawable.ic_chazuo_on));

                        data.get(0).put("dot", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(1).put("dot", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(2).put("dot", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                    } else if (area == 2) {
                        data.get(0).put("state", state2.get(0).get("state"));
                        data.get(1).put("state", state2.get(3).get("state"));
                        data.get(2).put("state", state2.get(4).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_smoke_off : R.drawable.ic_smoke_on));
                        data.get(1).put("Image", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_weibolu_off : R.drawable.ic_wobolu_on));
                        data.get(2).put("Image", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_bulb_off : R.drawable.ic_bulb_on));

                        data.get(0).put("dot", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(1).put("dot", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(2).put("dot", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                    } else if (area == 3) {
                        data.get(0).put("state", state3.get(5).get("state"));
                        data.get(1).put("state", state3.get(6).get("state"));
                        data.get(2).put("state", state3.get(8).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_bulb_off : R.drawable.ic_bulb_on));
                        data.get(1).put("Image", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_kongtiao_off : R.drawable.ic_kongtiao_on));
                        data.get(2).put("Image", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_computer_off : R.drawable.ic_computer_on));

                        data.get(0).put("dot", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(1).put("dot", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(2).put("dot", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                    } else if (area == 4) {
                        //更新状态
                        data.get(0).put("state", state2.get(5).get("state"));
                        data.get(1).put("state", state2.get(6).get("state"));
                        data.get(2).put("state", state2.get(8).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_bulb_off : R.drawable.ic_bulb_on));
                        data.get(1).put("Image", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_hotwater_off : R.drawable.ic_hotwater_on));
                        data.get(2).put("Image", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_chazuo_off : R.drawable.ic_chazuo_on));

                        data.get(0).put("dot", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(1).put("dot", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(2).put("dot", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                    } else if (area == 5) {
                        //更新状态
                        data.get(0).put("state", state3.get(0).get("state"));
                        data.get(1).put("state", state3.get(1).get("state"));
                        data.get(2).put("state", state3.get(2).get("state"));
                        data.get(3).put("state", state3.get(3).get("state"));
                        data.get(4).put("state", state3.get(7).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_wall_off : R.drawable.ic_wall_on));
                        data.get(1).put("Image", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_bulb_off : R.drawable.ic_bulb_on));
                        data.get(2).put("Image", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_bulb_off : R.drawable.ic_bulb_on));
                        data.get(3).put("Image", String.valueOf((data.get(3).get("state").equals("0")) ? R.drawable.ic_kongtiao_off : R.drawable.ic_kongtiao_on));
                        data.get(4).put("Image", String.valueOf((data.get(4).get("state").equals("0")) ? R.drawable.ic_chazuo_off : R.drawable.ic_chazuo_on));

                        data.get(0).put("dot", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(1).put("dot", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(2).put("dot", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(3).put("dot", String.valueOf((data.get(3).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(4).put("dot", String.valueOf((data.get(4).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));

                    } else if (area == 6) {//侧卧室
                        //更新状态
                        data.get(0).put("state", state1.get(0).get("state"));
                        data.get(1).put("state", state1.get(2).get("state"));
                        data.get(2).put("state", state1.get(3).get("state"));
                        data.get(3).put("state", state1.get(4).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_chazuo_off : R.drawable.ic_chazuo_on));
                        data.get(1).put("Image", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_kongtiao_off : R.drawable.ic_kongtiao_on));
                        data.get(2).put("Image", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_bulb_off : R.drawable.ic_bulb_on));
                        data.get(3).put("Image", String.valueOf((data.get(3).get("state").equals("0")) ? R.drawable.ic_wall_off : R.drawable.ic_wall_on));

                        data.get(0).put("dot", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(1).put("dot", String.valueOf((data.get(1).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(2).put("dot", String.valueOf((data.get(2).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                        data.get(3).put("dot", String.valueOf((data.get(3).get("state").equals("0")) ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                    } else if (area == 7) {
                        data.get(0).put("state", state3.get(4).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_bulb_off : R.drawable.ic_bulb_on));

                        data.get(0).put("dot", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                    } else if (area == 8) {
                        data.get(0).put("state", state1.get(7).get("state"));

                        //更新保存的应显示的图片
                        data.get(0).put("Image", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_xiyiji_off : R.drawable.ic_xiyiji_on));

                        data.get(0).put("dot", String.valueOf(TextUtils.equals(data.get(0).get("state"), "0") ? R.drawable.ic_dot_off : R.drawable.ic_dot_on));
                    }
                    if (adapter != null) {
                        UiUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (refreshLayout.isRefreshing())
                                    refreshLayout.setRefreshing(false);
//                            UiUtils.showToast(UiUtils.getContext(), "success！");
                                adapter.notifyDataSetChanged();
                            }
                        });

                    }else {
                        adapter = new mAdapter();
                        recyclerView.setAdapter(adapter);
                    }

                }
            }catch (Exception e){
                return;
            }
        }

    } ;//广播接收器


    private void parseClick(View v, int position) {
        if (binder==null){
            return;
        }
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
        if (dialog.isShowing()){
            dialog.setTipTextView("成功");
        }
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
                    if (dialog.isShowing())
                  dialog.dismiss();
                    which=100;
                }
            }
        }, 2000);
    }

    /*****************************华丽的分割线****************************************/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(null);
        }
        mRealPlaySh = null;
    }
    @SuppressLint("NewApi")
    @Override
    public boolean handleMessage(Message msg) {
        if (this.isFinishing()) {
            return false;
        }
        switch (msg.what) {
            case EZConstants.EZRealPlayConstants.MSG_GET_CAMERA_INFO_SUCCESS:
                updateLoadingProgress(20);
                handleGetCameraInfoSuccess();
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_START:
                updateLoadingProgress(40);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_CONNECTION_START:
                updateLoadingProgress(60);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_CONNECTION_SUCCESS:
                updateLoadingProgress(80);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                handlePlaySuccess(msg);
            case EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS:
                LogUtil.e("设置清晰度成功");
                handleSetVedioModeSuccess();
                break;
            case EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL:
                handleSetVedioModeFail(msg.arg1);
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                handlePlayFail(msg.arg1, msg.arg2);
                break;
        }
        return false;
    }

    private void handlePlayFail(int errorCode, int retryCount) {
        stopRealPlay();
        loading.setVisibility(View.VISIBLE);
        loading.setText("播放失败"+errorCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cut:
                if (mStatus==RealPlayStatus.STATUS_PLAY){
                    LogUtil.e("开始截屏");
                    captureImage();
                }
                break;
            case R.id.realplay_quality_btn:
                if (mStatus==RealPlayStatus.STATUS_PLAY)
                openQualityPopupWindow(mRealPlayQualityBtn);
                break;
//            case R.id.fullscreen_button:
//                setFullScreen();
//                break;
        }
    }

    private void captureImage() {
        new Thread(){
            @Override
            public void run() {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            UiUtils.showToast(SwitchControl_Activity.this,"SD卡不可用");
                        }
                    });
                    return;
                }
                Bitmap bmp = mEZPlayer.capturePicture();
                FileOutputStream fos = null;
                if(bmp != null) {
                    try {
                        File appDir = new File(Environment.getExternalStorageDirectory().getPath()
                                ,"smartshowPic");
                        if (!appDir.exists()){
                            appDir.mkdirs();
                        }
                        File file = new File(appDir.getAbsolutePath(),System.currentTimeMillis()+".png");
                        fos = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        bmp.recycle();
                        bmp = null;
                        Uri uri = Uri.fromFile(file);
                        Intent intent  = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                        SwitchControl_Activity.this.sendBroadcast(intent);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                UiUtils.showToast(SwitchControl_Activity.this,"图片已保存至相册");
                            }
                        });

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if(fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }.start();
    }


    private void openQualityPopupWindow(View anchor) {
        if (mEZPlayer == null) {
            return;
        }
        closeQualityPopupWindow();
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_quality_items, null, true);

        Button qualityHdBtn = (Button) layoutView.findViewById(R.id.quality_hd_btn);
        qualityHdBtn.setOnClickListener(mOnPopWndClickListener);
        Button qualityBalancedBtn = (Button) layoutView.findViewById(R.id.quality_balanced_btn);
        qualityBalancedBtn.setOnClickListener(mOnPopWndClickListener);
        Button qualityFlunetBtn = (Button) layoutView.findViewById(R.id.quality_flunet_btn);
        qualityFlunetBtn.setOnClickListener(mOnPopWndClickListener);

        // 视频质量，2-高清，1-标清，0-流畅
        if (mCameraInfo.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
            qualityFlunetBtn.setEnabled(false);
        } else if (mCameraInfo.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
            qualityBalancedBtn.setEnabled(false);
        } else if (mCameraInfo.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
            qualityHdBtn.setEnabled(false);
        }

        int height = 105;

        qualityFlunetBtn.setVisibility(View.VISIBLE);
        qualityBalancedBtn.setVisibility(View.VISIBLE);
        qualityHdBtn.setVisibility(View.VISIBLE);

        height = Utils.dip2px(this, height);
        mQualityPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.WRAP_CONTENT, height, true);
        mQualityPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mQualityPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                com.videogo.util.LogUtil.infoLog(TAG, "KEYCODE_BACK DOWN");
                mQualityPopupWindow = null;
                closeQualityPopupWindow();
            }
        });
        try {
            mQualityPopupWindow.showAsDropDown(anchor, -Utils.dip2px(this, 5),
                    -(height + anchor.getHeight() + Utils.dip2px(this, 8)));
        } catch (Exception e) {
            e.printStackTrace();
            closeQualityPopupWindow();
        }
    }

    private View.OnClickListener mOnPopWndClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.quality_hd_btn:
                    setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_HD);
                    break;
                case R.id.quality_balanced_btn:
                    setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED);
                    break;
                case R.id.quality_flunet_btn:
                    setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET);
                    break;
//                case R.id.ptz_close_btn:
//                    closePtzPopupWindow();
//                    break;
//                case R.id.ptz_flip_btn:
//                    //                    setPtzFlip();
//                    break;
//                case R.id.talkback_close_btn:
//                    closeTalkPopupWindow(true, false);
//                    break;
                default:
                    break;
            }
        }
    };

    private void closeQualityPopupWindow() {
        if (mQualityPopupWindow != null) {
            dismissPopWindow(mQualityPopupWindow);
            mQualityPopupWindow = null;
        }
    }

    private void dismissPopWindow(PopupWindow popupWindow) {
        if (popupWindow != null && !isFinishing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }
    /**
     * 开始播放
     *
     * @see
     * @since V2.0
     */
    private void startRealPlay() {
//        startPlay.setVisibility(View.INVISIBLE);
//        stopPlay.setVisibility(View.VISIBLE);
        // 增加手机客户端操作信息记录
        com.videogo.util.LogUtil.debugLog(TAG, "startRealPlay");
        loading.setVisibility(View.VISIBLE);
        if (mStatus == RealPlayStatus.STATUS_START || mStatus == STATUS_PLAY) {
            return;
        }

        // 检查网络是否可用
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络
//            setRealPlayFailUI(getString(R.string.realplay_play_fail_becauseof_network));
            UiUtils.showToast(SwitchControl_Activity.this,"当前网络不可用");
            return;
        }

        mStatus = RealPlayStatus.STATUS_START;
        setRealPlayLoadingUI();

        if (mCameraInfo != null) {
            final String cameraId = Utils.getCameraId(mCameraInfo.getCameraId());
//            System.out.println("sssssssssssssssss="+cameraId);
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    mEZPlayer = mEZOpenSDK.createPlayer(SwitchControl_Activity.this, cameraId);

                    if (mEZPlayer == null)
                        return;
                    if (mDeviceInfo == null) {
                        try {
                            mDeviceInfo = mEZOpenSDK.getDeviceInfoBySerial(mCameraInfo.getDeviceSerial());
                        } catch (BaseException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
//                    if (mEZPlayer == null)
//                        return;
                    mEZPlayer.setHandler(mHandler);
                    mEZPlayer.setSurfaceHold(mRealPlaySh);
                    mEZPlayer.startRealPlay();
                }
            };
            Thread thr = new Thread(run);
            thr.start();

        } else if (mRtspUrl != null) {
            // TODO 最好也改成跟上面一样，异步播放，避免ANR
            mEZPlayer = mEZOpenSDK.createPlayerWithUrl(this, mRtspUrl);
            //mStub.setCameraId(mCameraInfo.getCameraId());////****  mj
            if (mEZPlayer == null)
                return;
            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);
            mEZPlayer.startRealPlay();
        }
        updateLoadingProgress(0);
    }
    /**
     * 停止播放
     *
     * @see
     * @since V1.0
     */
    private void stopRealPlay() {
        com.videogo.util.LogUtil.debugLog(TAG, "stopRealPlay");
        mStatus = RealPlayStatus.STATUS_STOP;
//        stopUpdateTimer();
        if (mEZPlayer != null) {
//            stopRealPlayRecord();
            mEZPlayer.stopRealPlay();
            mEZOpenSDK.releasePlayer(mEZPlayer);
        }

    }

    /**
     * 获取设备信息成功
     *
     * @see
     * @since V1.0
     */
    private void handleGetCameraInfoSuccess() {
        com.videogo.util.LogUtil.infoLog(TAG, "handleGetCameraInfoSuccess");
        //通过能力级设置
//        updateUI();

    }
    //更新加载进度
    private void updateLoadingProgress(int i) {
        Random random = new Random();
        int s = random.nextInt(20);
        loading.setText("加载中.."+(i+s)+ "%");
    }

    private void setRealPlayLoadingUI() {
        loading.setText("loading..0%");
        loading.setVisibility(View.VISIBLE);
//        relativeLayout.setVisibility(View.VISIBLE);
    }

    private void handlePlaySuccess(Message msg) {
        mRealRatio = Constant.LIVE_VIEW_RATIO;
        mStatus = STATUS_PLAY;
        relativeLayout.setVisibility(View.INVISIBLE);
        setRealPlaySvLayout();
        setRealPlaySuccessUI();
//        setRealPlaySvLayout();
    }


    /**
     * 获取获取设备任务
     */
    private class GetCamersInfoListTask extends AsyncTask<Void, Void, List<EZCameraInfo>> {
        private boolean mHeaderOrFooter;
        private int mErrorCode = 0;

        public GetCamersInfoListTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mListView.setFooterRefreshEnabled(true);
        }

        @Override
        protected List<EZCameraInfo> doInBackground(Void... params) {

            if(SwitchControl_Activity.this.isFinishing()) {
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(SwitchControl_Activity.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }

            try {
                List<EZCameraInfo> result = null;
                if(mHeaderOrFooter) {

                    result = mEZOpenSDK.getCameraList(0, 20);
                } else {
//                    result = mEZOpenSDK.getCameraList(mAdapter.getCount()/20, 20);
                }

                return result;

            } catch (BaseException e) {
                mErrorCode = e.getErrorCode();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<EZCameraInfo> result) {
            super.onPostExecute(result);
            if (mErrorCode!=0){
                return;
            }
            for (EZCameraInfo info:result) {
                System.out.println("照相机信息:"+info.getCameraId()+" "
                        +"url="+info.getPicUrl());
                mCameraInfo = info;
            }
            startRealPlay();
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
//                    mEZOpenSDK.openLoginPage();
                    break;
                default:

                    break;
            }
        }
    }


    private void startZoom(float scale) {
        if (mEZPlayer == null) {
            return;
        }

//        hideControlRlAndFullOperateBar(false);
        boolean preZoomIn = mZoomScale > 1.01 ? true : false;
        boolean zoomIn = scale > 1.01 ? true : false;
        if (mZoomScale != 0 && preZoomIn != zoomIn) {
            com.videogo.util.LogUtil.debugLog(TAG, "startZoom stop:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_STOP);
            mZoomScale = 0;
        }
        if (scale != 0 && (mZoomScale == 0 || preZoomIn != zoomIn)) {
            mZoomScale = scale;
            com.videogo.util.LogUtil.debugLog(TAG, "startZoom start:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_START);
        }
    }

    private void stopZoom() {
        if (mEZPlayer == null) {
            return;
        }
        if (mZoomScale != 0) {
            com.videogo.util.LogUtil.debugLog(TAG, "stopZoom stop:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_STOP);
            mZoomScale = 0;
        }
    }

    private void setPlayScaleUI(float scale, CustomRect oRect, CustomRect curRect) {
        if (scale == 1) {
            if (mPlayScale == scale) {
                return;
            }
//            mRealPlayRatioTv.setVisibility(View.GONE);
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(false, null, null);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            if (mPlayScale == scale) {
                try {
                    if (mEZPlayer != null) {
                        mEZPlayer.setDisplayRegion(true, oRect, curRect);
                    }
                } catch (BaseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            }
//            RelativeLayout.LayoutParams realPlayRatioTvLp = (RelativeLayout.LayoutParams) mRealPlayRatioTv
//                    .getLayoutParams();
//            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
//                realPlayRatioTvLp.setMargins(SuntransUtils.dip2px(this, 10), SuntransUtils.dip2px(this, 10), 0, 0);
//            } else {
//                realPlayRatioTvLp.setMargins(SuntransUtils.dip2px(this, 70), SuntransUtils.dip2px(this, 20), 0, 0);
//            }
//            mRealPlayRatioTv.setLayoutParams(realPlayRatioTvLp);
            String sacleStr = String.valueOf(scale);
//            mRealPlayRatioTv.setText(sacleStr.subSequence(0, Math.min(3, sacleStr.length())) + "X");
            //mj mRealPlayRatioTv.setVisibility(View.VISIBLE);
//            mRealPlayRatioTv.setVisibility(View.GONE);
//            hideControlRlAndFullOperateBar(false);
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(true, oRect, curRect);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mPlayScale = scale;
    }
    /**
     * 码流配置 清晰度 2-高清，1-标清，0-流畅
     *
     * @see
     * @since V2.0
     */
    private void setQualityMode(final EZConstants.EZVideoLevel mode) {
        // 检查网络是否可用
        if (!ConnectionDetector.isNetworkAvailable(SwitchControl_Activity.this)) {
            // 提示没有连接网络
            Utils.showToast(SwitchControl_Activity.this, R.string.realplay_set_fail_network);
            return;
        }

        if (mEZPlayer != null) {
            mWaitDialog.setWaitText(this.getString(R.string.setting_video_level));
            mWaitDialog.show();

            Thread thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mEZPlayer.setVideoLevel(mode);
                        mCurrentQulityMode = mode;
                        Message msg = Message.obtain();
                        msg.what = MSG_SET_VEDIOMODE_SUCCESS;
                        mHandler.sendMessage(msg);
                        com.videogo.util.LogUtil.i(TAG, "setQualityMode success");
                        mCameraInfo.setVideoLevel(mode.getVideoLevel());
                        flag=1;
                    } catch (BaseException e) {
                        mCurrentQulityMode = EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET;
                        e.printStackTrace();
                        Message msg = Message.obtain();
                        msg.what = MSG_SET_VEDIOMODE_FAIL;
                        mHandler.sendMessage(msg);
                        com.videogo.util.LogUtil.i(TAG, "setQualityMode fail");
                    }

                }
            });
            thr.start();
        }
    }
    private int flag = 0;//由于EZopenSdk每次播放都会发送一个成功设置视频质量的消息，故设置一个标志位是否重新启动
    private void handleSetVedioModeSuccess() {
        closeQualityPopupWindow();
        setVideoLevel();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (flag==0)
            return;
        if (mStatus == STATUS_PLAY) {
            // 停止播放
            stopRealPlay();
            //下面语句防止stopRealPlay线程还没释放surface, startRealPlay线程已经开始使用surface
            relativeLayout.setVisibility(View.VISIBLE);
            //因此需要等待500ms
            SystemClock.sleep(500);
            // 开始播放
            startRealPlay();
        }
        flag=0;
    }
    //更新画质ui
    private void setVideoLevel() {
        if (mCameraInfo == null || mEZPlayer == null) {
            return;
        }

        if (mCameraInfo.getOnlineStatus() == 1) {
            mRealPlayQualityBtn.setEnabled(true);
        } else {
            mRealPlayQualityBtn.setEnabled(false);
        }
        // 视频质量，2-高清，1-标清，0-流畅
        if (mCurrentQulityMode.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_flunet);
        } else if (mCurrentQulityMode.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_balanced);
        } else if (mCurrentQulityMode.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_hd);
        }
    }

    private void handleSetVedioModeFail(int errorCode) {
        closeQualityPopupWindow();
        setVideoLevel();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        UiUtils.showToast(SwitchControl_Activity.this,"失败");
    }
    // 云台控制状态
    private float mZoomScale = 0;
    // 播放比例
    private float mPlayScale = 1;
    private LocalInfo mLocalInfo = null;
    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    private RelativeLayout mRealPlayPlayRl = null;
    private boolean isAccess = false;
    private PopupWindow mQualityPopupWindow = null;
    private EZConstants.EZVideoLevel mCurrentQulityMode = EZConstants.EZVideoLevel.VIDEO_LEVEL_HD;
    private WaitDialog mWaitDialog = null;
    // UI消息
    public static final int MSG_PLAY_UI_UPDATE = 200;
    public static final int MSG_AUTO_START_PLAY = 202;
    public static final int MSG_CLOSE_PTZ_PROMPT = 203;
    public static final int MSG_HIDE_PTZ_DIRECTION = 204;
    public static final int MSG_HIDE_PAGE_ANIM = 205;
    public static final int MSG_PLAY_UI_REFRESH = 206;
    public static final int MSG_PREVIEW_START_PLAY = 207;
    public static final int MSG_SET_VEDIOMODE_SUCCESS = 105;
    public static final int MSG_SET_VEDIOMODE_FAIL = 106;
    private AudioPlayUtil mAudioPlayUtil = null;

    /******************************以下为切换横屏放大主要代码******************/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {//屏幕方向变化调用此代码
        mOrientation = newConfig.orientation;
        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    private void onOrientationChanged() {
        mRealPlaySv.setVisibility(View.INVISIBLE);
        setRealPlaySvLayout();
        mRealPlaySv.setVisibility(View.VISIBLE);
        LogUtil.i("方向改变");
        updateOrientation();
        updateToolBar();

    }

    private void updateToolBar() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            toolbar.setVisibility(View.VISIBLE);
            root.setFitsSystemWindows(true);
        } else {
            root.setFitsSystemWindows(false);
            toolbar.setVisibility(View.GONE);
        }
    }

    private void setRealPlaySvLayout() {
        // 设置播放窗口位置
        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (mLocalInfo.getScreenHeight() - mLocalInfo
                .getNavigationBarHeight()) : mLocalInfo.getScreenHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);

        RelativeLayout.LayoutParams loadingR1Lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                realPlaySvlp.height);
        //        loadingR1Lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        //        mRealPlayLoadingRl.setLayoutParams(loadingR1Lp);
        //        mRealPlayPromptRl.setLayoutParams(loadingR1Lp);
        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        //mj svLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRealPlaySv.setLayoutParams(svLp);

        if (mRtspUrl == null) {
            //            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
            //                    LayoutParams.WRAP_CONTENT);
            //            realPlayPlayRlLp.gravity = Gravity.CENTER;
            //            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        } else {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            //realPlayPlayRlLp.weight = 1;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        }
        mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
        setPlayScaleUI(1, null, null);
    }

    private int mForceOrientation = 0;

    private void setOrientation(int sensor) {//设置方向
        if (mForceOrientation != 0) {
            com.videogo.util.LogUtil.debugLog(TAG, "setOrientation mForceOrientation:" + mForceOrientation);
            return;
        }

        if (sensor == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            mScreenOrientationHelper.enableSensorOrientation();
        else
            mScreenOrientationHelper.disableSensorOrientation();
    }

    boolean mIsOnTalk = false;
    private void updateOrientation() {
        if (mIsOnTalk) {
            if (mEZPlayer != null && mDeviceInfo.getSupportTalkValue() == EZConstants.EZRealPlayConstants.TALK_FULL_DUPLEX) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                setForceOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (mStatus == RealPlayStatus.STATUS_PLAY) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }
    }
    public void setForceOrientation(int orientation) {
        if (mForceOrientation == orientation) {
            com.videogo.util.LogUtil.debugLog(TAG, "setForceOrientation no change");
            return;
        }
        mForceOrientation = orientation;
        if (mForceOrientation != 0) {
            if (mForceOrientation != mOrientation) {
                if (mForceOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    mScreenOrientationHelper.portrait();
                } else {
                    mScreenOrientationHelper.landscape();
                }
            }
            mScreenOrientationHelper.disableSensorOrientation();
        } else {
            updateOrientation();
        }
    }

    /******************************以上为切换横屏放大主要代码*************************/

    private void setRealPlaySuccessUI() {//成功播放设置ui
        setVideoLevel();
        updateOrientation();
        if (mCameraInfo != null) {

            if (mCameraInfo.getOnlineStatus() == 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }

        }
    }


    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if (mOrientation ==Configuration.ORIENTATION_PORTRAIT )
                finish();
            else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                toolbar.setVisibility(View.VISIBLE);
                mFullScreenButton.setChecked(false);
            }
        }
        return super.onKeyDown(keyCode,event);
    }
}

