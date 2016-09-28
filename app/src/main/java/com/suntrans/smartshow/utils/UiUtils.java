package com.suntrans.smartshow.utils;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.suntrans.smartshow.base.BaseApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class UiUtils {

	private static Toast mToast;

	public static void showToast(Context context, String str) {
		if (mToast == null) {
			mToast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
		}
		mToast.setText(str);
		mToast.show();
	}

	/**
	 * 获取到字符数组
	 *
	 * @param tabNames 字符数组的id
	 */
	public static String[] getStringArray(int tabNames) {
		return getResource1().getStringArray(tabNames);
	}

	public static Resources getResource1() {
		return BaseApplication.getApplication().getResources();
	}

	/**
	 * dip转换px
	 */
	public static int dip2px(int dip) {
		final float scale = getResource1().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}

	/**
	 * dip转换px
	 */
	public static int dip2px(int dip,Context context) {
		final float scale =context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}

	/**
	 * pxz转换dip
	 */

	public static int px2dip(int px) {
		final float scale = getResource1().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	public static Context getContext() {
		return BaseApplication.getApplication();
	}

	//	public static void runOnUiThread(Runnable runnable) {
//		// 在主线程运行
//		if(android.os.Process.myTid()==BaseApplication.getMainTid()){
//			runnable.run();
//		}else{
//			//获取handler
//			BaseApplication.getHandler().post(runnable);
//		}
//	}
	public static void runOnUiThread(Runnable runnable) {
		if (android.os.Process.myTid() == BaseApplication.getMainTid()) {
			runnable.run();
		} else {
			BaseApplication.getHandler().post(runnable);
		}
	}


	/**
	 * 加载view
	 *
	 * @param layoutId
	 * @return
	 */
	public static View inflate(int layoutId) {
		return View.inflate(getContext(), layoutId, null);
	}

	public static int getDimens(int homePictureHeight) {
		return (int) getResource1().getDimension(homePictureHeight);
	}

	/**
	 * 检查网络是否可用
	 */
	public static boolean isNetworkAvailable() {
		Context context = UiUtils.getContext();
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
//					System.out.println(i + "===状态===" + networkInfo[i].getState());
//					System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
					// 判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static int getDisPlayWidth() {
		WindowManager wm = (WindowManager) UiUtils.getContext().getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay().getWidth();
	}


	public static Uri savePic(final Context context, final String url, final int index) {

		final File[] imgFile = new File[1];
//				Bitmap bitmap = ((BitmapDrawable) icon.getDrawable()).getBitmap();
		Target target = new Target() {
			@Override
			public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					File file = new File(Environment.getExternalStorageDirectory().getPath(), "pic");
					if (!file.exists()) {
						file.mkdirs();
					}
					imgFile[0] = new File(file.getAbsolutePath(), index + ".jpg");
					FileOutputStream outStream = null;
					try {
						outStream = new FileOutputStream(imgFile[0]);
						bitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream);
						outStream.flush();
						outStream.close();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}


			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {

			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {

			}
		};
		Picasso.with(context)
				.load(url)
				.into(target);
		Uri uri = Uri.fromFile(imgFile[0]);
		Intent mIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
		context.sendBroadcast(mIntent);
		return uri;
	}

	public static Observable savaPicAndShare(final Context context , final String url , final String title){

		return Observable.create(new Observable.OnSubscribe<Bitmap>() {

			@Override
			public void call(Subscriber<? super Bitmap> subscriber) {
					Bitmap bitmap = null;
				try {
					bitmap = Picasso.with(context).load(url).get();
				} catch (IOException e) {
					e.printStackTrace();
					subscriber.onError(e);
				}
				if (bitmap == null){
					subscriber.onError(new Exception("下载的图片为空"));
				}
				subscriber.onNext(bitmap);
				subscriber.onCompleted();
			}
		}).flatMap(new Func1<Bitmap, Observable<?>>() {
			@Override
			public Observable<?> call(Bitmap bitmap) {
				File appDir = new File(Environment.getExternalStorageDirectory().getPath()
				,"pic");
				if (!appDir.exists()){
					appDir.mkdirs();
				}
				File file = new File(appDir.getAbsolutePath(), title.replace('/', '-') + ".jpg");
				try {
					FileOutputStream fos = new FileOutputStream(file);
					assert  bitmap !=null;
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Uri uri = Uri.fromFile(file);
				Intent intent  = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
				context.sendBroadcast(intent);

				return Observable.just(uri);
			}
		}).subscribeOn(Schedulers.io());

	};



}
