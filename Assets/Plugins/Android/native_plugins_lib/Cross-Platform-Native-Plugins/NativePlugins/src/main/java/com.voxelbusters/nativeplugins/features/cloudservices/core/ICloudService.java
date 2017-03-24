package com.voxelbusters.nativeplugins.features.cloudservices.core;

public interface ICloudService
{
	boolean isAvailable(boolean resolveIfNotAvailable);

	void initialise();

	void signIn();

	void signOut();

	boolean isSignedIn();

	void loadFromCloud();

	void saveToCloud(String data);

	void synchronise();
}
