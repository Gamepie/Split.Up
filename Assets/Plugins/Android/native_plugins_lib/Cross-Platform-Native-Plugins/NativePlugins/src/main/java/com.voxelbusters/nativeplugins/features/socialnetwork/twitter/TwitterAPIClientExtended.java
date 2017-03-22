package com.voxelbusters.nativeplugins.features.socialnetwork.twitter;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import retrofit.http.GET;
import retrofit.http.Query;

class TwitterAPIClientExtended extends TwitterApiClient
{
	public TwitterAPIClientExtended(TwitterSession session)
	{
		super(session);
	}

	/**
	 * Provide CustomService with defined endpoints
	 */
	public CustomService getCustomService()
	{
		return getService(CustomService.class);
	}
}

// example users/show service endpoint
interface CustomService
{
	@GET("/1.1/users/show.json")
	void show(@Query("user_id") long id, Callback<User> cb);
}