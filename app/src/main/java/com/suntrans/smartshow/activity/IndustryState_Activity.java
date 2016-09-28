package com.suntrans.smartshow.activity;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.RxBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;
import rx.Subscription;


/**
 * Created by pc on 2016/9/16.
 * 三相电表参数页面
 */
public class IndustryState_Activity extends BaseActivity {

    public MainService1.ibinder binder;  //用于Activity与Service通信
    private LinearLayout layout_back;    //返回键
    private TextView tx_title;   //标题
    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private String title;
    private ArrayList<Map<String, String>> data = new ArrayList<>();
    private TextView textView;
    /**
     * 服务连接
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder=(MainService1.ibinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplication(), "网络错误！", Toast.LENGTH_SHORT).show();

        }
    };

    @Override
    public void initViews(Bundle savedInstanceState) {

//        Intent intent1 = new Intent(BaseApplication.getApplication(), MainService.class);    //指定要绑定的service
//        bindService(intent1, connection, Context.BIND_AUTO_CREATE);   //绑定主service
        initRx();
        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        tx_title = (TextView) findViewById(R.id.tx_title);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        textView = (TextView) findViewById(R.id.tv_back);
        textView.setText("工业用电");
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title = "";
        tx_title.setText(title);
        recyclerView.setLayoutManager(new LinearLayoutManager(IndustryState_Activity.this));   //设置布局方式
        recyclerView.addItemDecoration(new RecyclerViewDivider(IndustryState_Activity.this, LinearLayoutManager.VERTICAL));  //设置分割线
        recyclerView.setAdapter(new mAdapter());

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                String order = "FE 68 14 29 00 07 14 20 68 1F 00";
//                binder.sendOrder(order,3);
            }
        });
    }

    @Override
    protected void onDestroy() {
//        unbindService(connection);   //解除Service的绑定
        super.onDestroy();
    }

    private Subscription rxsub;
    private void initRx() {
        rxsub = RxBus.getInstance().toObserverable(byte[].class).subscribe(new Subscriber<byte[]>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(byte[] bytes) {
                //bytes 是返回的16进制命令
                refreshListView();
            }
        });
    }

    @Override
    public void initToolBar() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.meter;
    }

    @Override
    protected void onPause() {
//        rxsub.unsubscribe();
        super.onPause();
    }

    @Override
    public void initData(){
        data.clear();   //先清空数据

                Map<String, String> map1 = new HashMap<>();
                map1.put("Name","用电量");
                map1.put("Value","null");
                map1.put("Image", String.valueOf(R.drawable.ic_elec));
                data.add(map1);
                Map<String, String> map2 = new HashMap<>();
                map2.put("Name","有功功率");
                map2.put("Value","null");
                map2.put("Image", String.valueOf(R.drawable.ic_power));
                data.add(map2);
                Map<String, String> map3 = new HashMap<>();
                map3.put("Name","无功功率");
                map3.put("Value","null");
                map3.put("Image", String.valueOf(R.drawable.ic_power));
                data.add(map3);
                Map<String, String> map4 = new HashMap<>();
                map4.put("Name","功率因素");
                map4.put("Value","null");
                map4.put("Image", String.valueOf(R.drawable.ic_powerrate));
                data.add(map4);
                Map<String, String> map5 = new HashMap<>();
                map5.put("Name","A相电压");
                map5.put("Value","null");
                map5.put("Image", String.valueOf(R.drawable.ic_voltage));
                data.add(map5);
        Map<String, String> map6 = new HashMap<>();
        map6.put("Name","B相电压");
        map6.put("Value","null");
        map6.put("Image", String.valueOf(R.drawable.ic_voltage));
        data.add(map6);

        Map<String, String> map7 = new HashMap<>();
        map7.put("Name","C相电压");
        map7.put("Value","null");
        map7.put("Image", String.valueOf(R.drawable.ic_voltage));
        data.add(map7);

        Map<String, String> map8 = new HashMap<>();
        map8.put("Name","A相电流");
        map8.put("Value","null");
        map8.put("Image", String.valueOf(R.drawable.ic_current));
        data.add(map8);

        Map<String, String> map9 = new HashMap<>();
        map9.put("Name","B相电流");
        map9.put("Value","null");
        map9.put("Image", String.valueOf(R.drawable.ic_current));
        data.add(map9);

        Map<String, String> map10= new HashMap<>();
        map10.put("Name","C相电流");
        map10.put("Value","null");
        map10.put("Image", String.valueOf(R.drawable.ic_current));
        data.add(map8);
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
            if (viewType==1){
                RecyclerView.ViewHolder holder= new viewHolder1(LayoutInflater.from(
                        IndustryState_Activity.this).inflate(R.layout.meter_listview, parent,false));
                return holder;
            }else {
                RecyclerView.ViewHolder holder= new viewHolder2(LayoutInflater.from(
                        IndustryState_Activity.this).inflate(R.layout.meter_listview_time, parent,false));
                return holder;
            }

        }

        /***
         * 绑定数据
         * @param holder   绑定哪个holder，用if(holder instanceof mViewHolder1)来判断类型，再绑定数据
         * @param position
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
        {
            //判断holder是哪个类，从而确定是哪种布局
            /////布局1
            if(holder instanceof viewHolder1) {
                viewHolder1 viewholder = (viewHolder1) holder;
                Map<String, String> map = data.get(position-1);
                final String Name = map.get("Name");
                final String Value = map.get("Value");
                Bitmap bitmap = BitmapFactory.decodeResource(IndustryState_Activity.this.getResources(), Integer.valueOf(map.get("Image")));
                viewholder.image.setImageBitmap(bitmap);
                viewholder.name.setText(Name);
                viewholder.value.setText(Value);
            }else {
                viewHolder2 viewholder = (viewHolder2) holder;
                viewholder.value.setText("2016/9/23");
            }
        }



        @Override
        public int getItemCount()
        {
            return data.size()+1;
        }
        /**
         * 决定元素的布局使用哪种类型
         *在本activity中，布局1使用R.layout.roomgridview，
         * @param position 数据源的下标
         * @return 一个int型标志，传递给onCreateViewHolder的第二个参数 */
        @Override
        public int getItemViewType(int position) {
            if (position==0){
                return 0;
            }else
                return 1;
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 布局类型1对应的ViewHolder，R.layout.listmain_userinfo
         */
        class viewHolder1 extends RecyclerView.ViewHolder
        {
            LinearLayout layout;   //整体布局
            ImageView image;    //图标
            TextView name;    //名称
            TextView value;    //参数值
            public viewHolder1(View view)
            {
                super(view);
                layout=(LinearLayout)view.findViewById(R.id.layout);
                image=(ImageView)view.findViewById(R.id.image);
                name = (TextView) view.findViewById(R.id.name);
                value = (TextView) view.findViewById(R.id.value);
            }
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 布局类型1对应的ViewHolder，R.layout.listmain_userinfo
         */
        class viewHolder2 extends RecyclerView.ViewHolder
        {
            LinearLayout layout;   //整体布局
            TextView value;    //参数值
            public viewHolder2(View view)
            {
                super(view);
                layout=(LinearLayout)view.findViewById(R.id.layout);
                value = (TextView) view.findViewById(R.id.value);
            }
        }
    }

    /**
     * 刷新数据
     */
    private void refreshListView(){

    }
}
