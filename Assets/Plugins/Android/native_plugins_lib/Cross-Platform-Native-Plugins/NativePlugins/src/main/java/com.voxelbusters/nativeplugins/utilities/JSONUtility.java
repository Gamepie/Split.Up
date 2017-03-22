package com.voxelbusters.nativeplugins.utilities;

import android.os.Bundle;

import com.voxelbusters.nativeplugins.defines.CommonDefines;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

public class JSONUtility
{
	public static JSONObject getJSONfromBundle(Bundle bundle)
	{
		JSONObject json = new JSONObject();

		//Get all keys and create json
		Set<String> keys = bundle.keySet();

		for (String eachKey : keys)
		{
			Object eachVal = bundle.get(eachKey);

			try
			{
				json.put(eachKey, eachVal);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
				Debug.error(CommonDefines.JSON_UTILS_TAG, "Exception while entering key " + eachKey);
			}
		}
		return json;
	}

	public static String[] getKeys(JSONObject jsonData)
	{

		JSONArray jsonArray = jsonData.names();
		String[] keys = new String[jsonArray.length()];

		for (int i = 0, count = jsonArray.length(); i < count; i++)
		{
			try
			{
				keys[i] = jsonArray.getString(i);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}

		return keys;
	}

	public static JSONArray getJSONArray(String jsonArrayString)
	{
		JSONArray jsonArray;
		try
		{
			jsonArray = new JSONArray(jsonArrayString);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			jsonArray = new JSONArray();
		}

		return jsonArray;
	}

	public static JSONArray removeIndex(JSONArray jsonArray, int pos)
	{

		JSONArray newJsonArray = new JSONArray();
		try
		{
			for (int i = 0; i < jsonArray.length(); i++)
			{
				if (i != pos)
				{
					newJsonArray.put(jsonArray.get(i));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return newJsonArray;

	}

	public static int findString(JSONArray jsonArray, String stringToSearch)
	{
		int index = -1;
		for (int i = 0; i < jsonArray.length(); i++)
		{
			String str = null;
			try
			{
				str = jsonArray.getString(i);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			if ((str != null) && str.equals(stringToSearch))
			{
				index = i;
				break;
			}
		}
		return index;
	}

	@SuppressWarnings("rawtypes")
	public static String getJSONString(HashMap dataMap)
	{
		JSONObject json = new JSONObject(dataMap);
		return json.toString();
	}

	public static JSONObject getJSON(String jsonStr)
	{
		JSONObject json = null;
		try
		{
			json = new JSONObject(jsonStr);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
}
