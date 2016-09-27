package com.suntrans.smartshow.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.suntrans.smartshow.R;


//自定义仿IOS开关按钮控件
//this.getWidth()==bg_on.getWidth()
public class Switch extends ImageView {
	// 命名空间的值
	private final String namespace = "http://schemas.android.com/apk/res-auto";
	private boolean state=true;   //开关是打开还是关闭的状态
	private Bitmap bg_on;   //开关打开时的背景图片
	private Bitmap bg_off;  //开关关闭时 的背景图片
	private Bitmap btn_unpressed;   //没有按下时的开关按钮
	private Bitmap btn_pressed;   //被按下时的开关按钮
	private int mMoveDeltX = 0;   // Switch 移动的偏移量
	private int paddingleft=0;   //按钮的左内边距
	private float mLastX = 0;    // 第一次按下的有效区域 
	private float mCurrentX = 0;     //触摸点相对于本view的X轴坐标
	private int Max_X = 0;   // Switch X轴可以移动的最大偏移量
	private boolean mEnabled = true;   // enabled 属性 为 true 
	
	private OnSwitchChangedListener switchListener = null;  /** Switch 状态监听接口  */
	private boolean mFlag = false;
	private final int MAX_ALPHA = 255;//最大透明度，就是不透明 
	
	private int mAlpha = MAX_ALPHA;   //当前透明度，这里主要用于如果控件的enable属性为false时候设置半透明 ，即不可以点击 */
	
	private boolean mIsScrolled =false;    // Switch 判断是否在拖动 
	
	public Switch(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public Switch(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);  
	        // TODO Auto-generated constructor stub  
	}  
	  
	public Switch(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);  
	       // TODO Auto-generated constructor stub  	
	    init(attrs);   //初始化开关
	}  
	public void init(AttributeSet attrs){
		bg_on = BitmapFactory.decodeResource(getResources(), R.drawable.skin_switch_track_activited);
        bg_off = BitmapFactory.decodeResource(getResources(),R.drawable.skin_switch_track);
        btn_unpressed = BitmapFactory.decodeResource(getResources(),R.drawable.skin_switch_thumb_activited);
        btn_pressed = BitmapFactory.decodeResource(getResources(),R.drawable.skin_switch_thumb_activited_pressed);
        Max_X=bg_on.getWidth()-btn_pressed.getWidth();    //计算按钮可以移动的最大距离   
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(lp);    //设置属性
        state= attrs.getAttributeBooleanValue(namespace, "state", false);   //设置开关初始状态   从布局文件获取开关状态
        paddingleft=Max_X;
        this.setScaleType(ScaleType.FIT_START);       //设置按钮偏左显示，这样通过设置paddingLeft就可以改变按钮位置
        if(state)    //根据开关状态初始化开关显示   开
        {
	        this.setPadding(paddingleft, 0, 0, 0);   //设置内边距   通过设置paddingLeft来移动按钮   实现开关显示
	        this.setImageBitmap(btn_unpressed);       
	        this.setBackgroundResource(R.drawable.skin_switch_track_activited);
        }
        else       //关
        {
        	this.setPadding(0, 0, 0, 0);      //设置按钮偏左显示
			this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片						
			this.setBackgroundResource(R.drawable.skin_switch_track);  //设置背景为白色
        }
        
	}
	@Override
	public void onDraw(Canvas canvas)   //重写onDraw方法
	{
		// TODO Auto-generated method stub  
        super.onDraw(canvas); 
        
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		//如果Enabled属性设定为true,触摸效果才有效
		if(!mEnabled){
			return true;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.setImageBitmap(btn_pressed);		
			 Log.d("EVENT","ACTION_DOWN");
			mLastX = event.getX();    //获取点击位置X轴相对坐标
			break;
		case MotionEvent.ACTION_MOVE:
			 Log.d("EVENT","ACTION_MOVE");
			mCurrentX = event.getX();
			mMoveDeltX = (int) (mCurrentX - mLastX);
			 Log.d("EVENT", String.valueOf(mMoveDeltX));
			if((mMoveDeltX > 5) || (mMoveDeltX<-5)){
				//设置了2这个误差距离，可以更好的实现点击效果
				mIsScrolled = true;
			}
			/*mMoveDeltX = (int) (mCurrentX - mLastX);
			mLastX=mCurrentX;    //记录这次触摸点x坐标
			if(mMoveDeltX > 10){
				//设置了10这个误差距离，可以更好的实现点击效果
				mIsScrolled = true;
			}*/
			// 如果开关开着向右滑动，或者开关关着向左滑动（这时候是不需要处理的）
			/*if ((state && mMoveDeltX > 0) || (!state && mMoveDeltX < 0)) {
				mFlag = true;
				mMoveDeltX = 0;
			}*/
           /* //如果开关移动的距离超过了最大移动距离，则设置为最大距离
			if (Math.abs(mMoveDeltX) > Max_X) {
				mMoveDeltX = mMoveDeltX > 0 ? Max_X : -Max_X;
			}*/
			this.setImageBitmap(btn_pressed);
			
			paddingleft=(int)mCurrentX;   //设置滑动后左内边距   实现按钮移动
			
			if(paddingleft<0)    //设置paddingLeft在合适的范围内
				paddingleft=0;
			if(paddingleft>Max_X)
				paddingleft=Max_X;
			this.setPadding(paddingleft, 0, 0, 0);   //设置内边距   通过设置paddingLeft来移动按钮
			
			//invalidate();    //刷新显示
			break;
		case MotionEvent.ACTION_UP:    //按键弹起逻辑
		{
			//如果没有滑动过，就看作一次点击事件
			if(!mIsScrolled){
				int eventX = (int) event.getX();   // 手抬起时触摸点相对于该view左上角的x坐标
			    int eventY = (int) event.getY();   // 手抬起时触摸点相对于该view左上角的y坐标
			    this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片				   
			    Log.d("EVENT","ACTION_UP");
			    Log.d("EVENT", String.valueOf(event.getX()));
			    if(eventX>0&&eventY>0&&eventX<=bg_on.getWidth()&&eventY<=bg_on.getHeight())   //如果手抬起时还在按钮区域
			    {
					state=!state;			
					if (switchListener != null) {
						switchListener.onSwitchChange(this, state);    //调用监听按钮变化函数
					}
					if(state==false)     //如果开关状态是关
					{		
							//paddingleft=0;     //设置按钮偏左显示
							this.setPadding(0, 0, 0, 0);      //设置按钮偏左显示
							this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片						
							this.setBackgroundResource(R.drawable.skin_switch_track);  //设置背景为白色
					}
					else     //如果开关状态是开
					{
							//paddingleft=Max_X;     //设置按钮偏右显示
							this.setPadding(Max_X, 0, 0, 0);      //设置按钮偏右显示
							this.setImageBitmap(btn_unpressed);      //按钮为未按下的图片						
							this.setBackgroundResource(R.drawable.skin_switch_track_activited);   //设置背景为绿色
					}
			    }
			    else
			    {
			    	if(state==false)     //如果开关状态是关
					{		
							//paddingleft=0;     //设置按钮偏左显示
							this.setPadding(0, 0, 0, 0);      //设置按钮偏左显示
							this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片						
							this.setBackgroundResource(R.drawable.skin_switch_track);  //设置背景为白色
					}
					else     //如果开关状态是开
					{
							//paddingleft=Max_X;     //设置按钮偏右显示
							this.setPadding(Max_X, 0, 0, 0);      //设置按钮偏右显示
							this.setImageBitmap(btn_unpressed);      //按钮为未按下的图片						
							this.setBackgroundResource(R.drawable.skin_switch_track_activited);   //设置背景为绿色
					}
			    }
			   
			    	
				break;
			}
			else    //如果发生了滑动事件，就判断按钮的位置，来改变开关的状态
			{
				Log.d("EVENT","ACTION_UP");
			    mIsScrolled = false;
			    paddingleft=this.getPaddingLeft();
			   // paddingleft=(int)event.getX();
			    this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片
			    if(paddingleft<Max_X/2.0)   //如果按钮偏左，则开关变为关
			    {
			    	Log.d("EVENT","Left");
			    	this.setPadding(0, 0, 0, 0);      //设置按钮偏左显示
					this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片						
					this.setBackgroundResource(R.drawable.skin_switch_track);  //设置背景为白色
			    	if(state)   //如果原来是开，即状态发生了改变
			    	{			    	
			    		state=!state;   //开关状态改为关
			    		if (switchListener != null) {
							switchListener.onSwitchChange(this, state);    //调用监听按钮变化函数
			    		}
			    	} 
			    						
			    }
			    else       //按钮偏右，则开关变为开
			    {
			    	Log.d("EVENT","Right");
			    	Log.d("EVENT", String.valueOf(event.getX()));
			    	this.setPadding(Max_X, 0, 0, 0);      //设置按钮偏右显示
					this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片						
					this.setBackgroundResource(R.drawable.skin_switch_track_activited);  //设置背景为白色
			    	if(!state)    //如果原来的状态时关，即状态发生了改变
			    	{			    	
			    		state=!state;   //开关状态改为开
			    		if (switchListener != null) {
							switchListener.onSwitchChange(this, state);    //调用监听按钮变化函数
			    		}
			    	} 
			    }		    	
			   
			    	
				break;
				}
		}
			/*//如果没有滑动过，就看作一次点击事件
			if(!mIsScrolled){
				mMoveDeltX = mSwitchOn ? mMoveLength : -mMoveLength;
				mSwitchOn = !mSwitchOn;
				if (switchListener != null) {
					switchListener.onSwitchChange(this, mSwitchOn);
				}
				invalidate();
				mMoveDeltX = 0;
				break;
			}
			mIsScrolled = false;
			if (Math.abs(mMoveDeltX) > 0 && Math.abs(mMoveDeltX) < mMoveLength / 2) {
				mMoveDeltX = 0;
				invalidate();
			} else if (Math.abs(mMoveDeltX) > mMoveLength / 2
					&& Math.abs(mMoveDeltX) <= mMoveLength) {
				mMoveDeltX = mMoveDeltX > 0 ? mMoveLength : -mMoveLength;
				mSwitchOn = !mSwitchOn;
				if (switchListener != null) {
					switchListener.onSwitchChange(this, mSwitchOn);
				}
				invalidate();
				mMoveDeltX = 0;
			} else if (mMoveDeltX == 0 && mFlag) {
				// 这时候得到的是不需要进行处理的，因为已经move过了
				mMoveDeltX = 0;
				mFlag = false;
			}*/
		/*	case MotionEvent.ACTION_CANCEL:    //也是按键弹起逻辑
			{
				int eventX = (int) event.getX();   // 手抬起时触摸点相对于该view左上角的x坐标
			    int eventY = (int) event.getY();   // 手抬起时触摸点相对于该view左上角的y坐标
			    this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片
			    mIsScrolled = false;
			    Log.d("EVENT","ACTION_CANCEL");
			    
			    if(eventX>0&&eventY>0&&eventX<=bg_on.getWidth()&&eventY<=bg_on.getHeight())   //如果手抬起时还在按钮区域
			    {
					state=!state;			
					if(state==false)     //如果开关状态是关
					{		
							//paddingleft=0;     //设置按钮偏左显示
							this.setPadding(0, 0, 0, 0);      //设置按钮偏左显示
							this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片						
							this.setBackgroundResource(R.drawable.skin_switch_track);  //设置背景为白色
					}
					else     //如果开关状态是开
					{
							//paddingleft=Max_X;     //设置按钮偏右显示
							this.setPadding(Max_X, 0, 0, 0);      //设置按钮偏右显示
							this.setImageBitmap(btn_unpressed);      //按钮为未按下的图片						
							this.setBackgroundResource(R.drawable.skin_switch_track_activited);   //设置背景为绿色
					}
			    }
			   
			    	
				break;
			}*/
		default:
			Log.d("EVENT","DEFAULT");
		    mIsScrolled = false;
		    paddingleft=this.getPaddingLeft();
		   // paddingleft=(int)event.getX();
		    this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片
		    if(paddingleft<Max_X/2.0)   //如果按钮偏左，则开关变为关
		    {
		    	Log.d("EVENT","Left");
		    	this.setPadding(0, 0, 0, 0);      //设置按钮偏左显示
				this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片						
				this.setBackgroundResource(R.drawable.skin_switch_track);  //设置背景为白色
		    	if(state)   //如果原来是开，即状态发生了改变
		    	{			    	
		    		state=!state;   //开关状态改为关
		    		if (switchListener != null) {
						switchListener.onSwitchChange(this, state);    //调用监听按钮变化函数
		    		}
		    	} 
		    						
		    }
		    else       //按钮偏右，则开关变为开
		    {
		    	Log.d("EVENT","DEFAULT");
		    	
		    	this.setPadding(Max_X, 0, 0, 0);      //设置按钮偏右显示
				this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片						
				this.setBackgroundResource(R.drawable.skin_switch_track_activited);  //设置背景为白色
		    	if(!state)    //如果原来的状态时关，即状态发生了改变
		    	{			    	
		    		state=!state;   //开关状态改为开
		    		if (switchListener != null) {
						switchListener.onSwitchChange(this, state);    //调用监听按钮变化函数
		    		}
		    	} 
		    }		    	
		   
		    	
			
			break;
		}
		invalidate();
		return true;
	}
	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		mEnabled = enabled;
		mAlpha = enabled ? MAX_ALPHA : MAX_ALPHA/2;
		//Log.d("enabled",enabled ? "true": "false");
		super.setEnabled(enabled);
		invalidate();
	}

	//设置开关状态
	public void setState(boolean ss) {
		// TODO Auto-generated method stub
		if(state!=ss)
		{
			state=ss;           //设置开关状态
			//if (switchListener != null) {
			//switchListener.onSwitchChange(this, state);    //调用监听按钮变化函数
			//}
		}
				
		if(state==false)     //如果开关状态是关
		{		
				//paddingleft=0;     //设置按钮偏左显示
				this.setPadding(0, 0, 0, 0);      //设置按钮偏左显示
				this.setImageBitmap(btn_unpressed);    //按钮为未按下的图片						
				this.setBackgroundResource(R.drawable.skin_switch_track);  //设置背景为白色
		}
		else     //如果开关状态是开
		{
				//paddingleft=Max_X;     //设置按钮偏右显示
				this.setPadding(Max_X, 0, 0, 0);      //设置按钮偏右显示
				this.setImageBitmap(btn_unpressed);      //按钮为未按下的图片						
				this.setBackgroundResource(R.drawable.skin_switch_track_activited);   //设置背景为绿色
		}
		invalidate();
	}
	
	public boolean getState()
	{
		return state;
	}
	
	/** 
	 * 设置 switch 状态监听 
	 * */
	public void setOnChangeListener(OnSwitchChangedListener listener) {
		switchListener = listener;
	}
	/** 
	 * switch 开关监听接口
	 *  */
	public interface OnSwitchChangedListener{
		public void onSwitchChange(Switch switchView, boolean isChecked);
	}
}
