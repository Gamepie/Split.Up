package com.voxelbusters.nativeplugins.features.notification;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.features.notification.core.IRemoteNotificationService;
import com.voxelbusters.nativeplugins.features.notification.core.IRemoteNotificationServiceListener;
import com.voxelbusters.nativeplugins.features.notification.core.RemoteNotificationRegistrationInfo;
import com.voxelbusters.nativeplugins.features.notification.serviceprovider.gcm.GCM;
import com.voxelbusters.nativeplugins.utilities.Debug;

public class RemoteNotification implements IRemoteNotificationServiceListener
{
	static IRemoteNotificationService	serviceProvider	= null;

	// Create singleton instance
	private static RemoteNotification	INSTANCE;

	public static RemoteNotification getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new RemoteNotification();
			INSTANCE.checkForService();// find if any service is available for support or not. For now we have GCM alone for support.
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private RemoteNotification()
	{
	}

	void checkForService()
	{
		Debug.log(CommonDefines.NOTIFICATION_TAG, "checking for available Notification service...", true);
		IRemoteNotificationService[] list = new IRemoteNotificationService[] { new GCM(NativePluginHelper.getCurrentContext()),
		// new Amazon()
		};

		for (IRemoteNotificationService element : list)
		{

			if (element.isAvailable()) // Check for availability of each service
			{
				serviceProvider = element;
				break; // Will break once we find one service availability
			}
		}
		if (serviceProvider != null)
		{
			Debug.log(CommonDefines.NOTIFICATION_TAG, "Found Notification Service " + serviceProvider.getClass().getSimpleName(), true);
		}
		else
		{
			Debug.error(CommonDefines.NOTIFICATION_TAG, "No remote notification service found!");
		}
	}

	// Service Actions
	public void registerForRemoteNotifications(String[] senderIds)
	{
		if (isServiceAvailable())
		{
			serviceProvider.setListener(this);
			serviceProvider.register(senderIds);
		}
	}

	public void unregisterForRemoteNotifications()
	{
		if (isServiceAvailable())
		{
			serviceProvider.unRegister();
		}
	}

	// Helpers
	public boolean isServiceAvailable()
	{
		boolean available;

		if (serviceProvider != null)
		{
			available = true;
		}
		else
		{
			Debug.error(CommonDefines.NOTIFICATION_TAG, "No Remote Notification Service Available");
			available = false;
		}

		return available;
	}

	// Callbacks from Service Provider
	@Override
	public void onReceivingRegistrationID(RemoteNotificationRegistrationInfo registrationInfo)
	{
		if (registrationInfo.registrationId == null)
		{
			NativePluginHelper.sendMessage(UnityDefines.Notification.DID_FAIL_TO_REGISTER_FOR_REMOTE_NOTIFICATION, registrationInfo.errorMsg);
		}
		else
		{
			NativePluginHelper.sendMessage(UnityDefines.Notification.DID_REGISTER_FOR_REMOTE_NOTIFICATION, registrationInfo.registrationId);
		}
	}

	@Override
	public void onUnRegistration(String status)
	{
		Debug.log(CommonDefines.NOTIFICATION_TAG, "On unregistering remote notifcation " + status);
	}
}
