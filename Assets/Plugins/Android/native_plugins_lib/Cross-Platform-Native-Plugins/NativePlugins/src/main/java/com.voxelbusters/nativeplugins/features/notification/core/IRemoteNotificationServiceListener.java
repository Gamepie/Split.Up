package com.voxelbusters.nativeplugins.features.notification.core;

public interface IRemoteNotificationServiceListener
{
	void onReceivingRegistrationID(RemoteNotificationRegistrationInfo registrationInfo);

	void onUnRegistration(String status);
}
