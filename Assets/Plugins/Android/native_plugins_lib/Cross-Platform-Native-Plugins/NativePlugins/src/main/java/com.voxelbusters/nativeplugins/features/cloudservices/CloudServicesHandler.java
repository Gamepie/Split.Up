package com.voxelbusters.nativeplugins.features.cloudservices;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.features.cloudservices.core.ICloudService;
import com.voxelbusters.nativeplugins.features.cloudservices.core.ICloudServiceListener;
import com.voxelbusters.nativeplugins.features.cloudservices.serviceprovider.google.GooglePlayCloudService;

import java.util.HashMap;

public class CloudServicesHandler implements ICloudServiceListener
{
	// Create singleton instance
	private static CloudServicesHandler	INSTANCE;

	private final ICloudService			service;

	public static CloudServicesHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new CloudServicesHandler();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private CloudServicesHandler()
	{
		service = new GooglePlayCloudService(NativePluginHelper.getCurrentContext(), this);
	}

	public boolean isAvailable()
	{
		return service.isAvailable(false);
	}

	public void initialise()
	{
		if (service.isAvailable(true))
		{
			service.initialise();
		}
	}

	public boolean isSignedIn()
	{
		if (service.isAvailable(false))
		{
			return service.isSignedIn();
		}
		return false;
	}

	public void loadCloudData()
	{
		if (service.isAvailable(true))
		{
			service.loadFromCloud();
		}
	}

	public void saveCloudData(String data)
	{
		if (service.isAvailable(true))
		{
			service.saveToCloud(data);
		}
	}

	@Override
	public void onReceivingCloudData(String jsonData, String accountName)
	{
		// Pass to unity
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put(Keys.CloudServices.CLOUD_ACCOUNT_NAME, accountName == null ? "" : accountName);
		data.put(Keys.CloudServices.NEW_CLOUD_DATA, jsonData == null ? "" : jsonData);

		NativePluginHelper.sendMessage(UnityDefines.CloudServices.RECEIVED_NEW_CLOUD_DATA, data);

	}

	@Override
	public void onReceivingErrorOnLoad()
	{
		// Pass to unity
		NativePluginHelper.sendMessage(UnityDefines.CloudServices.ERROR_LOADING_CLOUD_DATA, "");

	}

	@Override
	public void onCommitingCloudData(boolean success, String commitedData)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put(Keys.CloudServices.IS_COMMIT_SUCCESS, success);
		data.put(Keys.CloudServices.NEW_CLOUD_DATA, commitedData == null ? "" : commitedData);

		NativePluginHelper.sendMessage(UnityDefines.CloudServices.FINISHED_COMMITING_TO_CLOUD, data);
	}

}
