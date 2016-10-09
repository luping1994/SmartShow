package com.suntrans.smartshow.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.suntrans.smartshow.R;

/**
 * Created by Looney on 2016/10/8.
 */

public class Welcome_Activity extends AppCompatActivity {
    TextView title;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        title = (TextView) findViewById(R.id.tx_title);
        AnimationSet set = new AnimationSet(true);
        ScaleAnimation animation =new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        animation.setDuration(2000);
        set.addAnimation(animation);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                handler.sendEmptyMessageDelayed(1,1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        title.startAnimation(set);

    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            finish();
            startActivity(new Intent(Welcome_Activity.this,Main_Activity.class));
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left);
        }
    };
}
