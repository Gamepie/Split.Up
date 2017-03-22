package com.voxelbusters.nativeplugins.features.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.notification.core.ApplicationLauncherFromNotification;
import com.voxelbusters.nativeplugins.features.notification.core.NotificationDefines;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;
import com.voxelbusters.nativeplugins.utilities.Debug;

import org.json.JSONArray;
import org.json.JSONException;

public class NotificationHandler
{
	public enum NotificationType
	{
		None, Badge, Sound, BadgeAndSound, Alert//Badge no option yet.
	}

	// Create singleton instance
	private static NotificationHandler	INSTANCE;

	public static Boolean isInitialised()
	{
		return (INSTANCE != null);
	}

	public static NotificationHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new NotificationHandler();

		}
		return INSTANCE;
	}

	String[]	senderIds;

	int			notificationTypes;

	// Make constructor private for making singleton interface
	private NotificationHandler()
	{
		notificationTypes = NotificationDefines.getAllowedNotificationTypes(NativePluginHelper.getCurrentContext());
	}

	public void initialize(String senderIdListJson, String customKeysForNotification, boolean useCustomIcon, boolean allowVibration, boolean usesExternalRemoteNotificationService)
	{

		try
		{
			JSONArray jsonArray = new JSONArray(senderIdListJson);
			senderIds = new String[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++)
			{
				senderIds[i] = (String) jsonArray.get(i);
			}

			//Updating custom keys if any for  notification
			NotificationDefines.saveConfigInfo(NativePluginHelper.getCurrentContext(), customKeysForNotification, useCustomIcon, allowVibration, usesExternalRemoteNotificationService);

			//Send launch notification info to host here.
			ApplicationLauncherFromNotification.sendLaunchNotificationInfo();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			Debug.error(CommonDefines.NOTIFICATION_TAG, "Unable to parse senderIDList!");
		}

	}

	public void setNotificationTypes(int types)
	{
		notificationTypes = types;

		SharedPreferences sharedPref = NativePluginHelper.getCurrentContext().getSharedPreferences(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(Keys.Notification.SAVED_ALLOWED_NOTIFICATION_TYPES, notificationTypes);
		editor.commit();
	}

	public int getAllowedNotificationTypes()
	{
		return  notificationTypes;
	}

	public boolean areNotificationsAllowedByUser()
	{
		Context context = NativePluginHelper.getCurrentContext();
		String settingsData = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
		String packageName = ApplicationUtility.getPackageName(context);

		Debug.log("Test", settingsData + " name : " + packageName);

		return settingsData.contains(packageName);
	}

	//For local notifications

	public void scheduleLocalNotification(String jsonInfo)
	{
		LocalNotification.scheduleLocalNotification(NativePluginHelper.getCurrentContext(), jsonInfo);
	}

	public void cancelLocalNotification(String notificationID)
	{
		//This should cancel notification with notificationID id 
		LocalNotification.cancelNotification(NativePluginHelper.getCurrentContext(), notificationID);
	}

	public void cancelAllLocalNotifications()
	{
		//This should cancel all scheduled notifications
		LocalNotification.cancelAllNotifications(NativePluginHelper.getCurrentContext());
	}

	public void clearAllNotifications()
	{
		//This should clear  all scheduled notifications in the notification bar
		NotificationManager notificationManager = (NotificationManager) ApplicationUtility.getSystemService(NativePluginHelper.getCurrentContext(), Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	//For remote notifications
	public void registerRemoteNotifications()
	{
		if (senderIds.length == 0)
		{
			Debug.error("NotificationHandler.registerRemoteNotifications", "Add senderId's in the NP Settings");
		}
		else
		{
			RemoteNotification.getInstance().registerForRemoteNotifications(senderIds);
		}
	}

	public void unregisterRemoteNotifications()
	{
		RemoteNotification.getInstance().unregisterForRemoteNotifications();
	}

}
