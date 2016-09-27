package com.suntrans.smartshow.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.service.carrier.CarrierService;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.data;

/**
 * Created by pc on 2016/9/15.
 * 公共区域管理页面
 */
public class Publicarea_Activity extends BaseActivity {
    private LinearLayout layout_back;   //返回键
    private RecyclerView recyclerView;    //列表控件
    private ArrayList<Map<String, String>> data = new ArrayList<>();

    @Override
    public void initViews(Bundle savedInstanceState) {

        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
    }

    @Override
    public int getLayoutId() {
        return R.layout.publicarea;
    }

    @Override
    public void initData() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("Name","路灯");
        map1.put("Image", String.valueOf(R.drawable.roadlight));
        data.add(map1);
        Map<String, String> map2 = new HashMap<>();
        map2.put("Name","氙气灯");
        map2.put("Image", String.valueOf(R.drawable.shanqideng));
        data.add(map2);
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter adapter = new mAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initToolBar() {

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

            RecyclerView.ViewHolder holder= new Publicarea_Activity.mAdapter.viewHolder1(LayoutInflater.from(
                    Publicarea_Activity.this).inflate(R.layout.publicarea_item, parent,false));
            return holder;
        }

        /***
         * 绑定数据
         *
         * @param holder   绑定哪个holder，用if(holder instanceof mViewHolder1)来判断类型，再绑定数据
         * @param position
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            String name = data.get(position).get("name");
            int id =Integer.valueOf(data.get(position).get("Image"));

            Bitmap bitmap = BitmapFactory.decodeResource(Publicarea_Activity.this.getResources(),id);
            bitmap = Converts.toRoundCorner(bitmap, UiUtils.dip2px(20));

            ((viewHolder1)holder).image.setImageBitmap(bitmap);
            ((viewHolder1)holder).textView.setText(data.get(position).get("Name"));
            ((viewHolder1)holder).image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position==0){
                        startActivity(new Intent(Publicarea_Activity.this,RoadBulb_Activity.class));
                    }else if (position==1){
                        startActivity(new Intent(Publicarea_Activity.this,Flashlight_Activity.class));
                    }
                }
            });

        }


        @Override
        public int getItemCount() {
            return 2;
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
          return 0;
        }

        /**
         * 自定义继承RecyclerView.ViewHolder的viewholder
         * 布局类型1对应的ViewHolder，R.layout.listmain_userinfo
         */
        class viewHolder1 extends RecyclerView.ViewHolder {
            ImageView image;    //图标
            TextView textView;

            public viewHolder1(View view) {
                super(view);
                image = (ImageView) view.findViewById(R.id.iv);
                textView = (TextView) view.findViewById(R.id.tv);
            }
        }

    }

}
