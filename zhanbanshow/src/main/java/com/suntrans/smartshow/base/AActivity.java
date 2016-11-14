package com.suntrans.smartshow.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.suntrans.smartshow.Convert.Converts;
import com.suntrans.smartshow.service.MainService1;
import com.suntrans.smartshow.utils.LogUtil;
import com.suntrans.smartshow.utils.StatusBarCompat;


/**
 * Created by Looney on 2016/8/9.
 */
public class AActivity extends AppCompatActivity {

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
    }
}
