package com.voxelbusters.nativeplugins.features.reachability;

import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// UNUSED CLASS

public class HostConnectionPoller
{
	//This class has a running task which will pool the host. If any error occurs it notifies the listeners

	private String	ip;
	private int		port;
	private float	timeGapBetweenPolls;		//In Seconds
	private long	connectionTimeOutPeriod;	//In Seconds
	private int		maxRetryCount;

	private int		currentRetryCount;

	@SuppressWarnings("rawtypes")
	private Future	socketFutureTask	= null;

	HostConnectionPoller()
	{
		ip = "8.8.8.8";
		port = 56;
		connectionTimeOutPeriod = 60;//60 secs
		maxRetryCount = 3;
		timeGapBetweenPolls = 2.0f;//2 secs gap
	}

	void Start()
	{
		if (socketFutureTask != null)
		{
			socketFutureTask.cancel(true);
		}

		ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();
		Runnable longRunningTask = new Runnable()
			{
				@Override
				public void run()
				{
					InetSocketAddress address = new InetSocketAddress(getIp(), getPort());

					while (true)
					{
						Socket socket = new Socket();
						try
						{
							socket.connect(address, (int) (getConnectionTimeOutPeriod() * 1000));
							ReportConnectionSuccess();
							socket.close();
						}
						catch (IOException e)
						{
							ReportConnectionFailure();
							e.printStackTrace();
						}

						try
						{
							Thread.sleep((long) (timeGapBetweenPolls * 1000));
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
			};

		socketFutureTask = threadPoolExecutor.submit(longRunningTask);
	}

	private void ReportConnectionFailure()
	{
		currentRetryCount++;

		if (currentRetryCount > getMaxRetryCount())
		{

			NetworkReachabilityHandler.sendSocketConnectionStatus(false);

			//Reset the counter to zero after reporting
			currentRetryCount = 0;
		}
	}

	private void ReportConnectionSuccess()
	{
		NetworkReachabilityHandler.sendSocketConnectionStatus(true);
	}

	public String getIp()
	{
		return ip;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public float getTimeGapBetweenPolls()
	{
		return timeGapBetweenPolls;
	}

	public void setTimeGapBetweenPolls(float timeGapBetweenPolls)
	{
		this.timeGapBetweenPolls = timeGapBetweenPolls;
	}

	public long getConnectionTimeOutPeriod()
	{
		return connectionTimeOutPeriod;
	}

	public void setConnectionTimeOutPeriod(int connectionTimeOutPeriod)
	{
		if (connectionTimeOutPeriod != 0)
		{
			this.connectionTimeOutPeriod = connectionTimeOutPeriod;
		}
		else
		{
			Debug.warning(CommonDefines.NETWORK_CONNECTIVITY_TAG, "time out value should not be zero. Considering default 60 secs for timeout");
		}
	}

	public int getMaxRetryCount()
	{
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount)
	{
		this.maxRetryCount = maxRetryCount;
	}
}
