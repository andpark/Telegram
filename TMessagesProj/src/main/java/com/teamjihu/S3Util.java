package com.teamjihu;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class S3Util {
	

	static ProgressDialog searchProgressDialog = null;
	public static void ShowProgressDialog(Context context, int progressDialogResId, boolean bShow) {
		if(bShow) {
			if(searchProgressDialog != null) {
				searchProgressDialog.dismiss();
				searchProgressDialog = null;
			}
			searchProgressDialog = new ProgressDialog(context);
			searchProgressDialog.setCancelable(false);
			searchProgressDialog.show();
			searchProgressDialog.setContentView(progressDialogResId);
		} else {
			if(searchProgressDialog != null) {
				searchProgressDialog.dismiss();
				searchProgressDialog = null;
			}
		}
	}
	
	public static Bundle JSONObjectToBundle(JSONObject obj) {
		Bundle b = new Bundle();
		Iterator<String> iter = obj.keys();
	    while (iter.hasNext()) {
	        String key = iter.next();
	        try {
	            Object v= obj.get(key);
	            if(v instanceof Integer) {
	            	b.putInt(key, (Integer)v);
	            } else if(v instanceof Boolean) {
	            	b.putBoolean(key, (Boolean)v);
	            } else if(v instanceof String) {
	            	b.putString(key, (String)v);
	            }
	        } catch (JSONException e) {
	        }
	    }
		return b;
	}
	
	public static float px2dp(Context context, float px) {
		float density = context.getResources().getDisplayMetrics().density;
		return px / density;
	}
	public static float dp2px(Context context, float dp) {
		float density = context.getResources().getDisplayMetrics().density;
		return dp * density;
	}
	public static void setViewWidthHeight(View v, int width, int height) {
		ViewGroup.LayoutParams lParam = v.getLayoutParams();
		lParam.height = height;
		lParam.width = width;
	}
	public static void setViewWidth(View v, int width) {
        v.getLayoutParams().width = width;
	}
	public static void setViewHeight(View v, int height) {
        v.getLayoutParams().height = height;
	}
	
	public static void setTextViewAlpha(TextView v, int alpha) {
		int color = v.getCurrentTextColor();
		int newColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
		v.setTextColor(newColor);
	}
	
	public static void setViewMarginLeft(View v, int left, boolean requestLayout) {
		ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		lParam.leftMargin = left;
		if(requestLayout)
			v.requestLayout();
	}
	public static int getViewMarginLeft(View v) {
		return ((ViewGroup.MarginLayoutParams)v.getLayoutParams()).leftMargin;
	}
	public static int getViewMarginRight(View v) {
		ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		return lParam.rightMargin;
	}
	public static int getViewMarginBottom(View v) {
		ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		return lParam.bottomMargin;
	}
	public static void setViewMarginRight(View v, int right, boolean requestLayout) {
		ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		lParam.rightMargin = right;
		if(requestLayout)
			v.requestLayout();
	}
	public static void setViewMarginTop(View v, int top, boolean requestLayout) {
		ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		lParam.topMargin = top;
		if(requestLayout)
			v.requestLayout();
	}
	public static void setViewMarginBottom(View v, int bottom, boolean requestLayout) {
		ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		lParam.bottomMargin = bottom;
		if(requestLayout)
			v.requestLayout();
	}
	public static ValueAnimator startValueAnimator(int from, int to, ValueAnimator.AnimatorUpdateListener updateListener, ValueAnimator.AnimatorListener animatorListener, int duration) {
		ValueAnimator ani = ValueAnimator.ofInt(from, to);
		if(updateListener != null)
			ani.addUpdateListener(updateListener);
		if(animatorListener != null)
			ani.addListener(animatorListener);
		ani.setDuration(duration);
		ani.start();	
		return ani;
	}
	public static void swipeView(final View v, int toMarginLeft, ValueAnimator.AnimatorListener animatorListener) {
		final ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		ValueAnimator ani = ValueAnimator.ofInt(lParam.leftMargin, toMarginLeft);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int toValue = (Integer) animation.getAnimatedValue();
				lParam.leftMargin = toValue;
				v.requestLayout();
			}
		});
		if(animatorListener != null)
			ani.addListener(animatorListener);
		ani.setDuration(300);
		ani.start();
	}
	
	public static void swipeView(final View v, int toMarginLeft, ValueAnimator.AnimatorListener animatorListener, int duration) {
		final ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		ValueAnimator ani = ValueAnimator.ofInt(lParam.leftMargin, toMarginLeft);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int toValue = (Integer) animation.getAnimatedValue();
				lParam.leftMargin = toValue;
				v.requestLayout();
			}
		});
		if(animatorListener != null)
			ani.addListener(animatorListener);
		ani.setDuration(duration);
		ani.start();
	}
	public static void swipeUpView(final View v, int toMarginBottom, ValueAnimator.AnimatorListener animatorListener) {
		final ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		ValueAnimator ani = ValueAnimator.ofInt(lParam.bottomMargin, toMarginBottom);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int toValue = (Integer) animation.getAnimatedValue();
				lParam.bottomMargin = toValue;
				v.requestLayout();
			}
		});
		if(animatorListener != null)
			ani.addListener(animatorListener);
		ani.setDuration(300);
		ani.start();
	}
	public static void swipeDownView(final View v, int toMarginBottom, ValueAnimator.AnimatorListener animatorListener) {
		final ViewGroup.MarginLayoutParams lParam = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
		ValueAnimator ani = ValueAnimator.ofInt(lParam.topMargin, toMarginBottom);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int toValue = (Integer) animation.getAnimatedValue();
				lParam.topMargin = toValue;
				v.requestLayout();
			}
		});
		if(animatorListener != null)
			ani.addListener(animatorListener);
		ani.setDuration(300);
		ani.start();
	}
	public static void de_collapseHeight(final View v, int toHeight, int duration, ValueAnimator.AnimatorListener animatorListener) {
		final ViewGroup.LayoutParams lParam = v.getLayoutParams();

		ValueAnimator ani = ValueAnimator.ofInt(0, toHeight);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int toValue = (Integer) animation.getAnimatedValue();
				lParam.height = toValue;
				v.requestLayout();
			}
		});
		if(animatorListener != null)
			ani.addListener(animatorListener);
		ani.setDuration(duration);
		ani.start();
	}
	public static void collapseHeight(final View v, int duration, ValueAnimator.AnimatorListener animatorListener) {
		int curHeight = v.getHeight();
		
		final ViewGroup.LayoutParams lParam = v.getLayoutParams();

		ValueAnimator ani = ValueAnimator.ofInt(curHeight, 0);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int toValue = (Integer) animation.getAnimatedValue();
				lParam.height = toValue;
				v.requestLayout();
			}
		});
		if(animatorListener != null)
			ani.addListener(animatorListener);
		ani.setDuration(duration);
		ani.start();
	}
	public static void collapseWidth(final View v, int duration, ValueAnimator.AnimatorListener animatorListener) {
		int curWidth = v.getWidth();
		
		final ViewGroup.LayoutParams lParam = v.getLayoutParams();

		ValueAnimator ani = ValueAnimator.ofInt(curWidth, 0);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int toValue = (Integer) animation.getAnimatedValue();
				lParam.width = toValue;
				v.requestLayout();
			}
		});
		if(animatorListener != null)
			ani.addListener(animatorListener);
		ani.setDuration(duration);
		ani.start();
	}
	
	public static String toCommaNumber(int number) {
		return (new DecimalFormat("#,###,###,###")).format(number);
	}
	
	public static byte[] loadFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);
 
	    long length = file.length();
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }
	    byte[] bytes = new byte[(int)length];
	    
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }
 
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }
 
	    is.close();
	    return bytes;
	}
	
	public static void sharePhoto(Context context, String filePath, String text) {
		Uri photoUri = Uri.parse(filePath);
		
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("image/*");
		i.putExtra(Intent.EXTRA_STREAM, photoUri);
		i.putExtra(Intent.EXTRA_TEXT, text);
//		i.putExtra(Intent.EXTRA_TITLE, text);
//		i.putExtra(Intent.EXTRA_SUBJECT, text);
		context.startActivity(Intent.createChooser(i, "Share Image"));		
	}
	
	public static void shareToEmail(Context context, String filePath, String text) {
		Uri photoUri = Uri.parse(filePath);

		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","", null));
		//emailIntent.setType("image/*");
		emailIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, text);
        context.startActivity(Intent.createChooser(emailIntent, "Send email"));
	}
	
	public static void HideKeyboard(Activity context) {
		InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
	    View v=context.getCurrentFocus();
	    if(v == null)
	        return;
	    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	public static void ShowKeyboard(final EditText editText) {
		editText.postDelayed(new Runnable() {
            public void run() {
            	editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
            	editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));                       
            }
        }, 200);	
	}
	
	public static String toReadableDate(Calendar c, String format) {
		Date d = new Date(c.getTimeInMillis());
		return toReadableDate(d, format);
	}
	
	public static String toReadableDate(Date d, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
		String formattedDate = df.format(d.getTime());
		return formattedDate;	
	}
	// GET Ref
	public static int getPreference(Context context, String prefName, String name, int defaultValue) {
		SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return prefs.getInt(name, defaultValue);
	}
	public static String getPreference(Context context, String prefName, String name, String defaultValue) {
		SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return prefs.getString(name, defaultValue);
	}
	public static void setPreference(Context context, String prefName, String name, int data) {
		SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(name, data);
        editor.commit();
	}
	public static void setPreference(Context context, String prefName, String name, String data) {
		SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
        editor.putString(name, data);
        editor.commit();
	}
	
	public static void removePreference(Context context, String prefName , String key) {
		SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.commit();
	}
	
}
