package com.voxelbusters.nativeplugins.features.socialnetwork.twitter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class TwitterHelperActivity extends Activity
{

	TwitterAuthClient		twitterAuthClient		= null;
	String					currentAction			= null;

	public static String	TWEET_COMPOSER_ACTION	= "TWEET_COMPOSER";
	public static String	TWITTER_LOGIN_ACTION	= "LOGIN";

	boolean					appCanResumeFromDifferentActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Bundle info = intent.getExtras();

		String action = info.getString("ACTION");
		if (action.equals(TWEET_COMPOSER_ACTION))
		{
			String message = info.getString("MESSAGE");
			String urlString = info.getString("URL_STRING");
			String imagePath = info.getString("IMAGE_PATH");

			// Start the composer
			startTweetComposer(message, urlString, imagePath);

		}
		else if (action.equals(TWITTER_LOGIN_ACTION))
		{
			Callback<TwitterSession> authorizeCallback = new Callback<TwitterSession>()
				{

					@Override
					public void success(Result<TwitterSession> twitterSessionResult)
					{
						Debug.log(CommonDefines.TWITTER_TAG, "Twitter login successful");

						TwitterSession session = twitterSessionResult.data;

						HashMap<String, String> loginDetails = new HashMap<String, String>();
						loginDetails.put(Keys.Twitter.USER_ID, session.getUserId() + "");
						loginDetails.put(Keys.Twitter.USER_NAME, session.getUserName());
						loginDetails.put(Keys.Twitter.AUTH_TOKEN, session.getAuthToken().token);
						loginDetails.put(Keys.Twitter.AUTH_TOKEN_SECRET, session.getAuthToken().secret);

						Debug.log(CommonDefines.TWITTER_TAG, "Twitter Login Details : " + loginDetails.toString());
						NativePluginHelper.sendMessage(UnityDefines.Twitter.LOGIN_SUCCESS, loginDetails);
						finish();
					}

					@Override
					public void failure(TwitterException exception)
					{
						Debug.error(CommonDefines.TWITTER_TAG, "Twitter login failed with exception " + exception.getMessage());
						NativePluginHelper.sendMessage(UnityDefines.Twitter.LOGIN_FAILED, exception.getMessage());
						finish();
					}

				};
			twitterAuthClient = new TwitterAuthClient();
			twitterAuthClient.authorize(this, authorizeCallback);
		}

		currentAction = action;
	}

	void startTweetComposer(String message, String urlString, String imagePath)
	{
		TweetComposer.Builder builder = new TweetComposer.Builder(this);

		// Set text
		builder = builder.text(message);

		if (imagePath != null)
		{
			Uri imageUri = Uri.parse(imagePath);
			builder = builder.image(imageUri);
		}

		if (urlString != null)
		{
			URL url;
			try
			{
				url = new java.net.URL(urlString);
				builder = builder.url(url);
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
				Debug.error(CommonDefines.TWITTER_TAG, "Unable to use shared url string.");
			}
		}

		builder.show(); //TODO
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		appCanResumeFromDifferentActivity = true;
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (appCanResumeFromDifferentActivity && currentAction.equals(TWEET_COMPOSER_ACTION))
		{
			NativePluginHelper.sendMessage(UnityDefines.Twitter.COMPOSER_DISMISSED, Keys.Twitter.COMPOSER_ACTION_DONE);
			finish();
		}
		appCanResumeFromDifferentActivity = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		Debug.log(CommonDefines.TWITTER_TAG, "requestCode: " + requestCode + "resultCode " + resultCode + "Data " + ((data == null) ? null : data.toString()));

		if (currentAction.equals(TWITTER_LOGIN_ACTION))//Pass the results to internal classes
		{
			twitterAuthClient.onActivityResult(requestCode, resultCode, data);
		}

		// Finishing this activity
		finish();
	}
}
