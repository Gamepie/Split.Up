package com.voxelbusters;

import android.util.Log;

import com.voxelbusters.nativeplugins.base.interfaces.IAppLifeCycleListener;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.util.ArrayList;

public class NativeBinding
{
	public static ArrayList<IAppLifeCycleListener>	appLifeCycleListeners	= new ArrayList<IAppLifeCycleListener>();
	public static boolean							isAppForeground			= true;

	public static void onApplicationQuit()
	{
		isAppForeground = false;
		for (IAppLifeCycleListener each : appLifeCycleListeners)
		{
			each.onApplicationQuit();
		}
	}

	public static boolean isApplicationForeground()
	{
		return isAppForeground;
	}

	public static void onApplicationResume()
	{

		isAppForeground = true;

		for (IAppLifeCycleListener each : appLifeCycleListeners)
		{
			each.onApplicationResume();
		}
	}

	public static void onApplicationPause()
	{
		isAppForeground = false;

		for (IAppLifeCycleListener each : appLifeCycleListeners)
		{
			each.onApplicationPause();
		}
	}

	//App life cycle listeners
	public static void addAppLifeCycleListener(IAppLifeCycleListener listener)
	{
		if (!appLifeCycleListeners.contains(listener))
		{
			appLifeCycleListeners.add(listener);
		}
	}

	public static void removeAppLifeCycleListener(IAppLifeCycleListener listener)
	{
		appLifeCycleListeners.remove(listener);
	}

	public static void enableDebug(boolean isDebugEnabled)
	{
		Debug.ENABLED = isDebugEnabled;
	}

	//message and stackTrace will be sent in Base64 format.
	public static void logMessage(String message, String logType, String stackTrace)
	{
		//"ERROR", "ASSERT", "WARNING", "INFO", "EXCEPTION"

		String tag = "Unity";
		String messageToDisplay = StringUtility.getBase64DecodedString(message) + "\n" + StringUtility.getBase64DecodedString(stackTrace);

		if (logType.equals("ERROR"))
		{
			Log.e(tag, messageToDisplay);
		}
		else if (logType.equals("WARNING"))
		{
			Log.w(tag, messageToDisplay);
		}
		else if (logType.equals("INFO"))
		{
			Log.i(tag, messageToDisplay);
		}
		else
		{
			Log.d(tag, messageToDisplay);
		}
	}
}
