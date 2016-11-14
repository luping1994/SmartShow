package com.suntrans.smartshow.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suntrans.smartshow.R;

/**
 * Created by Looney on 2016/10/4.
 */

public class LoadingDialog extends Dialog {
    private TextView tipTextView;
    private Context context;
    private ImageView loadingImage;
    private Animation loadingAnimation;    // 加载动画
    public LoadingDialog(Context context) {
        super(context, R.style.loading_dialog);
        this.context= context;
        init();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void show() {

        // 使用ImageView显示动画
        if (loadingImage!=null)
        loadingImage.startAnimation(loadingAnimation);
        super.show();
    }

    @Override
    public void dismiss() {
        if (loadingImage!=null)
            loadingImage.clearAnimation();
        super.dismiss();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        loadingImage = (ImageView) v.findViewById(R.id.img);
        loadingImage.post(new Runnable() {
            @Override
            public void run() {
                loadingImage.startAnimation(loadingAnimation);
            }
        });
        tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        loadingAnimation= AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        this.setCancelable(true);// 用“返回键”取消
        this.setCanceledOnTouchOutside(false);
        this.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
    }

    public void setTipTextView(String msg){
        tipTextView.setText(msg);
    };


}
