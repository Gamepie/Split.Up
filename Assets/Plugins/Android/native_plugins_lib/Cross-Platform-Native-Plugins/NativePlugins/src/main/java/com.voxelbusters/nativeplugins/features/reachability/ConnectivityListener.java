package com.voxelbusters.nativeplugins.features.reachability;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityListener extends BroadcastReceiver
{
	boolean	isConnected;

	@Override
	public void onReceive(Context context, Intent arg1)
	{
		//On receiving this event, find out if we are reachable 
		updateConnectionStatus(context);

	}

	public void updateConnectionStatus(Context context)
	{
		boolean connectionStatus = false;

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if ((networkInfo != null) && networkInfo.isConnected())
		{
			connectionStatus = true;
		}
		else
		{
			connectionStatus = false;
		}

		NetworkReachabilityHandler.sendWifiReachabilityStatus(connectionStatus);
	}
}
