package com.voxelbusters.nativeplugins.features.cloudservices.serviceprovider.google;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.games.snapshot.Snapshots.CommitSnapshotResult;
import com.voxelbusters.nativeplugins.features.cloudservices.core.BaseCloudService;
import com.voxelbusters.nativeplugins.features.cloudservices.core.ICloudServiceListener;
import com.voxelbusters.nativeplugins.features.gameservices.core.IGameServicesAuthListener;
import com.voxelbusters.nativeplugins.features.gameservices.serviceprovider.google.GooglePlayGameService;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;
import com.voxelbusters.nativeplugins.utilities.Debug;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class GooglePlayCloudService extends BaseCloudService implements IGameServicesAuthListener
{
	static final String				TAG					= "GooglePlayCloudService";
	private GooglePlayGameService	gameService			= null;

	private final String			snapshotPrefixName	= "cpnp-snapshot";

	private final String			encodingUsed		= "UTF-8";

	private Snapshot				currentOpenedSnapshot;

	private long					totalTimePlayedPreviously;
	private long					timeStampOnLoad;

	public GooglePlayCloudService(final Context context, final ICloudServiceListener listener)
	{
		super(context, listener);
		gameService = GooglePlayGameService.getInstance(context);
		gameService.register(true);
		gameService.addListener(this, null);
	}

	@Override
	public boolean isAvailable(boolean resolveIfNotAvailable)
	{
		return ApplicationUtility.isGooglePlayServicesAvailable(context, resolveIfNotAvailable);
	}

	@Override
	public void initialise()
	{
		super.initialise();
		signIn();
	}

	@Override
	public void signIn()
	{
		gameService.signIn();
	}

	@Override
	public void signOut()
	{
		gameService.signOut();
	}

	@Override
	public boolean isSignedIn()
	{
		return gameService.isSignedIn();
	}

	@Override
	public void loadFromCloud()
	{
		if (isSignedIn())
		{
			openSnapShot();
		}
		else
		{
			listener.onReceivingCloudData(null, null);
		}
	}

	@Override
	public void saveToCloud(String data)
	{
		Debug.log(TAG, "saveToCloud" + data);
		if (isSignedIn())
		{
			writeSnapshot(data);
		}
		else
		{
			listener.onCommitingCloudData(true, null);
		}
	}

	@Override
	public void onConnected(HashMap<String, Object> playerHash, String error)
	{

	}

	@Override
	public void onDisConnected()
	{

	}

	@Override
	public void onConnectionSuspended()
	{

	}

	@Override
	public void onConnectionFailure()
	{

	}

	@Override
	public void onSignOut(String error)
	{

	}

	void openSnapShot()
	{
		AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>()
			{
				@Override
				protected Integer doInBackground(Void... params)
				{
					String snapshotName = snapshotPrefixName + "-" + gameService.getSignedInPlayerId();

					Snapshots.OpenSnapshotResult result = Games.Snapshots.open(gameService.getApiService(), snapshotName, true, Snapshots.RESOLUTION_POLICY_LONGEST_PLAYTIME).await();

					currentOpenedSnapshot = null;
					if (result.getStatus().isSuccess())
					{
						Snapshot snapshot = result.getSnapshot();
						// Read the byte content of the saved game.
						try
						{
							byte[] data = snapshot.getSnapshotContents().readFully();
							String encodedString = new String(data, encodingUsed);

							currentOpenedSnapshot = snapshot;
							totalTimePlayedPreviously = snapshot.getMetadata().getPlayedTime();

							if (totalTimePlayedPreviously < 0) //No previous history
							{
								totalTimePlayedPreviously = 0;
							}
							timeStampOnLoad = System.currentTimeMillis();

							listener.onReceivingCloudData(encodedString, snapshotName);
						}
						catch (IOException e)
						{
							Debug.error(TAG, "Error while reading Snapshot. Try again later..." + e);
							listener.onReceivingErrorOnLoad();
						}
					}
					else
					{
						Debug.error(TAG, "Error while loading: " + result.getStatus().getStatusCode());
						listener.onReceivingErrorOnLoad();
					}

					return result.getStatus().getStatusCode();
				}

				@Override
				protected void onPostExecute(Integer status)
				{
					Debug.log(TAG, "Done with open snapshot request with status code : " + status);
				}
			};

		task.execute();
	}

	private void writeSnapshot(final String dataString)
	{
		Debug.log(TAG, "Data String " + dataString);
		if (currentOpenedSnapshot != null)
		{
			AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>()
				{
					@Override
					protected Boolean doInBackground(Void... params)
					{
						long timePlayedInthisSession = System.currentTimeMillis() - timeStampOnLoad;
						long timePlayedInTotal = totalTimePlayedPreviously + timePlayedInthisSession;

						Snapshot snapshot = currentOpenedSnapshot;

						byte[] data = null;
						Boolean status = false;
						String cachedDataString = null;

						try
						{
							data = dataString.getBytes(encodingUsed);
							snapshot.getSnapshotContents().writeBytes(data);

							// Create the change operation
							SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder().setPlayedTimeMillis(timePlayedInTotal).build();

							Debug.log(TAG, "Total Played Time - " + timePlayedInTotal + "Played time for this session" + timePlayedInthisSession);
							// Commit the operation

							CommitSnapshotResult result = Games.Snapshots.commitAndClose(gameService.getApiService(), snapshot, metadataChange).await();

							if (result.getStatus().isSuccess())
							{
								cachedDataString = dataString;
								status = true;
							}
							else
							{
								Debug.log(TAG, "Error commiting to snapshot");
								Games.Snapshots.discardAndClose(gameService.getApiService(), snapshot);
							}

						}
						catch (UnsupportedEncodingException e)
						{
							e.printStackTrace();
							Debug.error(TAG, "Error converting dataString to bytes!!!");
						}

						currentOpenedSnapshot = null;
						listener.onCommitingCloudData(status, cachedDataString);
						return status;
					}

					@Override
					protected void onPostExecute(Boolean status)
					{
						Debug.log(TAG, "Commiting to cloud status :" + status);
					}
				};

			task.execute();
		}
		else
		{
			Debug.error(TAG, "No current opened snapshot found!!!");
			listener.onCommitingCloudData(false, null);
		}

	}
}
