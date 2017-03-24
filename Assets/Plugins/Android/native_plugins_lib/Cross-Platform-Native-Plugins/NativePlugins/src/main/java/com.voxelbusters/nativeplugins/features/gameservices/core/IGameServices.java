package com.voxelbusters.nativeplugins.features.gameservices.core;

import java.util.HashMap;

public interface IGameServices
{

	boolean isAvailable();

	void register(boolean useCloudServices);

	void addListener(IGameServicesAuthListener auth, IGameServicesPlayerListener player);

	void removeListener(IGameServicesAuthListener auth, IGameServicesPlayerListener player);

	//Authentication
	void signIn();

	void signOut();

	boolean isSignedIn();

	//Achievements

	//Report score for an achievement.
	void reportProgress(String instanceId, String achievementId, int points, boolean immediate);

	//Reveal an hidden achievement
	//void revealAchievement(String achievementId, boolean immediate);

	//Unlock an achievement. This removes lock icon.
	//	void unlockAchievement(String achievementId, boolean immediate);

	//Increment an achievement by number of steps.
	//void incrementAchievement(String achievementId, int steps, boolean immediate);

	//Load all achievements details and send back to the listener.
	void loadAllAchievements();

	//Load user achievements  and send back to the listener.
	void loadUserAchievements();

	HashMap<String, Object> getAchievement(String achievementId);

	//Display default UI for achievements.
	void showAchievementsUi();

	//TODO provide reset all Achievements instructions in NPSetting editor. Once cross check with iOS resetting usage.

	//Leaderboards

	//Submit score for a leaderboard.
	void reportScore(String instanceId, String leaderboardId, long score, boolean immediate);

	//Display default UI for leaderboards.
	void showLeaderboardsUi(String leaderboardId, int timeSpan);

	//Misc Details

	//Fetch User detail for an user.
	void loadUsers(String instanceId, String[] userIdList);

	//Local player details
	void requestLocalPlayerDetails();

	//Local player details
	void loadLocalPlayerFriends(boolean forceLoad);

	//Load scores for a given leaderboard.

	void loadPlayerCenteredScores(String instanceId, String leaderBoardId, int timeScope, int userScope, int count);

	void loadTopScores(String instanceId, String leaderBoardId, int timeScope, int userScope, int count);

	void loadMoreScrores(String instanceId, String leaderBoardId, int direction, int count);

	void loadProfileImage(String instanceId, String uriString);

}
