package com.suntrans.smartshow.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.suntrans.smartshow.utils.RxBus;
import com.trello.rxlifecycle.components.support.RxFragment;

import rx.Subscriber;


public abstract class BaseFragment extends RxFragment
{

    public View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        RxBus.getInstance().toObserverable(byte[].class).subscribe(new Subscriber<byte[]>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(byte[] bytes) {
                parseObtainedMsg(bytes);
            }
        });
        rootView = inflater.inflate(getLayoutId(), container, false);

        return rootView;
    }

    /**
     * 解析从服务器发回的数据,是16进制的完整命令
     * @param bytes
     */
    protected abstract void parseObtainedMsg(byte[] bytes);

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {

        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    @Override
    public void onDetach()
    {

        super.onDetach();
    }

    /**
     * 获得Fragment的xml布局
     * @return
     */
    public abstract int getLayoutId();

    /**
     * 初始化布局
     */
    public abstract void initViews();
}
