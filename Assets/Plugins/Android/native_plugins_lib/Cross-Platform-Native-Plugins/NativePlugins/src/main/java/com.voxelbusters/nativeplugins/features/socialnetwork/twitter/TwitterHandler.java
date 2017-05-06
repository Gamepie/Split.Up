package com.voxelbusters.nativeplugins.features.socialnetwork.twitter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.FileUtility;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit.client.Header;
import retrofit.client.Request;

/*
 * TwitterCore core = Twitter.core; TweetUi tweetUi = Twitter.tweetUi;
 * TweetComposer composer = Twitter.tweetComposer; Digits digits =
 * Twitter.digits;
 */

// HttpClient class is deprecated from API 22. Need to update //TODO
@SuppressWarnings("deprecation")
public class TwitterHandler
{

	// Create singleton instance
	private static TwitterHandler	INSTANCE;
	boolean							initialized	= false;

	public static TwitterHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new TwitterHandler();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private TwitterHandler()
	{
	}

	public void initialize(String consumerKey, String secretKey)
	{

		if (Fabric.isInitialized() && isLoggedIn())// Logging out if we are already logged in.
		{
			logout();
		}

		if (StringUtility.isNullOrEmpty(consumerKey) || StringUtility.isNullOrEmpty(secretKey))
		{
			Debug.error(CommonDefines.TWITTER_TAG, "Consumer key/secret key for twitter is not set. Please set them in NPSettings.");
		}

		// Set consumer key and secret key
		TwitterAuthConfig authConfig = new TwitterAuthConfig(consumerKey, secretKey);

		// Initializes the component
		Fabric.with(NativePluginHelper.getCurrentContext(), new Twitter(authConfig), new TweetComposer());
		initialized = true;

	}

	// Helper methods for getting service implementations //TODO move these
	// helpers to proper place

	TwitterSession getActiveSession()
	{
		return Twitter.getSessionManager().getActiveSession();
	}

	TwitterApiClient getApiClient()
	{
		TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
		return twitterApiClient;
	}

	TwitterAuthConfig getAuthConfig()
	{
		return TwitterCore.getInstance().getAuthConfig();
	}

	StatusesService getStatusesService()
	{
		StatusesService service = getApiClient().getStatusesService();
		return service;
	}

	AccountService getAccountService()
	{
		AccountService service = getApiClient().getAccountService();
		return service;
	}

	SearchService getSearchService()
	{
		SearchService service = getApiClient().getSearchService();
		return service;
	}

	public void login()
	{
		if (initialized)
		{
			Intent intent = new Intent(NativePluginHelper.getCurrentContext(), TwitterHelperActivity.class);
			Bundle info = new Bundle();
			info.putString("ACTION", TwitterHelperActivity.TWITTER_LOGIN_ACTION);

			intent.putExtras(info);

			// Post the intent
			NativePluginHelper.startActivityOnUiThread(intent);
		}
		else
		{
			NativePluginHelper.sendMessage(UnityDefines.Twitter.LOGIN_FAILED, "Call initialize method first!");
		}
	}

	public void logout()
	{
		if (getActiveSession() != null)
		{
			Twitter.logOut();
			Debug.log(CommonDefines.TWITTER_TAG, "Logging out from twitter session");
		}
		else
		{
			Debug.log(CommonDefines.TWITTER_TAG, "Already logged out earlier!");
		}

	}

	public boolean isLoggedIn()
	{

		if (getActiveSession() != null)
		{
			return true;
		}
		else
		{
			Debug.error(CommonDefines.TWITTER_TAG, "No twitter session created. Not logged in!");
			return false;
		}
	}

	public String getAuthToken()
	{
		if (isLoggedIn())
		{
			return getActiveSession().getAuthToken().token;
		}
		else
		{
			return CommonDefines.DEFAULT_STRING;
		}
	}

	public String getAuthTokenSecret()
	{
		if (isLoggedIn())
		{
			return getActiveSession().getAuthToken().secret;
		}
		else
		{
			return CommonDefines.DEFAULT_STRING;
		}
	}

	public String getUserName()
	{
		if (isLoggedIn())
		{
			return getActiveSession().getUserName();
		}
		else
		{
			return CommonDefines.DEFAULT_STRING;
		}

	}

	public String getUserId()
	{
		if (isLoggedIn())
		{
			return "" + (getActiveSession().getUserId());
		}
		else
		{
			return CommonDefines.DEFAULT_STRING;
		}

	}

	public void showTweetComposer(String message, String urlString, byte[] imageByteArray, int byteArraylength)
	{

		Context context = NativePluginHelper.getCurrentContext();
		// Create a bitmap and save it
		Uri imagePathUri = null;

		if (imageByteArray != null)
		{
			imagePathUri = FileUtility.createSharingFileUri(context, imageByteArray, byteArraylength, CommonDefines.SHARING_DIR, System.currentTimeMillis() + ".png");

		}

		Intent intent = new Intent(context, TwitterHelperActivity.class);
		Bundle info = new Bundle();
		info.putString("ACTION", TwitterHelperActivity.TWEET_COMPOSER_ACTION);

		info.putString("MESSAGE", message);

		if (!StringUtility.isNullOrEmpty(urlString))
		{
			info.putString("URL_STRING", urlString);
		}

		if (imagePathUri != null)
		{
			info.putString("IMAGE_PATH", imagePathUri.toString());
		}

		intent.putExtras(info);

		// Post the intent
		NativePluginHelper.startActivityOnUiThread(intent);
	}

	public void requestAccountDetails()
	{
		Debug.log(CommonDefines.TWITTER_TAG, "Requesting for twitter user details");

		AccountService service = getAccountService();

		Callback<User> callback = new Callback<User>()
			{

				@Override
				public void success(Result<User> result)
				{

					User user = result.data;

					HashMap<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put(Keys.Twitter.USER_ID, user.idStr + "");
					dataMap.put(Keys.Twitter.USER_NAME, user.name);
					dataMap.put(Keys.Twitter.IS_VERIFIED, user.verified);
					dataMap.put(Keys.Twitter.IS_PROTECTED, user.protectedUser);
					dataMap.put(Keys.Twitter.PROFILE_IMAGE_URL, user.profileImageUrl);

					Debug.log(CommonDefines.TWITTER_TAG, "Completed loading twitter user details " + dataMap.toString());

					NativePluginHelper.sendMessage(UnityDefines.Twitter.REQUEST_ACCOUNT_DETAILS_SUCCESS, dataMap);
				}

				@Override
				public void failure(TwitterException exception)
				{
					String error = exception.getMessage();
					Debug.error(CommonDefines.TWITTER_TAG, "Failed to load twitter user details");

					NativePluginHelper.sendMessage(UnityDefines.Twitter.REQUEST_ACCOUNT_DETAILS_FAILED, error);

				}

			};

		// Call the required API
		service.verifyCredentials(true, true, callback);
	}

	public void requestEmailAccess()
	{

		TwitterAuthClient authClient = new TwitterAuthClient();

		authClient.requestEmail(getActiveSession(), new Callback<String>()
			{
				@Override
				public void success(Result<String> result)
				{
					String email = result.data;

					NativePluginHelper.sendMessage(UnityDefines.Twitter.REQUEST_EMAIL_ACCESS_SUCCESS, email);

				}

				@Override
				public void failure(TwitterException exception)
				{
					String error = exception.getMessage();
					NativePluginHelper.sendMessage(UnityDefines.Twitter.REQUEST_EMAIL_ACCESS_FAILED, error);
				}
			});
	}

	public void urlRequest(String method, String urlString, String parametersString)
	{
		JSONObject parameters = null;

		try
		{
			parameters = new JSONObject(parametersString);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			Debug.error(CommonDefines.TWITTER_TAG, "Exception while parsing twitter urlRequest parameters...");
		}

		try
		{

			HttpClient httpclient = new DefaultHttpClient();
			HttpUriRequest httpRequest = null;

			Uri uri = Uri.parse(urlString);
			Uri.Builder uriBuilder = uri.buildUpon();

			//Add params here.
			if (parameters != null)
			{
				Iterator<?> keys = parameters.keys();
				while (keys.hasNext())
				{
					String eachKey = (String) keys.next();
					uriBuilder.appendQueryParameter(eachKey, parameters.get(eachKey).toString());
				}
			}

			urlString = uriBuilder.build().toString();

			AuthenticatedClientExtended genericRequestClient = new AuthenticatedClientExtended(getAuthConfig(), getActiveSession(), TwitterCore.getInstance().getSSLSocketFactory());

			Request request = new Request(method, urlString, null, null);

			List<Header> headers = genericRequestClient.getHeaders(request);

			HashMap<String, String> headerMap = new HashMap<String, String>();
			for (Header h : headers)
			{
				headerMap.put(h.getName(), h.getValue());
			}

			if (method.equalsIgnoreCase("GET"))
			{
				httpRequest = new HttpGet(urlString);
			}
			else if (method.equalsIgnoreCase("POST"))
			{
				httpRequest = new HttpPost(urlString);
			}
			else if (method.equalsIgnoreCase("DELETE"))
			{
				httpRequest = new HttpDelete(urlString);
			}
			else if (method.equalsIgnoreCase("PUT"))
			{
				httpRequest = new HttpPut(urlString);
			}

			//Setting headers here
			httpRequest.setHeader("Content-Type", "application/json;charset=utf-8");
			for (String eachKey : headerMap.keySet())
			{
				httpRequest.setHeader(eachKey, headerMap.get(eachKey));
			}

			HttpResponse response = httpclient.execute(httpRequest);

			String result = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 200)
			{
				NativePluginHelper.sendMessage(UnityDefines.Twitter.URL_REQUEST_SUCCESS, result);
			}
			else
			{
				NativePluginHelper.sendMessage(UnityDefines.Twitter.URL_REQUEST_FAILED, result);
			}
		}
		catch (Exception e)
		{
			Debug.error(CommonDefines.TWITTER_TAG, e.getMessage());
			e.printStackTrace();
			NativePluginHelper.sendMessage(UnityDefines.Twitter.URL_REQUEST_FAILED, "Error while querying request");
			e.printStackTrace();
		}

	}
}
