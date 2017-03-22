package com.voxelbusters.nativeplugins.features.billing;

import android.content.Context;

import com.google.gson.JsonObject;
import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.features.billing.core.IBillingService;
import com.voxelbusters.nativeplugins.features.billing.core.IBillingServiceListener;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.amazon.AmazonBillingService;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.GoogleBillingService;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class BillingHandler implements IBillingServiceListener
{
	// Create singleton instance
	private static BillingHandler	INSTANCE;

	//To find if the service initialization status 
	private Boolean					isInitialized	= false;

	public static BillingHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new BillingHandler();
		}
		return INSTANCE;
	}

	static IBillingService	service	= null;

	// Make constructor private for making singleton interface
	private BillingHandler()
	{
		Context context = NativePluginHelper.getCurrentContext();
		if (ApplicationUtility.isAmazonPlatform(context))
		{
			//service = AmazonBillingService.getInstance();
		}
		else
		{
			service = GoogleBillingService.getInstance();
		}

	}

	public void initialize(String key, String consumableProductIDsJson)
	{
		if (key == null)
		{
			Debug.error(CommonDefines.BILLING_TAG, "Public key is null! . Set in settings.");
		}
		final String[] consumableProductIDs = StringUtility.convertJsonStringToStringArray(consumableProductIDsJson);
		service.setListener(this);
		service.init(key, NativePluginHelper.getCurrentContext(), consumableProductIDs);
	}

	//For fetching details of billing products.
	public void requestBillingProducts(String consumableProductIDsJson, String nonConsumableProductIDsJson)
	{
		//Convert to array
		final String[] consumableProductIDs = StringUtility.convertJsonStringToStringArray(consumableProductIDsJson);
		final String[] nonConsumableProductIDs = StringUtility.convertJsonStringToStringArray(nonConsumableProductIDsJson);

		Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					service.requestBillingProducts(consumableProductIDs, nonConsumableProductIDs);
				}
			};

		NativePluginHelper.executeOnUIThread(runnable);
	}

	//Check purchase status of  non-consumable product. Asking isProductPurchased for consumable product will always return false. 
	public boolean isProductPurchased(String productID)
	{
		if (isInitialized())
		{
			return service.isProductPurchased(productID);
		}
		else
		{
			return false;
		}
	}

	//Start purchaing a product. This will launch buy intent.
	public void buyProduct(String productID)
	{
		if (isInitialized())
		{
			service.buyProduct(productID, NativePluginHelper.getCurrentContext());
		}
	}

	//This restores the non-consumable product purchases done earlier.
	public void restoreCompletedTransactions()
	{
		if (isInitialized())
		{
			Runnable runnable = new Runnable()
				{
					@Override
					public void run()
					{
						service.restoreCompletedTransactions();
					}
				};

			NativePluginHelper.executeOnUIThread(runnable);
		}

	}

	//This is no-op on Android. Android maintains internally verification system which add the product to its internal list if its a valid purchase.
	public void customVerificationFinished(String transactionData)
	{
		//Here Just call 
	}

	//To check service initialization status.
	boolean isInitialized()
	{
		if (!isInitialized)
		{
			Debug.error(CommonDefines.BILLING_TAG, "Initialization not yet done");
		}

		return isInitialized;
	}

	//Callbacks to send to Host Platform
	@Override
	public void onRequestProductsFinished(ArrayList<JsonObject> productDetailsList, String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		if (error != null)
		{
			data.put(Keys.Billing.ERROR, error);
		}
		data.put(Keys.Billing.PRODUCTS_LIST, productDetailsList);

		NativePluginHelper.sendMessage(UnityDefines.Billing.REQUEST_PRODUCTS_FINISHED, data);
	}

	@Override
	public void onPurchaseTransactionFinished(ArrayList<JsonObject> transactionDetails, String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		if (error != null)
		{
			data.put(Keys.Billing.ERROR, error);
		}
		data.put(Keys.Billing.TRANSACTIONS_LIST, transactionDetails);

		NativePluginHelper.sendMessage(UnityDefines.Billing.PURCHASE_TRANSACTION_FINISHED, data);
	}

	@Override
	public void onSetupFinished(Boolean isBillingAvialable)
	{
		isInitialized = true;

		if (!isBillingAvialable)
		{
			Debug.log("BillingHandler.onSetupFinished", "Billing not supported!", true);
		}

		NativePluginHelper.sendMessage(UnityDefines.Billing.ON_SETUP_FINISHED, isBillingAvialable.toString());
	}

	@Override
	public void onRestoreTransactionFinished(ArrayList<JsonObject> transactionDetails, String error)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		if (error != null)
		{
			data.put(Keys.Billing.ERROR, error);
		}
		data.put(Keys.Billing.TRANSACTIONS_LIST, transactionDetails);

		NativePluginHelper.sendMessage(UnityDefines.Billing.RESTORE_TRANSACTION_FINISHED, data);

	}
}