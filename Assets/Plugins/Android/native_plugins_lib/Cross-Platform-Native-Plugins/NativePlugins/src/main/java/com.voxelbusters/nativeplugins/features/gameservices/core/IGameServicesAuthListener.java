package com.voxelbusters.nativeplugins.features.gameservices.core;

import java.util.HashMap;

public interface IGameServicesAuthListener
{
	void onConnected(HashMap<String, Object> playerHash, String error);//Signin

	void onDisConnected();//Signout

	void onConnectionSuspended();

	void onConnectionFailure();

	void onSignOut(String error);
}
