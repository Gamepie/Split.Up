package com.voxelbusters.nativeplugins.features.billing.serviceprovider.google;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.billing.core.IBillingEvents;
import com.voxelbusters.nativeplugins.features.billing.core.IBillingEvents.IBillingPurchaseFinishedListener;
import com.voxelbusters.nativeplugins.features.billing.core.IBillingEvents.IBillingQueryInventoryFinishedListener;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.BillingResponseCodes;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.BillingResult;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.IabHelper;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.Inventory;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.Purchase;
import com.voxelbusters.nativeplugins.utilities.Debug;

import java.util.ArrayList;

public class GoogleBillingActivity extends Activity implements IBillingQueryInventoryFinishedListener
{
	final int				PURCHASE_FLOW_REQUEST_CODE	= 1;

	String					productIdToPurchase			= null;
	GoogleBillingService	serviceInstance;
	IabHelper				helper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (productIdToPurchase == null)
		{
			Intent intent = getIntent();

			String productID = intent.getStringExtra(Keys.Billing.PRODUCT_IDENTIFIER);
			productIdToPurchase = productID;
			// Get required instances
			serviceInstance = GoogleBillingService.getInstance();
			helper = GoogleBillingService.getInstance().getHelper();

			if (!serviceInstance.allProducts.contains(productID))//We need the product details as we are not aware if its consumable or not to auto consume
			{
				Debug.error("Billing.buyProduct", "Request product details first!");

				IBillingEvents.IBillingPurchaseFinishedListener listener = getPurchaseFinishedListener(IabHelper.ITEM_TYPE_INAPP, productID);

				BillingResult result = new BillingResult(BillingResponseCodes.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE, "Item details not available. Request product details call is skipped. Its required to proceed!");

				listener.onBillingPurchaseFinished(result, null);
			}
			else
			{
				//Check if this product is consumable and not consumed yet. If so refreshing inventory will happen.
				if (helper.isConsumableProduct(productID))
				{
					helper.queryInventoryAsync(false, null, this);
				}
				else
				{
					launchPurchaseFlow();
				}
			}

		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		Debug.log("Billing.onActivityResult", requestCode + " " + resultCode + " " + data);

		if (requestCode == PURCHASE_FLOW_REQUEST_CODE)
		{
			// If its purchaseFlow request, pass it to IabHelper for delivering
			// the required callbacks
			GoogleBillingService serviceInstance = GoogleBillingService.getInstance();

			serviceInstance.getHelper().handleActivityResult(requestCode, resultCode, data);
		}
		else
		{
			finish();
			Debug.error("GoogleBillingActivity", "Some thing weird. We are looking for only one request code  Shouldn't reach here, closing this activity");
		}

	}

	public IBillingPurchaseFinishedListener getPurchaseFinishedListener(final String type, final String productID)
	{
		IBillingPurchaseFinishedListener purchaseFinishedListener = new IBillingPurchaseFinishedListener()
			{
				@Override
				public void onBillingPurchaseFinished(BillingResult result, Purchase purchaseInfo)
				{
					if (result.isFailure())
					{
						Debug.error("GoogleBilling", "Error purchasing: " + result);
						if (serviceInstance.getListener() != null)
						{
							purchaseInfo = new Purchase(type, productID);
							ArrayList<JsonObject> list = new ArrayList<JsonObject>();
							String error = result.getMessage();
							list.add(serviceInstance.getFailedPurchaseFormat(purchaseInfo, "onBillingPurchaseFailed : " + error));//This appends error tag

							serviceInstance.getListener().onPurchaseTransactionFinished(list, error);
						}
					}
					else
					{
						String productID = purchaseInfo.getSku();

						// consume if required
						if (serviceInstance.consumableProducts.contains(productID))
						{
							// Consume the product and send the callback
							helper.consumeAsync(purchaseInfo, serviceInstance.getConsumeFinishedListener());
						}
						else
						{
							serviceInstance.purchasedProducts.add(purchaseInfo);

							if (serviceInstance.getListener() != null)
							{
								ArrayList<JsonObject> list = new ArrayList<JsonObject>();
								list.add(purchaseInfo.getJsonObject());
								serviceInstance.getListener().onPurchaseTransactionFinished(list, null);
							}
						}

					}

					// Calling finish on this activity
					finish();
				}

			};

		return purchaseFinishedListener;
	}

	@Override
	public void onBillingQueryInventoryFinished(BillingResult result, Inventory inv)
	{
		Debug.log(CommonDefines.BILLING_TAG, "Inventory Query Result : " + result.getMessage());
		launchPurchaseFlow();
	}

	void launchPurchaseFlow()
	{
		helper.launchPurchaseFlow(this, productIdToPurchase, PURCHASE_FLOW_REQUEST_CODE, getPurchaseFinishedListener(IabHelper.ITEM_TYPE_INAPP, productIdToPurchase));
	}
}
