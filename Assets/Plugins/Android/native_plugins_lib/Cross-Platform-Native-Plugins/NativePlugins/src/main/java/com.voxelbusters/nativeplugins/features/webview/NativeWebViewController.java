package com.voxelbusters.nativeplugins.features.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

enum eWebViewEvent
{
	ePageLoadStarted, ePageLoadEnded
}

// This is for creating a webview and presenting it
public class NativeWebViewController extends WebViewClient
{
	private final String		tag;
	private NativeWebViewFrame	webViewDialog;
	private boolean				showLoadingOnLoad;

	ProgressDialog				loadingSpinner;
	volatile private boolean	autoShowAfterLoad;
	private boolean				canHide;
	private boolean				isLoading;

	private boolean				isShowing;

	private boolean				canGoBack			= true;
	private boolean				canGoForward		= true;

	public JavaScriptInterface	javaScriptInterface;

	ArrayList<String>			supportedSchemaList	= new ArrayList<String>();

	RectF						frameRect			= new RectF();

	public NativeWebViewController(String tag, RectF frameRect)
	{
		this.tag = tag;
		this.frameRect = frameRect;
	}

	public NativeWebViewController(Activity activity, String tag)
	{
		this.tag = tag;
		CreateDefaultWebView(activity);
	}

	@SuppressLint("NewApi")
	public void CreateDefaultWebView(Activity parentActivity)
	{
		webViewDialog = new NativeWebViewFrame(parentActivity);

		parentActivity.addContentView(webViewDialog, webViewDialog.getLayoutParams());

		setDefaultWebViewSettings(parentActivity);

		getWebView().setWebViewClient(this);
		setWebChromeClientListeners();

		//For enabling html5 videos
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			getWebView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}

		webViewDialog.hide();
		
		javaScriptInterface = new JavaScriptInterface(tag);
		getWebView().addJavascriptInterface(javaScriptInterface, "UnityInterface");

		//By default we set to autoShowOnComplete. And this will be overriden by show call.
		setAutoShowWhenLoadComplete(true);
		setControlType(Keys.WebView.TYPE_CLOSE_BUTTON);

		registerButtonCallbacks();

		setFrame(frameRect);
	}

	void registerButtonCallbacks()
	{

		NativeWebViewToolBar toolbar = webViewDialog.getToolBar();
		toolbar.getBackButton().setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					getWebView().goBack();
				}
			});

		toolbar.getForwardButton().setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					getWebView().goForward();
				}
			});

		toolbar.getReloadButton().setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					getWebView().reload();
				}
			});

		OnClickListener closeListener = new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					hide();
				}
			};

		toolbar.getCloseButton().setOnClickListener(closeListener);

		webViewDialog.setCloseButtonListener(closeListener);

	}

	@SuppressWarnings("deprecation")
	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	public void setDefaultWebViewSettings(Activity parentActivity)
	{
		WebSettings settings = getWebView().getSettings();

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
		{
			settings.setPluginState(PluginState.ON);
		}

		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setAllowFileAccess(true);
		settings.setDomStorageEnabled(true);

		// No cache by default
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		// Setting zoom by default to false
		settings.setSupportZoom(false);

		settings.setDatabaseEnabled(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			settings.setAllowUniversalAccessFromFileURLs(true);
		}

		settings.setGeolocationDatabasePath(parentActivity.getFilesDir().getPath());
	}

	void setWebChromeClientListeners()
	{

		getWebView().setWebChromeClient(new WebChromeClient()
			{

				@Override
				public boolean onJsConfirm(WebView view, String url, String message, JsResult result)
				{
					// TODO Auto-generated method stub
					return super.onJsConfirm(view, url, message, result);
				}

				@Override
				public boolean onJsAlert(WebView view, String url, String message, JsResult result)
				{
					return super.onJsAlert(view, url, message, result);
				}

				@Override
				public boolean onConsoleMessage(ConsoleMessage consoleMessage)
				{

					Debug.log(CommonDefines.WEB_VIEW_TAG, "message:" + consoleMessage.message(), true);

					return super.onConsoleMessage(consoleMessage);
				}

				@Override
				public void onGeolocationPermissionsShowPrompt(String origin, Callback callback)
				{
					callback.invoke(origin, true, false);//For obtaining permission.
				}

				//TODO Yet to support full screen for html 5
				//Support for older devices
				/*@Override
				public void onShowCustomView(View view, CustomViewCallback callback)
				{
					super.onShowCustomView(view, callback);
				}

				@Override
				public void onHideCustomView()
				{
					super.onHideCustomView();
				}

				@Override
				public View getVideoLoadingProgressView()
				{

					return super.getVideoLoadingProgressView();

				}*/

			});
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// / Accessors Functions///
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getTag()
	{
		return tag;
	}

	public WebView getWebView()
	{
		return webViewDialog.getWebView();
	}

	public boolean isShowLoadingOnLoad()
	{
		return showLoadingOnLoad;
	}

	public void setShowLoadingOnLoad(boolean showLoadingOnLoad)
	{
		this.showLoadingOnLoad = showLoadingOnLoad;
		showLoadingDialogIfNeeded();
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// / Memory Management Functions///
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void destroy()
	{
		webViewDialog.dismissAndDestroy();
	}

	public void loadURL(String urlString) throws MalformedURLException
	{
		Debug.log(CommonDefines.WEB_VIEW_TAG, "Loading : " + urlString, true);
		getWebView().loadUrl(urlString);
	}

	public void loadDataWithBaseURL(String baseURLStr, String data, String mimeType, String encoding, String hisotryURL)
	{
		getWebView().loadDataWithBaseURL(baseURLStr, data, mimeType, encoding, hisotryURL);
	}

	public void loadData(String data, String mimeType, String encoding)
	{
		getWebView().loadData(data, mimeType, encoding);
	}

	public void reload()
	{
		getWebView().reload();
	}

	public void show()
	{
		if (!isShowing)
		{
			isShowing = true;
			webViewDialog.showNow();
			NativePluginHelper.sendMessage(UnityDefines.WebView.WEB_VIEW_DID_SHOW, tag);
		}

	}

	public boolean isVisible()
	{
		return isShowing;
	}

	public void stop()
	{
		getWebView().stopLoading();
		hideLoadingDialog();
	}

	public void hide()
	{
		if (isShowing)
		{
			isShowing = false;
			webViewDialog.hide();
			NativePluginHelper.sendMessage(UnityDefines.WebView.WEB_VIEW_DID_HIDE, tag);
		}
	}

	public boolean isCanHide()
	{
		return canHide;
	}

	public void setCanHide(boolean canHide)
	{
		this.canHide = canHide;

		//Disable the close buttons if canHide is false

		webViewDialog.getToolBar().getBackButton().setEnabled(canHide);
		webViewDialog.getCloseButton().setEnabled(canHide);

	}

	@SuppressLint("NewApi")
	public void stringByEvaluatingJavaScriptFromString(String jsScript)
	{

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			getWebView().evaluateJavascript("javascript:" + jsScript, new ValueCallback<String>()
				{
					@Override
					public void onReceiveValue(String result)
					{
						javaScriptInterface.passToUnity(result);
					}
				});
		}
		else
		{
			try
			{
				loadURL("javascript:UnityInterface.passToUnity(" + jsScript + ")");//Just injecting our interface call along with the passed js script
			}
			catch (MalformedURLException e)
			{
				Debug.error(CommonDefines.WEB_VIEW_TAG, "Exception in stringByEvaluatingJavaScriptFromString");
				e.printStackTrace();
				javaScriptInterface.passToUnity("[Error] " + e.getMessage());
			}
		}
	}

	public void setBounce(boolean canBounce)
	{
		if (canBounce)
		{
			getWebView().setOverScrollMode(View.OVER_SCROLL_NEVER);
		}
		else
		{
			getWebView().setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
		}
	}

	public void setCanGoBack(boolean canGoBack)
	{
		this.canGoBack = canGoBack;
	}

	public void showLoadingIndicatorOnLoad(boolean showLoading)
	{
		setShowLoadingOnLoad(showLoading);
	}

	public void setAutoShowWhenLoadComplete(boolean autoShow)
	{
		autoShowAfterLoad = autoShow;

		if (autoShowAfterLoad && isLoading)
		{
			hide();
		}
	}

	public void setScalesPageToFit(boolean scaleToFit)
	{
		// TODO Auto-generated method stub
		getWebView().getSettings().setLoadWithOverviewMode(true);
		getWebView().getSettings().setUseWideViewPort(true);
		getWebView().getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		getWebView().setInitialScale(1);
	}

	public void setZoom(boolean enable)
	{
		getWebView().getSettings().setBuiltInZoomControls(true);
		getWebView().getSettings().setSupportZoom(true);
	}

	public void setFrame(float x, float y, float width, float height)
	{
		frameRect.left = x;
		frameRect.top = y;
		frameRect.right = x + width;
		frameRect.bottom = y + height;

		webViewDialog.setFrame(frameRect);
	}

	public void setFrame(RectF frameRect)
	{
		this.frameRect = frameRect;
		webViewDialog.setFrame(frameRect);
	}

	public void addNewScheme(String newSchema)
	{
		//TODO
		//This schema should be considered to check if we need to process the new url request we got
		supportedSchemaList.add(newSchema);
	}

	public void removeScheme(String newSchema)
	{
		//TODO
		//This schema should be considered to check if we need to process the new url request we got
		supportedSchemaList.remove(newSchema);
	}

	public void clearCache()
	{
		getWebView().clearCache(true);
	}

	public void setNavigation(boolean canGoBack, boolean canGoForward)
	{
		//Set the toolbar options accordingly
		this.canGoBack = canGoBack;
		this.canGoForward = canGoForward;

		//Update toolbar buttons
		setUpToolbarButtons();
	}

	public void setControlType(String type)
	{

		webViewDialog.getToolBar().hide();
		webViewDialog.hideCloseButton();

		if (type.equals(Keys.WebView.TYPE_TOOLBAR))
		{
			webViewDialog.getToolBar().show();
			setNavigation(true, true);
		}
		else if (type.equals(Keys.WebView.TYPE_CLOSE_BUTTON))
		{
			webViewDialog.showCloseButton();
		}
	}

	public void setShowToolBar(boolean showToolBar)
	{
		if (!showToolBar)
		{
			webViewDialog.getToolBar().hide();
		}
		else
		{
			webViewDialog.getToolBar().show();
		}
	}

	public void setBackgroundColor(int red, int green, int blue, int alpha)
	{
		getWebView().setBackgroundColor(Color.argb(alpha, red, green, blue));
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// / Callbacks
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon)
	{
		super.onPageStarted(view, url, favicon);

		isLoading = true;

		showLoadingDialogIfNeeded();

		//Update toolbar buttons
		setUpToolbarButtons();

		NativePluginHelper.sendMessage(UnityDefines.WebView.WEB_VIEW_DID_START_LOAD, tag);

	}

	@Override
	public void onPageFinished(WebView view, String url)
	{
		super.onPageFinished(view, url);

		isLoading = false;

		if (autoShowAfterLoad)
		{
			show();
		}
		hideLoadingDialog();

		//Update toolbar buttons
		setUpToolbarButtons();

		HashMap<String, String> data = new HashMap<String, String>();
		data.put(Keys.TAG, tag);
		data.put(Keys.URL, url);

		NativePluginHelper.sendMessage(UnityDefines.WebView.WEB_VIEW_DID_FINISH_LOAD, data);
	}

	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
	{
		super.onReceivedError(view, errorCode, description, failingUrl);

		isLoading = false;

		if (autoShowAfterLoad)
		{
			show();
		}

		hideLoadingDialog();

		//Update toolbar buttons
		setUpToolbarButtons();

		Debug.error(CommonDefines.WEB_VIEW_TAG, "Received Error : " + description);

		HashMap<String, String> data = new HashMap<String, String>();
		data.put(Keys.TAG, tag);
		data.put(Keys.ERROR, description);
		data.put(Keys.URL, failingUrl);

		NativePluginHelper.sendMessage(UnityDefines.WebView.WEB_VIEW_DID_FAIL_LOAD_WITH_ERROR, data);
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String urlString)
	{
		Debug.log(CommonDefines.WEB_VIEW_TAG, "URL STRING = " + urlString);

		//First filter userScemelist then move on to default handling
		Uri uri = Uri.parse(urlString);
		String schemeOfUrl = uri.getScheme();

		if (supportedSchemaList.contains(schemeOfUrl))
		{
			//Pass this info to unity.
			parseCustomScheme(uri);
			return true;
		}
		else
		{
			return false;
		}
	}

	private void parseCustomScheme(Uri uri)
	{
		String url = uri.toString();
		String host = uri.getHost();
		String scheme = uri.getScheme();
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
					String value = "";
					if (keyAndValue.length > 1)
					{
						value = keyAndValue[1];
					}
					keyValueMap.put(key, value);
				}
			}
		}

		HashMap<String, Object> hash = new HashMap<String, Object>();

		HashMap<String, Object> subHash = new HashMap<String, Object>();

		subHash.put(Keys.URL, url != null ? url : "");
		subHash.put(Keys.WebView.HOST, host != null ? host : "");
		subHash.put(Keys.WebView.ARGUMENTS, keyValueMap);
		subHash.put(Keys.WebView.URL_SCHEME, scheme);

		hash.put(Keys.WebView.TAG, tag);
		hash.put(Keys.WebView.MESSAGE_DATA, subHash);

		NativePluginHelper.sendMessage(UnityDefines.WebView.WEB_VIEW_DID_RECEIVE_MESSAGE, hash);
	}

	void setUpToolbarButtons()
	{
		//Setup toolbar options
		NativeWebViewToolBar toolBar = webViewDialog.getToolBar();

		toolBar.getBackButton().setEnabled((getWebView().canGoBack() && canGoBack));

		toolBar.getForwardButton().setEnabled((getWebView().canGoForward() && canGoForward));

	}

	void showLoadingDialogIfNeeded()
	{

		if (isLoading && showLoadingOnLoad)
		{
			webViewDialog.showProgressSpinner();
		}
		else
		{
			webViewDialog.hideProgressSpinner();
		}
	}

	void hideLoadingDialog()
	{
		webViewDialog.hideProgressSpinner();
	}

}
