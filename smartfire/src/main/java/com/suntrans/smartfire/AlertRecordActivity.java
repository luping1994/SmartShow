package com.suntrans.smartfire;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import Utils.DbHelper;

/**
 * Created by Looney on 2016/10/17.
 */
public class AlertRecordActivity extends AppCompatActivity {
    private LinearLayout layout_back;
    private ArrayList<Map<String,String>> data = new ArrayList<>();
    private LinearLayout layout_clear;
    private RecyclerView recyclerView;
    private LinearLayout layout_add;
    private TextView title;
    private TextView textView_empty;
    private LinearLayoutManager manager;
    private MyAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alertrecord_activity);
        initData();
        initView();
    }

    private void initData() {
        data.clear();
        DbHelper dh = new DbHelper(this, "IBMS", null, 1);
        SQLiteDatabase db=dh.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor=db.query(true,"alert_tb",new String[]{"RSAddr","Name","Time","Content"},null,null,null,null,"Time DESC",null);
        if(cursor.getCount()>=1) {
            while (cursor.moveToNext()) {
                Map<String, String> map = new HashMap<>();
                map.put("RSAddr",cursor.getString(0));
                map.put("Name", cursor.getString(1));
                map.put("Time", cursor.getString(2));
                map.put("Content", cursor.getString(3));
                data.add(map);
            }
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    private void initView() {
        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        layout_clear = (LinearLayout) findViewById(R.id.layout_delete);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        textView_empty = (TextView) findViewById(R.id.text_empty);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
        if (data.size()==0){
            textView_empty.setVisibility(View.VISIBLE);
            textView_empty.setText("暂无火灾报警记录");
        }else {
            textView_empty.setVisibility(View.GONE);
        }
        layout_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearData();
            }
        });
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 清除记录
     */
    private void clearData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DbHelper dh = new DbHelper(AlertRecordActivity.this, "IBMS", null, 1);
                SQLiteDatabase db=dh.getWritableDatabase();
                db.beginTransaction();
                db.delete("alert_tb",null,null);
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
                initData();
                adapter.notifyDataSetChanged();
                Toast.makeText(AlertRecordActivity.this,"清除完成",Toast.LENGTH_SHORT).show();
            }
        });
        builder.setTitle("确定要清除所有报警信息?");
        builder.create().show();

    }

    class MyAdapter extends RecyclerView.Adapter{

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            RecyclerView.ViewHolder holder= new viewHolder1(LayoutInflater.from(AlertRecordActivity.this)
                    .inflate(R.layout.record_item, parent,false));
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((viewHolder1)holder).setData(position);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class viewHolder1 extends RecyclerView.ViewHolder{
            TextView textView;
            public viewHolder1(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.content);
            }

            public void setData(int position) {
//                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");   //hh为小写是12小时制，为大写HH时时24小时制
//                String date = sDateFormat.format(new java.util.Date(Long.valueOf(data.get(position).get("Time"))));
                textView.setText(data.get(position).get("Content"));
            }
        }
    }
}
