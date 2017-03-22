package com.voxelbusters.nativeplugins.features.gameservices.core;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

public class BasicGameService implements IGameServices
{
	public enum eGameServiceState
	{
		NONE, SIGNING_IN, SIGNING_OUT, RESOLVING_ERROR
	}

	protected eGameServiceState							state;
	protected Context									context;

	protected ArrayList<IGameServicesAuthListener>		authListeners	= new ArrayList<IGameServicesAuthListener>();
	protected ArrayList<IGameServicesPlayerListener>	playerListeners	= new ArrayList<IGameServicesPlayerListener>();

	public BasicGameService(Context context)
	{
		this.context = context;
		state = eGameServiceState.NONE;
	}

	@Override
	public void register(boolean useCloudServices)
	{

	}

	@Override
	public boolean isAvailable()
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void signIn()
	{
		state = eGameServiceState.SIGNING_IN;
	}

	@Override
	public void signOut()
	{
		state = eGameServiceState.SIGNING_OUT;
	}

	@Override
	public boolean isSignedIn()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reportProgress(String instanceId, String achievementId, int points, boolean immediate)
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void loadAllAchievements()
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void loadUserAchievements()
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void showAchievementsUi()
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void reportScore(String instanceId, String leaderboardId, long score, boolean immediate)

	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void showLeaderboardsUi(String leaderboardId, int timeSpan)
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void loadUsers(String instanceId, String[] userIdList)
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void requestLocalPlayerDetails()
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void loadLocalPlayerFriends(boolean forceLoad)
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void loadPlayerCenteredScores(String instanceId, String leaderBoardId, int timeScope, int userScope, int count)
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void loadTopScores(String instanceId, String leaderBoardId, int timeScope, int userScope, int count)
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void loadMoreScrores(String instanceId, String leaderBoardId, int direction, int count)
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public void loadProfileImage(String requestId, String uriString)
	{
		throw new UnsupportedOperationException("Implement in sub class");
	}

	@Override
	public HashMap<String, Object> getAchievement(String achievementId)
	{
		return null;
	}

	@Override
	public void addListener(IGameServicesAuthListener auth, IGameServicesPlayerListener player)
	{
		if ((auth != null) && !authListeners.contains(auth))
		{
			authListeners.add(auth);
		}

		if ((player != null) && !playerListeners.contains(player))
		{
			playerListeners.add(player);
		}
	}

	@Override
	public void removeListener(IGameServicesAuthListener auth, IGameServicesPlayerListener player)
	{
		if ((auth != null) && authListeners.contains(auth))
		{
			authListeners.remove(auth);
		}

		if ((player != null) && playerListeners.contains(player))
		{
			playerListeners.remove(player);
		}
	}
}
