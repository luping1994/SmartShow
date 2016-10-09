package com.suntrans.smartshow.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.FlashLightAdapter;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.base.BaseActivity1;
import com.suntrans.smartshow.bean.FlashlightInfo;
import com.suntrans.smartshow.fragment.PowerInfoFragment;
import com.suntrans.smartshow.fragment.RoomConditionFragment;
import com.suntrans.smartshow.fragment.SmartControlFragment;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.utils.UiUtils;
import com.suntrans.smartshow.views.BottomMentTab;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.data;
import static com.suntrans.smartshow.R.id.recyclerView;

/**
 * Created by Looney on 2016/9/26.
 */
public class Smartroom_Activity extends AppCompatActivity {

    private TextView textView;//标题
    private Toolbar toolbar;//标题栏
    private ViewPager pager;
    private TabLayout tabLayout;
    public   MainService1.ibinder binder;  //用于Activity与Service通信

    private ServiceConnection con = new ServiceConnection() {
        //绑定服务成功后，调用此方法，获取返回的IBinder对象，可以用来调用Service中的方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("绑定成功");
            binder=(MainService1.ibinder)service;
            LogUtil.v("Time", "绑定后时间：" + String.valueOf(System.currentTimeMillis()));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v("Time", "绑定失败");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this, Color.TRANSPARENT);//设置状态栏为透明颜色
        setContentView(R.layout.smartroom_activity);
        Intent intent = new Intent(getApplicationContext(), MainService1.class);    //指定要绑定的service
        bindService(intent, con, Context.BIND_AUTO_CREATE);   //绑定主service
        // 注册自定义动态广播消息。根据Action识别广播
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction("com.suntrans.beijing.RECEIVE");  //为IntentFilter添加Action，接收的Action与发送的Action相同时才会出发onReceive
        registerReceiver(broadcastreceiver, filter_dynamic);    //动态注册broadcast receiver
        initViews();
        initToolBar();
        initData();
    }

    public void initViews() {
        pager = (ViewPager) findViewById(R.id.vp);
        tabLayout= (TabLayout) findViewById(R.id.tabLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView = (TextView) findViewById(R.id.tv_title);
    }

    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
            textView.setText("智能家居");
        }
    }


    public void initData() {
        pager.setAdapter(new Myadapter(getSupportFragmentManager()));
        pager.setOffscreenPageLimit(1);//ViewPager缓存页数1
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.setIcon(BottomMentTab.tabIcon_bule[tab.getPosition()]);
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setIcon(BottomMentTab.tabIcon_gray[tab.getPosition()]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.setting:
                startActivity(new Intent(Smartroom_Activity.this,Setting_Activity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private SmartControlFragment smartControlFragment;
    private RoomConditionFragment roomConditionFragment;
    private PowerInfoFragment powerInfoFragment;
    /**
     * ViewPager设置Adapter
     *
     */
    private class Myadapter extends FragmentStatePagerAdapter {

        private String[] mTitles = new String[]{"智能控制", "室内环境", "用电信息"};

        public  Myadapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                        if (smartControlFragment==null){
                            smartControlFragment = new SmartControlFragment();
                        }
                    return smartControlFragment;
                case 1:
                    if (roomConditionFragment==null){
                        roomConditionFragment = new RoomConditionFragment();
                    }
                    return roomConditionFragment;
                case 2:
                    if (powerInfoFragment==null){
                        powerInfoFragment = new PowerInfoFragment();
                    }
                    return powerInfoFragment;
                default:
                    return null;
            }


        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }
    }


    //广播接收器，接收服务器的数据分发给下面的fragment，
    protected BroadcastReceiver broadcastreceiver=new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent){
            byte[] data = intent.getByteArrayExtra("Content");
            int count = intent.getIntExtra("ContentNum",0);
            String content = "";   //接收的字符串
            for (int i = 0; i < count; i++) {
                String s1 = Integer.toHexString((data[i] + 256) % 256);   //byte转换成十六进制字符串(先把byte转换成0-255之间的非负数，因为java中的数据都是带符号的)
                if (s1.length() == 1)
                    s1 = "0" + s1;
                content = content + s1;
            }
            content = content.replace(" ","");
            content = content.toLowerCase();
            Map<String, Object> map = new HashMap<String, Object>();   //新建map存放要传递给主线程的数据
            map.put("data", data);    //客户端发回的数据

            Message msg =new Message();
            msg.what = count;
            msg.obj = map;
            if (count>10){
                if (MainService1.IsInnerNet){
                    if (content.substring(0,8).equals("f2fefe68")){
                        if (powerInfoFragment!=null)
                            powerInfoFragment.handler.sendMessage(msg);
                    }
                    if (content.substring(0,4).equals("ab68")){
                        if (roomConditionFragment!=null)
                        roomConditionFragment.handler1.sendMessage(msg);
                    }
                }else {
                    if (content.substring(0,22).equals("020000ff00571f92fefe68")){
                        if (powerInfoFragment!=null)
                        powerInfoFragment.handler.sendMessage(msg);
                    }
                    if (content.substring(0,8).equals("ab68ab68")){
                        if (roomConditionFragment!=null)
                        roomConditionFragment.handler1.sendMessage(msg);
                    }
                }

            }
        }
    };//广播接收器
}
