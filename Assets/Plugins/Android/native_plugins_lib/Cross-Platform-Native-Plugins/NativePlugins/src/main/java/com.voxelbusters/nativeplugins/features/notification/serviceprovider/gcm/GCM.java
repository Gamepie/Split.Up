package com.voxelbusters.nativeplugins.features.notification.serviceprovider.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.features.notification.core.BasicRemoteNotificationService;
import com.voxelbusters.nativeplugins.features.notification.core.RemoteNotificationRegistrationInfo;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;
import com.voxelbusters.nativeplugins.utilities.Debug;

import java.io.IOException;

public class GCM extends BasicRemoteNotificationService
{
	public GCM(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}

	GoogleCloudMessaging	service;

	@Override
	public void register(String[] senderIDs)
	{
		service = GoogleCloudMessaging.getInstance(context);
		registerInBackground(senderIDs);
	}

	@Override
	public void unRegister()
	{
		if (service != null)
		{
			unRegisterInBackground();
		}
	}

	@Override
	public boolean isAvailable()
	{
		boolean playServicesAvailable = ApplicationUtility.isGooglePlayServicesAvailable(context, false);

		return playServicesAvailable;
	}

	// Registration and UnRegistration are blocked calls. so should be in
	// seperate threads
	private void registerInBackground(final String[] senderIDs)
	{
		new AsyncTask<String, Void, RemoteNotificationRegistrationInfo>()
			{

				@Override
				protected RemoteNotificationRegistrationInfo doInBackground(String... params)
				{
					RemoteNotificationRegistrationInfo registrationInfo = new RemoteNotificationRegistrationInfo();
					registrationInfo.registrationId = null;

					try
					{
						registrationInfo.registrationId = service.register(senderIDs);
						Debug.log(CommonDefines.NOTIFICATION_TAG, "GCM Registration ID = " + registrationInfo.registrationId);

					}
					catch (Exception ex)
					{
						String error = ex.getMessage();
						Debug.error(CommonDefines.NOTIFICATION_TAG, "GCM Registration Failed : " + error);
						registrationInfo.errorMsg = error;
					}

					return registrationInfo;
				}

				@Override
				protected void onPostExecute(RemoteNotificationRegistrationInfo result)
				{
					GCM.this.getListener().onReceivingRegistrationID(result);
				}

			}.execute(null, null, null);
	}

	private void unRegisterInBackground()
	{
		new AsyncTask<Void, Void, String>()
			{

				@Override
				protected String doInBackground(Void... params)
				{
					String status = "SUCCESS";
					try
					{
						service.unregister();
					}
					catch (IOException ex)
					{
						status = "FAILED : " + ex.getMessage();
					}

					return status;
				}

				@Override
				protected void onPostExecute(String status)
				{
					GCM.this.getListener().onUnRegistration(status);
				}

			}.execute(null, null, null);
	}

}
