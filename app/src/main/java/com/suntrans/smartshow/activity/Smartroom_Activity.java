package com.suntrans.smartshow.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.adapter.FlashLightAdapter;
import com.suntrans.smartshow.adapter.RecyclerViewDivider;
import com.suntrans.smartshow.base.BaseActivity;
import com.suntrans.smartshow.base.BaseActivity1;
import com.suntrans.smartshow.bean.FlashlightInfo;
import com.suntrans.smartshow.fragment.PowerInfoFragment;
import com.suntrans.smartshow.fragment.RoomConditionFragment;
import com.suntrans.smartshow.fragment.SmartControlFragment;

import static com.suntrans.smartshow.R.id.recyclerView;

/**
 * Created by Looney on 2016/9/26.
 */
public class Smartroom_Activity extends BaseActivity1 {

    private TextView textView;//标题
    private Toolbar toolbar;//标题栏
    private ViewPager pager;
    private TabLayout tabLayout;

    @Override
    public int getLayoutId() {
        return R.layout.smartroom_activity;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        pager = (ViewPager) findViewById(R.id.vp);
        tabLayout= (TabLayout) findViewById(R.id.tabLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView = (TextView) findViewById(R.id.tv_title);
    }

    @Override
    public void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_normal);
            textView.setText("智能家居");
        }
    }


    @Override
    public void initData() {
        pager.setAdapter(new Myadapter(getSupportFragmentManager()));
//        tabLayout.setupWithViewPager(pager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.setting:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private SmartControlFragment smartControlFragment;
    private RoomConditionFragment roomConditionFragment;
    private PowerInfoFragment powerInfoFragment;
    /**
     * ViewPager设置Adapter
     *
     */
    private class Myadapter extends FragmentStatePagerAdapter {

        private String[] mTitles = new String[]{"智能控制", "室内环境", "用电信息"};

        public  Myadapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                        if (smartControlFragment==null){
                            smartControlFragment = new SmartControlFragment();
                        }
                    return smartControlFragment;
                case 1:
                    if (roomConditionFragment==null){
                        roomConditionFragment = new RoomConditionFragment();
                    }
                    return roomConditionFragment;
                case 2:
                    if (powerInfoFragment==null){
                        powerInfoFragment = new PowerInfoFragment();
                    }
                    return powerInfoFragment;
                default:
                    return null;
            }


        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }
    }
}
