package com.suntrans.smartshow.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseApplication;
import com.suntrans.smartshow.utils.StatusBarCompat;
import com.suntrans.smartshow.views.MySlidingMenu;
import com.videogo.openapi.EZOpenSDK;
//import com.videogo.openapi.EZOpenSDK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION_CODES.M;


/**
 * 主页面
 */
public class Main_Activity extends AppCompatActivity {
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
    private TextView mode;//内外网模式显示
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initViews();
        initToolBar();
        initData();
    }
    public void initViews() {
        StatusBarCompat.compat(this,Color.TRANSPARENT);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        slidingMenu=(MySlidingMenu)findViewById(R.id.slidingmenu);
        main_menu = findViewById(R.id.menu);
        main_content = findViewById(R.id.content);
        tv_title = (TextView) findViewById(R.id.tv_title);
        mode = (TextView) findViewById(R.id.moshi);
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
                logoutApp();
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

        String ip = BaseApplication.getSharedPreferences().getString("chunkouIpAddress","null");
        if (ip.equals("192.168.1.213"))
            mode.setText("当前模式:内网模式");
        else
            mode.setText("当前模式:外网模式");


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
                                    startActivity(new Intent(Main_Activity.this,OnlineDevices_Activity.class));
//                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    break;
                                }
                                case 2:{

                                    AlertDialog dialog = new AlertDialog.Builder(Main_Activity.this)
                                            .setTitle("提示")
                                            .setMessage("你确定要切换到内网模式吗?")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    BaseApplication.getSharedPreferences().edit().putString("sixIpAddress","192.168.1.235").commit();
                                                    BaseApplication.getSharedPreferences().edit().putInt("sixPort",8000).commit();
                                                    BaseApplication.getSharedPreferences().edit().putString("chunkouIpAddress","192.168.1.213").commit();
                                                    BaseApplication.getSharedPreferences().edit().putInt("chunkouPort",8000).commit();
                                                    dialog.dismiss();
                                                    mode.setText("当前模式:内网模式");
                                                    slidingMenu.toggleMenu();   //关闭侧滑
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).create();
                                    dialog.show();
                                    break;
                                }
                                case 3:{
                                    AlertDialog dialog = new AlertDialog.Builder(Main_Activity.this)
                                            .setTitle("提示")
                                            .setMessage("你确定要切换到外网模式吗?")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    BaseApplication.getSharedPreferences().edit().putString("sixIpAddress","61.235.65.160").commit();
                                                    BaseApplication.getSharedPreferences().edit().putInt("sixPort",8286).commit();
                                                    BaseApplication.getSharedPreferences().edit().putString("chunkouIpAddress","61.235.65.160").commit();
                                                    BaseApplication.getSharedPreferences().edit().putInt("chunkouPort",8286).commit();
                                                    dialog.dismiss();
                                                    slidingMenu.toggleMenu();   //关闭侧滑
                                                    mode.setText("当前模式:外网模式");
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).create();
                                    dialog.show();
                                    break;
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
        private long[] mHits = new long[5];



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




    private  long exitTime = 0;

    private void logoutApp()
    {

        if (System.currentTimeMillis() - exitTime > 2000)
        {
            Toast.makeText(Main_Activity.this,"再按一次退出",Toast.LENGTH_SHORT).show();

            exitTime = System.currentTimeMillis();
        } else
        {
//            finish();
//            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

}
