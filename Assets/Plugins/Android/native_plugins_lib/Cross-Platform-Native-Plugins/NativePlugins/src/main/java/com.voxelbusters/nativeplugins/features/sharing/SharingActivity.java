package com.voxelbusters.nativeplugins.features.sharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Enums.eShareOptions;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SharingActivity extends Activity
{

	final int	SHARING_REQUEST_CODE			= 1;
	final int	SEND_MAIL_REQUEST_CODE			= 2;
	final int	SEND_SMS_REQUEST_CODE			= 3;
	final int	SHARE_ON_WHATS_APP_REQUEST_CODE	= 4;

	Bundle		bundleInfo;

	File		currentImageFileShared			= null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (bundleInfo == null)
		{
			Intent intent = getIntent();
			bundleInfo = intent.getExtras();
		}

		String type = bundleInfo.getString(Keys.TYPE);

		String orientation = SharingHandler.allowedOrientation;

		if ("POTRAIT".equals(orientation))
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
		else if ("LANDSCAPE".equals(orientation))
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}

		if (StringUtility.isNullOrEmpty(type))
		{

			String message = bundleInfo.getString(Keys.MESSAGE);
			String url = bundleInfo.getString(Keys.URL);
			String imagePath = bundleInfo.getString(Keys.IMAGE_PATH);

			String[] exclusionList = bundleInfo.getStringArray(Keys.EXCLUDE_LIST);

			shareItem(message, url, imagePath, type, exclusionList);

		}
		else if (type.equals(Keys.Sharing.SMS))
		{
			shareWithSMS(bundleInfo);
		}
		else if (type.equals(Keys.Sharing.MAIL))
		{
			shareWithEmail(bundleInfo);
		}
		else if (type.equals(Keys.Sharing.WHATS_APP))
		{
			shareOnWhatsApp(bundleInfo);
		}
		else
		{
			Debug.log(CommonDefines.SHARING_TAG, "Sharing not implemented for this type " + type);
		}
	}

	private void shareItem(String message, String urlString, String imagePath, String type, String[] excludedShareOptions)
	{
		Context context = NativePluginHelper.getCurrentContext();

		// Create ACTION_SEND intent
		Intent shareIntent = new Intent(Intent.ACTION_SEND);

		String mimeType = getMimeType(type, !StringUtility.isNullOrEmpty(imagePath));
		// Set MIME type based on the available content
		shareIntent.setType(mimeType);

		if (StringUtility.isNullOrEmpty(urlString))
		{
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
		}
		else
		{
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, message + "\n" + urlString);
		}

		if (!StringUtility.isNullOrEmpty(imagePath))
		{
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imagePath));
		}

		shareIntent.addCategory(Intent.CATEGORY_DEFAULT);

		boolean failedSharing = false;

		boolean needsOnlySocialNetworkingServices = false;

		//Find if this is social network sharing or not. check if mail , messaging and whatsapp are in excluded list.
		if ((excludedShareOptions != null) && StringUtility.contains("" + eShareOptions.MESSAGE.ordinal(), excludedShareOptions) && StringUtility.contains("" + eShareOptions.MAIL.ordinal(), excludedShareOptions) && StringUtility.contains("" + eShareOptions.WHATSAPP.ordinal(), excludedShareOptions))
		{
			needsOnlySocialNetworkingServices = true;
		}

		List<ResolveInfo> availableActivitiesResInfo = context.getPackageManager().queryIntentActivities(shareIntent, 0); //TODO

		if (!availableActivitiesResInfo.isEmpty())
		{
			List<Intent> targetedIntents = new ArrayList<Intent>();

			for (ResolveInfo resolveInfo : availableActivitiesResInfo)
			{
				String packageName = resolveInfo.activityInfo != null ? resolveInfo.activityInfo.packageName : null;

				// Check here if exclusion share options list includes this
				// package.
				if ((packageName == null) || SharingHelper.checkIfPackageMatchesShareOptions(packageName, excludedShareOptions))
				{
					continue;
				}

				if (needsOnlySocialNetworkingServices)
				{
					if (!SharingHelper.isSocialNetwork(packageName))//Skipping if its not social network
					{
						continue;
					}
				}

				Intent intent = new Intent(shareIntent);

				intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
				intent.setPackage(packageName);

				targetedIntents.add(intent);
			}

			if (!targetedIntents.isEmpty())
			{
				//Share Intent
				Intent startIntent = targetedIntents.remove(0);

				final Intent chooserIntent = Intent.createChooser(startIntent, "Share via");
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toArray(new Parcelable[] {}));
				chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivityForResult(chooserIntent, SHARING_REQUEST_CODE);
			}
			else
			{
				failedSharing = true;
			}
		}
		else
		{
			failedSharing = true;
		}

		if (failedSharing)
		{

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this).setTitle("Share").setMessage("No services found!").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						NativePluginHelper.sendMessage(UnityDefines.Sharing.FINISHED, Keys.Sharing.FAILED);
						SharingActivity.this.finish();
					}

				});
			alertDialogBuilder.setOnKeyListener(new Dialog.OnKeyListener()
				{

					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
					{
						if (keyCode == KeyEvent.KEYCODE_BACK)
						{
							NativePluginHelper.sendMessage(UnityDefines.Sharing.FINISHED, Keys.Sharing.FAILED);
							SharingActivity.this.finish();
						}
						return true;
					}
				});

			AlertDialog warningDialog = alertDialogBuilder.create();
			warningDialog.show();

		}
	}

	void shareWithEmail(Bundle info)
	{
		CharSequence body = info.getCharSequence(Keys.BODY);

		String subject = info.getString(Keys.SUBJECT);

		String[] toRecipients = info.getStringArray(Keys.Sharing.TO_RECIPIENT_LIST);
		String[] ccRecipients = info.getStringArray(Keys.Sharing.CC_RECIPIENT_LIST);
		String[] bccRecipients = info.getStringArray(Keys.Sharing.BCC_RECIPIENT_LIST);

		String[] attachmentPaths = info.getStringArray(Keys.ATTACHMENT);

		String mimeType = getMimeType(Keys.Sharing.MAIL, false);

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType(mimeType);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

		//Set receipient list.
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, toRecipients);
		intent.putExtra(android.content.Intent.EXTRA_CC, ccRecipients);
		intent.putExtra(android.content.Intent.EXTRA_BCC, bccRecipients);

		if (attachmentPaths != null)
		{
			intent.setAction(Intent.ACTION_SEND_MULTIPLE);
			ArrayList<Uri> uris = new ArrayList<Uri>();

			for (String attachmentPath : attachmentPaths)
			{
				Uri _eachUri = Uri.parse(attachmentPath);
				uris.add(_eachUri);
			}

			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

			//Uri attachmentUri = Uri.parse(attachmentPath);
			//intent.putExtra(Intent.EXTRA_STREAM, attachmentUri);
		}

		startActivityForResult(intent, SEND_MAIL_REQUEST_CODE);

	}

	void shareWithSMS(Bundle info)
	{
		String message = info.getString(Keys.MESSAGE);
		String[] recipientsList = info.getStringArray(Keys.Sharing.TO_RECIPIENT_LIST);
		String recipientsListStr = "";

		if (recipientsList != null)
		{
			for (String each : recipientsList)
			{
				recipientsListStr += (each + ";");
			}
		}

		Uri smsUri = Uri.parse(Keys.Intent.SCHEME_SEND_TO + recipientsListStr);

		Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
		intent.putExtra(Keys.Intent.SMS_BODY, message);

		startActivityForResult(intent, SEND_SMS_REQUEST_CODE);
	}

	void shareOnWhatsApp(Bundle info)
	{
		//SHARE_ON_WHATS_APP
		String message = info.getString(Keys.MESSAGE);
		String imagePath = info.getString(Keys.IMAGE_PATH);

		String mimeType = getMimeType(Keys.Sharing.WHATS_APP, !StringUtility.isNullOrEmpty(imagePath));

		Intent intent = new Intent(Intent.ACTION_SEND);
		if (message != null)
		{
			intent.putExtra(Intent.EXTRA_TEXT, message);
		}

		if (imagePath != null)
		{
			Uri attachmentUri = Uri.parse(imagePath);
			intent.putExtra(Intent.EXTRA_STREAM, attachmentUri);
		}

		intent.setType(mimeType);
		intent.setPackage(Keys.Package.WHATS_APP);

		startActivityForResult(intent, SHARE_ON_WHATS_APP_REQUEST_CODE);
	}

	String getMimeType(String type, boolean hasImage)
	{
		String mimeType = Keys.Mime.PLAIN_TEXT;

		if (hasImage)
		{
			mimeType = Keys.Mime.IMAGE_ALL;
		}

		// Override if specific types
		if (Keys.Sharing.MAIL.equals(type))
		{
			mimeType = Keys.Mime.EMAIL;
		}

		return mimeType;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if ((requestCode == SHARING_REQUEST_CODE))
		{

			NativePluginHelper.sendMessage(UnityDefines.Sharing.FINISHED, Keys.Sharing.CLOSED);
		}
		else if (requestCode == SEND_MAIL_REQUEST_CODE)
		{
			NativePluginHelper.sendMessage(UnityDefines.Sharing.SENT_MAIL, Keys.Sharing.CLOSED);
		}
		else if (requestCode == SEND_SMS_REQUEST_CODE)
		{
			NativePluginHelper.sendMessage(UnityDefines.Sharing.SENT_SMS, Keys.Sharing.CLOSED);
		}
		else if (requestCode == SHARE_ON_WHATS_APP_REQUEST_CODE)
		{
			NativePluginHelper.sendMessage(UnityDefines.Sharing.WHATSAPP_SHARE_FINISHED, Keys.Sharing.CLOSED);
		}

		// Calling finish on any result currently
		finish();
	}
}
