package com.voxelbusters.nativeplugins.features.billing.core;

import android.content.Context;

public interface IBillingService
{
	// Init with public key - used for validating the receipt of transaction and context - to launch any buy intents
	public void init(String key, Context context, String[] consumableList);

	//Request the service to fetch details for consumable and non-consumable products
	public void requestBillingProducts(String[] consumableProductIDsJson, String[] nonConsumableProductIDs);

	//Query for purchase status of a productId
	public boolean isProductPurchased(String productId);

	//Restore non-consumable previous purchases
	public void restoreCompletedTransactions();

	//Listener to receive events from this service
	public void setListener(IBillingServiceListener serviceListener);

	//To launch buy intent from native provider
	void buyProduct(String productID, Context context);

}
