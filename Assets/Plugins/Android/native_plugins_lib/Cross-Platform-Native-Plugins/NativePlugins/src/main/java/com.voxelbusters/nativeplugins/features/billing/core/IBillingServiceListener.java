package com.voxelbusters.nativeplugins.features.billing.core;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public interface IBillingServiceListener
{
	void onSetupFinished(Boolean isBillingAvialable);

	void onRequestProductsFinished(ArrayList<JsonObject> productDetails, String error);

	void onPurchaseTransactionFinished(ArrayList<JsonObject> transactionDetails, String error);

	void onRestoreTransactionFinished(ArrayList<JsonObject> transactionDetails, String error);
}
