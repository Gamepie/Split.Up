package com.voxelbusters.nativeplugins.features.notification.core;

import android.content.Context;

// Make this class abstract
public class BasicRemoteNotificationService implements IRemoteNotificationService
{
	public String								serviceName	= "Basic Notification Service";
	private IRemoteNotificationServiceListener	listener	= null;
	protected Context							context		= null;

	public BasicRemoteNotificationService(Context context)
	{
		this.context = context;
	}

	@Override
	public void register(String[] senderIds)
	{
	}

	@Override
	public void unRegister()
	{

	}

	// Override this method in each subclass for sending the availability of its
	// service
	@Override
	public boolean isAvailable()
	{
		return false;
	}

	// Some common methods

	@Override
	public void setListener(IRemoteNotificationServiceListener listener)
	{
		this.listener = listener;
	}

	public IRemoteNotificationServiceListener getListener()
	{
		return listener;
	}
}
