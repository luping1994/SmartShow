package com.suntrans.smartshow.fragment;


import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.activity.Industry_Activity;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.RxBus;

import java.util.Map;

import rx.Subscriber;

/**
 * Created by Looney on 2016/9/24.
 */
public class IndustryControlFragment extends BaseFragment implements View.OnClickListener {
    private RelativeLayout relativeLayout_fail;//连接服务器失败时展示
    private Button bt_state;
    private Button bt_Fan;
    private Button bt_Zheng;
    private ProgressDialog dialog;

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Map<String,String> data = (Map<String, String>) msg.obj;
            String s = data.get("data");
            System.out.println(s);
        }
    };
    @Override
    public int getLayoutId() {
        return R.layout.fragment_industrycon;
    }

    @Override
    public void initViews() {
        relativeLayout_fail = (RelativeLayout) rootView.findViewById(R.id.fail);
        bt_state= (Button) rootView.findViewById(R.id.bt_stop);
        bt_Fan = (Button) rootView.findViewById(R.id.bt_fanzhuan);
        bt_Zheng = (Button) rootView.findViewById(R.id.bt_zhengzhuan);
        bt_Zheng.setOnClickListener(this);
        bt_Fan.setOnClickListener(this);
        bt_state.setOnClickListener(this);
    }



    @Override
    public void onDestroy() {

        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        String order = "";
        byte a[] = null;
        switch (v.getId()){
            case R.id.bt_fanzhuan:
                order = "AA 0B 31 03 ADD";
                a= Converts.HexString2Bytes(order);
                order += Converts.GetCRC(a,0,a.length);
                ((Industry_Activity)getActivity()).binder.sendOrder(order,5);
                LogUtil.i("反转被点击");
                break;
            case R.id.bt_zhengzhuan:
                order = "AA 0B 32 03 ADD";
                a= Converts.HexString2Bytes(order);
                order += Converts.GetCRC(a,0,a.length);
                ((Industry_Activity)getActivity()).binder.sendOrder(order,5);
                LogUtil.i("正转被点击");
                break;
            case R.id.bt_stop:
                order = "AA 0B 33 03 ADD";
                a= Converts.HexString2Bytes(order);
                order += Converts.GetCRC(a,0,a.length);
                ((Industry_Activity)getActivity()).binder.sendOrder(order,5);
                LogUtil.i("停止被点击");
                break;
        }
    }

    @Override
    protected void parseObtainedMsg(byte[] bytes) {

    }
}
