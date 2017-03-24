package com.voxelbusters.nativeplugins.features.notification.serviceprovider.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.features.notification.core.NotificationDispatcher;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.JSONUtility;

import org.json.JSONObject;

// Reference from android Documentation
public class GCMIntentService extends IntentService
{
	public GCMIntentService()
	{
		super("GCMIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging service = GoogleCloudMessaging.getInstance(this);

		String messageType = service.getMessageType(intent);

		Debug.log(CommonDefines.NOTIFICATION_TAG, "GCMIntentService received message type : " + messageType);

		if (!extras.isEmpty())
		{
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
			{
				// Post notification of received message.
				Debug.log(CommonDefines.NOTIFICATION_TAG, "GCM OnMessage : " + extras.toString());

				NotificationDispatcher dispatcher = new NotificationDispatcher(this);

				JSONObject notificationMap = JSONUtility.getJSONfromBundle(extras);
				dispatcher.dispatch(notificationMap, true);// Post the notification
			}
		}

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GCMBroadcastReceiver.completeWakefulIntent(intent);
	}
}