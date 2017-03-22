package com.voxelbusters.nativeplugins.features.gameservices.serviceprovider.google;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.common.images.ImageManager.OnImageLoadedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayerBuffer;
import com.google.android.gms.games.Players.LoadPlayersResult;
import com.google.android.gms.plus.Plus;
import com.voxelbusters.androidnativeplugin.R;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.gameservices.core.BasicGameService;
import com.voxelbusters.nativeplugins.features.gameservices.core.IGameServicesAuthListener;
import com.voxelbusters.nativeplugins.features.gameservices.core.IGameServicesPlayerListener;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.FileUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class GooglePlayGameService extends BasicGameService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

	private GoogleApiClient				apiService;

	public static final int				REQUEST_CODE_SIGN_IN_CONNECTION_FAILURE	= 1000;

	private final String				TAG										= "GooglePlayGameService";
	public static GooglePlayGameService	instance								= null;

	GooglePlayAchievements				achievements;
	GooglePlayLeaderboards				leaderboards;
	boolean								resolveAvailabilityError				= false;
	boolean								useCloudServices						= false;

	IGameServicesPlayerListener			playerListener;

	//Returns singleton instance
	public static GooglePlayGameService getInstance()
	{
		return instance;
	}

	public static GooglePlayGameService getInstance(final Context context)
	{
		if (instance != null)
		{
			return instance;
		}
		else
		{
			return new GooglePlayGameService(context);
		}
	}

	private GooglePlayGameService(final Context context)
	{
		super(context);

		instance = this;
		apiService = null;
	}

	@Override
	public void register(boolean useCloudServices)
	{
		this.useCloudServices = useCloudServices;
		resolveAvailabilityError = false;
		if (isAvailable())
		{
			registerAPIClient();
		}
		resolveAvailabilityError = true;
	}

	void registerAPIClient()
	{
		Builder builder = new GoogleApiClient.Builder(context).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).addApi(Games.API).addScope(Games.SCOPE_GAMES);

		if (useCloudServices)
		{
			builder = builder.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER);
		}

		apiService = builder.build();
		achievements = new GooglePlayAchievements(apiService);
		leaderboards = new GooglePlayLeaderboards(apiService);
	}

	@Override
	public void addListener(IGameServicesAuthListener auth, IGameServicesPlayerListener player)
	{
		super.addListener(auth, player);

		if (player != null)
		{
			achievements.setListener(player);
			leaderboards.setListener(player);
		}

		// Update player listener with top one if any.
		playerListener = getPlayerListener();

	}

	@Override
	public void removeListener(IGameServicesAuthListener auth, IGameServicesPlayerListener player)
	{
		super.removeListener(auth, player);

		if (player != null)
		{
			achievements.removeListener(player);
			leaderboards.removeListener(player);
		}
	}

	@Override
	public boolean isAvailable()
	{
		boolean isSupported = ApplicationUtility.isGooglePlayServicesAvailable(context, resolveAvailabilityError);
		return isSupported;
	}

	@Override
	public void signIn()
	{
		if (isAvailable())
		{

			if (getApiService() == null)
			{
				registerAPIClient();
			}

			super.signIn();
			apiService.connect();
		}
	}

	@Override
	public void signOut()
	{
		if (isAvailable())
		{
			super.signOut();

			if ((apiService != null) && apiService.isConnected())
			{
				Games.signOut(apiService);
				apiService.disconnect();
			}

			onSignOut(null);
		}
	}

	@Override
	public boolean isSignedIn()
	{
		return ((apiService != null) && apiService.isConnected());
	}

	@Override
	public void reportProgress(String instanceId, String achievementId, int points, boolean immediate)
	{
		if (isAvailable())
		{
			achievements.reportProgress(instanceId, achievementId, points, immediate);
		}
	}

	@Override
	public void loadAllAchievements()
	{
		if (isAvailable())
		{
			achievements.loadAchievementsDetails(true);
		}
	}

	@Override
	public void loadUserAchievements()
	{
		if (isAvailable())
		{
			achievements.loadAchievementsDetails(false);
		}
	}

	@Override
	public HashMap<String, Object> getAchievement(String achievementId)
	{
		if (isAvailable())
		{
			return achievements.getAchievementHash(achievementId);
		}
		return null;
	}

	@Override
	public void showAchievementsUi()
	{
		if (isAvailable())
		{
			achievements.showUI(context);
		}
	}

	@Override
	public void reportScore(String instanceId, String leaderboardId, long score, boolean immediate)
	{
		if (isAvailable())
		{
			leaderboards.reportScore(instanceId, leaderboardId, score, immediate);
		}
	}

	@Override
	public void showLeaderboardsUi(String leaderboardUi, int timeSpan)
	{
		if (isAvailable())
		{
			leaderboards.showUI(context, leaderboardUi, timeSpan);
		}
	}

	@Override
	public void loadPlayerCenteredScores(String instanceId, String leaderboardId, int span, int collectionType, int count)
	{
		leaderboards.loadPlayerCenteredScores(instanceId, leaderboardId, span, collectionType, count);
	}

	@Override
	public void loadTopScores(String instanceId, String leaderboardId, int span, int collectionType, int count)
	{
		leaderboards.loadTopScores(instanceId, leaderboardId, span, collectionType, count);
	}

	@Override
	public void loadMoreScrores(String instanceId, String leaderboardId, int direction, int count)
	{
		leaderboards.loadMoreScores(instanceId, leaderboardId, direction, count);
	}

	@Override
	public void loadUsers(final String instanceId, String[] userIdList)
	{
		if (isAvailable())
		{
			new AsyncTask<String[], Void, ArrayList<HashMap<String, Object>>>()
				{

					@Override
					protected ArrayList<HashMap<String, Object>> doInBackground(String[]... params)
					{
						String[] userList = params[0];

						ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();

						for (String eachId : userList)
						{
							PendingResult<LoadPlayersResult> loadedPlayerResult = Games.Players.loadPlayer(apiService, eachId);
							PlayerBuffer playerBuffer = getPlayerBuffer(loadedPlayerResult);

							if (playerBuffer != null)
							{
								if (playerBuffer.getCount() > 0)
								{
									result.add(getPlayerHash(playerBuffer.get(0)));
								}
								playerBuffer.release();
							}

						}

						return result;
					}

					@Override
					protected void onPostExecute(ArrayList<HashMap<String, Object>> result)
					{
						//Call callback here
						String error = null;
						if (result == null)
						{
							error = "Error loading user friend details.";
						}
						if (playerListener != null)
						{
							playerListener.onLoadUserProfiles(instanceId, result, error);
						}
					}

				}.execute(userIdList);
		}
	}

	@Override
	public void requestLocalPlayerDetails()
	{
		Player currentPlayer = Games.Players.getCurrentPlayer(apiService);
		if (playerListener != null)
		{
			playerListener.onLoadLocalUserDetails(getPlayerHash(currentPlayer));
		}
	}

	@Override
	public void loadLocalPlayerFriends(final boolean forceLoad)
	{

		if (isAvailable())
		{
			new AsyncTask<Void, Void, ArrayList<HashMap<String, Object>>>()
				{

					@Override
					protected ArrayList<HashMap<String, Object>> doInBackground(Void... params)
					{

						ArrayList<HashMap<String, Object>> result = null;

						PendingResult<LoadPlayersResult> loadedPlayerResult = Games.Players.loadConnectedPlayers(getApiService(), forceLoad);

						PlayerBuffer playerBuffer = getPlayerBuffer(loadedPlayerResult);

						if (playerBuffer != null)
						{
							result = new ArrayList<HashMap<String, Object>>();
							for (int i = 0; i < playerBuffer.getCount(); i++)
							{
								Player eachFriend = playerBuffer.get(i);
								result.add(getPlayerHash(eachFriend));
							}

							playerBuffer.release();
						}

						return result;
					}

					@Override
					protected void onPostExecute(ArrayList<HashMap<String, Object>> result)
					{

						//Call callback here
						String error = null;
						if (result == null)
						{
							error = "Error loading user friend details.";
						}

						if (playerListener != null)
						{
							playerListener.onLoadLocalUserFriendsDetails(result, error);
						}
					}

				}.execute();
		}
	}

	//Callback for connection
	@Override
	public void onConnectionFailed(ConnectionResult result)
	{

		if (state == eGameServiceState.RESOLVING_ERROR)
		{
			// Already resolving
			return;
		}

		String errorStr = ApplicationUtility.getString(context, R.string.gameservices_sign_in_failed);

		if (apiService != null)
		{
			state = eGameServiceState.RESOLVING_ERROR;
			Intent intent = new Intent(context, GooglePlayGameUIActivity.class);
			intent.putExtra(Keys.TYPE, Keys.GameServices.ON_CONNECTION_FAILURE);
			intent.putExtra(Keys.GameServices.RESULT_CODE, result.getErrorCode());
			intent.putExtra(Keys.GameServices.PENDING_INTENT, result.getResolution());
			intent.putExtra(Keys.GameServices.DISPLAY_STRING, errorStr);

			context.startActivity(intent);
		}
		else
		{
			Debug.error(CommonDefines.GAME_SERVICES_TAG, "google API client instance creation failed. Check if service isAvailable()");
			finishedResolvingConnectionFailure();
			reportConnectionFailed();
		}

		Debug.error(CommonDefines.GAME_SERVICES_TAG, errorStr);

	}

	@Override
	public void onConnected(Bundle bundle)
	{
		Debug.log(TAG, "onConnected(): connected to Google APIs");
		Player player = Games.Players.getCurrentPlayer(getApiService());
		onConnected(getPlayerHash(player), null);
	}

	@Override
	public void onConnectionSuspended(int reasonCode)
	{
		Debug.log(TAG, "onConnectionSuspended(): attempting to reconnect");
		apiService.connect();

		onConnectionSuspended();
	}

	@Override
	public void loadProfileImage(final String id, final String contentUriString)
	{

		Uri contentUri = Uri.parse(contentUriString);
		ImageManager imageManager = ImageManager.create(context);

		imageManager.loadImage(new OnImageLoadedListener()
			{
				@Override
				public void onImageLoaded(Uri requestedUri, Drawable drawable, boolean arg2)
				{
					try
					{
						Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
						String destinationFolder = context.getFilesDir().getPath() + "/" + "temp" + "/";
						String filePath = FileUtility.getScaledImagePathFromBitmap(bitmap, new File(destinationFolder), "Profile_Picture_" + id + ".jpg", 1.0f);
						if (playerListener != null)
						{
							playerListener.onLoadProfilePicture(id, filePath, null);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
						String error = "Exception while loading image : " + e.toString();

						Debug.error(CommonDefines.GAME_SERVICES_TAG, error);
						if (playerListener != null)
						{
							playerListener.onLoadProfilePicture(id, null, error);
						}
					}

				}
			}, contentUri);
	}

	public void finishedResolvingConnectionFailure()
	{
		if (state == eGameServiceState.RESOLVING_ERROR)
		{
			state = eGameServiceState.NONE;
		}
	}

	public GoogleApiClient getApiService()
	{
		return apiService;
	}

	PlayerBuffer getPlayerBuffer(PendingResult<LoadPlayersResult> pendingResult)
	{
		LoadPlayersResult playersResult = pendingResult.await();

		PlayerBuffer buffer = null;
		int status = playersResult.getStatus().getStatusCode();

		if ((status == GamesStatusCodes.STATUS_OK) || (status == GamesStatusCodes.STATUS_NETWORK_ERROR_STALE_DATA))
		{
			buffer = playersResult.getPlayers();
		}
		else
		{
			try
			{
				buffer = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			Debug.error(CommonDefines.GAME_SERVICES_TAG, "Failed loading top scores with error code : " + status + " Message: " + GamesStatusCodes.getStatusString(status));
		}

		return buffer;
	}

	HashMap<String, Object> getPlayerHash(Player player)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();

		map.put(Keys.GameServices.USER_ID, player.getPlayerId());
		map.put(Keys.GameServices.USER_NAME, player.getDisplayName());
		Uri highResImageUri = player.getHiResImageUri();

		if (highResImageUri != null)
		{
			map.put(Keys.GameServices.USER_HIGH_RES_IMAGE_URL, highResImageUri.toString());
		}
		Uri iconImageUri = player.getIconImageUri();

		if (iconImageUri != null)
		{
			map.put(Keys.GameServices.USER_ICON_IMAGE_URL, iconImageUri.toString());
		}

		map.put(Keys.GameServices.USER_TIME_STAMP, player.getRetrievedTimestamp());

		return map;
	}

	public void reportConnectionFailed()
	{
		onConnectionFailure();
	}

	public void reportAchievementsUIClosed()
	{
		if (playerListener != null)
		{
			playerListener.onAchievementsUIClosed();
		}
	}

	public void reportLeaderboardsUIClosed()
	{
		if (playerListener != null)
		{
			playerListener.onLeaderboardsUIClosed();
		}
	}

	public String getSignedInPlayerId()
	{
		if (isSignedIn())
		{
			Player currentPlayer = Games.Players.getCurrentPlayer(apiService);
			return currentPlayer.getPlayerId();
		}
		else
		{
			return "default-id";
		}
	}

	IGameServicesPlayerListener getPlayerListener()
	{
		if (playerListeners.size() > 0)
		{
			return playerListeners.get(0);
		}
		else
		{
			return null;
		}
	}

	void onConnected(HashMap<String, Object> playerHash, String error)
	{
		if (authListeners.size() > 0)
		{
			for (IGameServicesAuthListener each : authListeners)
			{
				each.onConnected(playerHash, error);
			}
		}
	}

	void onConnectionSuspended()
	{
		if (authListeners.size() > 0)
		{
			for (IGameServicesAuthListener each : authListeners)
			{
				each.onConnectionSuspended();
			}
		}
	}

	void onConnectionFailure()
	{
		if (authListeners.size() > 0)
		{
			for (IGameServicesAuthListener each : authListeners)
			{
				each.onConnectionFailure();
			}
		}
	}

	void onSignOut(String error)
	{
		if (authListeners.size() > 0)
		{
			for (IGameServicesAuthListener each : authListeners)
			{
				each.onSignOut(error);
			}
		}
	}

}
