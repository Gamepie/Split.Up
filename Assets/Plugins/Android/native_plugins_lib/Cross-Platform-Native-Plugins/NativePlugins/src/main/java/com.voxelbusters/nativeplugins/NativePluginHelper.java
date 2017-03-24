package com.voxelbusters.nativeplugins;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.unity3d.player.UnityPlayer;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class NativePluginHelper
{

	public static void sendMessage(String methodName)
	{
		sendMessage(methodName, "");
	}

	public static void sendMessage(String methodName, String message)
	{
		if (!StringUtility.isNullOrEmpty(methodName))
		{
			Debug.log("UnitySendMessage", "Method Name : " + methodName + " " + "Message : " + message);
			if (getCurrentContext() != null) //If null our app is not running
			{
				UnityPlayer.UnitySendMessage(UnityDefines.NATIVE_BINDING_EVENT_LISTENER, methodName, message);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static void sendMessage(String methodName, ArrayList dataList)
	{
		String message = "";

		if (dataList != null)
		{
			// Creating a string in json format
			Gson gson = new Gson();
			message = gson.toJson(dataList);
		}

		sendMessage(methodName, message);
	}

	@SuppressWarnings("rawtypes")
	public static void sendMessage(String methodName, HashMap dataMap)
	{
		String message = "";

		if (dataMap != null)
		{
			// Creating a string in json format
			Gson gson = new Gson();
			message = gson.toJson(dataMap);
		}

		sendMessage(methodName, message);
	}

	// For fetching current activity which is running
	public static Context getCurrentContext()
	{
		Context context = UnityPlayer.currentActivity;

		return context;
	}

	public static Activity getCurrentActivity()
	{

		return (Activity) getCurrentContext();
	}

	// Helper for running a runnable on UI thread
	public static void executeOnUIThread(Runnable runnableThread)
	{
		// Get current active activity
		Activity currentActivity = (Activity) getCurrentContext();
		if (currentActivity != null)
		{
			currentActivity.runOnUiThread(runnableThread);
		}
	}

	public static void startActivityOnUiThread(final Intent intent)
	{
		Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					getCurrentContext().startActivity(intent);
				}
			};

		executeOnUIThread(runnable);
	}

	public static boolean isApplicationRunning()
	{
		return (getCurrentContext() != null);
	}
}