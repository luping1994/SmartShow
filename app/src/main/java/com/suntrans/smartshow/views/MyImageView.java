package com.suntrans.smartshow.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.suntrans.smartshow.utils.UiUtils;

import static android.R.attr.width;
import static android.view.View.MeasureSpec.getMode;
import static android.view.View.MeasureSpec.getSize;

/**
 * Created by Looney on 2016/9/25.
 */

public class MyImageView extends ViewGroup {

    private View mImageView;
    private View mTextView;
    private int mImageViewHeight;
    private int mImageViewWidth;
    private int mTextViewHeight;
    private int mTextViewWidth;

    private Context mContext;
    int pading;
    public MyImageView(Context context) {
        this(context, null, 0);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pading=convertToDp(context,40);
        this.mContext =context;
        //        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        MeasureSpec.AT_MOST

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //拿到ImageView，第0个孩子
        mImageView = getChildAt(0);
        //拿到textView第1个孩子
        mTextView = getChildAt(1);

        mImageViewWidth = UiUtils.dip2px(mImageView.getLayoutParams().width,mContext);
        mImageViewHeight =UiUtils.dip2px(mImageView.getLayoutParams().height,mContext) ;

        mTextViewHeight = UiUtils.dip2px(mTextView.getLayoutParams().height,mContext);
        mTextViewWidth  =  UiUtils.dip2px(mTextView.getLayoutParams().width,mContext);

        //为了测量每个孩子需确定测量规则
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int HeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        int textViewWidthMode = widthMode;
        int textViewHeightMode = HeightMode;


        int textWidthMeasureSpec=MeasureSpec.makeMeasureSpec(mTextViewWidth,MeasureSpec.EXACTLY);
        int textdHeightMeasureSpec=MeasureSpec.makeMeasureSpec(mTextViewHeight,MeasureSpec.EXACTLY);

        int imageViewWSpec = MeasureSpec.makeMeasureSpec(mImageViewWidth,MeasureSpec.EXACTLY);
        int imageViewHSpec = MeasureSpec.makeMeasureSpec(mImageViewHeight,MeasureSpec.EXACTLY);

        //分别测量两个孩子
        mImageView.measure(imageViewWSpec,imageViewHSpec);
        //测量textView
        mTextView.measure(textWidthMeasureSpec,textdHeightMeasureSpec);

        setMeasuredDimension(mImageViewWidth,mImageViewHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mImageView.layout(0,0,mImageViewWidth,mImageViewHeight);
        mTextView.layout(0,mImageViewHeight- pading,mImageViewWidth,mImageViewHeight);
    }

    /**
     * 将传进来的数转化为dp
     */
    private int convertToDp(Context context , int num){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,num,context.getResources().getDisplayMetrics());
    }
}
