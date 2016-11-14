package views;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

import com.suntrans.smartfire.R;

public class InputBox extends EditText{
	

	private final static String TAG = "EditTextWithDel";
    private Drawable imgSearch;
    private Drawable imgAble;
    private Context mContext;

    public InputBox(Context context) {
            super(context);
            mContext = context;
            init();
    }

    public InputBox(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            mContext = context;
            init();
    }

    public InputBox(Context context, AttributeSet attrs) {
            super(context, attrs);
            mContext = context;
            init();
    }
    
    private void init() {
            imgSearch = mContext.getResources().getDrawable(R.drawable.ic_search);
            imgAble = mContext.getResources().getDrawable(R.drawable.ic_clear);
            addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                          setDrawable();
                    }
            });
            setDrawable();
    }
    
    //设置删除图片
    private void setDrawable() {
            if(length() < 1)
                    setCompoundDrawablesWithIntrinsicBounds(imgSearch, null, null, null);
            else
                    setCompoundDrawablesWithIntrinsicBounds(imgSearch, null, imgAble, null);
    }
    
     // 处理删除事件
@Override
public boolean onTouchEvent(MotionEvent event) {
    if (imgAble != null && event.getAction() == MotionEvent.ACTION_UP) {
        int eventX = (int) event.getRawX();   //相对于手机屏幕左上角的x坐标值  getX相对于触摸view左上角的x坐标
        int eventY = (int) event.getRawY();   //相对于屏幕左上角的y坐标值
        Log.e(TAG, "eventX = " + eventX + "; eventY = " + eventY);
        Rect rect = new Rect();
        getGlobalVisibleRect(rect);    //获取套住该view的矩形框
        rect.left = rect.right - 50;   //重新设置矩形框的大小，这样矩形框的大小就刚好套住删除图标
        if(rect.contains(eventX, eventY))     //如果触摸的位置是自定义的删除图片，则将editview中的文字清空
                setText("");
    }
    return super.onTouchEvent(event);
}

@Override
protected void finalize() throws Throwable {
    super.finalize();
}



}
