package com.voxelbusters.nativeplugins.features.cloudservices.core;

import android.content.Context;

import com.voxelbusters.nativeplugins.features.gameservices.core.BasicGameService.eGameServiceState;

public class BaseCloudService implements ICloudService
{

	protected ICloudServiceListener	listener;

	protected eGameServiceState		state;
	protected Context				context;

	public BaseCloudService(Context context, ICloudServiceListener listener)
	{
		this.context = context;
		state = eGameServiceState.NONE;
		this.listener = listener;
	}

	@Override
	public boolean isAvailable(boolean resolveIfNotAvailable)
	{
		return false;
	}

	@Override
	public void initialise()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void signIn()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void signOut()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void loadFromCloud()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void synchronise()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToCloud(String data)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSignedIn()
	{
		return false;
	}
}
