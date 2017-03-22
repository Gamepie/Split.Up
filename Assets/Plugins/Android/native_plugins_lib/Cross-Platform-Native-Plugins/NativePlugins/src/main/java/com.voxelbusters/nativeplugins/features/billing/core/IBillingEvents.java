package com.voxelbusters.nativeplugins.features.billing.core;

import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.BillingResult;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.Inventory;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.Purchase;

import org.json.JSONException;

import java.util.List;

public interface IBillingEvents
{
	public interface IBillingEventListeners
	{
		IBillingSetupFinishedListener getSetupFinishedListener();

		IBillingQueryInventoryFinishedListener getQueryInventoryListener();

		IBillingConsumeFinishedListener getConsumeFinishedListener();
	}

	public interface IBillingSetupFinishedListener
	{
		/**
		 * Called to notify that setup is complete.
		 * 
		 * @param result
		 *            The result of the setup process.
		 */
		public void onBillingSetupFinished(BillingResult result);
	}

	/**
	 * Callback that notifies when a purchase is finished.
	 */
	public interface IBillingPurchaseFinishedListener
	{
		/**
		 * Called to notify that an in-app purchase finished. If the purchase
		 * was successful, then the sku parameter specifies which item was
		 * purchased. If the purchase failed, the sku and extraData parameters
		 * may or may not be null, depending on how far the purchase process
		 * went.
		 * 
		 * @param result
		 *            The result of the purchase.
		 * @param info
		 *            The purchase information (null if purchase failed)
		 * @throws JSONException
		 */
		public void onBillingPurchaseFinished(BillingResult result, Purchase info);
	}

	/**
	 * Listener that notifies when an inventory query operation completes.
	 */
	public interface IBillingQueryInventoryFinishedListener
	{
		/**
		 * Called to notify that an inventory query operation completed.
		 * 
		 * @param result
		 *            The result of the operation.
		 * @param inv
		 *            The inventory.
		 */
		public void onBillingQueryInventoryFinished(BillingResult result, Inventory inv);
	}

	/**
	 * Callback that notifies when a consumption operation finishes.
	 */
	public interface IBillingConsumeFinishedListener
	{
		/**
		 * Called to notify that a consumption has finished.
		 * 
		 * @param purchase
		 *            The purchase that was (or was to be) consumed.
		 * @param result
		 *            The result of the consumption operation.
		 */
		public void onBillingConsumeFinished(Purchase purchase, BillingResult result);
	}

	/**
	 * Callback that notifies when a multi-item consumption operation finishes.
	 */
	public interface IBillingConsumeMultiFinishedListener
	{
		/**
		 * Called to notify that a consumption of multiple items has finished.
		 * 
		 * @param purchases
		 *            The purchases that were (or were to be) consumed.
		 * @param results
		 *            The results of each consumption operation, corresponding
		 *            to each sku.
		 */
		public void onBillingConsumeMultiFinished(List<Purchase> purchases, List<BillingResult> results);
	}

	public interface IBillingPendingProductsConsumeFinishedListener
	{
		/**
		 * Called to notify that a consumption has finished.
		 * 
		 * @param purchase
		 *            The purchase that was (or was to be) consumed.
		 * @param result
		 *            The result of the consumption operation.
		 */
		public void onBillingPendingProductsConsumeFinished();
	}
}
