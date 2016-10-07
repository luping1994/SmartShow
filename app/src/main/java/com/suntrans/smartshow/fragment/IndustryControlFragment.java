package com.suntrans.smartshow.fragment;


import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.R;
import com.suntrans.smartshow.activity.MotorTriphase_Activity;
import com.suntrans.smartshow.base.BaseFragment;
import com.suntrans.smartshow.views.LoadingDialog;

import java.util.Map;


/**
 * Created by Looney on 2016/9/24.
 */
public class IndustryControlFragment extends BaseFragment implements View.OnClickListener {
    private RelativeLayout relativeLayout_fail;//连接服务器失败时展示
    private Button bt_state;
    private Button bt_Fan;
    private Button bt_Zheng;
    private LoadingDialog dialog;
    private int which = 100;//1表示成功 100表示成功界面显示完毕
    private TextView state;
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Map<String,String> data = (Map<String, String>) msg.obj;
            String s = data.get("data");
            s=s.toLowerCase();
//            aa0b3303c10f53774883650d0a
            if (s.substring(0,10).equals("aa0b3103c1")){
                showSuccessDialog();
                state.setText("电机状态:反转");
            }else if (s.substring(0,10).equals("aa0b3203c1")){
                showSuccessDialog();
                state.setText("电机状态:正转");
            }else if (s.substring(0,10).equals("aa0b3303c1")){
                showSuccessDialog();
                state.setText("电机状态:停止");
            }

        }

    };

    private void showSuccessDialog() {
        which=1;
        dialog.setTipTextView("成功");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                which=100;
            }
        }, 500);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_industrycon;
    }

    @Override
    public void initViews() {
        dialog=new LoadingDialog(getActivity());
        state = (TextView) rootView.findViewById(R.id.state);
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
                order = "AA 0B 31 03 c1";
                a= Converts.HexString2Bytes(order);
                order += Converts.GetCRC(a,0,a.length);
                showFailedDialog();
                ((MotorTriphase_Activity)getActivity()).binder.sendOrder(order,5);
                break;
            case R.id.bt_zhengzhuan:
                order = "AA 0B 32 03 c1";
                a= Converts.HexString2Bytes(order);
                order += Converts.GetCRC(a,0,a.length);
                showFailedDialog();
                ((MotorTriphase_Activity)getActivity()).binder.sendOrder(order,5);
                break;
            case R.id.bt_stop:
                order = "AA 0B 33 03 c1";
                a= Converts.HexString2Bytes(order);
                order += Converts.GetCRC(a,0,a.length);
                showFailedDialog();
                ((MotorTriphase_Activity)getActivity()).binder.sendOrder(order,5);
                break;
        }
    }

    private void showFailedDialog() {
        dialog.show();
        dialog.setTipTextView("执行中...");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (which==100){
                    dialog.setTipTextView("执行失败");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            which=100;
                        }
                    }, 500);
                }
            }
        }, 2000);
    }

    //    Handler handler1 = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            int what = msg.what;
//            switch (what){
//                case 1://显示进度
//                    LoadingDialog dialog = new LoadingDialog(getActivity());
//                    dialog.show();
//                    dialog.setTipTextView("发送中...");
//                    break;
//                case 0:
//                    final LoadingDialog dialog1 = new LoadingDialog(getActivity());
//                    dialog1.setTipTextView("成功");
//                    dialog1.show();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            dialog1.dismiss();
//                        }
//                    },5000);
//            }
//        }
//    };
    @Override
    protected void parseObtainedMsg(byte[] bytes) {

    }
}
