package com.voxelbusters.nativeplugins.features.webview;

import android.webkit.JavascriptInterface;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;

import java.util.HashMap;

// This is used to get call backs from js scripts : passToUnity should be used
// in the js code
public class JavaScriptInterface
{
	String	tag;

	public JavaScriptInterface(String tag)
	{
		this.tag = tag;
	}

	@JavascriptInterface
	public void passToUnity(String result)
	{
		// Notify unity
		HashMap<String, String> data = new HashMap<String, String>();
		data.put(Keys.TAG, tag);
		data.put(Keys.RESULT, result);

		NativePluginHelper.sendMessage(UnityDefines.WebView.WEB_VIEW_DID_FINISH_EVALUATING_JS, data);

	}
}
