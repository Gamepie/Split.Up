package com.voxelbusters.nativeplugins.features.gameservices.serviceprovider.google;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.voxelbusters.androidnativeplugin.R;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.gameservices.serviceprovider.google.util.BaseGameUtils;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

public class GooglePlayGameUIActivity extends Activity
{
	private final String	TAG								= "GooglePlayGameUIActivity";
	private final int		REQUEST_CODE_CONNECTION_RESOLVE	= 1000;
	private final int		REQUEST_CODE_SHOW_ACHIEVEMENTS	= 1001;
	private final int		REQUEST_CODE_SHOW_LEADERBOARDS	= 1002;
	GooglePlayGameService	service;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		service = GooglePlayGameService.getInstance();

		if (service == null)
		{
			finish();
			return;
		}

		Intent intent = getIntent();

		String type = intent.getStringExtra(Keys.TYPE);
		int resultCode = intent.getIntExtra(Keys.GameServices.RESULT_CODE, 0);
		PendingIntent pendingIntent = intent.getParcelableExtra(Keys.GameServices.PENDING_INTENT);
		String displayString = intent.getStringExtra(Keys.GameServices.DISPLAY_STRING);

		ConnectionResult result = new ConnectionResult(resultCode, pendingIntent);

		if (type.equals(Keys.GameServices.ON_CONNECTION_FAILURE))
		{
			if ((service.getApiService() == null) || !BaseGameUtils.resolveConnectionFailure(this, service.getApiService(), result, REQUEST_CODE_CONNECTION_RESOLVE, displayString))
			{
				service.finishedResolvingConnectionFailure();
				service.reportConnectionFailed();
				finish();
			}
		}
		else if (type.equals(Keys.GameServices.SHOW_ACHIEVEMENTS))
		{
			startActivityForResult(Games.Achievements.getAchievementsIntent(service.getApiService()), REQUEST_CODE_SHOW_ACHIEVEMENTS);
		}
		else if (type.equals(Keys.GameServices.SHOW_LEADERBOARDS))
		{
			Intent requiredIntent = null;
			String leaderboardId = intent.getStringExtra(Keys.GameServices.LEADERBOARD_ID);
			String timeSpan = intent.getStringExtra(Keys.GameServices.TIME_SPAN);

			if (!StringUtility.isNullOrEmpty(leaderboardId))//If not null load specific leaderboard. Else load all leaderboards.
			{
				requiredIntent = Games.Leaderboards.getLeaderboardIntent(service.getApiService(), leaderboardId, Integer.parseInt(timeSpan));
			}
			else
			{
				requiredIntent = Games.Leaderboards.getAllLeaderboardsIntent(service.getApiService());
			}

			startActivityForResult(requiredIntent, REQUEST_CODE_SHOW_LEADERBOARDS);

		}
		else
		{
			Debug.error(TAG, "Type unspecified for this intent");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_CONNECTION_RESOLVE)
		{
			Debug.log(CommonDefines.GAME_SERVICES_TAG, "Request Code : " + requestCode + " Result Code : " + resultCode);
			service.finishedResolvingConnectionFailure();
			if (resultCode == RESULT_OK)
			{
				service.signIn();
			}
			else
			{
				BaseGameUtils.showActivityResultError(NativePluginHelper.getCurrentActivity(), requestCode, resultCode, R.string.gameservices_sign_in_failed);

				service.reportConnectionFailed();
			}

		}
		else if (requestCode == REQUEST_CODE_SHOW_ACHIEVEMENTS)
		{
			Debug.log(TAG, "onActivityResult from Show Achievements");
			service.reportAchievementsUIClosed();

			if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED)//When clicked on signout from UI.
			{
				service.getApiService().disconnect();
			}
		}
		else if (requestCode == REQUEST_CODE_SHOW_LEADERBOARDS)
		{
			Debug.log(TAG, "onActivityResult from Show specific Leaderboard/All Leaderboards");
			service.reportLeaderboardsUIClosed();

			if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED)//When clicked on signout from UI.
			{
				service.getApiService().disconnect();
			}

		}

		finish();
	}

}
