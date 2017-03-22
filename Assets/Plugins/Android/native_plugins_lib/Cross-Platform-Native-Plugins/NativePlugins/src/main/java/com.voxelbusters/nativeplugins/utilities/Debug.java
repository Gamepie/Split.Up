package com.voxelbusters.nativeplugins.utilities;

import android.util.Log;
import android.widget.Toast;

import com.voxelbusters.nativeplugins.NativePluginHelper;

public class Debug
{

	public static boolean	ENABLED	= false;

	public static void log(String tag, String msg, boolean showToast)//R
	{
		if (ENABLED)
		{
			if (showToast)
			{
				//toast("[" + tag + "]" + msg);
			}
			Log.d(tag, msg);
		}
	}

	public static void log(String tag, String msg)
	{
		log(tag, msg, false);
	}

	public static void error(String tag, String msg)
	{
		if (ENABLED)
		{
			Log.e(tag, msg);
			toast("[" + tag + "]" + msg);
		}
	}

	public static void warning(String tag, String msg)
	{
		if (ENABLED)
		{
			Log.w(tag, msg);
		}
	}

	static void toast(final String msg)
	{

		Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(NativePluginHelper.getCurrentContext(), msg, Toast.LENGTH_LONG).show();//TODO remove this
				}
			};

		NativePluginHelper.executeOnUIThread(runnable);
	}

}
