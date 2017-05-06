package com.voxelbusters.nativeplugins.features.external.sdk.soomla.soomlagrow;

import com.soomla.highway.lite.GrowHighway;
import com.soomla.highway.lite.HighwayEventListener;
import com.soomla.highway.lite.Social;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.UnityDefines;

public class SoomlaGrow implements HighwayEventListener
{
	// Create singleton instance
	private static SoomlaGrow	INSTANCE;

	public static SoomlaGrow getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new SoomlaGrow();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private SoomlaGrow()
	{
	}

	public void initialise(String gameKey, String environmentKey, String referrerName)
	{

		GrowHighway.getInstance().initialize(NativePluginHelper.getCurrentActivity(), gameKey, environmentKey, referrerName, this);

	}

	public void getSoomlaUid()
	{
		GrowHighway.getInstance().getSoomlaUID();
	}

	public void onBillingSupported()
	{
		GrowHighway.getInstance().onBillingSupported();
	}

	public void onBillingNotSupported()
	{
		GrowHighway.getInstance().onBillingNotSupported();
	}

	public void onMarketPurchaseStarted(String itemId)
	{
		GrowHighway.getInstance().onMarketPurchaseStarted(itemId);
	}

	public void onMarketPurchaseFinished(String itemId, long priceInMicros, String currencyCode)
	{
		GrowHighway.getInstance().onMarketPurchaseFinished(itemId, priceInMicros, currencyCode);
	}

	public void onMarketPurchaseCancelled(String itemId)
	{
		GrowHighway.getInstance().onMarketPurchaseCancelled(itemId);
	}

	public void onMarketPurchaseFailed(String itemId)
	{
		GrowHighway.getInstance().onMarketPurchaseFailed();
	}

	public void onRestoreTransactionsStarted()
	{
		GrowHighway.getInstance().onRestoreTransactionsStarted();
	}

	public void onRestoreTransactionsFinished(boolean success)
	{
		GrowHighway.getInstance().onRestoreTransactionsFinished(success);
	}

	public void onBillingPurchaseVerificationFailed()
	{
		GrowHighway.getInstance().onVerificationFailed();
	}

	public void onSocialLoginStartedForProvider(int providerId)
	{
		GrowHighway.getInstance().onLoginStarted(Social.Provider.values()[providerId]);
	}

	public void onSocialLoginFinishedForProvider(int providerId, String profileId)
	{
		GrowHighway.getInstance().onLoginFinished(Social.Provider.values()[providerId], profileId);
	}

	public void onSocialLoginCancelledForProvider(int providerId)
	{
		GrowHighway.getInstance().onLoginCancelled(Social.Provider.values()[providerId]);
	}

	public void onSocialLoginFailedForProvider(int providerId)
	{
		GrowHighway.getInstance().onLoginFailed(Social.Provider.values()[providerId]);
	}

	public void onSocialLogoutStartedForProvider(int providerId)
	{
		GrowHighway.getInstance().onLogoutStarted(Social.Provider.values()[providerId]);
	}

	public void onSocialLogoutFinishedForProvider(int providerId)
	{
		GrowHighway.getInstance().onLogoutFinished(Social.Provider.values()[providerId]);
	}

	public void onSocialLogoutFailedForProvider(int providerId)
	{
		GrowHighway.getInstance().onLogoutFailed(Social.Provider.values()[providerId]);
	}

	public void onGetContactsStartedForProvider(int providerId)
	{
		GrowHighway.getInstance().onGetContactsStarted(Social.Provider.values()[providerId]);
	}

	public void onGetContactsFinishedForProvider(int providerId)
	{
		GrowHighway.getInstance().onGetContactsFinished(Social.Provider.values()[providerId]);
	}

	public void onGetContactsFailedForProvider(int providerId)
	{
		GrowHighway.getInstance().onGetContactsFailed(Social.Provider.values()[providerId]);
	}

	public void onSocialActionStartedForProvider(int providerId, int actionId)
	{
		GrowHighway.getInstance().onSocialActionStarted(Social.Provider.values()[providerId], Social.ActionType.values()[actionId]);
	}

	public void onSocialActionFinishedForProvider(int providerId, int actionId)
	{
		GrowHighway.getInstance().onSocialActionFinished(Social.Provider.values()[providerId], Social.ActionType.values()[actionId]);
	}

	public void onSocialActionCancelledForProvider(int providerId, int actionId)
	{
		GrowHighway.getInstance().onSocialActionCancelled(Social.Provider.values()[providerId], Social.ActionType.values()[actionId]);
	}

	public void onSocialActionFailedForProvider(int providerId, int actionId)
	{
		GrowHighway.getInstance().onSocialActionCancelled(Social.Provider.values()[providerId], Social.ActionType.values()[actionId]);
	}

	public void onLatestScore(String scoreId, double latestScore)
	{
		GrowHighway.getInstance().onLatestScoreChanged(scoreId, latestScore);
	}

	public void onReportLevelEnded(String levelId, boolean isCompleted, int timesPlayed, int timesStarted, long fastestDurationMillis, long slowestDurationMillis)
	{
		GrowHighway.getInstance().onLevelEnded(levelId, isCompleted, timesPlayed, timesStarted, fastestDurationMillis, slowestDurationMillis);
	}

	public void onReportLevelStarted(String levelId, int timesPlayed, int timesStarted, long fastestDurationMillis, long slowestDurationMillis)
	{
		GrowHighway.getInstance().onLevelStarted(levelId, timesPlayed, timesStarted, fastestDurationMillis, slowestDurationMillis);
	}

	public void onReportScoreRecord(String scoreId, double latestScore)
	{
		GrowHighway.getInstance().onScoreRecordChanged(scoreId, latestScore);
	}

	public void onReportOnWorld(String worldID, boolean isCompleted)
	{
		GrowHighway.getInstance().onWorldCompleted(worldID, isCompleted);
	}

	public void onAdShown()
	{
		GrowHighway.getInstance().onAdShown();
	}

	public void onAdHidden()
	{
		GrowHighway.getInstance().onAdHidden();
	}

	public void onAdClicked()
	{
		GrowHighway.getInstance().onAdClicked();
	}

	public void onVideoAdStarted()
	{
		GrowHighway.getInstance().onVideoAdStarted();
	}

	public void onVideoAdCompleted()
	{
		GrowHighway.getInstance().onVideoAdCompleted();
	}

	public void onVideoAdClicked()
	{
		GrowHighway.getInstance().onVideoAdClicked();
	}

	public void onVideoAdClosed()
	{
		GrowHighway.getInstance().onVideoAdClosed();
	}

	public void onReportUserRating()
	{
		GrowHighway.getInstance().onUserRating();
	}

	@Override
	public void onHighwayConnected()
	{
		NativePluginHelper.sendMessage(UnityDefines.ExternalSDKSoomlaGrow.HIGHWAY_CONNECTED_EVENT, "");
	}

	@Override
	public void onHighwayDisconnected()
	{
		NativePluginHelper.sendMessage(UnityDefines.ExternalSDKSoomlaGrow.HIGHWAY_DISCONNECTED_EVENT, "");
	}

	@Override
	public void onHighwayInitialized()
	{
		NativePluginHelper.sendMessage(UnityDefines.ExternalSDKSoomlaGrow.HIGHWAY_INITIALISED_EVENT, "");
	}
}