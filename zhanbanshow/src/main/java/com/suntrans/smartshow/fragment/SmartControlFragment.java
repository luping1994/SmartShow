package com.suntrans.smartshow.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.activity.SwitchControl_Activity;
import com.suntrans.smartshow.adapter.SmartControlAdapter;
import com.suntrans.smartshow.base.BaseApplication;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.bean.SmartSwitch;
import com.suntrans.smartshow.database.DbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 智能家居页面中第1个Fragment,显示第开关信息。
 * Created by Looney on 2016/9/26.
 */
public class SmartControlFragment extends BaseFragment {
    private ArrayList<Map<String,String>> data = new ArrayList<>();

    private SwipeRefreshLayout refreshLayout;   //下拉刷新控件
    private RecyclerView recyclerView;   //列表控件
    private SmartControlAdapter adapter;



    @Override
    public int getLayoutId() {
        return R.layout.roomcontrol_fragment;
    }

    private SQLiteDatabase db;
    private String sqlPath;

    @Override
    public void onResume() {
        super.onResume();
        initData1();
    }

    public void initData1() {
//        data.clear();
//        sqlPath = "data/data/" + BaseApplication.getApplication().getPackageName() + "/databases/IBMS";
//        db=SQLiteDatabase.openDatabase(sqlPath,null,SQLiteDatabase.OPEN_READWRITE);
//        db.beginTransaction();   //事务开始
//        db.beginTransaction();
//        Cursor cursor = db.rawQuery("select DISTINCT Area from switchs_tb",null);
//        if(cursor.getCount()>=1) {
//            while (cursor.moveToNext()) {
//                if (cursor.getString(0)!=null){
//                    Map<String, String> map = new HashMap<>();
//                    if (cursor.getString(0).equals("预留通道")){
//                        continue;
//                    }
//                    map.put("Area",cursor.getString(0));
//                    System.out.println(cursor.getString(0));
//
//                    data.add(map);
////                    System.out.println("AREA=" + cursor.getString(0));
//                }
//            }
//        }
//        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();
//        db.close();
    }

    @Override
    public void initViews() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new GridLayoutManager(getActivity(),3);
        adapter = new SmartControlAdapter(getActivity(),data);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new SmartControlAdapter.onItemClickListener() {
            @Override
            public void onclick(View v, int position) {
                Intent intent = new Intent();
                switch (position){
                    case 0:
                        intent.putExtra("area",0);
                        break;
                    case 1:
                        intent.putExtra("area",1);
                        break;
                    case 2:
                        intent.putExtra("area",2);
                        break;
                    case 3:
                        intent.putExtra("area",3);
                        break;
                    case 4:
                        intent.putExtra("area",4);
                        break;
                    case 5:
                        intent.putExtra("area",5);
                        break;
                    case 6:
                        intent.putExtra("area",6);
                        break;
                    case 7:
                        intent.putExtra("area",7);
                        break;
                    case 8:
                        intent.putExtra("area",8);
                        break;
                }
                intent.setClass(getActivity(), SwitchControl_Activity.class);
                startActivity(intent);   //跳转到单相表详细信息页面
//                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }


    @Override
    protected void parseObtainedMsg(byte[] bytes) {

    }
}
