package com.suntrans.smartshow.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.utils.StatusBarCompat;

/**
 * Created by Looney on 2016/10/8.
 */

public class Welcome_Activity extends AppCompatActivity {
    private static String appKey ="bc2aa07aff1140e6adc06733e9be94ca";
    TextView title;
    TextView flag;
    Toolbar toolbar;
    TextView textView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        StatusBarCompat.compat(this, Color.TRANSPARENT);
        setContentView(R.layout.welcome_activity);
        flag = (TextView) findViewById(R.id.center_text);
//        if (ContextCompat.checkSelfPermission(Welcome_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            //申请WRITE_EXTERNAL_STORAGE权限
//            ActivityCompat.requestPermissions(Welcome_Activity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    1000);
//        }

        title = (TextView) findViewById(R.id.tx_title);
        handler.sendEmptyMessageDelayed(1,2000);
//        AnimationSet set = new AnimationSet(true);
//        ScaleAnimation animation =new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
//                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//
//        animation.setDuration(2000);
//        set.addAnimation(animation);
//        set.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        title.startAnimation(set);

    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            startActivity(new Intent(Welcome_Activity.this,Main_Activity.class));
//            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            scaleUpAnimation(flag);
           Welcome_Activity.this.finish();
        }
    };

    private void scaleUpAnimation(View view) {
        //让新的Activity从一个小的范围扩大到全屏
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeScaleUpAnimation(view, //The View that the new activity is animating from
                        (int)view.getWidth()/2, (int)view.getHeight()/2, //拉伸开始的坐标
                        0, 0);//拉伸开始的区域大小，这里用（0，0）表示从无到
        startNewAcitivity(options);
    }
    private void startNewAcitivity(ActivityOptionsCompat options) {
        Intent intent = new Intent(this,Main_Activity.class);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
            textView.setText("");
        }
    }
}
