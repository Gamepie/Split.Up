package com.voxelbusters.nativeplugins.utilities;

import android.util.Base64;

import com.voxelbusters.nativeplugins.defines.CommonDefines;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

public class StringUtility
{
	public static boolean isNullOrEmpty(String str)
	{
		if ((str == null) || str.equals("") || str.equals("null"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static String[] convertJsonStringToStringArray(String jsonString)
	{
		String[] stringArray = null;

		if (isNullOrEmpty(jsonString))
		{
			return null;
		}

		//First create JsonArray from this json string
		try
		{
			JSONArray jsonArray = new JSONArray(jsonString);
			int size = jsonArray.length();
			stringArray = new String[size];

			for (int i = 0; i < size; i++)
			{
				stringArray[i] = new String(jsonArray.getString(i));
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			Debug.error(CommonDefines.STRING_UTILS_TAG, "Error in parsing jsonString " + jsonString);
		}
		return stringArray;
	}

	public static boolean contains(String source, String[] list)
	{
		for (String element : list)
		{
			if (source.contains(element))
			{
				return true;
			}
		}
		return false;
	}

	public static String getCurrentTimeStamp()
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new Date());
		return timeStamp;

	}

	//Converts from base64 to utf-8 encoded string.
	public static String getBase64DecodedString(String base64String)
	{
		byte[] dataBytes = Base64.decode(base64String, Base64.DEFAULT);
		String text = "";

		try
		{
			text = new String(dataBytes, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return text;
	}

	public static String getCurrencySymbolFromCode(String currencyCode)
	{
		String symbol = "";
		try
		{
			Currency currency = Currency.getInstance(currencyCode);

			symbol = currency.getSymbol();
		}
		catch (Exception e)
		{
			Debug.log(CommonDefines.STRING_UTILS_TAG, "Error in converting currency code : " + currencyCode);
		}

		return symbol;
	}

	public static String getFileNameWithoutExtension(String fileWithExt)
	{
		String fileWithoutExt = fileWithExt;
		int dotIndex = fileWithExt.lastIndexOf('.');

		if (dotIndex >= 0)
		{
			fileWithoutExt = fileWithExt.substring(0, dotIndex);
		}

		return fileWithoutExt;
	}

}
