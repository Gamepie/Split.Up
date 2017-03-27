package com.voxelbusters.nativeplugins.features.gameservices;

import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.features.gameservices.core.IGameServices;
import com.voxelbusters.nativeplugins.features.gameservices.core.IGameServicesAuthListener;
import com.voxelbusters.nativeplugins.features.gameservices.core.IGameServicesPlayerListener;
import com.voxelbusters.nativeplugins.features.gameservices.serviceprovider.google.GooglePlayGameService;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.JSONUtility;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class GameServicesHandler implements IGameServicesPlayerListener, IGameServicesAuthListener
{
	// Create singleton instance
	private static GameServicesHandler	INSTANCE;

	private final IGameServices			service;
	HashMap<String, Integer>			keyMap	= new HashMap<String, Integer>();

	public static GameServicesHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new GameServicesHandler();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private GameServicesHandler()
	{
		service = GooglePlayGameService.getInstance(NativePluginHelper.getCurrentContext());

		keyMap.put(Keys.GameServices.TIME_SCOPE_ALL_TIME, LeaderboardVariant.TIME_SPAN_ALL_TIME);
		keyMap.put(Keys.GameServices.TIME_SCOPE_WEEK, LeaderboardVariant.TIME_SPAN_WEEKLY);
		keyMap.put(Keys.GameServices.TIME_SCOPE_TODAY, LeaderboardVariant.TIME_SPAN_DAILY);

		keyMap.put(Keys.GameServices.USER_SCOPE_FRIENDS, LeaderboardVariant.COLLECTION_SOCIAL);
		keyMap.put(Keys.GameServices.USER_SCOPE_GLOBAL, LeaderboardVariant.COLLECTION_PUBLIC);
	}

	//Availability
	public boolean isServiceAvailable()
	{
		boolean isAvailable = service.isAvailable();

		if (!isAvailable)
		{
			Debug.error(CommonDefines.GAME_SERVICES_TAG, "Service not functional. Either not avaialble or update required.");
		}

		return isAvailable;
	}

	public void register(boolean useCloudServices)
	{
		service.register(useCloudServices);
		service.addListener(this, this);
	}

	public boolean isSignedIn()
	{
		return service.isSignedIn();
	}

	//Achievements
	public void loadAchievementDescriptions()
	{
		service.loadAllAchievements();
	}

	public void loadAchievements()
	{
		service.loadUserAchievements();
	}

	public String getAchievement(String achievementId)
	{
		HashMap<String, Object> hash = service.getAchievement(achievementId);

		return JSONUtility.getJSONString(hash);
	}

	public void reportProgress(String instanceId, String achievementId, int points, boolean immediate)
	{
		service.reportProgress(instanceId, achievementId, points, immediate);
	}

	public void showAchievementsUi()
	{
		service.showAchievementsUi();
	}

	//Leaderboards

	public void loadTopScores(final String instanceId, final String leaderboardId, final String timeScope, final String userScope, final int count)
	{
		Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					service.loadTopScores(instanceId, leaderboardId, keyMap.get(timeScope), keyMap.get(userScope), count);
				}
			};
		thread.start();
	}

	public void loadPlayerCenteredScores(final String instanceId, final String leaderboardId, final String timeScope, final String userScope, final int count)
	{
		Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					service.loadPlayerCenteredScores(instanceId, leaderboardId, keyMap.get(timeScope), keyMap.get(userScope), count);
				}
			};
		thread.start();

	}

	public void loadMoreScores(final String instanceId, final String leaderboardId, final int direction, final int count)
	{

		Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					service.loadMoreScrores(instanceId, leaderboardId, direction, count);
				}
			};
		thread.start();
	}

	public void reportScore(String instanceId, String leaderboardId, long score, boolean immediate)
	{
		service.reportScore(instanceId, leaderboardId, score, immediate);
	}

	public void showLeaderboardsUi(String leaderboardId, String timeScope)
	{
		service.showLeaderboardsUi(leaderboardId, keyMap.get(timeScope));
	}

	//Users API
	public void loadUsers(String instanceId, String userIdListJsonStr)
	{
		String[] userList = StringUtility.convertJsonStringToStringArray(userIdListJsonStr);
		service.loadUsers(instanceId, userList);
	}

	public void loadLocalUserFriends(boolean loadFromServer)
	{
		service.loadLocalPlayerFriends(loadFromServer);
	}

	public void authenticateLocalUser()
	{
		service.signIn();
	}

	public void signOut()
	{
		service.signOut();
	}

	public void loadProfilePicture(final String requestId, final String uriString)
	{

		Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					service.loadProfileImage(requestId, uriString);
				}
			};

		NativePluginHelper.executeOnUIThread(runnable);
	}

	@Override
	public void onConnected(HashMap<String, Object> playerMap, String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		if (error != null)
		{
			data.put(Keys.GameServices.ERROR, error);
		}
		data.put(Keys.GameServices.LOCAL_USER_INFO, playerMap);
		NativePluginHelper.sendMessage(UnityDefines.GameServices.AUTHENTICATION_FINISHED, data);
	}

	@Override
	public void onDisConnected()
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put(Keys.GameServices.ERROR, "Disconnected!");
		NativePluginHelper.sendMessage(UnityDefines.GameServices.AUTHENTICATION_FINISHED, data);
	}

	@Override
	public void onConnectionSuspended()
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put(Keys.GameServices.ERROR, "Connection Suspended!");
		NativePluginHelper.sendMessage(UnityDefines.GameServices.AUTHENTICATION_FINISHED, data);
	}

	@Override
	public void onConnectionFailure()
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put(Keys.GameServices.ERROR, "Connection Failure while connecting!");
		NativePluginHelper.sendMessage(UnityDefines.GameServices.AUTHENTICATION_FINISHED, data);
	}

	@Override
	public void onLoadingScores(String instanceId, HashMap<String, Object> localPlayerScore, ArrayList<HashMap<String, Object>> scores, String error)
	{

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put(Keys.GameServices.INSTANCE_ID, instanceId);
		if (error != null)
		{
			data.put(Keys.GameServices.ERROR, error);
		}
		else
		{
			HashMap<String, Object> leaderboardInfo = new HashMap<String, Object>();
			leaderboardInfo.put(Keys.GameServices.LEADERBOARD_LOCAL_SCORE, localPlayerScore);
			leaderboardInfo.put(Keys.GameServices.LEADERBOARD_SCORES_LIST, scores);

			data.put(Keys.GameServices.LEADERBOARD_INFO, leaderboardInfo);
		}

		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_SCORES, data);
	}

	@Override
	public void onLoadAchievementDetails(HashMap<String, Object> data)
	{
		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_ACHIEVEMENT_DESCRIPTIONS, data);
	}

	@Override
	public void onLoadUserAchievements(HashMap<String, Object> data)
	{
		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_ACHIEVEMENTS, data);
	}

	@Override
	public void onLoadLocalUserFriendsDetails(ArrayList<HashMap<String, Object>> friendsData, String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		if (error != null)
		{
			data.put(Keys.GameServices.ERROR, error);
		}
		data.put(Keys.GameServices.LOCAL_USER_FRIENDS, friendsData);
		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_LOCAL_USER_FRIENDS_REQUEST, data);
	}

	@Override
	public void onLoadLocalUserDetails(HashMap<String, Object> userHash)
	{
		//Send here details of local player
	}

	@Override
	public void onLoadUserProfiles(String instanceId, ArrayList<HashMap<String, Object>> users, String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		if (error != null)
		{
			data.put(Keys.GameServices.ERROR, error);
		}
		data.put(Keys.GameServices.INSTANCE_ID, instanceId);
		data.put(Keys.GameServices.USERS_LIST, users);

		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_USER_PROFILES_LIST, data);
	}

	@Override
	public void onLoadProfilePicture(String requestId, String filePath, String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		if (error != null)
		{
			data.put(Keys.GameServices.ERROR, error);
		}
		data.put(Keys.GameServices.INSTANCE_ID, requestId);
		data.put(Keys.GameServices.IMAGE_FILE_PATH, filePath);

		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_PICTURE_LOAD_REQUEST, data);
	}

	@Override
	public void onReportProgress(String instanceID, HashMap<String, Object> achievementData, String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put(Keys.GameServices.INSTANCE_ID, instanceID);
		if (achievementData != null)
		{
			data.put(Keys.GameServices.ACHIEVEMENT_INFO, achievementData);
		}

		if (error != null)
		{
			data.put(Keys.GameServices.ERROR, error);
		}

		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_REPORT_PROGRESS, data);
	}

	@Override
	public void onReportScore(String instanceId, HashMap<String, Object> scoreDetails, String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put(Keys.GameServices.INSTANCE_ID, instanceId);
		data.put(Keys.GameServices.SCORE_INFO, scoreDetails);

		if (error != null)
		{
			data.put(Keys.GameServices.ERROR, error);
		}

		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_REPORT_SCORE, data);
	}

	@Override
	public void onAchievementsUIClosed()
	{
		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_ACHIEVEMENTS_UI_CLOSED);
	}

	@Override
	public void onLeaderboardsUIClosed()
	{
		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_LEADERBOARDS_UI_CLOSED);
	}

	@Override
	public void onSignOut(String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();

		if (!StringUtility.isNullOrEmpty(error))
		{
			data.put(Keys.GameServices.ERROR, error);
		}

		NativePluginHelper.sendMessage(UnityDefines.GameServices.RECEIVED_SIGN_OUT_STATUS, data);
	}

}
