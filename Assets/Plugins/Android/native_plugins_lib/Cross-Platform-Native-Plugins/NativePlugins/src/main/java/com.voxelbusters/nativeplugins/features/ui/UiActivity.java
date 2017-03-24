package com.voxelbusters.nativeplugins.features.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.util.HashMap;

public class UiActivity extends Activity
{
	AlertDialog	alertDialog;
	Bundle		bundleInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (bundleInfo == null)
		{
			Intent intent = getIntent();
			bundleInfo = intent.getExtras();
		}

		//Get the type first.
		eUiType type = eUiType.values()[bundleInfo.getInt(Keys.TYPE)];

		if (type == eUiType.ALERT_DIALOG)
		{
			showAlertDialog(bundleInfo);
		}
		else if (type == eUiType.SINGLE_FIELD_PROMPT)
		{
			showSinglePrompt(bundleInfo);
		}
		else if (type == eUiType.LOGIN_PROMPT)
		{
			showLoginPrompt(bundleInfo);
		}

	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume()
	{
		super.onResume();

		if (paused)
		{

			finish();

			android.os.Handler h = new android.os.Handler();
			h.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						startActivity(getIntent());

					}
				}, 10);
		}
	}

	boolean	paused;

	@Override
	protected void onPause()
	{
		super.onPause();
		paused = true;
	}

	@Override
	public void onConfigurationChanged(Configuration new_config)
	{
		super.onConfigurationChanged(new_config);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (alertDialog != null)
		{
			//All windows and dialogs should be closed before exiting the activity.
			alertDialog.dismiss();
			alertDialog = null;
		}
	}

	private void showAlertDialog(Bundle bundle)
	{
		final String tag = bundle.getString(Keys.TAG);

		String[] buttonsList = bundle.getStringArray(Keys.BUTTON_LIST);

		alertDialog = getDialogWithDefaultDetails(bundle);

		int length = buttonsList.length > 3 ? 3 : buttonsList.length;//Setting max of 3 buttons.
		final String[] tempButtonList = buttonsList;
		for (int eachButtonIndex = 0; eachButtonIndex < length; eachButtonIndex++)
		{
			// Because Android's Dialog interface is restricted to 3 buttons at
			// max.
			int buttonActionIndex = DialogInterface.BUTTON_POSITIVE - eachButtonIndex;

			final int index = eachButtonIndex;
			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						String buttonPressed = tempButtonList[index];
						String callerTag = tag;

						HashMap<String, String> dataMap = new HashMap<String, String>();
						dataMap.put(Keys.Ui.BUTTON_PRESSED, buttonPressed);
						dataMap.put(Keys.Ui.CALLER, callerTag);

						NativePluginHelper.sendMessage(UnityDefines.Ui.ALERT_DIALOG_CLOSED, dataMap);
						UiHandler.getInstance().onFinish(callerTag);

						finish();//Close this activity
					}

				};

			alertDialog.setButton(buttonActionIndex, buttonsList[eachButtonIndex], onClickListener);
		}

		alertDialog.show();

	}

	private void showSinglePrompt(Bundle bundle)
	{
		String[] buttonsList = bundle.getStringArray(Keys.BUTTON_LIST);

		alertDialog = getDialogWithDefaultDetails(bundle);

		final EditText promptField = new EditText(this);

		alertDialog.setView(promptField);

		boolean isSecureText = bundle.getBoolean(Keys.IS_SECURE);
		String placeHolderText = bundle.getString(Keys.PLACE_HOLDER_TEXT_1);

		if (isSecureText)
		{
			promptField.setTransformationMethod(new PasswordTransformationMethod());
		}

		if (placeHolderText != null)
		{
			promptField.setHint(placeHolderText);
		}

		// Add the buttons
		// [DialogInterface.BUTTON_NEUTRAL,DialogInterface.BUTTON_NEGATIVE],DialogInterface.BUTTON_POSITIVE]

		int length = buttonsList.length > 3 ? 3 : buttonsList.length;//Setting max of 3 buttons.
		final String[] tempButtonList = buttonsList;
		for (int eachButtonIndex = 0; eachButtonIndex < length; eachButtonIndex++)
		{
			// Because Android's Dialog interface is restricted to 3 buttons at
			// max.
			int buttonActionIndex = DialogInterface.BUTTON_POSITIVE - eachButtonIndex;

			final int index = eachButtonIndex;
			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						String promptText = promptField.getText().toString();

						HashMap<String, String> dataMap = new HashMap<String, String>();
						dataMap.put(Keys.Ui.BUTTON_PRESSED, tempButtonList[index]);
						dataMap.put(Keys.Ui.INPUT, promptText);

						NativePluginHelper.sendMessage(UnityDefines.Ui.SINGLE_FIELD_PROMPT_DIALOG_CLOSED, dataMap);

						//close the activity
						finish();
					}

				};

			alertDialog.setButton(buttonActionIndex, buttonsList[eachButtonIndex], onClickListener);
		}

		alertDialog.show();
	}

	private void showLoginPrompt(Bundle bundle)
	{
		String[] buttonsList = bundle.getStringArray(Keys.BUTTON_LIST);

		LinearLayout mainlayout = new LinearLayout(this);
		setContentView(mainlayout);

		final String[] finalButtonList = buttonsList;

		alertDialog = getDialogWithDefaultDetails(bundle);

		final EditText usernameField = new EditText(this);
		final EditText passwordField = new EditText(this);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(usernameField);
		layout.addView(passwordField);

		// Save the ContentView and set the view
		alertDialog.setView(layout);

		String placeHolder1Text = bundle.getString(Keys.PLACE_HOLDER_TEXT_1);
		String placeHolder2Text = bundle.getString(Keys.PLACE_HOLDER_TEXT_2);

		//Set password field to secure text
		passwordField.setTransformationMethod(new PasswordTransformationMethod());

		//Set placeholder text
		usernameField.setHint(placeHolder1Text);
		passwordField.setHint(placeHolder2Text);

		// Add the buttons
		// [DialogInterface.BUTTON_NEUTRAL,DialogInterface.BUTTON_NEGATIVE],DialogInterface.BUTTON_POSITIVE]

		int length = finalButtonList.length > 3 ? 3 : finalButtonList.length;//Setting max of 3 buttons.
		for (int eachButtonIndex = 0; eachButtonIndex < length; eachButtonIndex++)
		{
			// Because Android's Dialog interface is restricted to 3 buttons at
			// max.
			int buttonActionIndex = DialogInterface.BUTTON_POSITIVE - eachButtonIndex;

			final int index = eachButtonIndex;
			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						HashMap<String, String> dataMap = new HashMap<String, String>();

						dataMap.put(Keys.Ui.BUTTON_PRESSED, finalButtonList[index]);

						// Update the key,values to send to unity
						dataMap.put(Keys.Ui.USER_NAME, usernameField.getText().toString());
						dataMap.put(Keys.Ui.PASSWORD, passwordField.getText().toString());

						NativePluginHelper.sendMessage(UnityDefines.Ui.LOGIN_PROMPT_DIALOG_CLOSED, dataMap);

						//close the activity
						finish();
					}

				};

			alertDialog.setButton(buttonActionIndex, finalButtonList[eachButtonIndex], onClickListener);
		}

		alertDialog.show();
	}

	AlertDialog getDialogWithDefaultDetails(Bundle bundle)
	{
		//Create alert dialog here and set its properties.
		String title = bundle.getString(Keys.TITLE);
		String message = bundle.getString(Keys.MESSAGE);

		// Make the builder by sending current context
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// Set text
		if (!StringUtility.isNullOrEmpty(title))
		{
			builder.setTitle(title);
		}
		if (!StringUtility.isNullOrEmpty(message))
		{
			builder.setMessage(message);
		}
		// Setting to false as user needs to take an action for sure
		builder.setCancelable(false);

		// Create the AlertDialog with above details
		return builder.create();
	}

}
