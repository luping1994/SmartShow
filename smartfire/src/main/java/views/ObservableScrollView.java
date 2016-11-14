package views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {

	 private ScrollViewListener scrollViewListener = null;  
	   
	     public ObservableScrollView(Context context) {  
	         super(context);  
	     }  
	   
	     public ObservableScrollView(Context context, AttributeSet attrs,  
	             int defStyle) {  
	         super(context, attrs, defStyle);  
	     }  
	   
	     public ObservableScrollView(Context context, AttributeSet attrs) {  
	         super(context, attrs);  
	     }  
	     @Override
	     public boolean onInterceptTouchEvent(MotionEvent ev)
	     {
	    	return false;   //返回false表示将该触摸事件传递给子控件 
	     }
	   
	     public void setScrollViewListener(ScrollViewListener scrollViewListener) {  
	         this.scrollViewListener = scrollViewListener;  
	     }  
	   
	     @Override  
	     protected void onScrollChanged(int x, int y, int oldx, int oldy) {  
	         super.onScrollChanged(x, y, oldx, oldy);  
	         if (scrollViewListener != null) {  
	             scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);  
	         }  
	     }  

}
