package com.suntrans.smartshow.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.suntrans.smartshow.base.BaseApplication;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAccessToken;

/**
 * Created by Looney on 2016/10/9.
 */

public class EzvizBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "EzvizBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(),"com.videogo.action.OAUTH_SUCCESS_ACTION")){
            EZAccessToken accessToken = EZOpenSDK.getInstance().getEZAccessToken();
//            Intent toIntent = new Intent(context, EZCameraListActivity.class);
//            toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            BaseApplication.getSharedPreferences().edit().putInt("video",1).commit();
            //保存token及token超时时间
            EZOpenSDK openSdk = EZOpenSDK.getInstance();
            if(openSdk != null) {
                EZAccessToken token = openSdk.getEZAccessToken();
                //保存token，获取超时时间，在token过期时重新获取
                Log.i(TAG, "t:" + token.getAccessToken().substring(0, 5) + " expire:" + token.getExpire());
                BaseApplication.getSharedPreferences().edit().putString("timeout",token.getExpire()+"").commit();
                String time = System.currentTimeMillis()+"";
                Log.i(TAG,"当前保存的时间为:"+time);
                BaseApplication.getSharedPreferences().edit().putString("fristTime",time).commit();
            }
//            context.startActivity(toIntent);
        }
    }
}
