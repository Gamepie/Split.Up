package com.voxelbusters.nativeplugins.features.notification.core;

public interface IRemoteNotificationService
{
	void register(String[] senderIDs);

	void unRegister();

	boolean isAvailable();

	void setListener(IRemoteNotificationServiceListener listener);

}
