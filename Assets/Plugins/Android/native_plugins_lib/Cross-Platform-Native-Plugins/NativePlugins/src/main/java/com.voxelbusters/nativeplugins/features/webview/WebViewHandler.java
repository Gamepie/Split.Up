package com.voxelbusters.nativeplugins.features.webview;

import android.app.Activity;
import android.content.Context;
import android.graphics.RectF;
import android.net.Uri;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.FileUtility;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Set;

// This helps in creation of instances of NativeWebview and offers search based
// on tag
public class WebViewHandler
{

	// Create singleton instance
	private static WebViewHandler	INSTANCE;

	public static WebViewHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new WebViewHandler();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private WebViewHandler()
	{
	}

	// This is for holding instance based on tag
	static HashMap<String, NativeWebViewController>	webViewContainer	= new HashMap<String, NativeWebViewController>();
	boolean											startAsNewActivity	= false;

	public void initialise(boolean startAsNewActivity)
	{
		this.startAsNewActivity = startAsNewActivity;
	}

	public void createNativeWebView(final String tag, final float x, final float y, final float width, final float height)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					Context currentContext = NativePluginHelper.getCurrentContext();
					RectF frameRect = new RectF();
					frameRect.left = x;
					frameRect.top = y;
					frameRect.right = x + width;
					frameRect.bottom = y + height;

					NativeWebViewController nativeWebViewController = new NativeWebViewController(tag, frameRect);
					webViewContainer.put(tag, nativeWebViewController);

					nativeWebViewController.CreateDefaultWebView((Activity) currentContext);
					Debug.log(CommonDefines.WEB_VIEW_TAG, "Web view created!");
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	// For fetching webview based on tag
	NativeWebViewController getNativeWebViewWithTag(String tag)
	{

		if (webViewContainer.containsKey(tag))
		{
			return webViewContainer.get(tag);
		}
		else
		{
			Debug.error(CommonDefines.WEB_VIEW_TAG, "Webview Tag Not Found");
			return null;
		}
	}

	void destoryWebViewWithTag(final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView != null)
					{
						webView.destroy();
						webViewContainer.remove(tag);
					}
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void loadRequest(final String urlString, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);

					if (webView != null)
					{
						try
						{
							webView.loadURL(urlString);
						}
						catch (MalformedURLException e)
						{
							e.printStackTrace();
						}
					}
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	/*
	 * Note that content specified in this way can access local device files
	 * (via 'file' scheme URLs) only if baseUrl specifies a scheme other than
	 * 'http', 'https', 'ftp', 'ftps', 'about' or 'javascript'.
	 */
	public void loadHTMLString(final String string, final String baseURLStr, final String tag)
	{
		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);

					if (webView == null)
					{
						return;
					}

					webView.loadDataWithBaseURL(baseURLStr, string, "text/html", "UTF-8", null);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void loadData(final byte[] data, final int length, final String mimeType, final String textEncodingName, final String baseURL, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}

					int index = mimeType.lastIndexOf("/");

					String extension = mimeType.substring(index + 1);

					String fileName = "tempFile" + "" + extension;

					try
					{
						Uri imagePathUri = FileUtility.createSharingFileUri(NativePluginHelper.getCurrentContext(), data, length, CommonDefines.SHARING_DIR, fileName);

						webView.loadURL(imagePathUri.toString());
					}
					catch (MalformedURLException e)
					{
						e.printStackTrace();
					}

				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void reloadWebViewWithTag(final String tag)
	{
		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}

					webView.reload();
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void stopLoadingWebViewWithTag(final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}

					webView.stop();
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);

	}

	public void showWebViewWithTag(final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}

					webView.show();
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);

	}

	public void hideWebViewWithTag(final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}

					webView.hide();
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);

	}

	public void setCanHide(final boolean canHide, final String tag)
	{

		Debug.log(CommonDefines.WEB_VIEW_TAG, "Set canHide " + tag);

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}

					webView.setCanHide(canHide);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void evaluateJavaScriptFromString(final String jsScript, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.stringByEvaluatingJavaScriptFromString(jsScript);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	// Properties
	public void setCanBounce(final boolean canBounce, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setBounce(canBounce);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);

	}

	public void setCanGoBack(final boolean canGoBack, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setCanGoBack(canGoBack);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void setNavigation(final boolean canGoBack, final boolean canGoForward, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setNavigation(canGoBack, canGoForward);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void setControlType(final String type, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setControlType(type);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void setShowToolBar(final boolean showToolBar, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setShowToolBar(showToolBar);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void setShowLoadingSpinner(final boolean showLoading, final String tag)
	{
		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.showLoadingIndicatorOnLoad(showLoading);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void setAutoShowWhenLoadComplete(final boolean autoShow, final String tag)
	{
		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setAutoShowWhenLoadComplete(autoShow);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void setScalesPageToFit(final boolean scaleToFit, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setScalesPageToFit(scaleToFit);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void addNewScheme(final String newScheme, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.addNewScheme(newScheme);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void removeScheme(final String schemaToRemove, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.removeScheme(schemaToRemove);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void clearCache()
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					Set<String> set = webViewContainer.keySet();

					for (String eachTag : set)
					{
						clearCache(eachTag);

					}
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	void clearCache(String tag)
	{
		NativeWebViewController webView = getNativeWebViewWithTag(tag);
		if (webView != null)
		{
			webView.clearCache();
		}
	}

	public void setZoom(final boolean enable, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setZoom(enable);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void setFrame(final float x, final float y, final float width, final float height, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setFrame(x, y, width, height);
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void setBackgroundColor(final float r, final float g, final float b, final float a, final String tag)
	{

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					NativeWebViewController webView = getNativeWebViewWithTag(tag);
					if (webView == null)
					{
						return;
					}
					webView.setBackgroundColor((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}
}
