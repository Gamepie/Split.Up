package com.voxelbusters.nativeplugins.features.medialibrary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

public class YoutubePlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener, YouTubePlayer.PlayerStateChangeListener, YouTubePlayer.PlaybackEventListener
{
	public static final int	REQUEST_VIDEO_PLAY_FROM_INTENT	= 1;

	YouTubePlayerView		playerView						= null;
	String					videoId							= null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		videoId = intent.getStringExtra(Keys.MediaLibrary.YOUTUBE_VIDEO_ID);

		playVideoFromYoutube(videoId);

	}

	void playVideoFromYoutube(String videoId)
	{
		String developerKey = MediaLibraryHandler.YOUTUBE_DEVELOPER_KEY;
		try
		{
			if (YouTubeIntents.isYouTubeInstalled(this))
			{

				if (videoId == null)
				{
					Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "videoID is null");
					NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.PLAY_VIDEO_ERROR);
					finish();
					return;
				}

				if (!StringUtility.isNullOrEmpty(developerKey) && (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this) == YouTubeInitializationResult.SUCCESS))
				{
					playerView = new YouTubePlayerView(this);
					playerView.initialize(developerKey, this);

					setContentView(playerView);
				}
				else if (YouTubeIntents.canResolvePlayVideoIntent(this))
				{
					Intent intent = YouTubeIntents.createPlayVideoIntent(this, videoId);
					startActivityForResult(intent, REQUEST_VIDEO_PLAY_FROM_INTENT);
				}
			}
			else
			{
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoId));
				startActivityForResult(intent, REQUEST_VIDEO_PLAY_FROM_INTENT);
			}
		}
		catch (Exception e)//This will trigger if no activities are found for this intent
		{
			Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Unable to play video");
			NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.PLAY_VIDEO_ERROR);
		}
	}

	void close()
	{
		if (playerView != null)
		{
			NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.USER_EXITED);
			finish();
		}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		close();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode)
		{
			case REQUEST_VIDEO_PLAY_FROM_INTENT:
				Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, " " + requestCode + "  " + resultCode + "  " + data);

				NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.PLAY_VIDEO_ENDED);//Sending Played all the time if result unknown.

				break;

			default:
				Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Unknown request code in youtube activity!");
				break;
		}

		finish();
	}

	@Override
	public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean restored)
	{
		player.setPlaybackEventListener(this);
		player.setPlayerStateChangeListener(this);
		if (!restored) //if not restored, just load the video
		{
			player.loadVideo(videoId);
			Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "Youtube Initialized....");
		}

	}

	@Override
	public void onInitializationFailure(Provider provider, YouTubeInitializationResult result)
	{
		Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Youtube failed initializing " + result.toString());
		Toast.makeText(this, "Error in initializing video " + result.toString(), Toast.LENGTH_SHORT).show();
		NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.PLAY_VIDEO_ERROR);
		finish();
	}

	@Override
	public void onVideoEnded()
	{
		NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.PLAY_VIDEO_ENDED);
		finish();
	}

	@Override
	public void onVideoStarted()
	{
		Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "Youtube Video started playing");
	}

	@Override
	public void onError(ErrorReason arg0)
	{
		Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Error while playing yourube video " + arg0.toString());
		NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.PLAY_VIDEO_ERROR);
		finish();
	}

	@Override
	public void onAdStarted()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoaded(String arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoading()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onBuffering(boolean arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPaused()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlaying()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSeekTo(int arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopped()
	{
		// TODO Auto-generated method stub

	}

}
