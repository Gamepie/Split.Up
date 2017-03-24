package com.voxelbusters.nativeplugins.features.reachability;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.voxelbusters.NativeBinding;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.base.interfaces.IAppLifeCycleListener;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;

public class NetworkReachabilityHandler implements IAppLifeCycleListener
{
	//Here create the polling task and register the broadcast receiver for connectivity
	ConnectivityListener						connectivityListener;

	Context										context;

	static boolean								isWifiReachable		= false;

	static boolean								isSocketConnected	= false;

	HostConnectionPoller						socketPoller;
	// Create singleton instance
	private static NetworkReachabilityHandler	INSTANCE;

	public static NetworkReachabilityHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new NetworkReachabilityHandler();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private NetworkReachabilityHandler()
	{
		socketPoller = new HostConnectionPoller();
	}

	public void initialize(String ipAddress, int port, float timeGapBetweenPolls, int connectionTimeOutPeriod, int maxRetryCount)
	{
		//Get the context
		context = NativePluginHelper.getCurrentContext();

		StartTestingNetworkHardware();
		StartSocketPoller(ipAddress, port, timeGapBetweenPolls, connectionTimeOutPeriod, maxRetryCount);

		//Start listening to app life cycle events
		NativeBinding.addAppLifeCycleListener(this);
	}

	void StartSocketPoller(String ipAddress, int port, float timeGapBetweenPolls, int connectionTimeOutPeriod, int maxRetryCount)
	{
		socketPoller.setIp(ipAddress);
		socketPoller.setPort(port);
		socketPoller.setConnectionTimeOutPeriod(connectionTimeOutPeriod);
		socketPoller.setMaxRetryCount(maxRetryCount);
		socketPoller.setTimeGapBetweenPolls(timeGapBetweenPolls);

		socketPoller.Start();
	}

	void StartTestingNetworkHardware()
	{
		pauseReachability();//This will unregister if any receivers are currently running. So that we can start fresh

		connectivityListener = new ConnectivityListener();

		registerBroadcastReceiver(connectivityListener);

		//Check at start
		connectivityListener.updateConnectionStatus(context);
	}

	void registerBroadcastReceiver(BroadcastReceiver receiver)
	{
		IntentFilter connectivityChangeIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		IntentFilter wifiStateChangeIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
		IntentFilter networkStateChangeIntentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);

		context.registerReceiver(receiver, connectivityChangeIntentFilter);
		context.registerReceiver(receiver, wifiStateChangeIntentFilter);
		context.registerReceiver(receiver, networkStateChangeIntentFilter);
	}

	public void resumeReachability()
	{
		try
		{
			registerBroadcastReceiver(connectivityListener);
			connectivityListener.updateConnectionStatus(context);
		}
		catch (IllegalArgumentException e)
		{
			Debug.warning(CommonDefines.NETWORK_CONNECTIVITY_TAG, "Already registered! " + e.getMessage());
		}
	}

	public void pauseReachability()
	{
		try
		{
			context.unregisterReceiver(connectivityListener);
		}
		catch (IllegalArgumentException e)
		{
			Debug.warning(CommonDefines.NETWORK_CONNECTIVITY_TAG, "Already unregistered!" + e.getMessage());
		}
	}

	public static void sendWifiReachabilityStatus(boolean newWifiStatus)
	{
		if (isWifiReachable != newWifiStatus)
		{
			isWifiReachable = newWifiStatus;
			NativePluginHelper.sendMessage(UnityDefines.Reachability.NETWORK_CONNECTIVITY_HARDWARE_STATUS_CHANGE, isWifiReachable ? "true" : "false");
		}
	}

	public static void sendSocketConnectionStatus(boolean newSocketStatus)
	{
		if (isSocketConnected != newSocketStatus)
		{
			isSocketConnected = newSocketStatus;
			NativePluginHelper.sendMessage(UnityDefines.Reachability.NETWORK_CONNECTIVITY_SOCKET_STATUS_CHANGE, isSocketConnected ? "true" : "false");
		}
	}

	//AppLifeCycle calls
	@Override
	public void onApplicationPause()
	{

	}

	@Override
	public void onApplicationResume()
	{

	}

	@Override
	public void onApplicationQuit()
	{
		pauseReachability();
		NativeBinding.removeAppLifeCycleListener(this);
	}
}
