package views;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

//触摸监听
//如果是单独使用，要将返回值改为true   
//如果与OnClickListener一起用，则返回值为false
public class TouchListener implements OnTouchListener {

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		//按键按下
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		{
			v.setAlpha(0.5f);
			
		}
		//按键抬起
		else if(event.getAction()==MotionEvent.ACTION_UP)
		{
			v.setAlpha(1f);
		}
		return false;
	}

}
