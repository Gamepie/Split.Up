package com.voxelbusters.nativeplugins.features.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.util.ArrayList;
import java.util.HashMap;

enum eUiType
{
	ALERT_DIALOG, SINGLE_FIELD_PROMPT, LOGIN_PROMPT
}

public class UiHandler
{
	private final HashMap<String, Bundle>	uiElementsMap;

	//This holds if any ui elements in queue to be displayed
	private final ArrayList<String>			queueList;

	private String							currentDisplayedUiTag	= null;

	// Create singleton instance
	private static UiHandler				INSTANCE;

	public static UiHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new UiHandler();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private UiHandler()
	{
		uiElementsMap = new HashMap<String, Bundle>();
		queueList = new ArrayList<String>();
	}

	public void showAlertDialogWithMultipleButtons(String title, String message, String buttonsListJson, String tag)
	{
		Bundle bundle = new Bundle();
		bundle.putString(Keys.TITLE, title);
		bundle.putString(Keys.MESSAGE, message);
		bundle.putStringArray(Keys.BUTTON_LIST, StringUtility.convertJsonStringToStringArray(buttonsListJson));
		bundle.putString(Keys.TAG, tag);
		bundle.putInt(Keys.TYPE, eUiType.ALERT_DIALOG.ordinal());

		pushToActivityQueue(bundle, tag);
	}

	public void showSingleFieldPromptDialog(String title, String message, String placeHolder, boolean useSecureText, String buttonsListJson)
	{
		Bundle bundle = new Bundle();
		bundle.putString(Keys.TITLE, title);
		bundle.putString(Keys.MESSAGE, message);
		bundle.putStringArray(Keys.BUTTON_LIST, StringUtility.convertJsonStringToStringArray(buttonsListJson));
		bundle.putBoolean(Keys.IS_SECURE, useSecureText);
		bundle.putString(Keys.PLACE_HOLDER_TEXT_1, placeHolder);

		bundle.putInt(Keys.TYPE, eUiType.SINGLE_FIELD_PROMPT.ordinal());

		startActivity(bundle);
	}

	public void showLoginPromptDialog(String title, String message, String placeHolder1, String placeHolder2, String buttonsListJson)
	{
		Bundle bundle = new Bundle();
		bundle.putString(Keys.TITLE, title);
		bundle.putString(Keys.MESSAGE, message);
		bundle.putStringArray(Keys.BUTTON_LIST, StringUtility.convertJsonStringToStringArray(buttonsListJson));
		bundle.putString(Keys.PLACE_HOLDER_TEXT_1, placeHolder1);
		bundle.putString(Keys.PLACE_HOLDER_TEXT_2, placeHolder2);

		bundle.putInt(Keys.TYPE, eUiType.LOGIN_PROMPT.ordinal());

		startActivity(bundle);
	}

	public void showToast(final String message, String lengthType)
	{
		final int toastLength = lengthType.equals("SHORT") ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;

		Runnable runnableThread = new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(NativePluginHelper.getCurrentContext(), message, toastLength).show();
				}
			};

		NativePluginHelper.executeOnUIThread(runnableThread);
	}

	public void pushToActivityQueue(Bundle bundleInfo, String tag)
	{

		//Check if any Ui activity is already active. If so don't do anything.
		if (currentDisplayedUiTag != null)
		{
			Debug.log(CommonDefines.UI_TAG, "Queuing this ui element");
			queueList.add(tag);
			uiElementsMap.put(tag, bundleInfo);
		}
		else
		{
			startActivity(bundleInfo);
			currentDisplayedUiTag = tag;
		}

	}

	void startActivity(Bundle bundleInfo)
	{
		Intent intent = new Intent(NativePluginHelper.getCurrentContext(), UiActivity.class);
		intent.putExtras(bundleInfo);
		NativePluginHelper.startActivityOnUiThread(intent);
	}

	public void onFinish(String tag)
	{
		currentDisplayedUiTag = null;
		if (queueList.size() > 0)
		{
			String newTag = queueList.remove(queueList.size() - 1);
			pushToActivityQueue(uiElementsMap.get(newTag), newTag);
			uiElementsMap.remove(newTag);
		}
	}
}