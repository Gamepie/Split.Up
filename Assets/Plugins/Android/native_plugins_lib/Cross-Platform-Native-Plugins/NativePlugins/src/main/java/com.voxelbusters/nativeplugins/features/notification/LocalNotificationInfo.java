package com.voxelbusters.nativeplugins.features.notification;

import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.features.notification.core.NotificationDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;

import org.json.JSONException;
import org.json.JSONObject;

public class LocalNotificationInfo
{

	String		id;
	String		tickerText;
	String		contentTitle;
	String		contentText;
	float		fireAfterSeconds;
	long		repeatInterval;
	JSONObject	userInfo;
	String		tag;
	String		customSound;
	String		largeIcon;

	public LocalNotificationInfo(String jsonInfo) throws JSONException
	{
		//Parse the json string here.
		JSONObject json = new JSONObject(jsonInfo);
		tickerText = json.getString(NotificationDefines.GetCustomKey(NotificationDefines.TICKER_TEXT));
		contentTitle = json.getString(NotificationDefines.GetCustomKey(NotificationDefines.CONTENT_TITLE));
		contentText = json.getString(NotificationDefines.GetCustomKey(NotificationDefines.CONTENT_TEXT));
		tag = json.getString(NotificationDefines.GetCustomKey(NotificationDefines.TAG));
		userInfo = json.getJSONObject(NotificationDefines.GetCustomKey(NotificationDefines.USER_INFO));

		customSound = json.getString(NotificationDefines.GetCustomKey(NotificationDefines.CUSTOM_SOUND));
		largeIcon = json.getString(NotificationDefines.GetCustomKey(NotificationDefines.LARGE_ICON));

		//Id is included in userinfo dict
		id = userInfo.getString(NotificationDefines.GetCustomKey(NotificationDefines.NOTIFICATION_IDENTIFIER));

		long timeInMillis = json.getLong(NotificationDefines.GetCustomKey(NotificationDefines.FIRE_DATE));
		fireAfterSeconds = (float) Math.ceil((timeInMillis - System.currentTimeMillis()) / 1000f);//Fire time converted to seconds

		repeatInterval = json.optInt(NotificationDefines.GetCustomKey(NotificationDefines.REPEAT_INTERVAL), 0);

		Debug.log(CommonDefines.NOTIFICATION_TAG, "Scheduling firing after : " + fireAfterSeconds + " Secs...");

	}

	public JSONObject getNotificationData()
	{
		JSONObject json = new JSONObject();

		try
		{
			json.put(NotificationDefines.GetCustomKey(NotificationDefines.NOTIFICATION_IDENTIFIER), id);
			json.put(NotificationDefines.GetCustomKey(NotificationDefines.TICKER_TEXT), tickerText);
			json.put(NotificationDefines.GetCustomKey(NotificationDefines.CONTENT_TITLE), contentTitle);
			json.put(NotificationDefines.GetCustomKey(NotificationDefines.CONTENT_TEXT), contentText);
			json.put(NotificationDefines.GetCustomKey(NotificationDefines.USER_INFO), userInfo);
			json.put(NotificationDefines.GetCustomKey(NotificationDefines.TAG), tag);
			json.put(NotificationDefines.GetCustomKey(NotificationDefines.CUSTOM_SOUND), customSound);
			json.put(NotificationDefines.GetCustomKey(NotificationDefines.LARGE_ICON), largeIcon);

			json.put(NotificationDefines.GetCustomKey(NotificationDefines.FIRE_DATE), fireAfterSeconds);
			json.put(NotificationDefines.GetCustomKey(NotificationDefines.REPEAT_INTERVAL), repeatInterval);

		}
		catch (JSONException e)
		{
			e.printStackTrace();
			Debug.error(CommonDefines.NOTIFICATION_TAG, "Error parsing creating json in localNotification");
		}

		return json;
	}

}
