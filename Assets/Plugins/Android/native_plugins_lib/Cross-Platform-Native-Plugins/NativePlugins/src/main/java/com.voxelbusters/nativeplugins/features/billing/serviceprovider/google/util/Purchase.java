/*
 * Copyright (c) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.voxelbusters.nativeplugins.defines.Keys;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents an in-app billing purchase.
 */
public class Purchase
{
	String	mItemType;			// ITEM_TYPE_INAPP or ITEM_TYPE_SUBS
	String	mOrderId;
	String	mPackageName;
	String	mSku;
	long	mPurchaseTime;
	String	mPurchaseTimeStr;
	int		mPurchaseState;
	String	mDeveloperPayload;
	String	mToken;
	String	mOriginalJson;
	String	mSignature;
	String	mValidationState;

	//This creates dummy failed transaction
	public Purchase(String itemType, String productId)
	{
		JSONObject o = new JSONObject();

		try
		{
			o.put("purchaseState", -1);//Setting to -1 by default
			o.put("productId", productId);
			o.put("type", itemType);

		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		mItemType = itemType;
		mOriginalJson = o.toString();

		mOrderId = o.optString("orderId");
		mPackageName = o.optString("packageName");
		mSku = o.optString("productId");
		mPurchaseTime = o.optLong("purchaseTime");
		mPurchaseTimeStr = convertPurchaseTimeToString(mPurchaseTime);

		mPurchaseState = o.optInt("purchaseState");
		mDeveloperPayload = o.optString("developerPayload");
		mToken = o.optString("token", o.optString("purchaseToken"));
		mSignature = null;
		mValidationState = Keys.Billing.Validation.FAILED;//by default setting failed;
	}

	public Purchase(String itemType, String jsonPurchaseInfo, String signature) throws JSONException
	{
		mItemType = itemType;
		mOriginalJson = jsonPurchaseInfo;
		JSONObject o = new JSONObject(mOriginalJson);
		mOrderId = o.optString("orderId");
		mPackageName = o.optString("packageName");
		mSku = o.optString("productId");
		mPurchaseTime = o.optLong("purchaseTime");
		mPurchaseTimeStr = convertPurchaseTimeToString(mPurchaseTime);

		mPurchaseState = o.optInt("purchaseState");
		mDeveloperPayload = o.optString("developerPayload");
		mToken = o.optString("token", o.optString("purchaseToken"));
		mSignature = signature;
		mValidationState = Keys.Billing.Validation.FAILED;//by default setting failed;
	}

	String convertPurchaseTimeToString(long time)
	{
		Date purchaseDate = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");//http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
		String str = formatter.format(purchaseDate);
		return str;
	}

	public String getItemType()
	{
		return mItemType;
	}

	public String getOrderId()
	{
		return mOrderId;
	}

	public String getPackageName()
	{
		return mPackageName;
	}

	public String getSku()
	{
		return mSku;
	}

	public long getPurchaseTime()
	{
		return mPurchaseTime;
	}

	public int getPurchaseState()
	{
		return mPurchaseState;
	}

	public String getDeveloperPayload()
	{
		return mDeveloperPayload;
	}

	public String getToken()
	{
		return mToken;
	}

	public String getOriginalJson()
	{
		return mOriginalJson;
	}

	public String getSignature()
	{
		return mSignature;
	}

	public String getValidationState()
	{
		return mValidationState;
	}

	public void setValidationState(String state)
	{
		mValidationState = state;
	}

	@Override
	public String toString()
	{
		return "PurchaseInfo(type:" + mItemType + "):" + mOriginalJson;
	}

	public JsonObject getJsonObject()
	{
		JsonObject info = null;

		info = new JsonObject();

		JsonObject originalJson = new JsonObject();
		JsonParser jsonParser = new JsonParser();
		originalJson = (JsonObject) jsonParser.parse(mOriginalJson);

		info.add(Keys.Billing.ORIGINAL_JSON, originalJson);
		info.addProperty(Keys.Billing.RAW_PURCHASE_DATA, mOriginalJson);
		info.addProperty(Keys.Billing.PURCHASE_VALIDATION_STATE, mValidationState);
		info.addProperty(Keys.Billing.SIGNATURE, mSignature);

		return info;
	}
}
