package com.voxelbusters.nativeplugins.features.webview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.RectF;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.voxelbusters.NativeBinding;
import com.voxelbusters.androidnativeplugin.R;
import com.voxelbusters.nativeplugins.base.interfaces.IAppLifeCycleListener;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;

import java.lang.reflect.Method;

public class NativeWebViewFrame extends FrameLayout implements IAppLifeCycleListener
{

	NativeWebViewToolBar	toolBar;

	WebView					webView;

	LinearLayout			progressViewLayout;

	ImageButton				closeButton;

	RectF					rect	= new RectF();

	public NativeWebViewFrame(Context context)
	{
		super(context);

		setUpLayout(context);
	}

	void setUpLayout(Context context)
	{
		FrameLayout.LayoutParams frameLayoutparams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.TOP | Gravity.LEFT);

		setLayoutParams(frameLayoutparams);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.np_webview_layout, this);

		getReferences();
		addProgressBar();

	}

	void getReferences()
	{

		toolBar = new NativeWebViewToolBar((LinearLayout) findViewById(R.id.np_topbar_layout), (ImageButton) findViewById(R.id.np_toolbar_back), (ImageButton) findViewById(R.id.np_toolbar_forward), (ImageButton) findViewById(R.id.np_toolbar_reload), (ImageButton) findViewById(R.id.np_toolbar_close));

		closeButton = (ImageButton) findViewById(R.id.np_webview_closebutton);

		webView = (WebView) findViewById(R.id.np_webview);

	}

	void addProgressBar()
	{

		//Add as child to webview. So, we can have close button on top.
		View view = LayoutInflater.from(getContext()).inflate(R.layout.np_progressbar_layout, (WebView) findViewById(R.id.np_webview), true);

		progressViewLayout = (LinearLayout) view.findViewById(R.id.np_progressbar_root);
		progressViewLayout.setBackgroundColor(getContext().getResources().getColor(R.color.np_semi_transparent));

		hideProgressSpinner();

	}

	public WebView getWebView()
	{
		return webView;
	}

	public NativeWebViewToolBar getToolBar()
	{
		return toolBar;
	}

	public ImageButton getCloseButton()
	{
		return closeButton;
	}

	public void setCloseButtonListener(android.view.View.OnClickListener listener)
	{
		closeButton.setOnClickListener(listener);
	}

	public void setFrame(RectF frame)
	{

		rect = frame;
		adjustLayout();

	}

	public void setFrame(float x, float y, float width, float height)
	{

		rect.left = x;
		rect.top = y;
		rect.right = x + width;
		rect.bottom = y + height;

		adjustLayout();

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		ViewTreeObserver observer = getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
			{

				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout()
				{
					adjustLayout();

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
					{
						NativeWebViewFrame.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
					else
					{
						NativeWebViewFrame.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			});
	}

	private void adjustLayout()
	{
		int parentViewWidth = ((View) getParent()).getWidth();
		int parentViewHeight = ((View) getParent()).getHeight();

		int width = (int) (rect.width() * parentViewWidth);
		int height = (int) (rect.height() * parentViewHeight);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height, Gravity.TOP | Gravity.LEFT);

		params.leftMargin = (int) (rect.left * parentViewWidth);
		params.topMargin = (int) (rect.top * parentViewHeight);

		setLayoutParams(params);

		this.invalidate();

	}

	public void showProgressSpinner()
	{
		progressViewLayout.setVisibility(View.VISIBLE);
	}

	public void hideProgressSpinner()
	{
		progressViewLayout.setVisibility(View.GONE);
	}

	public void showCloseButton()
	{
		closeButton.setVisibility(View.VISIBLE);
	}

	public void hideCloseButton()
	{
		closeButton.setVisibility(View.GONE);
	}

	public void showNow()
	{
		NativeBinding.addAppLifeCycleListener(this);
		setVisibility(VISIBLE);
	}

	public void hide()
	{
		NativeBinding.removeAppLifeCycleListener(this);
		setVisibility(GONE);
	}

	public void dismissAndDestroy()
	{
		if (webView != null)
		{
			hide();
			webView.destroy();
		}
	}

	protected void callInternalWebViewMethod(final String name)
	{
		Activity activity = (Activity) getContext();

		activity.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					if (webView != null)
					{
						try
						{
							Method method = WebView.class.getDeclaredMethod(name);
							method.setAccessible(true);
							method.invoke(webView);
						}
						catch (Exception e)
						{
							Debug.error(CommonDefines.WEB_VIEW_TAG, "Could not find method " + name);
						}
					}

				}

			});

	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility)
	{
		super.onVisibilityChanged(changedView, visibility);

		if (visibility == View.VISIBLE)
		{
			resumeWebView();
		}
		else
		{
			pauseWebView();
		}

	}

	@Override
	public void onApplicationPause()
	{
		pauseWebView();
	}

	@Override
	public void onApplicationResume()
	{
		resumeWebView();
	}

	@Override
	public void onApplicationQuit()
	{
		dismissAndDestroy();
	}

	void pauseWebView()
	{
		callInternalWebViewMethod("onPause");
	}

	void resumeWebView()
	{
		callInternalWebViewMethod("onResume");
	}

}
