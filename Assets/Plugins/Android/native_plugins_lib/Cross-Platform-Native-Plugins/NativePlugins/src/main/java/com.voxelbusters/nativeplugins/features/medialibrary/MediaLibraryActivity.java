package com.voxelbusters.nativeplugins.features.medialibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.voxelbusters.androidnativeplugin.R;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.features.webview.VideoWebViewPlayer;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.FileUtility;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@SuppressLint({ "SetJavaScriptEnabled" })
public class MediaLibraryActivity extends Activity implements IVideoPlayBackListener
{
	public static final String	CAMERA_CAPTURE_FILE_NAME		= "mediaPicker-camera-image";
	public static final int		REQUEST_CODE_UNKNOWN			= -1;
	public static final int		REQUEST_CODE_LIBRARY_ACCESS		= 1;
	public static final int		REQUEST_CODE_CAMERA_ACCESS		= 2;
	public static final int		REQUEST_VIDEO_PLAY_FROM_PATH	= 3;
	public static final int		REQUEST_VIDEO_PLAY_FROM_YOUTUBE	= 4;
	public static final int		REQUEST_VIDEO_PLAY_FROM_INTENT	= 5;

	float						scaleFactorAllowedForImage		= 1.0f;
	VideoView					videoView						= null;
	FrameLayout					layout;
	VideoWebViewPlayer			webView							= null;
	LinearLayout				progressBarLayout				= null;
	String						currentAction					= "";

	ArrayList<String>			allowedTypes					= new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle bundleInfo = intent.getExtras();

		Intent requiredIntent = null;
		int requestCode = REQUEST_CODE_UNKNOWN;

		scaleFactorAllowedForImage = bundleInfo.getFloat(Keys.SCALE_FACTOR);
		String type = bundleInfo.getString(Keys.TYPE);

		currentAction = type;

		allowedTypes.clear();
		allowedTypes.add("jpeg");
		allowedTypes.add("jpg");
		allowedTypes.add("png");

		setFullScreen();

		if (type.equals(MediaLibraryHandler.IMAGE_FROM_LIBRARY))
		{
			requiredIntent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			requiredIntent.setType("image/*");
			requestCode = REQUEST_CODE_LIBRARY_ACCESS;
		}
		else if (type.equals(MediaLibraryHandler.IMAGE_FROM_CAMERA))
		{
			requiredIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			requiredIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

			Uri imageUri = null;

			imageUri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), CAMERA_CAPTURE_FILE_NAME));

			requiredIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

			requestCode = REQUEST_CODE_CAMERA_ACCESS;

		}
		else if (type.equals(MediaLibraryHandler.PLAY_VIDEO_FROM_PATH))
		{

			String path = bundleInfo.getString(Keys.URL);

			playVideoWithPath(path);
		}
		else if (type.equals(MediaLibraryHandler.PLAY_VIDEO_FROM_YOUTUBE))
		{

			String videoId = bundleInfo.getString(Keys.MediaLibrary.YOUTUBE_VIDEO_ID);

			startYoutubeActivity(videoId);
		}
		else if (type.equals(MediaLibraryHandler.PLAY_VIDEO_FROM_WEBVIEW))
		{

			String htmlString = bundleInfo.getString(Keys.HTML);

			playFromWebView(htmlString);

		}
		else
		{
			Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Unknown source specified! " + type);
			finish();
			return;
		}

		if (requiredIntent != null)
		{
			startActivityForResult(Intent.createChooser(requiredIntent, Keys.MediaLibrary.SELECT_MEDIA), requestCode);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	void setFullScreen()
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	void showProgressDialog()
	{
		if (progressBarLayout == null)
		{
			View view = LayoutInflater.from(this).inflate(R.layout.np_progressbar_layout, (ViewGroup) getWindow().getDecorView(), true);

			progressBarLayout = (LinearLayout) view.findViewById(R.id.np_progressbar_root);
			progressBarLayout.setBackgroundColor(Color.BLACK);

		}

		progressBarLayout.setVisibility(View.VISIBLE);
	}

	void hideProgressDialog()
	{
		progressBarLayout.setVisibility(View.GONE);
	}

	void startYoutubeActivity(String videoId)
	{
		Intent intent = new Intent(this, YoutubePlayerActivity.class);
		intent.putExtra(Keys.MediaLibrary.YOUTUBE_VIDEO_ID, videoId);
		startActivityForResult(intent, REQUEST_VIDEO_PLAY_FROM_YOUTUBE);
	}

	void playVideoWithPath(final String urlPath)
	{
		showProgressDialog();

		if (StringUtility.isNullOrEmpty(urlPath))
		{
			Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Url path is null!");
			onVideoPlayError("Url path is null!");
			finish();
			return;
		}

		Uri uri = Uri.parse(urlPath);

		videoView = new VideoView(this);
		videoView.setVideoURI(uri);

		videoView.setOnPreparedListener(new OnPreparedListener()
			{

				@Override
				public void onPrepared(MediaPlayer mp)
				{
					hideProgressDialog();
				}
			});

		videoView.setOnCompletionListener(new OnCompletionListener()
			{

				@Override
				public void onCompletion(MediaPlayer mp)
				{
					Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Completed playing video");
					onVideoPlayEnded();
					finish();
				}
			});

		videoView.setOnErrorListener(new OnErrorListener()
			{
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra)
				{
					Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Error occured while playing video!!!" + what + " " + extra);

					onVideoPlayError("Error occured while playing video!!!" + what + " " + extra);

					finish();
					return true;//we are handling the error
				}
			});

		layout = new FrameLayout(this);
		layout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));

		FrameLayout.LayoutParams videoLayoutparams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);

		videoView.setLayoutParams(videoLayoutparams);

		layout.addView(videoView);

		setContentView(layout);

		//To let it rotate in landscape direction
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		layout.setBackgroundColor(Color.BLACK);

		//Moving to top so that we don't have clear screen issue.
		videoView.setZOrderOnTop(true);

		//Add media controller
		MediaController mediaController = new MediaController(MediaLibraryActivity.this);
		videoView.setMediaController(mediaController);
		mediaController.setAnchorView(videoView);

		//Start the video
		videoView.start();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (webView != null)
		{
			webView.close();
		}
		if (videoView != null)
		{
			videoView.stopPlayback();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "onResume Media activity");
		if (webView != null)
		{
			webView.resumeWebView();
		}

		if (videoView != null)
		{
			videoView.resume();
		}

	}

	@Override
	protected void onPause()
	{
		super.onPause();

		Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "onPause Media activity");
		if (webView != null)
		{
			webView.pauseWebView();
		}

		if (videoView != null)
		{
			videoView.pause();
		}
	}

	void playFromWebView(String htmlString)
	{
		webView = new VideoWebViewPlayer(this);

		webView.loadVideoFromHtml(htmlString);

		webView.setListener(this);

		setContentView(webView);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);//Allowing in all orientations.
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		close();
	}

	void close()
	{
		if (videoView != null)
		{
			onVideoPlayUserExited();
		}
		if (webView != null)
		{
			onVideoPlayUserExited();
		}

		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case REQUEST_CODE_LIBRARY_ACCESS:

				if ((resultCode == RESULT_OK) && (data != null))
				{
					Uri selectedImgUri = data.getData();

					Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "Uri : " + selectedImgUri.toString());

					createAsycTaskForImageCopy(selectedImgUri);

				}
				else
				{
					Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "resultCode " + resultCode);

					sendPickImageResult(null, Keys.MediaLibrary.PICK_IMAGE_CANCELLED);

				}

				break;
			case REQUEST_CODE_CAMERA_ACCESS:

				File imageFileDestination = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), CAMERA_CAPTURE_FILE_NAME);

				if ((resultCode == RESULT_OK) && imageFileDestination.exists())
				{
					sendPickImageResult(imageFileDestination.getAbsolutePath(), Keys.MediaLibrary.PICK_IMAGE_SELECTED);
				}
				else
				{
					sendPickImageResult(null, Keys.MediaLibrary.PICK_IMAGE_CANCELLED);
				}

				break;

			case REQUEST_VIDEO_PLAY_FROM_YOUTUBE:
				//Do nothing...
				Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "Returning from youtube player!");

				break;

			case REQUEST_VIDEO_PLAY_FROM_INTENT:
				onVideoPlayEnded();
				break;
			default:
				Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Unknown request code!");

				break;
		}
		finish();

	}

	void sendPickImageResult(String imagePath, String reason)
	{
		//Send back result to unity
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(com.voxelbusters.nativeplugins.defines.Keys.MediaLibrary.IMAGE_PATH, imagePath);
		map.put(com.voxelbusters.nativeplugins.defines.Keys.MediaLibrary.FINISH_REASON, reason);
		NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PICK_IMAGE_FINISHED, map);
	}

	@Override
	public void onVideoPlayEnded()
	{
		NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.PLAY_VIDEO_ENDED);
	}

	@Override
	public void onVideoPlayPaused()
	{

	}

	@Override
	public void onVideoPlayError(String description)
	{
		NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.PLAY_VIDEO_ERROR);
	}

	@Override
	public void onVideoPlayUserExited()
	{
		NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FINISHED, "" + Keys.MediaLibrary.USER_EXITED);
	}

	//Create an sync task for copying the image
	void createAsycTaskForImageCopy(final Uri selectedImageUri)
	{

		new AsyncTask<String, Void, String>()
			{

				@Override
				protected String doInBackground(String... params)
				{
					InputStream inputStream = null;
					String fileType = "";
					try
					{
						Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, selectedImageUri.toString());
						ContentResolver contentResolver = getContentResolver();
						MimeTypeMap mime = MimeTypeMap.getSingleton();
						fileType = mime.getExtensionFromMimeType(contentResolver.getType(selectedImageUri)).toLowerCase(Locale.ENGLISH);

						inputStream = contentResolver.openInputStream(selectedImageUri);

					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					String timeStamp = StringUtility.getCurrentTimeStamp();
					String destFileName = "Library_Image_" + timeStamp;

					String absoluteDestinationFilePath = null;

					if (allowedTypes.contains(fileType))
					{
						absoluteDestinationFilePath = FileUtility.createFileFromStream(inputStream, ApplicationUtility.getLocalSaveDirectory(MediaLibraryActivity.this, "MediaLibrary"), destFileName);
					}
					else
					{
						Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, null);
						absoluteDestinationFilePath = FileUtility.getScaledImagePathFromBitmap(bitmap, ApplicationUtility.getLocalSaveDirectory(MediaLibraryActivity.this, "MediaLibrary"), destFileName, 1.0f);
					}

					return absoluteDestinationFilePath;
				}

				@Override
				protected void onPostExecute(String result)
				{
					sendPickImageResult(result, Keys.MediaLibrary.PICK_IMAGE_SELECTED);
				}

			}.execute(null, null, null);
	}

	void createAsycTaskForCameraCaptureResult(final File imageFileDestination)
	{
		new AsyncTask<String, Void, String>()
			{
				@Override
				protected String doInBackground(String... params)
				{
					Debug.log("NativePlugins.MediaLibrary", "Temp Path [" + imageFileDestination.toString() + "]" + "URI : " + imageFileDestination.toURI());

					String timeStamp = StringUtility.getCurrentTimeStamp();
					String destFileName = "Camera_Image_" + timeStamp + ".jpg";

					String sourcePath = imageFileDestination.getAbsolutePath();

					String absoluteDestFilePath = FileUtility.getScaledImagePath(sourcePath, ApplicationUtility.getLocalSaveDirectory(MediaLibraryActivity.this, "MediaLibrary"), destFileName, scaleFactorAllowedForImage, true);

					imageFileDestination.delete();

					return absoluteDestFilePath;
				}

				@Override
				protected void onPostExecute(String result)
				{
					MediaLibraryActivity.this.sendPickImageResult(result, Keys.MediaLibrary.PICK_IMAGE_SELECTED);
				}
			}.execute(null, null, null);//TODO pass from execute
	}
}