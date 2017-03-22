package com.voxelbusters.nativeplugins.features.gameservices.serviceprovider.google;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.achievement.Achievements.LoadAchievementsResult;
import com.google.android.gms.games.achievement.Achievements.UpdateAchievementResult;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.gameservices.core.IGameServicesPlayerListener;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class GooglePlayAchievements
{
	String								TAG					= "GooglePlayAchievements";
	Hashtable<String, AchievementInfo>	achievementsData	= new Hashtable<String, AchievementInfo>();
	GoogleApiClient						apiService;
	IGameServicesPlayerListener			listener;

	String								instanceId			= null;

	public GooglePlayAchievements(GoogleApiClient apiService)
	{
		this.apiService = apiService;
	}

	public void setListener(IGameServicesPlayerListener listener)
	{
		this.listener = listener;
	}

	public void removeListener(IGameServicesPlayerListener listener)
	{
		if (listener == this.listener)
		{
			this.listener = null;
		}
	}

	public void reportProgress(String instanceId, String achievementId, int pointsScored, boolean immediate)
	{
		AchievementInfo achievement = GetAchievement(achievementId);

		if (achievement.type == Achievement.TYPE_INCREMENTAL)
		{

			Debug.log(TAG, "Achievement Type : " + achievement.type);
			if ((pointsScored >= 0) && (achievement.state == Achievement.STATE_HIDDEN))
			{
				//TODO : mention in docs if any progress set beyond 0.0f revels the achievement
				Debug.log(TAG, "Revel the achievement now!");
				revealAchievement(instanceId, achievementId, false);
			}


			int totalSteps = achievement.totalSteps;
			int previousReportedSteps = achievement.currentSteps; // Change this

			Debug.log(TAG, "totalSteps : " + totalSteps + " completedSteps : " + previousReportedSteps);

			if (pointsScored < previousReportedSteps)
			{
				Debug.error(TAG, "Reported negative progress!!!");
				if (listener != null)
				{
						listener.onReportProgress(instanceId, achievement.getHash(), "Reported wrong progress value!");
				}
			}
			else
			{

				int incrementedSteps = pointsScored - previousReportedSteps;

				Debug.log("Report", "incrementedSteps : " + incrementedSteps);

				if (incrementedSteps != 0)
				{
					incrementAchievement(instanceId, achievementId, incrementedSteps, immediate);
				}
				else
				{
					if (listener != null)
					{
						listener.onReportProgress(instanceId, achievement.getHash(), null);
					}
				}
			}

		}
		else
		{

			if (pointsScored == 0)
			{
				//TODO : Mention in Docs about the progress value and reveal/unlock details
				Debug.log(TAG, "This is not an incremental achievement. So just trying to reveal it as 100% progress was not sent as progress.");
				revealAchievement(instanceId, achievementId, immediate);
			}
			else if (pointsScored > 0)
			{
				Debug.log(TAG, "Unlocking Achievement");
				unlockAchievement(instanceId, achievementId, immediate);
			}
			else
			{
				if (listener != null)
				{
					listener.onReportProgress(instanceId, achievement.getHash(), "Reported wrong progress value. Cannot be negative!");
				}
			}
		}

		if (!immediate)
		{
			loadAchievementsDetails(false, false);
		}

	}

	public void revealAchievement(String instanceId, String achievementId, boolean immediate)
	{
		if (!immediate)
		{
			Games.Achievements.reveal(apiService, achievementId);
		}
		else
		{
			PendingResult<Achievements.UpdateAchievementResult> pendingResult = Games.Achievements.revealImmediate(apiService, achievementId);
			setAchievementProgressCallback(instanceId, achievementId, pendingResult);
		}
	}

	public void unlockAchievement(String instanceId, String achievementId, boolean immediate)
	{

		if (!immediate)
		{
			Games.Achievements.unlock(apiService, achievementId);
		}
		else
		{
			PendingResult<Achievements.UpdateAchievementResult> pendingResult = Games.Achievements.unlockImmediate(apiService, achievementId);
			setAchievementProgressCallback(instanceId, achievementId, pendingResult);
		}

        AchievementInfo achievement = GetAchievement(achievementId);
        achievement.SetCurrentSteps(1); // This will create a cache internally.
	}

	public void incrementAchievement(String instanceId, String achievementId, int steps, boolean immediate)
	{

		if (!immediate)
		{

			Games.Achievements.increment(apiService, achievementId, steps);
		}
		else
		{
			PendingResult<Achievements.UpdateAchievementResult> pendingResult = Games.Achievements.incrementImmediate(apiService, achievementId, steps);
			setAchievementProgressCallback(instanceId, achievementId, pendingResult);
		}

		AchievementInfo achievement = GetAchievement(achievementId);
		achievement.SetCurrentSteps(achievement.currentSteps + steps); // This will create a cache internally.
	}

	private void setAchievementProgressCallback(final String instanceId, final String achievementId, PendingResult<Achievements.UpdateAchievementResult> pendingResult)
	{
		pendingResult.setResultCallback(new ResultCallback<Achievements.UpdateAchievementResult>()
			{

				@Override
				public void onResult(UpdateAchievementResult result)
				{
					int status = result.getStatus().getStatusCode();
					if (status == GamesStatusCodes.STATUS_OK)
					{
						if (listener != null)
						{
							listener.onReportProgress(instanceId, getAchievementHash(result.getAchievementId()), null);
						}
					}
					else
					{
						Debug.log(CommonDefines.GAME_SERVICES_TAG, "Report Progress successful : " + GamesStatusCodes.getStatusString(status));
						if (listener != null)
						{
							listener.onReportProgress(instanceId, getAchievementHash(achievementId), GamesStatusCodes.getStatusString(status));
						}
					}

					loadAchievementsDetails(false, false);//Just update the achievements data user own.
				}

			});
	}

	public void loadAchievementsDetails(final boolean loadAll)
	{
		loadAchievementsDetails(loadAll, true);
	}

	//Here loadall is for forcing fetch of all available achievements. else it will just fetch user achieved/aware achievments which are cached currently.
	private void loadAchievementsDetails(final boolean loadAll, final boolean reportListener)
	{
		PendingResult<Achievements.LoadAchievementsResult> pendingResult = Games.Achievements.load(apiService, loadAll);
		pendingResult.setResultCallback(new ResultCallback<Achievements.LoadAchievementsResult>()
			{

				@Override
				public void onResult(LoadAchievementsResult results)
				{

					ArrayList<HashMap<String, Object>> listData = null;

					HashMap<String, Object> data = new HashMap<String, Object>();

					int status = results.getStatus().getStatusCode();
					if ((status == GamesStatusCodes.STATUS_OK) || (status == GamesStatusCodes.STATUS_NETWORK_ERROR_STALE_DATA))
					{
						listData = new ArrayList<HashMap<String, Object>>();

						AchievementBuffer achievementBuffer = results.getAchievements();

						for (int i = 0; i < achievementBuffer.getCount(); i++)
						{
							Achievement eachAchievement = achievementBuffer.get(i);

							AchievementInfo info = new AchievementInfo(eachAchievement);
							achievementsData.put(eachAchievement.getAchievementId(), info);

							if (!loadAll)
							{
								//Check if this is reported earlier or not. If not reported, don't send the info.
								if (info.lastReportedDate == -1)
								{
									continue;
								}
							}

							listData.add(info.getHash());
						}

						data.put(Keys.GameServices.ACHIEVEMENTS_LIST, listData);

						achievementBuffer.close();
						achievementBuffer.release();
					}
					else
					{
						String error = GamesStatusCodes.getStatusString(status);
						data.put(Keys.GameServices.ERROR, error);
						Debug.error(CommonDefines.GAME_SERVICES_TAG, "Error loading achievements info " + error);
					}

					//Send the results to listener
					if (reportListener)
					{
						if (loadAll)
						{
							if (listener != null)
							{
								listener.onLoadAchievementDetails(data);
							}
						}
						else
						{
							if (listener != null)
							{
								listener.onLoadUserAchievements(data);
							}
						}
					}

				}
			});
	}

	public void showUI(Context context)
	{
		//startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), REQUEST_ACHIEVEMENTS);
		Intent intent = new Intent(context, GooglePlayGameUIActivity.class);
		intent.putExtra(Keys.TYPE, Keys.GameServices.SHOW_ACHIEVEMENTS);
		context.startActivity(intent);
	}

	public HashMap<String, Object> getAchievementHash(String achievementId)
	{
		AchievementInfo info = GetAchievement(achievementId);
		if (info != null)
		{
			return info.getHash();
		}
		else
		{
			return null;
		}
	}

	AchievementInfo GetAchievement(String id)
	{
		return achievementsData.get(id);
	}

}

class AchievementInfo
{
	String	identifier;
	String	name;
	String	description;
	String	imagePath;
	int		type;
	int		currentSteps;
	int		totalSteps;
	int		state;
	long	lastReportedDate;
	boolean	isCompleted;

	AchievementInfo(Achievement achievement)
	{
		identifier = achievement.getAchievementId();
		name = achievement.getName();
		description = achievement.getDescription();
		imagePath = getImagePathForAchievement(achievement);
		type = achievement.getType();
		state = achievement.getState();

		if (type == Achievement.TYPE_INCREMENTAL)
		{
			currentSteps = achievement.getCurrentSteps();
			totalSteps = achievement.getTotalSteps();
		}
		else
		{
			currentSteps = (state == Achievement.STATE_UNLOCKED) ? 1 : 0;
			totalSteps = 1;
		}

		lastReportedDate = achievement.getLastUpdatedTimestamp();
		isCompleted = (achievement.getState() == Achievement.STATE_UNLOCKED);

	}

	void SetCurrentSteps(int _steps)
	{
		currentSteps = _steps;
	}

	//Convert achievement to HashMap

	HashMap<String, Object> getHash()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();

		map.put(Keys.GameServices.ACHIEVEMENT_ID, identifier);
		map.put(Keys.GameServices.ACHIEVEMENT_TITLE, name);
		map.put(Keys.GameServices.UNACHIEVED_DESCRIPTION, description);
		map.put(Keys.GameServices.ACHIEVED_DESCRIPTION, "");//No info for this.

		map.put(Keys.GameServices.IMAGE_PATH, imagePath);

		//Here if its not incremental, check if its reveled, if not set it to zero
		map.put(Keys.GameServices.POINTS_SCORED, currentSteps);
		map.put(Keys.GameServices.MAXIMUM_POINTS, totalSteps);

		map.put(Keys.GameServices.LAST_REPORT_DATE, lastReportedDate); // This will be -1 if not reported
		map.put(Keys.GameServices.IS_COMPLETED, isCompleted ? "true" : "false");

		map.put(Keys.GameServices.STATE, getStateForAchievement());
		map.put(Keys.GameServices.TYPE, getTypeForAchievement());

		return map;
	}

	String getImagePathForAchievement(Achievement achievement)
	{
		String _imagePath = "";
		String _unlockedImageUrl = achievement.getUnlockedImageUrl();
		String _revealedImageUrl = achievement.getRevealedImageUrl();

		if (StringUtility.isNullOrEmpty(_unlockedImageUrl))
		{
			if (!StringUtility.isNullOrEmpty(_revealedImageUrl))
			{
				_imagePath = _revealedImageUrl;
			}
		}
		else
		{
			_imagePath = _unlockedImageUrl;
		}

		return _imagePath;
	}

	String getStateForAchievement()
	{

		String stateStr = Keys.GameServices.STATE_HIDDEN;

		if (state == Achievement.STATE_UNLOCKED)
		{
			stateStr = Keys.GameServices.STATE_UNLOCKED;
		}
		else if (state == Achievement.STATE_REVEALED)
		{
			stateStr = Keys.GameServices.STATE_REVEALED;
		}
		else if (state == Achievement.STATE_HIDDEN)
		{
			stateStr = Keys.GameServices.STATE_HIDDEN;
		}

		return stateStr;
	}

	String getTypeForAchievement()
	{
		String _typeStr = Keys.GameServices.TYPE_STANDARD;

		if (type == Achievement.TYPE_STANDARD)
		{
			_typeStr = Keys.GameServices.TYPE_STANDARD;
		}
		else if (type == Achievement.TYPE_INCREMENTAL)
		{
			_typeStr = Keys.GameServices.TYPE_INCREMENTAL;
		}

		return _typeStr;
	}

}
