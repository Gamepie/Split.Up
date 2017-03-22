package com.voxelbusters.nativeplugins.features.medialibrary;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;

public class GalleryVideoLauncherActivity extends Activity
{
	public static final int	REQUEST_CODE_UNKNOWN	= -1;
	public static final int	REQUEST_CODE_PICK_VIDEO	= 1;
	public static final int	REQUEST_CODE_PLAY_VIDEO	= 2;

	Bundle					bundleInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (bundleInfo == null)
		{
			Intent intent = getIntent();
			bundleInfo = intent.getExtras();
		}

		Intent requiredIntent = null;
		int requestCode = REQUEST_CODE_UNKNOWN;

		String type = bundleInfo.getString(Keys.TYPE);

		if (type.equals(MediaLibraryHandler.VIDEO_FROM_LIBRARY))
		{
			requiredIntent = new Intent(Intent.ACTION_PICK);
			requiredIntent.setType(Keys.Mime.VIDEO_ALL);
			requestCode = REQUEST_CODE_PICK_VIDEO;
		}

		if (requiredIntent != null)
		{
			startActivityForResult(Intent.createChooser(requiredIntent, Keys.MediaLibrary.SELECT_MEDIA), requestCode);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case REQUEST_CODE_PICK_VIDEO:
				if ((resultCode == RESULT_OK) && (data != null))
				{
					NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FROM_GALLERY_FINISHED, Keys.MediaLibrary.PICK_VIDEO_SELECTED);

					Uri videoUri = data.getData();

					Bundle info = new Bundle();
					info.putString(Keys.TYPE, MediaLibraryHandler.PLAY_VIDEO_FROM_PATH);

					info.putString(Keys.URL, videoUri.toString());

					Intent intent = new Intent(this, MediaLibraryActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtras(info);
					startActivity(intent);

				}
				else
				{
					NativePluginHelper.sendMessage(UnityDefines.MediaLibrary.PLAY_VIDEO_FROM_GALLERY_FINISHED, Keys.MediaLibrary.PICK_VIDEO_CANCELLED);
				}
				break;
		}
		finish();
	}
}
