package com.voxelbusters.nativeplugins.features.notification.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.notification.NotificationHandler.NotificationType;
import com.voxelbusters.nativeplugins.utilities.Debug;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class NotificationDefines
{
	public final static String	NOTIFICATION_IDENTIFIER	= "np-notification-identifier";
	public final static String	FIRE_DATE				= "fire-date";
	public final static String	REPEAT_INTERVAL			= "repeat-interval";

	//Meta and non-configurable
	public final static String	CUSTOM_SOUND			= "custom-sound";
	public final static String	LARGE_ICON				= "large-icon";

	//Customizable keys
	public final static String	USER_INFO				= "user-info";
	public final static String	TICKER_TEXT				= "ticker-text";
	public final static String	CONTENT_TITLE			= "content-title";
	public final static String	CONTENT_TEXT			= "content-text";
	public final static String	TAG						= "notification-tag";

	private static JSONObject	keysInfoForNotification	= new JSONObject();

	static
	{
		try
		{
			//Setting default keys for remote notifications
			keysInfoForNotification.put(NOTIFICATION_IDENTIFIER, NOTIFICATION_IDENTIFIER);
			keysInfoForNotification.put(FIRE_DATE, FIRE_DATE);
			keysInfoForNotification.put(REPEAT_INTERVAL, REPEAT_INTERVAL);
			keysInfoForNotification.put(USER_INFO, USER_INFO);
			keysInfoForNotification.put(TICKER_TEXT, TICKER_TEXT);
			keysInfoForNotification.put(CONTENT_TITLE, CONTENT_TITLE);
			keysInfoForNotification.put(CONTENT_TEXT, CONTENT_TEXT);
			keysInfoForNotification.put(TAG, TAG);

			keysInfoForNotification.put(CUSTOM_SOUND, CUSTOM_SOUND);
			keysInfoForNotification.put(LARGE_ICON, LARGE_ICON);

			Debug.error(CommonDefines.NOTIFICATION_TAG, "Start keys : " + keysInfoForNotification.toString());

		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

	}

	public static JSONObject getKeysInfo(Context context)
	{
		//Check if any keys available on storage
		SharedPreferences sharedPref = context.getSharedPreferences(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE);

		String jsonData = sharedPref.getString(Keys.Notification.SAVED_KEYS_INFO, null);
		if (jsonData != null)
		{
			updateNotificationKeys(jsonData);
		}

		return keysInfoForNotification;
	}

	public static boolean needsCustomIconDrawing(Context context)
	{
		//Check if any keys available on storage
		SharedPreferences sharedPref = context.getSharedPreferences(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE);

		boolean isCustomIconAllowed = sharedPref.getBoolean(Keys.Notification.SAVED_CUSTOM_ICON_SELECTION, false);

		return isCustomIconAllowed;
	}

	public static boolean needsVibration(Context context)
	{

		//Check if any keys available on storage
		SharedPreferences sharedPref = context.getSharedPreferences(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE);

		boolean needsVibration = sharedPref.getBoolean(Keys.Notification.SAVED_VIBRATION_SELECTION, true);//By default true

		return needsVibration;
	}

	public static int getAllowedNotificationTypes(Context context)
	{
		SharedPreferences sharedPref = context.getSharedPreferences(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE);

		int allowedNotificationTypes = sharedPref.getInt(Keys.Notification.SAVED_ALLOWED_NOTIFICATION_TYPES, NotificationType.BadgeAndSound.ordinal() | NotificationType.Alert.ordinal());

		return allowedNotificationTypes;
	}

	public static boolean hasNotificationType(NotificationType type, Context context)
	{
		boolean hasType = false;

		int notificationTypes = getAllowedNotificationTypes(context);
		if ((notificationTypes & type.ordinal()) > 0)
		{
			hasType = true;
		}
		else if ((notificationTypes == 0) && (type.ordinal() == 0))//For case zero
		{
			hasType = true;
		}
		else
		{
			hasType = false;
		}

		return hasType;
	}

	public static boolean usesExtenralRemoteNotificationService(Context context)
	{
		//Check if any keys available on storage
		SharedPreferences sharedPref = context.getSharedPreferences(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE);

		boolean extenralService = sharedPref.getBoolean(Keys.Notification.USES_EXTERNAL_REMOTE_NOTIFICATION_SERVICE, false);//By default false

		return extenralService;
	}

	public static void saveConfigInfo(Context context, String customKeysJsonData, boolean allowCustomIcon, boolean allowVibration, boolean usesExternalRemoteNotificationService)
	{
		updateNotificationKeys(customKeysJsonData);

		//Save the key info to disk so that we can access this even if our app is not running.
		SharedPreferences sharedPref = context.getSharedPreferences(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(Keys.Notification.SAVED_KEYS_INFO, customKeysJsonData);
		editor.putBoolean(Keys.Notification.SAVED_CUSTOM_ICON_SELECTION, allowCustomIcon);
		editor.putBoolean(Keys.Notification.SAVED_VIBRATION_SELECTION, allowVibration);
		editor.putBoolean(Keys.Notification.USES_EXTERNAL_REMOTE_NOTIFICATION_SERVICE, usesExternalRemoteNotificationService);
		editor.commit();
	}

	private static void updateNotificationKey(String key, String value)
	{
		try
		{
			keysInfoForNotification.put(key, value);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public static void updateNotificationKeys(String jsonData)
	{
		try
		{
			JSONObject data = new JSONObject(jsonData);
			Iterator<?> keys = data.keys();

			while (keys.hasNext())
			{
				String key = (String) keys.next();
				updateNotificationKey(key, (String) data.get(key));
			}

			Debug.log(CommonDefines.NOTIFICATION_TAG, "New keys : " + keysInfoForNotification.toString());
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public static String GetCustomKey(String key)
	{
		return keysInfoForNotification.optString(key);
	}

}
