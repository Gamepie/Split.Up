package com.voxelbusters.nativeplugins.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

public class SharedPreferencesUtility
{

	public static JSONArray getJsonArray(String preferencesName, int mode, Context context, String keyName)
	{

		SharedPreferences sharedPref = getSharedPreferences(preferencesName, mode, context);

		String jsonData = sharedPref.getString(keyName, "");

		return JSONUtility.getJSONArray(jsonData);
	}

	public static SharedPreferences getSharedPreferences(String preferencesName, int mode, Context context)
	{
		SharedPreferences sharedPref = context.getSharedPreferences(preferencesName, mode);
		return sharedPref;
	}

	//

	public static void setJSONArray(String preferencesName, int mode, Context context, String keyName, JSONArray array)
	{
		SharedPreferences sharedPref = getSharedPreferences(preferencesName, mode, context);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(keyName, array.toString());
		editor.commit();
	}
}
