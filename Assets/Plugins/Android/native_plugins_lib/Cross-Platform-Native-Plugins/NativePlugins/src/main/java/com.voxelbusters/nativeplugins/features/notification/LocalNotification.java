package com.voxelbusters.nativeplugins.features.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.notification.core.AlarmEventReceiver;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.JSONUtility;
import com.voxelbusters.nativeplugins.utilities.SharedPreferencesUtility;

import org.json.JSONArray;
import org.json.JSONException;

public class LocalNotification
{

	@SuppressLint("NewApi")
	public static void scheduleLocalNotification(Context context, String jsonInfo)
	{
		//Convert this data to a hashMap and add extra flag to identify its created by us.
		/*
		 *  Intent intent = getIntent();
				HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("map");

		 */
		//Set this extra info to the scheduling notification. can use Alarm to fire one
		//Create an intent , set info and schedule with AlarmManager

		try
		{
			Debug.log(CommonDefines.NOTIFICATION_TAG, jsonInfo);
			LocalNotificationInfo notificationInfo = new LocalNotificationInfo(jsonInfo);
			//Call alarm and schedule it

			Intent alarmIntent = new Intent(context.getApplicationContext(), AlarmEventReceiver.class);

			//This is used for filtering incase we need to cancel
			alarmIntent.setData(Uri.parse("custom://" + notificationInfo.id));
			alarmIntent.setAction(notificationInfo.id);

			Bundle extraData = new Bundle();

			//Set event type
			extraData.putString(AlarmEventReceiver.EVENT_TYPE, AlarmEventReceiver.ALARM_EVENT_FOR_NOTIFICATION);

			//Set the data that need to be passed
			extraData.putString(AlarmEventReceiver.DATA, notificationInfo.getNotificationData().toString());

			//Set the extra data here
			alarmIntent.putExtras(extraData);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, alarmIntent, 0);

			long fireTime = System.currentTimeMillis() + (long) (notificationInfo.fireAfterSeconds * 1000);

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			{
				alarmManager.set(AlarmManager.RTC_WAKEUP, fireTime, pendingIntent);//TODO	
			}
			else
			{
				alarmManager.setExact(AlarmManager.RTC_WAKEUP, fireTime, pendingIntent);//TODO
			}

			addNotificationId(context, notificationInfo.id);
			Debug.log(CommonDefines.NOTIFICATION_TAG, "Local notification Scheduled : " + extraData.getString(AlarmEventReceiver.DATA), true);

		}
		catch (JSONException e)
		{
			Debug.error(CommonDefines.NOTIFICATION_TAG, "Unable to parse notification json info");
			e.printStackTrace();
		}

	}

	public static void cancelNotification(Context context, String notificationID)
	{
		//Remove from Alarm Manager

		Intent alarmIntent = new Intent(context, AlarmEventReceiver.class);

		alarmIntent.setData(Uri.parse("custom://" + notificationID));
		alarmIntent.setAction(notificationID);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Debug.log("LocalNotification.cancelNotification", "" + pendingIntent + " " + Uri.parse("custom://" + notificationID));
		if (pendingIntent != null)
		{
			alarmManager.cancel(pendingIntent);
		}

		removeNotificationId(context, notificationID);

	}

	public static void cancelAllNotifications(Context context)
	{
		JSONArray list = SharedPreferencesUtility.getJsonArray(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE, context, Keys.Notification.SAVED_NOTIFICATION_META_INFO);

		for (int i = 0; i < list.length(); i++)
		{
			try
			{
				cancelNotification(context, list.getString(i));
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}

		Debug.log("LocalNotification.cancelAllNotifications", "Finished cancelling local notifications");

	}

	public static void clearAllNotifications(Context context)
	{
		//Clear from notification bar if possible //TODO currently clears all notifications.
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	public static void addNotificationId(Context context, String notificationId)
	{
		JSONArray jsonArray = SharedPreferencesUtility.getJsonArray(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE, context, Keys.Notification.SAVED_NOTIFICATION_META_INFO);
		//Add notification id and save
		jsonArray.put(notificationId);
		SharedPreferencesUtility.setJSONArray(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE, context, Keys.Notification.SAVED_NOTIFICATION_META_INFO, jsonArray);
	}

	public static void removeNotificationId(Context context, String notificationId)
	{
		JSONArray jsonArray = SharedPreferencesUtility.getJsonArray(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE, context, Keys.Notification.SAVED_NOTIFICATION_META_INFO);

		int index = JSONUtility.findString(jsonArray, notificationId);
		if (index != -1)
		{
			jsonArray = JSONUtility.removeIndex(jsonArray, index);
			SharedPreferencesUtility.setJSONArray(Keys.Notification.SAVED_KEYS_FILE, Context.MODE_PRIVATE, context, Keys.Notification.SAVED_NOTIFICATION_META_INFO, jsonArray);
		}
	}

}
