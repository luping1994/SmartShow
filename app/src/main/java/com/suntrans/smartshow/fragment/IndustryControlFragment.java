package com.suntrans.smartshow.fragment;


import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.RxBus;

import rx.Subscriber;

/**
 * Created by Looney on 2016/9/24.
 */
public class IndustryControlFragment extends BaseFragment implements View.OnClickListener {
    private RelativeLayout relativeLayout_fail;//连接服务器失败时展示
    private Button bt_state;
    private Button bt_Fan;
    private Button bt_Zheng;



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
        switch (v.getId()){
            case R.id.bt_fanzhuan:
                LogUtil.i("反转被点击");
                break;
            case R.id.bt_zhengzhuan:
                LogUtil.i("正转被点击");
                break;
            case R.id.bt_stop:
                LogUtil.i("停止被点击");
                break;
        }
    }

    @Override
    protected void parseObtainedMsg(byte[] bytes) {

    }
}
