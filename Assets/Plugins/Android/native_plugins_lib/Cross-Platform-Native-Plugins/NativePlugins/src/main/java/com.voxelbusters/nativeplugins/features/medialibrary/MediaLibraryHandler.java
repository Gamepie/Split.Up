package com.voxelbusters.nativeplugins.features.medialibrary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Enums.eMediaLibrarySource;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.FileUtility;

import java.io.File;
import java.util.HashMap;

public class MediaLibraryHandler
{

	public final static String			IMAGE_FROM_LIBRARY		= "image-from-library";
	public final static String			IMAGE_FROM_CAMERA		= "image-from-camera";
	public final static String			VIDEO_FROM_LIBRARY		= "video-from-library";
	public final static String			PLAY_VIDEO_FROM_PATH	= "play-video-from-path";
	public final static String			PLAY_VIDEO_FROM_YOUTUBE	= "play-video-from-youtube";
	public final static String			PLAY_VIDEO_FROM_WEBVIEW	= "play-video-from-webview";
	public final static String			STOP_PLAYING_VIDEO		= "stop-video";
	public final static String			VIDEO_INTENT_ACTION		= "com.nativeplugins.medialibrary.video";

	public static String				YOUTUBE_DEVELOPER_KEY	= "";

	// Create singleton instance
	private static MediaLibraryHandler	INSTANCE;

	public static MediaLibraryHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new MediaLibraryHandler();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private MediaLibraryHandler()
	{
	}

	public void initialize(String youtubeDeveloperKey)
	{
		YOUTUBE_DEVELOPER_KEY = youtubeDeveloperKey;
	}

	@SuppressWarnings("deprecation")
	public boolean isCameraSupported()
	{

		PackageManager packageManager = NativePluginHelper.getCurrentContext().getPackageManager();

		// Later case is for double checking. On some devices it was reported
		// FEATURE_CAMERA check will return true for non camera old devices
		// because
		// of a bug.

		//CameraManager.getCameraIds for latest versions only so going with deprecated api
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) || (Camera.getNumberOfCameras() == 0))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public void showImagePicker(int accessPhotoInt, float scaleFactor)
	{
		if (eMediaLibrarySource.ALBUM.ordinal() == accessPhotoInt)
		{
			takePictureFromGallery(scaleFactor);
		}
		else if (eMediaLibrarySource.CAMERA.ordinal() == accessPhotoInt)
		{
			takePictureFromCamera(scaleFactor);
		}
		else if (eMediaLibrarySource.BOTH.ordinal() == accessPhotoInt)
		{
			// Show an alert and ask where user wants to pick and trigger that
			// corresponding method
			takePictureFromBothGalleryAndCamera(scaleFactor);
		}
		else
		{
			Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Unknown source as access point for photo!");
			return;
		}

	}

	public void saveImageToAlbum(byte[] imageFileByteArray, int imgByteArrayLength)//, String fileFormat)
	{

		// Create a bitmap and save it

		try
		{
			String applicationName = ApplicationUtility.getApplicationName(getContext());
			String destinationPath = Environment.getExternalStorageDirectory().getPath() + "/" + applicationName + "/";
			String path = FileUtility.getSavedFile(imageFileByteArray, imgByteArrayLength, new File(destinationPath), System.currentTimeMillis() + ".png", false);

			if (path != null)
			{
				// Create a bitmap and save it in gallery
				File file = new File(path);
				Uri contentUri = Uri.fromFile(file);
				Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "Saving to " + contentUri.toString());

				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				mediaScanIntent.setData(contentUri);

				// Send the broadcast to inform others that there is a new addition to media
				getContext().sendBroadcast(mediaScanIntent);

				NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.SAVE_IMAGE_TO_GALLERY_FINISHED, Boolean.toString(true));
			}
			else
			{
				Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Creating image from byte array failed!!!");
				NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.SAVE_IMAGE_TO_GALLERY_FINISHED, Boolean.toString(false));
			}
		}
		catch (Exception e)
		{
			//This can happen if no external storage available
			NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.SAVE_IMAGE_TO_GALLERY_FINISHED, Boolean.toString(false));
		}

	}

	public void playVideoFromGallery()
	{
		Bundle info = new Bundle();
		info.putString(Keys.TYPE, VIDEO_FROM_LIBRARY);
		startMediaPickerActivityWithInfo(info, GalleryVideoLauncherActivity.class);
	}

	public void playVideoFromURL(String urlPathString)
	{

		Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "Path to play : " + urlPathString);

		// Send intent details as picking from album
		Bundle info = new Bundle();

		info.putString(Keys.TYPE, PLAY_VIDEO_FROM_PATH);

		info.putString(Keys.URL, urlPathString);
		startMediaPickerActivityWithInfo(info, MediaLibraryActivity.class);
	}

	public void playVideoFromYoutube(String youtubeVideoId)
	{
		Bundle info = new Bundle();

		info.putString(Keys.TYPE, PLAY_VIDEO_FROM_YOUTUBE);

		info.putString(Keys.MediaLibrary.YOUTUBE_VIDEO_ID, youtubeVideoId);
		startMediaPickerActivityWithInfo(info, MediaLibraryActivity.class);
	}

	public void playVideoFromWebView(String htmlString)
	{

		Debug.log(CommonDefines.MEDIAL_LIBRARY_TAG, "Path from html : " + htmlString);

		// Send intent details as picking from album
		Bundle info = new Bundle();

		info.putString(Keys.TYPE, PLAY_VIDEO_FROM_WEBVIEW);

		info.putString(Keys.HTML, htmlString);
		startMediaPickerActivityWithInfo(info, MediaLibraryActivity.class);
	}

	public void stopVideo()
	{
		Intent intent = new Intent();
		intent.setAction(VIDEO_INTENT_ACTION);
		intent.putExtra(Keys.TYPE, STOP_PLAYING_VIDEO);
		getContext().sendBroadcast(intent);
	}

	void takePictureFromGallery(float scale)
	{
		// Send intent details as picking from album
		Bundle info = new Bundle();
		info.putFloat(Keys.SCALE_FACTOR, scale);
		info.putString(Keys.TYPE, IMAGE_FROM_LIBRARY);
		startMediaPickerActivityWithInfo(info, MediaLibraryActivity.class);
	}

	void takePictureFromCamera(float scale)
	{
		if (isCameraSupported())
		{
			Bundle info = new Bundle();
			info.putFloat(Keys.SCALE_FACTOR, scale);
			info.putString(Keys.TYPE, IMAGE_FROM_CAMERA);
			startMediaPickerActivityWithInfo(info, MediaLibraryActivity.class);
		}
		else
		{
			Debug.error(CommonDefines.MEDIAL_LIBRARY_TAG, "Camera not supported on this device");
		}
	}

	void startMediaPickerActivityWithInfo(Bundle info, Class<?> activityClass)
	{
		Intent intent = new Intent(NativePluginHelper.getCurrentContext(), activityClass);
		intent.putExtras(info);
		NativePluginHelper.startActivityOnUiThread(intent);
	}

	void takePictureFromBothGalleryAndCamera(final float scale)
	{
		//Create thread to present UI
		Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					AlertDialog.Builder alert = new AlertDialog.Builder(NativePluginHelper.getCurrentContext());

					final CharSequence[] items = { Keys.MediaLibrary.CHOOSE_FROM_GALLERY, Keys.MediaLibrary.OPEN_CAMERA, Keys.MediaLibrary.CANCEL };

					alert.setTitle(Keys.MediaLibrary.SELECT_SOURCE);

					alert.setItems(items, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int item)
							{
								if (items[item].equals(Keys.MediaLibrary.CHOOSE_FROM_GALLERY))
								{
									takePictureFromGallery(scale);
								}
								else if (items[item].equals(Keys.MediaLibrary.OPEN_CAMERA))
								{
									takePictureFromCamera(scale);
								}
								else if (items[item].equals(Keys.MediaLibrary.CANCEL))
								{
									dialog.dismiss();
									onCancelEvent();
								}
							}
						});
					alert.setOnCancelListener(new OnCancelListener()
						{
							@Override
							public void onCancel(DialogInterface dialog)
							{
								onCancelEvent();
							}
						});
					alert.show();
				}

				void onCancelEvent()
				{
					HashMap<String, String> map = new HashMap<String, String>(); //TODO reduce duplicate code
					map.put(com.voxelbusters.nativeplugins.defines.Keys.MediaLibrary.IMAGE_PATH, null);
					map.put(com.voxelbusters.nativeplugins.defines.Keys.MediaLibrary.FINISH_REASON, Keys.MediaLibrary.PICK_IMAGE_CANCELLED);
					NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PICK_IMAGE_FINISHED, map);
				}
			};

		NativePluginHelper.executeOnUIThread(runnable);

	}

	Context getContext()
	{
		return NativePluginHelper.getCurrentContext();
	}
}
