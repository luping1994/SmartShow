package views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Line extends View {

	public Line(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	 @Override
	 protected void onDraw(Canvas canvas) {
	//  super.onDraw(canvas);
	     Paint paint=new Paint();
	     paint.setColor(Color.WHITE);
	     DashPathEffect effect = new DashPathEffect(new float[] {6, 6}, 0);
	     paint.setPathEffect(effect);
	     paint.setStyle(Paint.Style.STROKE);
	     paint.setStrokeWidth(2);
	     canvas.drawLine(0, 0, 0, getHeight(), paint);
	        
	 }

}
