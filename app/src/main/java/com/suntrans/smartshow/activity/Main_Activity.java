package com.suntrans.smartshow.activity;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.base.BaseActivity1;
import com.suntrans.smartshow.utils.RxBus;
import com.suntrans.smartshow.views.MySlidingMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.suntrans.smartshow.R.drawable.control;


/**
 * 主页面
 */
public class Main_Activity extends BaseActivity1 {
    private MySlidingMenu slidingMenu;   //侧滑菜单控件
    private View main_menu;    //左边菜单栏
    private View main_content;    //右边主页面
    private RecyclerView recyclerView;    //菜单列表
    private LinearLayout layout_fourmeters;    //四表合一
    private LinearLayout layout_industrypower;    //工业用电
    private LinearLayout layout_publicarea;    //公共区域智能管控
    private LinearLayout layout_smartroom;    //智能家居
    private ArrayList<Map<String,String>> data=new ArrayList<>();   //菜单列表的内容
    private Toolbar toolbar;
    private TextView tv_title;
    boolean i =true;
    @Override
    public int getLayoutId() {
        return R.layout.main;
    }
    @Override
    public void initViews(Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        slidingMenu=(MySlidingMenu)findViewById(R.id.slidingmenu);
        main_menu = findViewById(R.id.menu);
        main_content = findViewById(R.id.content);
        tv_title = (TextView) findViewById(R.id.tv_title);
        //菜单栏部分
        recyclerView=(RecyclerView)main_menu.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new mAdapter());
        //内容主体部分

        layout_fourmeters = (LinearLayout) main_content.findViewById(R.id.layout_fourmeters);
        layout_industrypower = (LinearLayout) main_content.findViewById(R.id.layout_industrypower);
        layout_publicarea = (LinearLayout) main_content.findViewById(R.id.layout_publicarea);
        layout_smartroom = (LinearLayout) main_content.findViewById(R.id.layout_smartroom);

        layout_fourmeters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(Main_Activity.this,Fourmeters_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        layout_industrypower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(Main_Activity.this, Industrypower_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);   //设置页面切换效果
            }
        });
        layout_publicarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(Main_Activity.this, Publicarea_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);   //设置页面切换效果
            }
        });
        layout_smartroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(Main_Activity.this,Smartroom_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void initToolBar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            tv_title.setText("三川智控展示");
        }
    }




    @Override
    public void onDestroy(){
        i=false;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if(slidingMenu.getState()){     //如果侧滑菜单正开着，那么就关闭
                 slidingMenu.toggleMenu();
                return true;
            }
            else {
                finish();   //否则关闭页面
                return true;
            }
        }
        return super.onKeyDown(keyCode,event);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                slidingMenu.toggleMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initData(){
        Map<String,String> map1=new HashMap<String,String>();
        map1.put("Name","主页");    //名称
        map1.put("Image",String.valueOf(R.mipmap.ic_home));   //图标
        data.add(map1);
        Map<String,String> map2=new HashMap<String,String>();
        map2.put("Name","查看在线设备");    //名称
        map2.put("Image",String.valueOf(R.mipmap.ic_online));   //图标
        data.add(map2);
        Map<String,String> map3=new HashMap<String,String>();
        map3.put("Name","内网模式");    //名称
        map3.put("Image",String.valueOf(R.mipmap.ic_net));   //图标
        data.add(map3);
        Map<String,String> map4=new HashMap<String,String>();
        map4.put("Name","外网模式");    //名称
        map4.put("Image",String.valueOf(R.mipmap.ic_net));   //图标
        data.add(map4);
        Map<String,String> map5=new HashMap<String,String>();
        map5.put("Name","帮助");    //名称
        map5.put("Image",String.valueOf(R.mipmap.ic_help));   //图标
        data.add(map5);
        Map<String,String> map6=new HashMap<String,String>();
        map6.put("Name","关于我们");    //名称
        map6.put("Image",String.valueOf(R.mipmap.ic_about));   //图标
        data.add(map6);
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
                    Main_Activity.this).inflate(R.layout.main_menu_listview, parent,false));

            return holder;
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
                Map<String, String> map = data.get(position);
                final String Name = map.get("Name").toString();
                final String Image = map.get("Image").toString();
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), Integer.valueOf(Image));
                viewholder.name.setText(Name);       //设置名称
                viewholder.image.setImageBitmap(bitmap);   //设置图标


                viewholder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {   //根据点击的选项跳转到相应页面
                        if(slidingMenu.getState()) {   //如果当前菜单正在显示，才执行操作。否则在主页面点击也会触发这个操作
                            switch (position){
                                case 0:{
                                    slidingMenu.toggleMenu();   //关闭侧滑
                                    break;
                                }
                                case 1:{

                                }
                                case 2:{

                                }
                                case 3:{

                                }
                                case 4:{

                                }
                                case 5:{

                                }
                                default:break;
                            }
                        }
                    }
                });
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
            ImageView image;    //图标
            TextView name;    //名称
            public viewHolder1(View view)
            {
                super(view);
                layout=(LinearLayout)view.findViewById(R.id.layout);
                image=(ImageView)view.findViewById(R.id.image);
                name = (TextView) view.findViewById(R.id.tx_name);

            }
        }


    }


    public void regiset(){

//发送广播
//        Intent intent = new Intent();
//        intent.setAction("com.suntrans.beijing.RECEIVE");
//        intent.putExtra("ContentNum", tem.length);   //数组长度
//        intent.putExtra("Content", tem);   //命令内容数组
//        sendBroadcast(intent);   //发送广播，通知各个activity

    }

}
