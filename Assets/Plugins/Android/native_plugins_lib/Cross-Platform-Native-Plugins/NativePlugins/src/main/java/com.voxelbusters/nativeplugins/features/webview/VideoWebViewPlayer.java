package com.voxelbusters.nativeplugins.features.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.voxelbusters.androidnativeplugin.R;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.medialibrary.IVideoPlayBackListener;
import com.voxelbusters.nativeplugins.utilities.Debug;

import java.lang.reflect.Method;
import java.util.HashMap;

public class VideoWebViewPlayer extends WebView
{

	final String			ON_STATE_CHANGE			= "OnStateChange";
	final String			ON_READY				= "OnReady";
	final String			ON_ERROR				= "OnError";
	final String			VALUE_KEY				= "value";

	//States

	final String			VIDEO_STATE_ENDED		= "ENDED";
	final String			VIDEO_STATE_PLAYING		= "PLAYING";
	final String			VIDEO_STATE_PAUSED		= "PAUSED";
	final String			VIDEO_STATE_BUFFERING	= "BUFFERING";
	final String			VIDEO_STATE_CUED		= "CUED";
	final String			VIDEO_STATE_UNSTARTED	= "UNSTARTED";

	WebSettings				settings;
	IVideoPlayBackListener	listener;

	LinearLayout			progressBarLayout;

	public VideoWebViewPlayer(Context context)
	{
		super(context);

		initializeSettings();
	}

	public void setListener(IVideoPlayBackListener listener)
	{
		this.listener = listener;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void initializeSettings()
	{
		settings = getSettings();

		settings.setPluginState(PluginState.ON);
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setAllowFileAccess(true);
		settings.setDomStorageEnabled(true);

		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			settings.setMediaPlaybackRequiresUserGesture(false);
		}

		setInitialScale(1);

		setupWebViewClient();
		setupChromeClient();
	}

	void showProgressDialog()
	{
		if (progressBarLayout == null)
		{
			//Add as child to webview. So, we can have close button on top.
			View view = LayoutInflater.from(getContext()).inflate(R.layout.np_progressbar_layout, this, true);

			progressBarLayout = (LinearLayout) view.findViewById(R.id.np_progressbar_root);
			progressBarLayout.setBackgroundResource(R.color.np_semi_transparent);
		}

		progressBarLayout.setVisibility(View.VISIBLE);
	}

	void hideProgressDialog()
	{
		progressBarLayout.setVisibility(View.GONE);
	}

	public void loadVideoFromHtml(String htmlString)
	{
		loadDataWithBaseURL(null, htmlString, Keys.Mime.HTML_TEXT, "UTF-8", null);
	}

	void setupWebViewClient()
	{
		setWebViewClient(new WebViewClient()
			{

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url)
				{

					if (url.startsWith(Keys.MediaLibrary.EMBEDDED_PLAYER_SCHEME))
					{
						Debug.log(Keys.MediaLibrary.EMBEDDED_PLAYER_SCHEME, url);
						parseEmbeddedScheme(url);
						return true;
					}
					else
					{
						return false;
					}

				}

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon)
				{
					super.onPageStarted(view, url, favicon);
					showProgressDialog();
				}

				@Override
				public void onPageFinished(WebView view, String url)
				{
					super.onPageFinished(view, url);
					hideProgressDialog();

					triggerPlayVideo();

				}

				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
				{
					super.onReceivedError(view, errorCode, description, failingUrl);
					hideProgressDialog();
				}

			});

	}

	void setupChromeClient()
	{

		setWebChromeClient(new WebChromeClient()
			{
				@Override
				public boolean onConsoleMessage(ConsoleMessage consoleMessage)
				{

					Debug.log(this.getClass().getCanonicalName(), "message: " + consoleMessage.message(), true);

					return super.onConsoleMessage(consoleMessage);
				}

			});
	}

	public void triggerPlayVideo()
	{
		((Activity) getContext()).runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					loadUrl("javascript:playVideo()");
				}

			}

		);
	}

	void parseEmbeddedScheme(String url)//TODO remove this duplicate code. Can subclass webview feature webview
	{
		Uri uri = Uri.parse(url);
		String host = uri.getHost();
		String query = uri.getQuery();

		HashMap<String, String> keyValueMap = new HashMap<String, String>();

		if (query != null)
		{
			String[] keyValuePairs = query.split("&");

			if (keyValuePairs != null)
			{
				for (String keyValuePair : keyValuePairs)
				{
					String[] keyAndValue = keyValuePair.split("=");
					String key = keyAndValue[0];
					String value = keyAndValue[1];
					keyValueMap.put(key, value);
				}
			}
		}

		String stateValue = keyValueMap.get(VALUE_KEY);

		if (host.equals(ON_STATE_CHANGE))
		{
			OnStateChange(stateValue);
		}
		else if (host.equals(ON_READY))
		{
			triggerPlayVideo();
		}
		else if (host.equals(ON_ERROR))
		{
			OnError(stateValue);
		}
	}

	void OnStateChange(String state)
	{
		if (VIDEO_STATE_ENDED.equals(state))
		{
			if (listener != null)
			{
				listener.onVideoPlayEnded();
			}

			finishActivity();
		}
	}

	void OnError(String desc)
	{
		if (listener != null)
		{
			listener.onVideoPlayError(desc);
		}
		finishActivity();
	}

	public void close()
	{
		loadUrl("");
		stopLoading();
		destroy();
	}

	void finishActivity()
	{

		final Activity activity = (Activity) getContext();
		activity.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					close();
					activity.finish();
				}

			});
	}

	protected void callInternalWebViewMethod(final String name)
	{
		Activity activity = (Activity) getContext();

		activity.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{

					try
					{

						Method method = VideoWebViewPlayer.this.getClass().getSuperclass().getDeclaredMethod(name);
						method.setAccessible(true);
						method.invoke(VideoWebViewPlayer.this);
					}
					catch (Exception e)
					{
						Debug.error("WebView", "Could not find method " + name);
						e.printStackTrace();
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

	public void pauseWebView()
	{
		callInternalWebViewMethod("onPause");
	}

	public void resumeWebView()
	{
		callInternalWebViewMethod("onResume");
	}

}
