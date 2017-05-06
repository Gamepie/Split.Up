package com.voxelbusters.nativeplugins.features.socialnetwork.twitter;

import com.twitter.sdk.android.core.AuthenticatedClient;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import retrofit.client.Header;
import retrofit.client.Request;

public class AuthenticatedClientExtended extends AuthenticatedClient
{

	public AuthenticatedClientExtended(TwitterAuthConfig config, @SuppressWarnings("rawtypes") Session session, SSLSocketFactory sslSocketFactory)
	{
		super(config, session, sslSocketFactory);
	}

	public List<Header> getHeaders(Request request)
	{
		try
		{
			return getAuthHeaders(request);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
