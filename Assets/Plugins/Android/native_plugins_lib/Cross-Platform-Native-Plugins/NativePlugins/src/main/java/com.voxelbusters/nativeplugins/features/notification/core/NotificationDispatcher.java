package com.voxelbusters.nativeplugins.features.notification.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.voxelbusters.NativeBinding;
import com.voxelbusters.androidnativeplugin.R;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.features.notification.NotificationHandler.NotificationType;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.JSONUtility;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class NotificationDispatcher
{
	Context		context;
	MediaPlayer	mediaPlayer;

	public NotificationDispatcher(Context context)
	{
		this.context = context;
	}

	public void dispatch(JSONObject notificationData, boolean isRemoteNotification)
	{

		if (isRemoteNotification && NotificationDefines.usesExtenralRemoteNotificationService(context))
		{
			return;// Don't do any if user opts for external notification service.
		}

		JSONObject keyMap = NotificationDefines.getKeysInfo(context);
		Debug.log(CommonDefines.NOTIFICATION_TAG, "Current keymapping used is " + keyMap.toString());

		Debug.log(CommonDefines.NOTIFICATION_TAG, "notificationData " + notificationData.toString());

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		String appName = ApplicationUtility.getApplicationName(context);

		// If app is in foreground we don't need to push the notification in notification bar
		JSONObject formattedNotification = getFormattedNotification(notificationData, keyMap);

		String formattedNotificationString = formattedNotification.toString();

		boolean isAppRunning = NativePluginHelper.isApplicationRunning();
		boolean isAppForeground = NativeBinding.isApplicationForeground();

		if (!isAppRunning || !isAppForeground)
		{
			try
			{
				Intent notificationIntent = new Intent(context, ApplicationLauncherFromNotification.class);//ApplicationUtility.GetMainLauncherActivity(context));

				notificationIntent.putExtra(Keys.Notification.NOTIFICATION_PAYLOAD, formattedNotificationString);
				notificationIntent.putExtra(Keys.Notification.IS_REMOTE_NOTIFICATION, isRemoteNotification);

				PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				String contentTitle = notificationData.optString(keyMap.getString(NotificationDefines.CONTENT_TITLE), null);
				String contentText = notificationData.optString(keyMap.getString(NotificationDefines.CONTENT_TEXT), null);
				String tickerText = notificationData.optString(keyMap.getString(NotificationDefines.TICKER_TEXT), null);
				String notificationTag = notificationData.optString(keyMap.getString(NotificationDefines.TAG), null);

				String customSoundName = notificationData.optString(keyMap.getString(NotificationDefines.CUSTOM_SOUND), "");
				String largeIconName = notificationData.optString(keyMap.getString(NotificationDefines.LARGE_ICON), "");

				NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
				if (NotificationDefines.needsVibration(context))
				{
					builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
				}
				else
				{
					builder.setDefaults(Notification.DEFAULT_LIGHTS);
				}

				builder.setSmallIcon(context.getApplicationInfo().icon);

				if (NotificationDefines.needsCustomIconDrawing(context))
				{
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
					{
						builder.setSmallIcon(R.drawable.app_icon_custom_white);
					}
					else
					{
						builder.setSmallIcon(R.drawable.app_icon_custom_coloured);
					}
				}

				//Set large icon now.
				Bitmap customLargeIcon = getCustomLargeIconBitmap(largeIconName);
				if (customLargeIcon != null)
				{
					builder.setLargeIcon(customLargeIcon);
				}

				builder.setWhen(System.currentTimeMillis());
				builder.setAutoCancel(true);
				builder.setContentIntent(pendingIntent);

				if (NotificationDefines.hasNotificationType(NotificationType.Sound, context))
				{
					if (StringUtility.isNullOrEmpty(customSoundName))
					{
						builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
					}
					else
					{
						PlayCustomNotificationSound(customSoundName);
					}
				}

				if (contentText != null)
				{
					if (NotificationDefines.hasNotificationType(NotificationType.Alert, context))
					{
						builder.setTicker(tickerText);

						builder.setContentTitle(contentTitle == null ? appName : contentTitle);
						builder.setContentText(contentText);

					}
					else
					{
						Debug.warning(CommonDefines.NOTIFICATION_TAG, "Alerts off. No Notification type was set");
					}
					Notification notification = builder.build();
					notificationManager.notify(notificationTag, 0, notification);

				}
				else
				{
					Debug.warning(CommonDefines.NOTIFICATION_TAG, "No data for content text to show in notification bar! key : " + keyMap.getString(NotificationDefines.CONTENT_TEXT));
					if (Debug.ENABLED)
					{
						builder.setContentText("No Message!!!");
						notificationManager.notify(notificationTag, 0, builder.build());
					}
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}

		if (isAppRunning)
		{
			// TODO make this a single point of calling. App launch and this.
			if (isRemoteNotification)
			{
				NativePluginHelper.sendMessage(UnityDefines.Notification.DID_RECEIVE_REMOTE_NOTIFICATION, formattedNotificationString);
			}
			else
			{
				NativePluginHelper.sendMessage(UnityDefines.Notification.DID_RECEIVE_LOCAL_NOTIFICATION, formattedNotificationString);
			}
		}
	}

	JSONObject getFormattedNotification(JSONObject notificationData, JSONObject keyMap)
	{
		JSONObject formattedNotification = null;

		try
		{

			formattedNotification = new JSONObject(notificationData, JSONUtility.getKeys(notificationData));

			formattedNotification.put(keyMap.getString(NotificationDefines.FIRE_DATE), System.currentTimeMillis());

			if (formattedNotification.has(keyMap.getString(NotificationDefines.USER_INFO)))
			{
				try
				{
					String userInfoString = formattedNotification.getString(keyMap.getString(NotificationDefines.USER_INFO));
					formattedNotification.remove(keyMap.getString(NotificationDefines.USER_INFO));
					formattedNotification.put(keyMap.getString(NotificationDefines.USER_INFO), new JSONObject(userInfoString));
				}
				catch (Exception e)
				{
					Debug.error(CommonDefines.NOTIFICATION_TAG, "UserInfo Data should be a dictionary");
					Debug.error(CommonDefines.NOTIFICATION_TAG, e.getMessage());
				}
			}

		}
		catch (JSONException e)
		{
			e.printStackTrace();
			formattedNotification = new JSONObject();
		}

		return formattedNotification;

	}

	Bitmap getCustomLargeIconBitmap(String largeIconName)
	{
		if (!StringUtility.isNullOrEmpty(largeIconName))
		{
			Bitmap largeIcon = null;
			InputStream stream = null;

			int resourceId = ApplicationUtility.getResourceId(context, largeIconName.toLowerCase(Locale.US), "raw");

			if (resourceId == 0)
			{

				stream = getLargeIconStreamFromAssetsFolder(CommonDefines.PROJECT_ASSETS_FOLDER_OLD + largeIconName);

				if (stream == null)
				{
					Debug.error(CommonDefines.NOTIFICATION_TAG, "Custom icon set for notification not found. Make sure it is kept in " + CommonDefines.PROJECT_ASSETS_EXPECTED_FOLDER);
				}
			}
			else
			{
				stream = context.getResources().openRawResource(resourceId);
			}

			largeIcon = BitmapFactory.decodeStream(stream);

			return largeIcon;
		}
		else
		{
			return null;
		}
	}

	void PlayCustomNotificationSound(String customSoundName)
	{
		if (!StringUtility.isNullOrEmpty(customSoundName))
		{
			AssetFileDescriptor assetFileDescriptor = null;
			try
			{
				//get the resource id from the file name  
				int resourceId = ApplicationUtility.getResourceId(context, customSoundName.toLowerCase(Locale.US), "raw");

				if (resourceId == 0)
				{
					//Check deprecated path for backward compat
					assetFileDescriptor = getCustomNotificationSoundFromAssetsFolder(customSoundName);

					if (assetFileDescriptor == null)
					{
						Debug.error(CommonDefines.NOTIFICATION_TAG, "Expecting " + customSoundName + " in " + CommonDefines.PROJECT_ASSETS_EXPECTED_FOLDER);
					}
				}
				else
				{
					assetFileDescriptor = context.getResources().openRawResourceFd(resourceId);
				}

				//Start media player directly.
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
				mediaPlayer.prepare();
				mediaPlayer.start();

				mediaPlayer.setOnCompletionListener(new OnCompletionListener()
					{
						@Override
						public void onCompletion(MediaPlayer player)
						{
							player.release();
						}
					});

				assetFileDescriptor.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	@Deprecated
	AssetFileDescriptor getCustomNotificationSoundFromAssetsFolder(String customSoundName)
	{
		AssetFileDescriptor assetFileDescriptor = null;

		AssetManager assetManager = context.getAssets();

		try
		{
			assetFileDescriptor = assetManager.openFd((CommonDefines.PROJECT_ASSETS_FOLDER_OLD) + customSoundName);
			Debug.error(CommonDefines.NOTIFICATION_TAG, "This path for custom sounds is deprecated! Keep your files in " + CommonDefines.PROJECT_ASSETS_EXPECTED_FOLDER);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return assetFileDescriptor;
	}

	@Deprecated
	InputStream getLargeIconStreamFromAssetsFolder(String path)
	{
		InputStream stream = null;
		try
		{
			stream = context.getAssets().open(path);
			Debug.error(CommonDefines.NOTIFICATION_TAG, "This path for custom large icon is deprecated! Keep your files in " + CommonDefines.PROJECT_ASSETS_EXPECTED_FOLDER);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return stream;
	}
}