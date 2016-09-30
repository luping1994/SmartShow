package com.suntrans.smartshow.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.FlashLightAdapter;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.adapter.RoadBulbAdapter;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.bean.FlashlightInfo;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.RxBus;
import com.suntrans.smartshow.utils.ThreadManager;
import com.suntrans.smartshow.utils.UiUtils;
import com.suntrans.smartshow.views.Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;
import rx.Subscription;

import static android.R.attr.order;
import static com.suntrans.smartshow.R.id.bottom;
import static com.suntrans.smartshow.R.id.state;

/**
 * 公共区域管理中的氙气灯主页面，控制和显示氙气灯信息
 * Created by Looney on 2016/9/26.
 */

public class Flashlight_Activity extends BaseActivity {
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private FlashlightInfo data;
    private TextView textView;//标题
    private Toolbar toolbar;
    private FlashLightAdapter adapter;
    private String lightAddress = "010108";
    @Override
    public int getLayoutId() {
        return R.layout.flashlight_activity;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        textView = (TextView) findViewById(R.id.tv_title);
    }

    @Override
    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
            textView.setText("氙气灯");
        }
    }
    Handler handler = new Handler();

    @Override
    public void initData() {
        data= new FlashlightInfo();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        adapter = new FlashLightAdapter(this,data);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL));  //设置分割线
        recyclerView.setAdapter(adapter);
        //读取数据
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String s =lightAddress+"03 0200 000d";
                s+=getCRC(s);
                s= s.replace(" ","");
                binder.sendOrder(s,5);

                String s1 = lightAddress+"03 0300 0001";
                s1+=getCRC(s1);
                s1= s1.replace(" ","");
                binder.sendOrder(s1,5);//读挡位信息

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
        adapter.setmOnSwitchListener(new FlashLightAdapter.onSwitchListener() {
            @Override
            public void onChanged(Switch switchView, boolean isChecked) {
                if (!data.isOpen()){
                    binder.sendOrder("01 01 08 06 02 00 00 01 D8 37",5);
                }else {
                    binder.sendOrder("01 01 08 06 02 00 00 00 19 F7",5);
                }
            }
        });

        ThreadManager.getInstance().createLongPool().execute(new Runnable() {
            @Override
            public void run() {
                boolean isrun =true;
                while (isrun){
                    if (binder!=null){
                        String s =lightAddress+"03 0200 000d";
                        s+=getCRC(s);
                        s= s.replace(" ","");
                        binder.sendOrder(s,5);
                        try {
                            Thread.sleep(300);
                            String s1 = lightAddress+"03 0300 0001";
                            s1+=getCRC(s1);
                            s1= s1.replace(" ","");
                            binder.sendOrder(s1,5);//读挡位信息
                            isrun=false;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
    }

    private String getCRC(String s) {
        s=s.replace(" ","");
        byte [] a =Converts.HexString2Bytes(s);

        return Converts.GetCRC(a,0,a.length);
    }

    @Override
    protected void onDestroy() {
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

    //解析数据1
    @Override
    protected void parseData(Context context, Intent intent) {

        int count = intent.getIntExtra("ContentNum", 0);   //byte数组的长度
        byte[] data1 = intent.getByteArrayExtra("Content");  //内容数组
        String s = Converts.Bytes2HexString(data1);
        s= s.replace(" ","");
        s = s.substring(2,s.length());
        s=s.toLowerCase();
        if(s.substring(0,6).equals("010108")&&s.substring(6,8).equals("03"))//地址为氙气灯地址并且为读寄存器13个
        {
            if (s.substring(8,10).equals("1a")){
                LogUtil.i("氙气灯数据：========>>>>fuck"+s);
                String state = s.substring(10,14);//总开关状态0000或者0001
                double alter_UV = HexToDec(s.substring(14,18))/100;//交流电压
                double alter_current = HexToDec(s.substring(18,22))/100;//交流电流
                double alter_rete = HexToDec(s.substring(22,26))/100;//交流功率
                double elec_power =HexToDec(s.substring(26,34))/100;//用电量
                double power_rate = HexToDec(s.substring(34,38))/100;//功率因素
                double out_UV  =HexToDec(s.substring(38,42))/100;//输出电压
                double out_current = HexToDec(s.substring(42,46))/100;//输出电流
                double out_power = HexToDec(s.substring(46,50))/100;//输出功率
                double tem  = HexToDec(s.substring(50,54))/100;//温度
                double light = HexToDec(s.substring(54,58))/100;//光照强度
                double k = HexToDec(s.substring(58,62))/100;//系数

                data.setAlter_UV(alter_UV);
                data.setAlter_current(alter_current);
                data.setAlter_rate(alter_rete);
                data.setElec_power(elec_power);
                data.setPower_rate(power_rate);
                data.setOut_current(out_current);
                data.setOut_UV(out_UV);
                data.setOut_power(out_power);
                data.setTem(tem);
                data.setLight(light);
                data.setK(k);
                data.setOpen(state.equals("0001")?true:false);

            }

        }else if (s.substring(6,8).equals("06")){//写单个寄存器
            if (s.substring(8,12).equals("0200")){//总开关
                String state = s.substring(12,16);
                data.setOpen(state.equals("0001")?true:false);
            }else if (s.substring(8,12).equals("0300")){//写了档位信息后返回的挡位信息
                String grade = s.substring(15,16);
                data.setGrade(Integer.valueOf(grade));
            }
        }
        //更新界面
        UiUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (refreshLayout.isRefreshing()){
                    refreshLayout.setRefreshing(false);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 将十六进制的字符串转化为十进制的数值
     */
    public  long HexToDec(String hexStr) {
        Map<String, Integer> hexMap = prepareDate(); // 先准备对应关系数据
        int length = hexStr.length();
        long result = 0L; // 保存最终的结果
        for (int i = 0; i < length; i++) {
            result += hexMap.get(hexStr.subSequence(i, i + 1)) * Math.pow(16, length - 1 - i);
        }
//        System.out.println("hexStr=" + hexStr + ",result=" + result);
        return result;
    }

    /**
     * 准备十六进制字符对应关系。如("1",1)...("A",10),("B",11)
     */
    private  HashMap<String, Integer> prepareDate() {
        HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
        for (int i = 0; i <= 9; i++) {
            hashMap.put(i + "", i);
        }
        hashMap.put("a", 10);
        hashMap.put("b", 11);
        hashMap.put("c", 12);
        hashMap.put("d", 13);
        hashMap.put("e", 14);
        hashMap.put("f", 15);
        return hashMap;
    }

}
