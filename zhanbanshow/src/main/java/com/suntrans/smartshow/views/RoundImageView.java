package com.suntrans.smartshow.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.suntrans.smartshow.R;
import com.suntrans.smartshow.utils.LogUtil;

/**
 * Created by Looney on 2016/9/24.
 */

public class RoundImageView extends ImageView {

    private static final int COLORDRAWABLE_DIMENSION = 1;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    // ImageView类型
    private int type;
    // 圆形图片
    private static final int TYPE_CIRCLE = 0;
    // 圆角图片
    private static final int TYPE_ROUND = 1;
    // 默认圆角宽度
    private static final int BORDER_RADIUS_DEFAULT = 10;
    // 获取圆角宽度
    private int mBorderRadius;
    // 画笔
    private Paint mPaint;
    // 半径
    private int mRadius;
    // 缩放矩阵
    private Matrix mMatrix;
    // 渲染器,使用图片填充形状
    private BitmapShader mBitmapShader;
    // 宽度
    private int mWidth;
    // 圆角范围
    private RectF mRectF;

    private Bitmap mBitmap;
    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // 初始化画笔等属性
        mMatrix = new Matrix();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        // 获取自定义属性值
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundImageView, defStyle, 0);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.RoundImageView_borderRadius:
                    // 获取圆角大小
                    mBorderRadius = array.getDimensionPixelSize(R.styleable.RoundImageView_borderRadius, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BORDER_RADIUS_DEFAULT, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.RoundImageView_imageType:
                    // 获取ImageView的类型
                    type = array.getInt(R.styleable.RoundImageView_imageType, TYPE_CIRCLE);
                    break;
            }
        }
        // Give back a previously retrieved StyledAttributes, for later re-use.
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 如果是圆形，则强制宽高一致，以最小的值为准
        if (type == TYPE_CIRCLE) {
            mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
            mRadius = mWidth / 2;
            setMeasuredDimension(mWidth, mWidth);
        }
        mDrawableRect.set(0,0,getWidth(),getHeight());

    }
    String textString = "疝气灯";
    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }
        // 设置渲染器
        setShader();
        if (type == TYPE_ROUND) {
            canvas.drawRoundRect(mRectF, mBorderRadius, mBorderRadius, mPaint);
        } else {
            canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);
        }

//        paint.setColor(0x1ca4c6);
//        RectF rectF = new RectF();
//        rectF.set(0,getMeasuredHeight()-BORDER_RADIUS_DEFAULT,getMeasuredWidth(),getMeasuredHeight());
//        canvas.drawRect(rectF,paint);
    }
    private int mBitmapWidth;
    private int mBitmapHeight;
    private final RectF mDrawableRect = new RectF();

    private void setShader() {
        float scale=1.0f;
        float dx = 0;
        float dy = 0;
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        if (type == TYPE_ROUND) {
            mMatrix.set(null);

            // shaeder的变换矩阵，我们这里主要用于放大或者缩小。
            mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mMatrix.postTranslate((int) (dx + 0.5f) + mDrawableRect.left, (int) (dy + 0.5f) + mDrawableRect.top);

            scale = Math.max(getWidth() * 1.0f / mBitmap.getWidth(), getHeight() * 1.0f / mBitmap.getHeight());
            LogUtil.e("scale="+scale);
        } else if (type == TYPE_CIRCLE) {
            // 取小值，如果取大值的话，则不能覆盖view
            int bitmapWidth = Math.min(mBitmap.getWidth(), getHeight());
            scale = mWidth * 1.0f / bitmapWidth;
        }
        mMatrix.setScale(scale, scale);
        mBitmapShader.setLocalMatrix(mMatrix);
        mPaint.setShader(mBitmapShader);
    }

    /**
     * 将Drawable转化为Bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        // 创建画布
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectF = new RectF(0, 0, getWidth(), getHeight());
    }

    /**
     * 对外公布的设置borderRadius方法
     *
     * @param borderRadius
     */
    public void setBorderRadius(int borderRadius) {
        int pxValue = dp2px(borderRadius);
        if (this.mBorderRadius != pxValue) {
            this.mBorderRadius = pxValue;
            // 这时候不需要父布局的onLayout,所以只需要调用onDraw即可
            invalidate();
        }
    }

    /**
     * 对外公布的设置形状的方法
     *
     * @param type
     */
    public void setType(int type) {
        if (this.type != type) {
            this.type = type;
            if (this.type != TYPE_CIRCLE && this.type != TYPE_ROUND) {
                this.type = TYPE_CIRCLE;
            }
            // 这个时候改变形状了，就需要调用父布局的onLayout，那么此view的onMeasure方法也会被调用
            requestLayout();
        }
    }

    /**
     * dp2px
     */
    public int dp2px(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }

    @Override
    public void setImageBitmap(Bitmap bm)
    {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable)
    {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();

    }

    @Override
    public void setImageResource(int resId)
    {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();

    }

    @Override
    public void setImageURI(Uri uri)
    {
        super.setImageURI(uri);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    private void setup() {
        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable)
    {
        if (drawable == null)
        {
            return null;
        }

        if (drawable instanceof BitmapDrawable)
        {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try
        {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable)
            {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            }
            else
            {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e)
        {
            return null;
        }
    }
}
