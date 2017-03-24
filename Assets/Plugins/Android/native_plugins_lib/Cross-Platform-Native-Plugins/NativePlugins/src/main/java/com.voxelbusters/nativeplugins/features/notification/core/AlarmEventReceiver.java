package com.voxelbusters.nativeplugins.features.notification.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Enums.eNotificationRepeatInterval;
import com.voxelbusters.nativeplugins.features.notification.LocalNotification;
import com.voxelbusters.nativeplugins.utilities.Debug;

import org.json.JSONObject;

import java.util.Calendar;

public class AlarmEventReceiver extends BroadcastReceiver
{
	public static String	EVENT_TYPE						= "EVENT_TYPE";
	public static String	DATA							= "DATA";
	public static String	ALARM_EVENT_FOR_NOTIFICATION	= "EVENT_FOR_NOTIFICATION";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			Bundle info = intent.getExtras();
			String eventType = info.getString(EVENT_TYPE);

			Debug.log(CommonDefines.NOTIFICATION_TAG, "Received Alarm event " + info.toString());
			if (eventType.equals(ALARM_EVENT_FOR_NOTIFICATION))
			{
				//Use notification dispatcher to dispatch the notification. This can be a service
				String jsonData = info.getString(DATA);

				//Create a bundle and fill in here.
				JSONObject json = new JSONObject(jsonData);

				//Pass this to notification dispatcher
				NotificationDispatcher dispatcher = new NotificationDispatcher(context);
				dispatcher.dispatch(json, false);// Post the notification

				//Remove from the tracked list of notification ids
				String notificationId = json.getString(NotificationDefines.GetCustomKey(NotificationDefines.NOTIFICATION_IDENTIFIER));
				LocalNotification.removeNotificationId(context, notificationId);

				//Set repeat notification if this has any repeat interval
				int repeatIntervalType = json.getInt(NotificationDefines.GetCustomKey(NotificationDefines.REPEAT_INTERVAL));

				if (repeatIntervalType != eNotificationRepeatInterval.NONE.ordinal())
				{
					//Here Get repeat interval value
					long repeatInterval = getRepeatIntervalInSeconds(repeatIntervalType);
					json.put(NotificationDefines.GetCustomKey(NotificationDefines.FIRE_DATE), System.currentTimeMillis() + (repeatInterval * 1000));
					LocalNotification.scheduleLocalNotification(context, json.toString());
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Debug.error(CommonDefines.NOTIFICATION_TAG, "Error on receiving Alarm notification");
		}
	}

	long getRepeatIntervalInSeconds(int type)
	{
		if (type == eNotificationRepeatInterval.MINUTE.ordinal())
		{
			return 60;
		}
		else if (type == eNotificationRepeatInterval.HOUR.ordinal())
		{
			return 60 * 60;
		}
		else if (type == eNotificationRepeatInterval.DAY.ordinal())
		{
			return 60 * 60 * 24;
		}
		else if (type == eNotificationRepeatInterval.WEEK.ordinal())
		{
			return 60 * 60 * 24 * 7;
		}
		else if (type == eNotificationRepeatInterval.MONTH.ordinal())
		{
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, 1);

			return ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000);
		}
		else if (type == eNotificationRepeatInterval.YEAR.ordinal())
		{
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.YEAR, 1);

			return ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000);
		}
		else
		{
			return 0;
		}
	}
}
