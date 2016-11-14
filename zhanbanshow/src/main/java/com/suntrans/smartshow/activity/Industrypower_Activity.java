package com.suntrans.smartshow.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseActivity1;

/**
 * 工业用电页面，三相开关状态获取和电机控制。
 * Created by Looney on 2016/9/24.
 */

public class Industrypower_Activity extends BaseActivity1 implements View.OnClickListener {
    private LinearLayout layout_back;
    private Button layout_con;
    private Button layout_state;
    @Override
    public int getLayoutId() {
        return R.layout.industrypower_activity;
    }
    @Override
    public void initViews(Bundle savedInstanceState) {
        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        layout_con = (Button) findViewById(R.id.ll_con);
        layout_state = (Button) findViewById(R.id.ll_state);

    }

    @Override
    public void initData() {
        layout_back.setOnClickListener(this);
        layout_state.setOnClickListener(this);
        layout_con.setOnClickListener(this);
    }

    @Override
    public void initToolBar() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layout_back:
                finish();
                break;
            case R.id.ll_state:
                startActivity(new Intent(this,IndustryState_Activity.class));
                break;
            case R.id.ll_con:
                startActivity(new Intent(this,MotorTriphase_Activity.class));
                break;
        }


    }
}
