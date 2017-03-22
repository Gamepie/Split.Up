package com.voxelbusters.nativeplugins.features.notification.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.features.notification.NotificationHandler;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;

import org.json.JSONException;
import org.json.JSONObject;

// This class is to get the launch notification info.
public class ApplicationLauncherFromNotification extends Activity
{
	public static JSONObject	launchNotificationData	= null;
	public static Boolean		isRemoteNotification;

	@Override
	protected void onCreate(Bundle paramBundle)
	{
		super.onCreate(paramBundle);

		Intent intent = getIntent();

		String payloadString = intent.getStringExtra(Keys.Notification.NOTIFICATION_PAYLOAD);
		try
		{
			launchNotificationData = new JSONObject(payloadString);
			launchNotificationData.put(Keys.Notification.IS_LAUNCH_NOTIFICATION, true);
			isRemoteNotification = intent.getBooleanExtra(Keys.Notification.IS_REMOTE_NOTIFICATION, false);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		launchMainActivity(intent);
		Log.v(CommonDefines.NOTIFICATION_TAG, getIntent().getStringExtra(Keys.Notification.NOTIFICATION_PAYLOAD) + "Is Launch notification Remote Notification?" + isRemoteNotification);//We can save this in prefs and tell the app that with this app got launched.

		if (NotificationHandler.isInitialised())
		{
			sendLaunchNotificationInfo();
		}

		finish();
	}

	void launchMainActivity(Intent intent)
	{
		Class<?> launcher = ApplicationUtility.GetMainLauncherActivity(this);
		Log.v(CommonDefines.NOTIFICATION_TAG, "Main Launcher Class : " + launcher);
		Intent newIntent = new Intent(this, launcher);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(newIntent);
	}

	public static void sendLaunchNotificationInfo()
	{
		if (launchNotificationData != null)
		{
			String launchNotificationDataString = launchNotificationData.toString();
			if (isRemoteNotification)
			{
				NativePluginHelper.sendMessage(UnityDefines.Notification.DID_RECEIVE_REMOTE_NOTIFICATION, launchNotificationDataString);
			}
			else
			{
				NativePluginHelper.sendMessage(UnityDefines.Notification.DID_RECEIVE_LOCAL_NOTIFICATION, launchNotificationDataString);
			}
		}
	}
}
