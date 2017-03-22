package com.voxelbusters.nativeplugins.features.gameservices.core;

import java.util.ArrayList;
import java.util.HashMap;

public interface IGameServicesPlayerListener
{
	void onReportProgress(String instanceID, HashMap<String, Object> achievementData, String error);

	void onLoadingScores(String instanceId, HashMap<String, Object> localPlayerScore, ArrayList<HashMap<String, Object>> scores, String error);

	void onLoadAchievementDetails(HashMap<String, Object> data);

	void onLoadUserAchievements(HashMap<String, Object> data);

	void onLoadLocalUserFriendsDetails(ArrayList<HashMap<String, Object>> friendsData, String error);

	void onLoadLocalUserDetails(HashMap<String, Object> userHash);

	void onLoadUserProfiles(String instanceId, ArrayList<HashMap<String, Object>> result, String error);

	void onLoadProfilePicture(String instanceId, String filePath, String error);

	void onReportScore(String instanceId, HashMap<String, Object> playerHash, String error);

	void onLeaderboardsUIClosed();

	void onAchievementsUIClosed();

}
