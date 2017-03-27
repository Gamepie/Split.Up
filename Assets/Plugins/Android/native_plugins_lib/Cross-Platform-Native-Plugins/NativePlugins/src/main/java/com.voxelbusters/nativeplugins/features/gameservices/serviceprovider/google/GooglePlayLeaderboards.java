package com.voxelbusters.nativeplugins.features.gameservices.serviceprovider.google;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.PageDirection;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadPlayerScoreResult;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadScoresResult;
import com.google.android.gms.games.leaderboard.Leaderboards.SubmitScoreResult;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Enums.eLoadScoresType;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.gameservices.core.IGameServicesPlayerListener;
import com.voxelbusters.nativeplugins.utilities.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class GooglePlayLeaderboards
{

	String										TAG						= "GooglePlayLeaderboards";
	GoogleApiClient								apiService;
	Hashtable<String, LeaderboardScoreBuffer>	leaderboardScoreData	= new Hashtable<String, LeaderboardScoreBuffer>();
	IGameServicesPlayerListener					listener;
	LeaderboardScore							localUserScore;

	long										minRank					= 0;
	long										maxRank					= 0;
	int											emptyPageCount			= 0;
	int											pageSize				= 0;

	public GooglePlayLeaderboards(GoogleApiClient apiService)
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

	public void reportScore(final String instanceId, final String leaderboardId, long score, boolean immediate)
	{
		if (!immediate)
		{
			Games.Leaderboards.submitScore(apiService, leaderboardId, score);
		}
		else
		{
			PendingResult<SubmitScoreResult> pendingResult = Games.Leaderboards.submitScoreImmediate(apiService, leaderboardId, score);

			pendingResult.setResultCallback(new ResultCallback<SubmitScoreResult>()
				{
					@Override
					public void onResult(SubmitScoreResult result)
					{
						int status = result.getStatus().getStatusCode();

						if (status == GamesStatusCodes.STATUS_OK)
						{
							ScoreSubmissionData _data = result.getScoreData();
							Debug.log(CommonDefines.GAME_SERVICES_TAG, "Score Submitted! - " + _data.toString());

							HashMap<String, Object> scoreDetails = new HashMap<String, Object>();
							scoreDetails.put(Keys.GameServices.SCORE_VALUE, result.getScoreData().getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME).rawScore);
							scoreDetails.put(Keys.GameServices.SCORE_DATE, System.currentTimeMillis());
							if (listener != null)
							{
								listener.onReportScore(instanceId, scoreDetails, null);
							}
						}
						else
						{
							String error = GamesStatusCodes.getStatusString(status);
							Debug.error(CommonDefines.GAME_SERVICES_TAG, "Error Submitting Score : " + error);
							if (listener != null)
							{
								listener.onReportScore(instanceId, null, error);
							}
						}
					}
				});
		}
	}

	public void showUI(Context context, String leaderboardId, int timeSpan)
	{
		Intent intent = new Intent(context, GooglePlayGameUIActivity.class);
		intent.putExtra(Keys.TYPE, Keys.GameServices.SHOW_LEADERBOARDS);
		intent.putExtra(Keys.GameServices.LEADERBOARD_ID, leaderboardId);
		intent.putExtra(Keys.GameServices.TIME_SPAN, "" + timeSpan);

		context.startActivity(intent);
	}

	public void loadPlayerCenteredScores(String instanceId, String leaderboardId, int span, int collectionType, int count)
	{
		PendingResult<Leaderboards.LoadScoresResult> pendingResult = Games.Leaderboards.loadPlayerCenteredScores(apiService, leaderboardId, span, collectionType, count);
		localUserScore = getLocalUserScore(leaderboardId, span, collectionType);
		loadScoresAsync(instanceId, eLoadScoresType.PlayerCentered, PageDirection.NONE, count, pendingResult);
	}

	public void loadTopScores(String instanceId, String leaderboardId, int span, int collectionType, int count)
	{
		PendingResult<Leaderboards.LoadScoresResult> pendingResult = Games.Leaderboards.loadTopScores(apiService, leaderboardId, span, collectionType, count);
		localUserScore = getLocalUserScore(leaderboardId, span, collectionType);
		loadScoresAsync(instanceId, eLoadScoresType.Top, PageDirection.NONE, count, pendingResult);
	}

	public void loadMoreScores(String instanceId, String leaderboardId, int direction, int count)
	{
		LeaderboardScoreBuffer currentLeaderboardScoreBuffer = leaderboardScoreData.get(leaderboardId);
		if (currentLeaderboardScoreBuffer == null)
		{
			PendingResult<Leaderboards.LoadScoresResult> pendingResult = Games.Leaderboards.loadTopScores(apiService, leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, count);
			localUserScore = getLocalUserScore(leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC);
			loadScoresAsync(instanceId, eLoadScoresType.More, PageDirection.NONE, count, pendingResult);
		}
		else
		{
			if ((minRank <= 1) && (direction == PageDirection.PREV))
			{
				minRank = maxRank = 0;
				String error = "No score pages available.";
				if (listener != null)
				{
					listener.onLoadingScores(instanceId, null, null, error);
				}
			}
			else
			{
				PendingResult<Leaderboards.LoadScoresResult> pendingResult = Games.Leaderboards.loadMoreScores(apiService, currentLeaderboardScoreBuffer, pageSize, direction == 0 ? PageDirection.NEXT : PageDirection.PREV);
				loadScoresAsync(instanceId, eLoadScoresType.More, direction, count, pendingResult);
			}
		}
	}

	void loadScoresAsync(final String instanceId, final eLoadScoresType type, final int direction, final int maxResults, PendingResult<Leaderboards.LoadScoresResult> pendingResult)
	{
		if (type != eLoadScoresType.More)
		{
			pageSize = maxResults;
		}

		pendingResult.setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>()
			{

				@Override
				public void onResult(LoadScoresResult results)
				{
					int status = results.getStatus().getStatusCode();

					if ((status == GamesStatusCodes.STATUS_OK) || (status == GamesStatusCodes.STATUS_NETWORK_ERROR_STALE_DATA))
					{
						String leaderboardId = results.getLeaderboard().getLeaderboardId();

						LeaderboardScoreBuffer leaderboardBuffer = results.getScores();

						ArrayList<HashMap<String, Object>> scoreData = new ArrayList<HashMap<String, Object>>();

						synchronized (leaderboardScoreData) //This needs to be synchronized code block and executed sequentially
						{
							LeaderboardScoreBuffer currentLeaderboardScoreBuffer = leaderboardScoreData.get(leaderboardId);

							if (currentLeaderboardScoreBuffer != null)
							{
								currentLeaderboardScoreBuffer.release();
							}

							leaderboardScoreData.put(leaderboardId, leaderboardBuffer);

							//Calculate rank bounds
							long tempMinRank = minRank;
							long tempMaxRank = maxRank;

							calculateRankRange(leaderboardBuffer, direction, maxResults);

							int count = leaderboardBuffer.getCount();
							for (int i = 0; i < count; i++)
							{
								LeaderboardScore eachScore = leaderboardBuffer.get(i);

								if (direction != PageDirection.NONE)
								{
									if ((eachScore.getRank() < minRank) || (eachScore.getRank() > maxRank))
									{
										continue;
									}
								}

								scoreData.add(getHash(leaderboardId, eachScore));
							}

							if (scoreData.size() == 0)
							{
								if (emptyPageCount > 0)
								{
									minRank = tempMinRank;
									maxRank = tempMaxRank;
								}
								else
								{
									emptyPageCount++;
								}
							}
							else
							{
								emptyPageCount = 0;
							}
						}
						if (listener != null)
						{
							listener.onLoadingScores(instanceId, getHash(leaderboardId, localUserScore), scoreData, null);
						}
					}
					else
					{

						//Send a failure
						String error = GamesStatusCodes.getStatusString(status);
						Debug.error(CommonDefines.GAME_SERVICES_TAG, "Failed loading " + type.toString() + " scores with error code : " + status + " Message: " + error);
						if (listener != null)
						{
							listener.onLoadingScores(instanceId, null, null, error);
						}

					}
				}
			});
	}

	LeaderboardScore getLocalUserScore(String leaderboardId, int span, int leaderboardCollection)
	{
		PendingResult<LoadPlayerScoreResult> pendingResult = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(apiService, leaderboardId, span, leaderboardCollection);
		LoadPlayerScoreResult _result = pendingResult.await();

		return _result.getScore();
	}

	void calculateRankRange(LeaderboardScoreBuffer leaderboardBuffer, int direction, int maxResults)
	{
		if (direction == PageDirection.NONE)
		{
			int count = leaderboardBuffer.getCount();
			if (count > 0)
			{
				minRank = leaderboardBuffer.get(0).getRank();
				maxRank = leaderboardBuffer.get(count - 1).getRank();
			}
		}
		else
		{
			if (direction == PageDirection.NEXT)
			{
				minRank = maxRank + 1;
				maxRank = (minRank + maxResults) - 1;
			}
			else if (direction == PageDirection.PREV)
			{
				minRank = Math.max(1, minRank - maxResults);
				maxRank = Math.max(1, (minRank + maxResults) - 1);
			}
		}
	}

	//Convert achievement to HashMap

	HashMap<String, Object> getHash(String leaderboardId, LeaderboardScore score)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();

		map.put(Keys.GameServices.SCORE_ID, leaderboardId);

		if (score != null)
		{
			Player _player = score.getScoreHolder();

			map.put(Keys.GameServices.SCORE_USER, getPlayerHash(_player));
			map.put(Keys.GameServices.SCORE_DATE, score.getTimestampMillis());
			map.put(Keys.GameServices.SCORE_VALUE, score.getRawScore());
			map.put(Keys.GameServices.SCORE_FORMATTED_VALUE, score.getDisplayScore());
			map.put(Keys.GameServices.SCORE_RANK, score.getRank());
		}

		return map;
	}

	//TODO remove this duplicate code and move to separate class for users.
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

}
