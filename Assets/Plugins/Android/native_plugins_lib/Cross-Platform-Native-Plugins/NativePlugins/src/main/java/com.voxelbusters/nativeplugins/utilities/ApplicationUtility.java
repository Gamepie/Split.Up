package com.voxelbusters.nativeplugins.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Environment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;

import java.io.File;
import java.util.List;

public class ApplicationUtility
{
	final static int	PLAY_SERVICES_RESOLUTION_REQUEST	= 100000;	// Some
																		// random
																		// value

	// number unique
	// with in this
	// app

	public static boolean isGooglePlayServicesAvailable(final Context context, boolean resolveError)
	{
		final GoogleApiAvailability apiAvailabilityInstance = GoogleApiAvailability.getInstance();
		final int resultCode = apiAvailabilityInstance.isGooglePlayServicesAvailable(context);

		if (resultCode == ConnectionResult.SUCCESS)
		{
			return true;
		}
		else if ((resultCode == ConnectionResult.SERVICE_MISSING) || (resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) || (resultCode == ConnectionResult.SERVICE_DISABLED) || (resultCode == ConnectionResult.SERVICE_INVALID))
		{
			if (resolveError)
			{
				Runnable runnableThread = new Runnable()
					{
						@Override
						public void run()
						{
							if (apiAvailabilityInstance.isUserResolvableError(resultCode))
							{
								String errorString = apiAvailabilityInstance.getErrorString(resultCode);

								Debug.log(CommonDefines.APPLICATION_UTILITY_TAG, "Google Play Services error - " + errorString);

								apiAvailabilityInstance.getErrorDialog((Activity) context, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
							}
							else
							{
								Debug.error(CommonDefines.APPLICATION_UTILITY_TAG, "This device does not support Google Play Services.");
							}
						}
					};
				NativePluginHelper.executeOnUIThread(runnableThread);
			}
			return false;
		}
		else
		{
			Debug.error(CommonDefines.APPLICATION_UTILITY_TAG, "This device does not support Google Play Services. Unknown Error Occured!");
			return false;
		}

	}

	//

	public static ApplicationInfo getApplicationInfo(Context context)
	{
		PackageManager packageManager = context.getPackageManager();
		ApplicationInfo appInfo = null;
		try
		{
			appInfo = packageManager.getApplicationInfo(getPackageName(context), 0);
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
			Debug.error(CommonDefines.APPLICATION_UTILITY_TAG, "Package name not found!");
		}
		return appInfo;
	}

	public static String getApplicationName(Context context)
	{

		ApplicationInfo appInfo = getApplicationInfo(context);
		PackageManager packageManager = context.getPackageManager();
		String name = packageManager.getApplicationLabel(appInfo).toString();

		return name;
	}

	// Get Package name com.company.product
	public static String getPackageName(Context context)
	{
		String packageName = context.getPackageName();

		return packageName;
	}

	public static String getFileProviderAuthoityName(Context context)//Assuming to be unique
	{
		String authorityName = getPackageName(context) + ".fileprovider";
		return authorityName;
	}

	public static Context getApplicationContext(Context context)
	{
		return context.getApplicationContext();
	}

	public static boolean isIntentAvailable(Context context, String action, String type, String packageName)
	{
		Intent intent = new Intent(action);
		intent.setType(type);
		if (packageName != null)
		{
			intent.setPackage(packageName);
		}

		return isIntentAvailable(context, intent);
	}

	public static boolean isIntentAvailable(Context context, Intent intent)
	{

		PackageManager packageManager = context.getPackageManager();

		List<ResolveInfo> listOfIntents = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return listOfIntents.size() > 0;
	}

	public static int getResourceId(Context context, String name, String defType)
	{
		name = StringUtility.getFileNameWithoutExtension(name);
		String packageName = ApplicationUtility.getPackageName(context);
		int resourceId = context.getResources().getIdentifier(name, defType, packageName);
		return resourceId;
	}

	public static Class<?> GetMainLauncherActivity(Context context)
	{
		String packageName = context.getPackageName();
		Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
		String className = launchIntent.getComponent().getClassName();
		try
		{
			return Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static boolean hasPermission(Context context, String permissionName)
	{
		PackageManager pm = context.getPackageManager();
		int hasPermission = pm.checkPermission(permissionName, ApplicationUtility.getPackageName(context));

		return hasPermission == PackageManager.PERMISSION_GRANTED;
	}

	public static File getLocalSaveDirectory(Context context, String dirName)
	{
		return getSaveDirectory(context, dirName, context.getApplicationContext().getFilesDir());
	}

	public static File getExternalTempDirectoryIfExists(Context context, String dirName)
	{
		if (hasExternalStorageWritable(context))
		{
			return getSaveDirectory(context, dirName, context.getApplicationContext().getExternalCacheDir());
		}
		else
		{
			return getLocalSaveDirectory(context, dirName);
		}
	}

	static File getSaveDirectory(Context context, String dirName, File destinationDir)
	{
		if (StringUtility.isNullOrEmpty(dirName))
		{
			dirName = getApplicationName(context);
		}

		File file = new File(destinationDir, dirName);
		file.mkdirs();

		return file;
	}

	public static boolean hasExternalStorageWritable(Context context)
	{
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static Object getSystemService(Context context, String serviceName)
	{
		return context.getSystemService(serviceName);
	}

	public static String getString(Context context, int stringId)
	{
		return context.getResources().getString(stringId);
	}

	public static boolean isAmazonPlatform(Context context)
	{
		String pkgInstaller = getPackageInstallerName(context);

		return pkgInstaller.startsWith("com.amazon");
	}

	public static boolean isGooglePlatform(Context context)
	{
		String pkgInstaller = getPackageInstallerName(context);
		return ("com.android.vending".equals(pkgInstaller));
	}

	public static String getPackageInstallerName(Context context)
	{
		PackageManager pkgManager = context.getPackageManager();

		String installerPackageName = pkgManager.getInstallerPackageName(context.getPackageName());

		Debug.log(CommonDefines.APPLICATION_UTILITY_TAG, "Installer Name : " + installerPackageName);

        if (StringUtility.isNullOrEmpty(installerPackageName))
        {
            installerPackageName = "";
        }

		return installerPackageName;
	}

}
