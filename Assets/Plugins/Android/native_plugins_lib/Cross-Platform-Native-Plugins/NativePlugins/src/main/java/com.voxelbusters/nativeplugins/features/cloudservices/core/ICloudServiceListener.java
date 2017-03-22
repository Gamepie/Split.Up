package com.voxelbusters.nativeplugins.features.cloudservices.core;

public interface ICloudServiceListener
{
	public void onReceivingCloudData(String jsonData, String accountName);

	public void onReceivingErrorOnLoad();

	public void onCommitingCloudData(boolean success, String commitedData);
}
