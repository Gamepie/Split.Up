package com.voxelbusters.nativeplugins.features.sharing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.voxelbusters.nativeplugins.defines.Enums.eShareOptions;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.Keys.Package;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class SharingHelper
{
	public static HashMap<String, eShareOptions>	packageNameMap	= null;

	// Adding a static block
	static
	{
		packageNameMap = new HashMap<String, eShareOptions>();

		packageNameMap.put(Keys.Package.FACEBOOK_1, eShareOptions.FB);
		packageNameMap.put(Keys.Package.FACEBOOK_2, eShareOptions.FB);
		packageNameMap.put(Keys.Package.TWITTER, eShareOptions.TWITTER);
		packageNameMap.put(Keys.Package.GOOGLE_PLUS, eShareOptions.GOOGLE_PLUS);
		packageNameMap.put(Keys.Package.INSTAGRAM, eShareOptions.INSTAGRAM);

		packageNameMap.put(Keys.Package.WHATS_APP, eShareOptions.WHATSAPP);

	}

	public static boolean checkIfPackageMatchesShareOptions(String packageName, String[] shareOptions)
	{
		eShareOptions shareOption = packageNameMap.get(packageName);

		// If shareOptionName exists
		if ((shareOptions != null) && (shareOptions.length > 0) && (shareOption != null))
		{
			// Check if this name exists in the shareOptions list.
			for (String each : shareOptions)
			{
				if (Integer.parseInt(each) == shareOption.ordinal())
				{
					return true;// exists
				}
			}
		}

		return false;
	}

	public static Intent[] getPrioritySocialNetworkingIntents(Intent referenceIntent)
	{
		ArrayList<Intent> list = new ArrayList<Intent>();
		for (String eachPackage : packageNameMap.keySet())
		{
			if (isSocialNetwork(eachPackage))
			{
				Intent intent = new Intent(referenceIntent);
				intent.setPackage(eachPackage);
				list.add(intent);
			}
		}

		return list.toArray(new Intent[list.size()]);
	}

	public static Intent[] getPriorityIntents(Intent sampleIntent)
	{
		ArrayList<Intent> list = new ArrayList<Intent>();
		for (String eachPackage : packageNameMap.keySet())
		{
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setPackage(eachPackage);
			list.add(intent);
		}

		return list.toArray(new Intent[list.size()]);
	}

	public static boolean isSocialNetwork(String packageName)
	{
		eShareOptions shareOption = packageNameMap.get(packageName);

		if ((shareOption != null) && ((eShareOptions.FB == shareOption) || (eShareOptions.TWITTER == shareOption) || (eShareOptions.GOOGLE_PLUS == shareOption) || (eShareOptions.INSTAGRAM == shareOption)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean isServiceAvailable(Context context, eShareOptions serviceType)
	{
		boolean isSupported = false;
		if (serviceType == eShareOptions.FB)
		{
			if (ApplicationUtility.isIntentAvailable(context, Intent.ACTION_SEND, Keys.Mime.PLAIN_TEXT, Package.FACEBOOK_1) || ApplicationUtility.isIntentAvailable(context, Intent.ACTION_SEND, Keys.Mime.PLAIN_TEXT, Package.FACEBOOK_2))
			{
				isSupported = true;
			}
		}
		else if (serviceType == eShareOptions.TWITTER)
		{
			isSupported = ApplicationUtility.isIntentAvailable(context, Intent.ACTION_SEND, Keys.Mime.PLAIN_TEXT, Package.TWITTER);
		}
		else if (serviceType == eShareOptions.WHATSAPP)
		{
			isSupported = ApplicationUtility.isIntentAvailable(context, Intent.ACTION_SEND, Keys.Mime.PLAIN_TEXT, Package.WHATS_APP);
		}
		else if (serviceType == eShareOptions.MESSAGE)
		{
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse(Keys.Intent.SCHEME_SEND_TO));

			isSupported = ApplicationUtility.isIntentAvailable(context, intent);
		}
		else if (serviceType == eShareOptions.MAIL)
		{
			isSupported = ApplicationUtility.isIntentAvailable(context, Intent.ACTION_SEND, Keys.Mime.EMAIL, null);
		}

		return isSupported;
	}
}