package com.voxelbusters.nativeplugins.features.sharing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Enums.eShareOptions;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.utilities.FileUtility;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

public class SharingHandler
{

	public enum eShareCategeories
	{
		UNDEFINED, TEXT,
	};

	// Create singleton instance
	private static SharingHandler	INSTANCE;

	public static String			allowedOrientation;

	public static SharingHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new SharingHandler();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private SharingHandler()
	{
	}

	public boolean isServiceAvailable(int serviceTypeInt)
	{
		eShareOptions serviceType = eShareOptions.values()[serviceTypeInt];
		return SharingHelper.isServiceAvailable(getContext(), serviceType);
	}

	public void share(String message, String urlString, byte[] imageByteArray, int byteArrayLength, String excludedShareOptionsJson)
	{

		String[] excludedShareOptions = StringUtility.convertJsonStringToStringArray(excludedShareOptionsJson);

		Bundle bundle = new Bundle();
		bundle.putString(Keys.TYPE, "");
		bundle.putString(Keys.MESSAGE, message);
		bundle.putString(Keys.URL, urlString);

		Uri imageUri = FileUtility.createSharingFileUri(getContext(), imageByteArray, byteArrayLength, CommonDefines.SHARING_DIR, System.currentTimeMillis() + ".png");
		if (imageUri != null)
		{
			bundle.putString(Keys.IMAGE_PATH, imageUri.toString());
		}

		bundle.putStringArray(Keys.EXCLUDE_LIST, excludedShareOptions);

		startActivity(bundle);
	}

	public void sendSms(String messageBody, String recipientsListJson)
	{
		Bundle bundle = new Bundle();
		bundle.putString(Keys.TYPE, Keys.Sharing.SMS);
		bundle.putString(Keys.MESSAGE, messageBody);

		String[] recipientsList = StringUtility.convertJsonStringToStringArray(recipientsListJson);

		bundle.putStringArray(Keys.Sharing.TO_RECIPIENT_LIST, recipientsList);
		startActivity(bundle);
	}

	public void startActivity(Bundle bundleInfo)
	{
		Intent intent = new Intent(NativePluginHelper.getCurrentContext(), SharingActivity.class);
		intent.putExtras(bundleInfo);
		NativePluginHelper.startActivityOnUiThread(intent);
	}

	public void sendMail(String subject, String body, boolean isHtmlBody, byte[] attachmentByteArray, int attachmentByteArrayLength, String mimeType, String attachmentFileNameWithExtn, String toRecipientsJson, String ccRecipientsJson, String bccRecipientsJson)
	{
		Bundle bundle = new Bundle();
		bundle.putString(Keys.TYPE, Keys.Sharing.MAIL);

		bundle.putString(Keys.SUBJECT, subject);

		if (StringUtility.isNullOrEmpty(body))
		{
			body = "";
		}
		CharSequence messageBody = body;

		if (isHtmlBody)
		{
			messageBody = Html.fromHtml(body);
		}

		bundle.putCharSequence(Keys.BODY, messageBody);

		if (attachmentByteArrayLength != 0)
		{
			Uri attachmentUri = FileUtility.createSharingFileUri(getContext(), attachmentByteArray, attachmentByteArrayLength, CommonDefines.SHARING_DIR, attachmentFileNameWithExtn);

			if (attachmentUri != null)
			{
				String[] attachments = new String[] { attachmentUri.toString() };
				bundle.putStringArray(Keys.ATTACHMENT, attachments);
			}

		}

		String[] toRecipients = StringUtility.convertJsonStringToStringArray(toRecipientsJson);
		String[] ccRecipients = StringUtility.convertJsonStringToStringArray(ccRecipientsJson);
		String[] bccRecipients = StringUtility.convertJsonStringToStringArray(bccRecipientsJson);

		bundle.putStringArray(Keys.Sharing.TO_RECIPIENT_LIST, toRecipients);
		bundle.putStringArray(Keys.Sharing.CC_RECIPIENT_LIST, ccRecipients);
		bundle.putStringArray(Keys.Sharing.BCC_RECIPIENT_LIST, bccRecipients);

		startActivity(bundle);
	}

	public void shareOnWhatsApp(String message, byte[] imageByteArray, int imageArrayLength)
	{
		Uri imageUri = null;

		if (imageArrayLength != 0)
		{
			imageUri = FileUtility.createSharingFileUri(getContext(), imageByteArray, imageArrayLength, CommonDefines.SHARING_DIR, System.currentTimeMillis() + ".png");
		}

		Bundle bundle = new Bundle();
		bundle.putString(Keys.TYPE, Keys.Sharing.WHATS_APP);
		bundle.putString(Keys.MESSAGE, message);
		if (imageUri != null)
		{
			bundle.putString(Keys.IMAGE_PATH, imageUri.toString());
		}

		startActivity(bundle);

	}

	public void setAllowedOrientation(int orientation)
	{
		if ((orientation == 1) || (orientation == 2))
		{
			allowedOrientation = "POTRAIT";
		}
		else if ((orientation == 3) || (orientation == 4))
		{
			allowedOrientation = "LANDSCAPE";
		}
		else
		{
			allowedOrientation = null;
		}
	}

	Context getContext()
	{
		return NativePluginHelper.getCurrentContext();
	}

}