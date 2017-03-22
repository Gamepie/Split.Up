/*
 * Copyright 2014-2015 Voxel Busters
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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.billing.core.IBillingEvents;
import com.voxelbusters.nativeplugins.features.billing.core.IBillingEvents.IBillingPendingProductsConsumeFinishedListener;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.BillingResponseCodes;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides convenience methods for in-app billing. You can create one instance
 * of this class for your application and use it to process in-app billing
 * operations. It provides synchronous (blocking) and asynchronous
 * (non-blocking) methods for many common in-app billing operations, as well as
 * automatic signature verification.
 * 
 * After instantiating, you must perform setup in order to start using the
 * object. To perform setup, call the {@link #startSetup} method and provide a
 * listener; that listener will be notified when setup is complete, after which
 * (and not before) you may call other methods.
 * 
 * After setup is complete, you will typically want to request an inventory of
 * owned items and subscriptions. See {@link #queryInventory},
 * {@link #queryInventoryAsync} and related methods.
 * 
 * When you are done with this object, don't forget to call {@link #dispose} to
 * ensure proper cleanup. This object holds a binding to the in-app billing
 * service, which will leak unless you dispose of it correctly. If you created
 * the object on an Activity's onCreate method, then the recommended place to
 * dispose of it is the Activity's onDestroy method.
 * 
 * A note about threading: When using this object from a background thread, you
 * may call the blocking versions of methods; when using from a UI thread, call
 * only the asynchronous versions and handle the results via callbacks. Also,
 * notice that you can only call one asynchronous operation at a time;
 * attempting to start a second asynchronous operation while the first one has
 * not yet completed will result in an exception being thrown.
 * 
 * @author Bruno Oliveira (Google)
 * 
 */
public class IabHelper
{
	// Is debug logging enabled?
	boolean						mDebugLog						= false;
	String						mDebugTag						= "IabHelper";

	// Is setup done?
	boolean						mSetupDone						= false;

	// Has this object been disposed of? (If so, we should ignore callbacks,
	// etc)
	boolean						mDisposed						= false;

	// Are subscriptions supported?
	boolean						mSubscriptionsSupported			= false;

	// Is an asynchronous operation in progress?
	// (only one at a time can be in progress)
	boolean						mAsyncInProgress				= false;

	// (for logging/debugging)
	// if mAsyncInProgress == true, what asynchronous operation is in progress?
	String						mAsyncOperation					= "";

	// Context we were passed during initialization
	Context						mContext;

	// Connection to the service
	IInAppBillingService		mService;
	ServiceConnection			mServiceConn;

	// The request code used to launch purchase flow
	int							mRequestCode;

	// The item type of the current purchase flow
	String						mPurchasingItemType;

	// Public key for verifying signature, in base64 encoding
	String						mSignatureBase64				= null;

	// Item types
	public static final String	ITEM_TYPE_INAPP					= "inapp";
	public static final String	ITEM_TYPE_SUBS					= "subs";

	// some fields on the getSkuDetails response bundle
	public static final String	GET_SKU_DETAILS_ITEM_LIST		= "ITEM_ID_LIST";
	public static final String	GET_SKU_DETAILS_ITEM_TYPE_LIST	= "ITEM_TYPE_LIST";

	ArrayList<String>			mConsumableProducts				= new ArrayList<String>();

	//public boolean				mSkipVerification;

	/**
	 * Creates an instance. After creation, it will not yet be ready to use. You
	 * must perform setup by calling {@link #startSetup} and wait for setup to
	 * complete. This constructor does not block and is safe to call from a UI
	 * thread.
	 * 
	 * @param ctx
	 *            Your application or Activity context. Needed to bind to the
	 *            in-app billing service.
	 * @param base64PublicKey
	 *            Your application's public key, encoded in base64. This is used
	 *            for verification of purchase signatures. You can find your
	 *            app's base64-encoded public key in your application's page on
	 *            Google Play Developer Console. Note that this is NOT your
	 *            "developer public key".
	 */
	public IabHelper(Context ctx, String base64PublicKey)
	{
		mContext = ctx.getApplicationContext();
		mSignatureBase64 = base64PublicKey;
		logDebug("IAB helper created.");
	}

	/**
	 * Enables or disable debug logging through LogCat.
	 */
	public void enableDebugLogging(boolean enable, String tag)
	{
		checkNotDisposed();
		mDebugLog = enable;
		mDebugTag = tag;
	}

	public void enableDebugLogging(boolean enable)
	{
		checkNotDisposed();
		mDebugLog = enable;
	}

	/**
	 * Starts the setup process. This will start up the setup process
	 * asynchronously. You will be notified through the listener when the setup
	 * process is complete. This method is safe to call from a UI thread.
	 * 
	 * @param listener
	 *            The listener to notify when the setup process is complete.
	 */
	public void startSetup(final IBillingEvents.IBillingSetupFinishedListener listener)
	{
		// If already set up, can't do it again.
		checkNotDisposed();
		if (mSetupDone)
		{
			throw new IllegalStateException("IAB helper is already set up.");
		}

		// Connection to IAB service
		logDebug("Starting in-app billing setup.");
		mServiceConn = new ServiceConnection()
			{
				@Override
				public void onServiceDisconnected(ComponentName name)
				{
					logDebug("Billing service disconnected.");
					mService = null;
				}

				@Override
				public void onServiceConnected(ComponentName name, IBinder service)
				{
					if (mDisposed)
					{
						return;
					}
					logDebug("Billing service connected.");
					mService = IInAppBillingService.Stub.asInterface(service);
					String packageName = mContext.getPackageName();
					try
					{
						logDebug("Checking for in-app billing 3 support.");

						// check for in-app billing v3 support
						int response = mService.isBillingSupported(3, packageName, ITEM_TYPE_INAPP);
						if (response != BillingResponseCodes.BILLING_RESPONSE_RESULT_OK)
						{
							if (listener != null)
							{
								listener.onBillingSetupFinished(new BillingResult(response, "Error checking for billing v3 support."));
							}

							// if in-app purchases aren't supported, neither are
							// subscriptions.
							mSubscriptionsSupported = false;
							return;
						}
						logDebug("In-app billing version 3 supported for " + packageName);

						// check for v3 subscriptions support
						response = mService.isBillingSupported(3, packageName, ITEM_TYPE_SUBS);
						if (response == BillingResponseCodes.BILLING_RESPONSE_RESULT_OK)
						{
							logDebug("Subscriptions AVAILABLE.");
							mSubscriptionsSupported = true;
						}
						else
						{
							logDebug("Subscriptions NOT AVAILABLE. Response: " + response);
						}

						mSetupDone = true;
					}
					catch (RemoteException e)
					{
						if (listener != null)
						{
							listener.onBillingSetupFinished(new BillingResult(BillingResponseCodes.BILLING_REMOTE_EXCEPTION, "RemoteException while setting up in-app billing."));
						}
						e.printStackTrace();
						return;
					}

					if (listener != null)
					{
						listener.onBillingSetupFinished(new BillingResult(BillingResponseCodes.BILLING_RESPONSE_RESULT_OK, "Setup successful."));
					}
				}
			};

		Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage("com.android.vending");
		if (!mContext.getPackageManager().queryIntentServices(serviceIntent, 0).isEmpty())
		{
			// service available to handle that Intent
			mContext.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
		}
		else
		{
			// no service available to handle that Intent
			if (listener != null)
			{
				listener.onBillingSetupFinished(new BillingResult(BillingResponseCodes.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE, "Billing service unavailable on device."));
			}
		}
	}

	/**
	 * Dispose of object, releasing resources. It's very important to call this
	 * method when you are done with this object. It will release any resources
	 * used by it such as service connections. Naturally, once the object is
	 * disposed of, it can't be used again.
	 */
	public void dispose()
	{
		logDebug("Disposing.");
		mSetupDone = false;
		if (mServiceConn != null)
		{
			logDebug("Unbinding from service.");
			if (mContext != null)
			{
				mContext.unbindService(mServiceConn);
			}
		}
		mDisposed = true;
		mContext = null;
		mServiceConn = null;
		mService = null;
		mPurchaseListener = null;
	}

	private void checkNotDisposed()
	{
		if (mDisposed)
		{
			throw new IllegalStateException("IabHelper was disposed of, so it cannot be used.");
		}
	}

	/** Returns whether subscriptions are supported. */
	public boolean subscriptionsSupported()
	{
		checkNotDisposed();
		return mSubscriptionsSupported;
	}

	public boolean isSetupDone()
	{
		return mSetupDone;
	}

	public void setConsumableProducts(ArrayList<String> consumableProducts)
	{
		mConsumableProducts = consumableProducts;
	}

	// The listener registered on launchPurchaseFlow, which we have to call back
	// when
	// the purchase finishes
	IBillingEvents.IBillingPurchaseFinishedListener	mPurchaseListener;

	public void launchPurchaseFlow(Activity act, String sku, int requestCode, IBillingEvents.IBillingPurchaseFinishedListener listener)
	{
		launchPurchaseFlow(act, sku, requestCode, listener, "");
	}

	public void launchPurchaseFlow(Activity act, String sku, int requestCode, IBillingEvents.IBillingPurchaseFinishedListener listener, String extraData)
	{
		launchPurchaseFlow(act, sku, ITEM_TYPE_INAPP, requestCode, listener, extraData);
	}

	public void launchSubscriptionPurchaseFlow(Activity act, String sku, int requestCode, IBillingEvents.IBillingPurchaseFinishedListener listener)
	{
		launchSubscriptionPurchaseFlow(act, sku, requestCode, listener, "");
	}

	public void launchSubscriptionPurchaseFlow(Activity act, String sku, int requestCode, IBillingEvents.IBillingPurchaseFinishedListener listener, String extraData)
	{
		launchPurchaseFlow(act, sku, ITEM_TYPE_SUBS, requestCode, listener, extraData);
	}

	/**
	 * Initiate the UI flow for an in-app purchase. Call this method to initiate
	 * an in-app purchase, which will involve bringing up the Google Play
	 * screen. The calling activity will be paused while the user interacts with
	 * Google Play, and the result will be delivered via the activity's
	 * {@link android.app.Activity#onActivityResult} method, at which point you
	 * must call this object's {@link #handleActivityResult} method to continue
	 * the purchase flow. This method MUST be called from the UI thread of the
	 * Activity.
	 * 
	 * @param act
	 *            The calling activity.
	 * @param sku
	 *            The sku of the item to purchase.
	 * @param itemType
	 *            indicates if it's a product or a subscription (ITEM_TYPE_INAPP
	 *            or ITEM_TYPE_SUBS)
	 * @param requestCode
	 *            A request code (to differentiate from other responses -- as in
	 *            {@link android.app.Activity#startActivityForResult}).
	 * @param listener
	 *            The listener to notify when the purchase process finishes
	 * @param extraData
	 *            Extra data (developer payload), which will be returned with
	 *            the purchase data when the purchase completes. This extra data
	 *            will be permanently bound to that purchase and will always be
	 *            returned when the purchase is queried.
	 */
	public void launchPurchaseFlow(Activity act, String sku, String itemType, int requestCode, IBillingEvents.IBillingPurchaseFinishedListener listener, String extraData)
	{
		checkNotDisposed();
		checkSetupDone("launchPurchaseFlow");

		flagEndAsync();//If previous intent exits without callback, this ensures the launch of new.

		if (!flagStartAsync("launchPurchaseFlow"))
		{
			logError("Aborting this call request as another consume running currnelty");
			return;
		}

		BillingResult result;

		if (itemType.equals(ITEM_TYPE_SUBS) && !mSubscriptionsSupported)
		{
			BillingResult r = new BillingResult(BillingResponseCodes.BILLING_SUBSCRIPTIONS_NOT_AVAILABLE, "Subscriptions are not available.");
			flagEndAsync();
			if (listener != null)
			{
				listener.onBillingPurchaseFinished(r, null);
			}
			return;
		}

		//Just make sure we query inventory first and if we find any pending consumable products, consume them and then launch purchase flow

		try
		{
			logDebug("Constructing buy intent for " + sku + ", item type: " + itemType);
			Bundle buyIntentBundle = mService.getBuyIntent(3, mContext.getPackageName(), sku, itemType, extraData);
			int response = getResponseCodeFromBundle(buyIntentBundle);
			if (response != BillingResponseCodes.BILLING_RESPONSE_RESULT_OK)
			{
				logError("Unable to buy item, Error response: " + BillingResponseCodes.getResponseDesc(response));
				flagEndAsync();
				result = new BillingResult(response, "Unable to buy item");
				if (listener != null)
				{
					listener.onBillingPurchaseFinished(result, null);
				}
				return;
			}

			PendingIntent pendingIntent = buyIntentBundle.getParcelable(BillingResponseCodes.BILLING_RESPONSE_BUY_INTENT);
			logDebug("Launching buy intent for " + sku + ". Request code: " + requestCode);
			mRequestCode = requestCode;
			mPurchaseListener = listener;
			mPurchasingItemType = itemType;

			act.startIntentSenderForResult(pendingIntent.getIntentSender(), requestCode, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
		}
		catch (SendIntentException e)
		{
			logError("SendIntentException while launching purchase flow for sku " + sku);
			e.printStackTrace();
			flagEndAsync();

			result = new BillingResult(BillingResponseCodes.BILLING_SEND_INTENT_FAILED, "Failed to send intent.");
			if (listener != null)
			{
				listener.onBillingPurchaseFinished(result, null);
			}
		}
		catch (RemoteException e)
		{
			logError("RemoteException while launching purchase flow for sku " + sku);
			e.printStackTrace();
			flagEndAsync();

			result = new BillingResult(BillingResponseCodes.BILLING_REMOTE_EXCEPTION, "Remote exception while starting purchase flow");
			if (listener != null)
			{
				listener.onBillingPurchaseFinished(result, null);
			}
		}
	}

/**
	 * Handles an activity result that's part of the purchase flow in in-app
	 * billing. If you are calling {@link #launchPurchaseFlow}, then you must
	 * call this method from your Activity's {@link android.app.Activity
	 * @onActivityResult} method. This method MUST be called from the UI thread
	 * of the Activity.
	 * 
	 * @param requestCode
	 *            The requestCode as you received it.
	 * @param resultCode
	 *            The resultCode as you received it.
	 * @param data
	 *            The data (Intent) as you received it.
	 * @return Returns true if the result was related to a purchase flow and was
	 *         handled; false if the result was not related to a purchase, in
	 *         which case you should handle it normally.
	 */
	public boolean handleActivityResult(int requestCode, int resultCode, Intent data)
	{
		BillingResult result;
		if (requestCode != mRequestCode)
		{
			return false;
		}

		checkNotDisposed();
		checkSetupDone("handleActivityResult");

		// end of async purchase operation that started on launchPurchaseFlow
		flagEndAsync();

		if (data == null)
		{
			logError("Null data in IAB activity result.");
			result = new BillingResult(BillingResponseCodes.BILLING_BAD_RESPONSE, "Null data in IAB result");
			if (mPurchaseListener != null)
			{
				mPurchaseListener.onBillingPurchaseFinished(result, null);
			}
			return true;
		}

		int responseCode = getResponseCodeFromIntent(data);
		String purchaseData = data.getStringExtra(BillingResponseCodes.BILLING_RESPONSE_INAPP_PURCHASE_DATA);
		String dataSignature = data.getStringExtra(BillingResponseCodes.BILLING_RESPONSE_INAPP_SIGNATURE);

		if ((resultCode == Activity.RESULT_OK) && (responseCode == BillingResponseCodes.BILLING_RESPONSE_RESULT_OK))
		{
			logDebug("Successful resultcode from purchase activity.");
			logDebug("Purchase data: " + purchaseData);
			logDebug("Data signature: " + dataSignature);
			logDebug("Extras: " + data.getExtras());
			logDebug("Expected item type: " + mPurchasingItemType);

			boolean isSandboxProductId = false;

			Purchase purchase = null;
			String sku = null;

			if (purchaseData != null)
			{
				try
				{
					purchase = new Purchase(mPurchasingItemType, purchaseData, dataSignature);
				}
				catch (JSONException e)
				{
					logError("Failed to parse purchase data.");
					e.printStackTrace();
					result = new BillingResult(BillingResponseCodes.BILLING_BAD_RESPONSE, "Failed to parse purchase data.");
					if (mPurchaseListener != null)
					{
						mPurchaseListener.onBillingPurchaseFinished(result, null);
					}
					return true;
				}

				if (purchase != null)
				{
					sku = purchase.getSku();

					if (isSandboxBillingProduct(sku))
					{
						isSandboxProductId = true;
						dataSignature = "No signature available - This is test product id";
					}
				}
			}

			if (!isSandboxProductId && ((purchaseData == null) || (dataSignature == null)))
			{
				logError("BUG: either purchaseData or dataSignature is null.");
				logDebug("Extras: " + data.getExtras().toString());
				result = new BillingResult(BillingResponseCodes.BILLING_UNKNOWN_ERROR, "IAB returned null purchaseData or dataSignature");
				if (mPurchaseListener != null)
				{
					mPurchaseListener.onBillingPurchaseFinished(result, null);
				}
				return true;
			}

			// Verify signature
			if (!isSandboxProductId && !Security.verifyPurchase(mSignatureBase64, purchaseData, dataSignature)) //For sandbox id we don't need to verify the purchase
			{
				logError("Purchase signature verification FAILED for sku " + sku);
				purchase.setValidationState(Keys.Billing.Validation.FAILED);
				result = new BillingResult(BillingResponseCodes.BILLING_VERIFICATION_FAILED, "Signature verification failed for sku " + sku);
				if (mPurchaseListener != null)
				{
					mPurchaseListener.onBillingPurchaseFinished(result, purchase);
				}
				return true;
			}
			else
			{
				purchase.setValidationState(Keys.Billing.Validation.SUCCESS);
			}
			logDebug("Purchase signature successfully verified.");

			if (mPurchaseListener != null)
			{
				mPurchaseListener.onBillingPurchaseFinished(new BillingResult(BillingResponseCodes.BILLING_RESPONSE_RESULT_OK, "Success"), purchase);
			}
		}
		else if (resultCode == Activity.RESULT_OK)
		{
			// result code was OK, but in-app billing response was not OK.
			logDebug("Result code was OK but in-app billing response was not OK: " + BillingResponseCodes.getResponseDesc(responseCode));
			if (mPurchaseListener != null)
			{
				result = new BillingResult(responseCode, "Problem purchashing item.");
				mPurchaseListener.onBillingPurchaseFinished(result, null);
			}
		}
		else if (resultCode == Activity.RESULT_CANCELED)
		{
			logDebug("Purchase canceled - Response: " + BillingResponseCodes.getResponseDesc(responseCode));
			result = new BillingResult(BillingResponseCodes.BILLING_USER_CANCELLED, "User canceled.");

			//Added
			if (responseCode == BillingResponseCodes.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE)
			{
				result = new BillingResult(BillingResponseCodes.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE, "Item not available.");
			}
			else if (responseCode == BillingResponseCodes.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED)
			{
				result = new BillingResult(BillingResponseCodes.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED, "Item already owned.");
			}

			if (mPurchaseListener != null)
			{
				mPurchaseListener.onBillingPurchaseFinished(result, null);
			}
		}
		else
		{
			logError("Purchase failed. Result code: " + Integer.toString(resultCode) + ". Response: " + BillingResponseCodes.getResponseDesc(responseCode));
			result = new BillingResult(BillingResponseCodes.BILLING_UNKNOWN_PURCHASE_RESPONSE, "Unknown purchase response.");
			if (mPurchaseListener != null)
			{
				mPurchaseListener.onBillingPurchaseFinished(result, null);
			}
		}
		return true;
	}

	boolean isSandboxBillingProduct(String sku)
	{
		if (sku.equals("android.test.purchased") || sku.equals("android.test.canceled") || sku.equals("android.test.refunded") || sku.equals("android.test.item_unavailable"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public Inventory queryInventory(boolean querySkuDetails, List<String> moreSkus) throws IabException
	{
		return queryInventory(querySkuDetails, moreSkus, null);
	}

	/**
	 * Queries the inventory. This will query all owned items from the server,
	 * as well as information on additional skus, if specified. This method may
	 * block or take long to execute. Do not call from a UI thread. For that,
	 * use the non-blocking version {@link #refreshInventoryAsync}.
	 * 
	 * @param querySkuDetails
	 *            if true, SKU details (price, description, etc) will be queried
	 *            as well as purchase information.
	 * @param moreItemSkus
	 *            additional PRODUCT skus to query information on, regardless of
	 *            ownership. Ignored if null or if querySkuDetails is false.
	 * @param moreSubsSkus
	 *            additional SUBSCRIPTIONS skus to query information on,
	 *            regardless of ownership. Ignored if null or if querySkuDetails
	 *            is false.
	 * @throws IabException
	 *             if a problem occurs while refreshing the inventory.
	 */
	public Inventory queryInventory(boolean querySkuDetails, List<String> moreItemSkus, List<String> moreSubsSkus) throws IabException
	{
		checkNotDisposed();
		checkSetupDone("queryInventory");
		try
		{
			Inventory inv = new Inventory();
			int r = queryPurchases(inv, ITEM_TYPE_INAPP);
			if ((r != BillingResponseCodes.BILLING_RESPONSE_RESULT_OK))
			{
				if (!querySkuDetails)//If SKU details are not queried, we can stop here by throwing exception
				{
					throw new IabException(r, "Error refreshing inventory (querying owned items).");
				}
				else
				{
					Debug.error(CommonDefines.BILLING_TAG, "Error refreshing inventory while fetching purchased items.");
				}
			}

			if (querySkuDetails)
			{
				r = querySkuDetails(ITEM_TYPE_INAPP, inv, moreItemSkus);
				if (r != BillingResponseCodes.BILLING_RESPONSE_RESULT_OK)
				{
					throw new IabException(r, "Error refreshing inventory (querying prices of items).");
				}
			}

			// if subscriptions are supported, then also query for subscriptions
			if (mSubscriptionsSupported)
			{
				r = queryPurchases(inv, ITEM_TYPE_SUBS);
				if (r != BillingResponseCodes.BILLING_RESPONSE_RESULT_OK)
				{
					throw new IabException(r, "Error refreshing inventory (querying owned subscriptions).");
				}

				if (querySkuDetails)
				{
					r = querySkuDetails(ITEM_TYPE_SUBS, inv, moreItemSkus);
					if (r != BillingResponseCodes.BILLING_RESPONSE_RESULT_OK)
					{
						throw new IabException(r, "Error refreshing inventory (querying prices of subscriptions).");
					}
				}
			}

			return inv;
		}
		catch (RemoteException e)
		{
			throw new IabException(BillingResponseCodes.BILLING_REMOTE_EXCEPTION, "Remote exception while refreshing inventory.", e);
		}
		catch (JSONException e)
		{
			throw new IabException(BillingResponseCodes.BILLING_BAD_RESPONSE, "Error parsing JSON response while refreshing inventory.", e);
		}
	}

	/**
	 * Asynchronous wrapper for inventory query. This will perform an inventory
	 * query as described in {@link #queryInventory}, but will do so
	 * asynchronously and call back the specified listener upon completion. This
	 * method is safe to call from a UI thread.
	 * 
	 * @param querySkuDetails
	 *            as in {@link #queryInventory}
	 * @param moreSkus
	 *            as in {@link #queryInventory}
	 * @param listener
	 *            The listener to notify when the refresh operation completes.
	 */
	public void queryInventoryAsync(final boolean querySkuDetails, final List<String> moreSkus, final IBillingEvents.IBillingQueryInventoryFinishedListener listener)
	{
		final Handler handler = new Handler();
		checkNotDisposed();
		checkSetupDone("queryInventory");
		if (!flagStartAsync("refresh inventory"))
		{
			logError("Aborting this call request as another request running!");
			return;
		}

		(new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					BillingResult result = new BillingResult(BillingResponseCodes.BILLING_RESPONSE_RESULT_OK, "Inventory refresh successful.");
					Inventory inv = null;
					try
					{
						inv = queryInventory(querySkuDetails, moreSkus);
					}
					catch (IabException ex)
					{
						logError(ex.getMessage());
						result = ex.getResult();
					}

					flagEndAsync();

					final BillingResult result_f = result;
					final Inventory inv_f = inv;
					if (!mDisposed && (listener != null))
					{
						handler.post(new Runnable()
							{
								@Override
								public void run()
								{
									listener.onBillingQueryInventoryFinished(result_f, inv_f);
								}
							});
					}
				}
			})).start();
	}

	public void queryInventoryAsync(IBillingEvents.IBillingQueryInventoryFinishedListener listener)
	{
		queryInventoryAsync(true, null, listener);
	}

	public void queryInventoryAsync(boolean querySkuDetails, IBillingEvents.IBillingQueryInventoryFinishedListener listener)
	{
		queryInventoryAsync(querySkuDetails, null, listener);
	}

	/**
	 * Consumes a given in-app product. Consuming can only be done on an item
	 * that's owned, and as a result of consumption, the user will no longer own
	 * it. This method may block or take long to return. Do not call from the UI
	 * thread. For that, see {@link #consumeAsync}.
	 * 
	 * @param itemInfo
	 *            The PurchaseInfo that represents the item to consume.
	 * @throws IabException
	 *             if there is a problem during consumption.
	 */
	void consume(Purchase itemInfo) throws IabException
	{
		checkNotDisposed();
		checkSetupDone("consume");

		if (!itemInfo.mItemType.equals(ITEM_TYPE_INAPP))
		{
			throw new IabException(BillingResponseCodes.BILLING_INVALID_CONSUMPTION, "Items of type '" + itemInfo.mItemType + "' can't be consumed.");
		}

		try
		{
			String token = itemInfo.getToken();
			String sku = itemInfo.getSku();
			if ((token == null) || token.equals(""))
			{
				logError("Can't consume " + sku + ". No token.");
				throw new IabException(BillingResponseCodes.BILLING_MISSING_TOKEN, "PurchaseInfo is missing token for sku: " + sku + " " + itemInfo);
			}

			logDebug("Consuming sku: " + sku + ", token: " + token);
			int response = mService.consumePurchase(3, mContext.getPackageName(), token);
			if (response == BillingResponseCodes.BILLING_RESPONSE_RESULT_OK)
			{
				logDebug("Successfully consumed sku: " + sku);
			}
			else
			{
				logDebug("Error consuming consuming sku " + sku + " " + BillingResponseCodes.getResponseDesc(response));
				throw new IabException(response, "Error consuming sku " + sku);
			}
		}
		catch (RemoteException e)
		{
			throw new IabException(BillingResponseCodes.BILLING_REMOTE_EXCEPTION, "Remote exception while consuming. PurchaseInfo: " + itemInfo, e);
		}
	}

	/**
	 * Asynchronous wrapper to item consumption. Works like {@link #consume},
	 * but performs the consumption in the background and notifies completion
	 * through the provided listener. This method is safe to call from a UI
	 * thread.
	 * 
	 * @param purchase
	 *            The purchase to be consumed.
	 * @param listener
	 *            The listener to notify when the consumption operation
	 *            finishes.
	 */
	public void consumeAsync(Purchase purchase, IBillingEvents.IBillingConsumeFinishedListener listener)
	{
		checkNotDisposed();
		checkSetupDone("consume");
		List<Purchase> purchases = new ArrayList<Purchase>();
		purchases.add(purchase);
		consumeAsyncInternal(purchases, listener, null);
	}

	/**
	 * Same as {@link consumeAsync}, but for multiple items at once.
	 * 
	 * @param purchases
	 *            The list of PurchaseInfo objects representing the purchases to
	 *            consume.
	 * @param listener
	 *            The listener to notify when the consumption operation
	 *            finishes.
	 */
	public void consumeAsync(List<Purchase> purchases, IBillingEvents.IBillingConsumeMultiFinishedListener listener)
	{
		checkNotDisposed();
		checkSetupDone("consume");
		consumeAsyncInternal(purchases, null, listener);
	}

	// Checks that setup was done; if not, throws an exception.
	void checkSetupDone(String operation)
	{
		if (!mSetupDone)
		{
			logError("Illegal state for operation (" + operation + "): IAB helper is not set up.");
			throw new IllegalStateException("IAB helper is not set up. Can't perform operation: " + operation);
		}
	}

	// Workaround to bug where sometimes response codes come as Long instead of
	// Integer
	int getResponseCodeFromBundle(Bundle b)
	{
		Object o = b.get(BillingResponseCodes.BILLING_RESPONSE_CODE);
		if (o == null)
		{
			logDebug("Bundle with null response code, assuming OK (known issue)");
			return BillingResponseCodes.BILLING_RESPONSE_RESULT_OK;
		}
		else if (o instanceof Integer)
		{
			return ((Integer) o).intValue();
		}
		else if (o instanceof Long)
		{
			return (int) ((Long) o).longValue();
		}
		else
		{
			logError("Unexpected type for bundle response code.");
			logError(o.getClass().getName());
			throw new RuntimeException("Unexpected type for bundle response code: " + o.getClass().getName());
		}
	}

	// Workaround to bug where sometimes response codes come as Long instead of
	// Integer
	int getResponseCodeFromIntent(Intent i)
	{
		Object o = i.getExtras().get(BillingResponseCodes.BILLING_RESPONSE_CODE);
		if (o == null)
		{
			logError("Intent with no response code, assuming OK (known issue)");
			return BillingResponseCodes.BILLING_RESPONSE_RESULT_OK;
		}
		else if (o instanceof Integer)
		{
			return ((Integer) o).intValue();
		}
		else if (o instanceof Long)
		{
			return (int) ((Long) o).longValue();
		}
		else
		{
			logError("Unexpected type for intent response code.");
			logError(o.getClass().getName());
			throw new RuntimeException("Unexpected type for intent response code: " + o.getClass().getName());
		}
	}

	boolean flagStartAsync(String operation)
	{
		if (mAsyncInProgress)
		{
			logError("Can't start async operation (" + operation + ") because another async operation(" + mAsyncOperation + ") is in progress.");
			return false;
		}
		mAsyncOperation = operation;
		mAsyncInProgress = true;
		logDebug("Starting async operation: " + operation);
		return true;
	}

	void flagEndAsync()
	{
		logDebug("Ending async operation: " + mAsyncOperation);
		mAsyncOperation = "";
		mAsyncInProgress = false;
	}

	int queryPurchases(Inventory inv, String itemType) throws JSONException, RemoteException
	{
		// Query purchases
		logDebug("Querying owned items, item type: " + itemType);
		logDebug("Package name: " + mContext.getPackageName());
		boolean verificationFailed = false;
		String continueToken = null;

		do
		{
			logDebug("Calling getPurchases with continuation token: " + continueToken);
			Bundle ownedItems = mService.getPurchases(3, mContext.getPackageName(), itemType, continueToken);

			int response = getResponseCodeFromBundle(ownedItems);
			logDebug("Owned items response: " + String.valueOf(response));
			if (response != BillingResponseCodes.BILLING_RESPONSE_RESULT_OK)
			{
				logDebug("getPurchases() failed: " + BillingResponseCodes.getResponseDesc(response));
				return response;
			}
			if (!ownedItems.containsKey(BillingResponseCodes.BILLING_RESPONSE_INAPP_ITEM_LIST) || !ownedItems.containsKey(BillingResponseCodes.BILLING_RESPONSE_INAPP_PURCHASE_DATA_LIST) || !ownedItems.containsKey(BillingResponseCodes.BILLING_RESPONSE_INAPP_SIGNATURE_LIST))
			{
				logError("Bundle returned from getPurchases() doesn't contain required fields.");
				return BillingResponseCodes.BILLING_BAD_RESPONSE;
			}

			ArrayList<String> ownedSkus = ownedItems.getStringArrayList(BillingResponseCodes.BILLING_RESPONSE_INAPP_ITEM_LIST);
			ArrayList<String> purchaseDataList = ownedItems.getStringArrayList(BillingResponseCodes.BILLING_RESPONSE_INAPP_PURCHASE_DATA_LIST);
			ArrayList<String> signatureList = ownedItems.getStringArrayList(BillingResponseCodes.BILLING_RESPONSE_INAPP_SIGNATURE_LIST);

			for (int i = 0; i < purchaseDataList.size(); ++i)
			{
				String purchaseData = purchaseDataList.get(i);
				String signature = signatureList.get(i);
				String sku = ownedSkus.get(i);

				Purchase purchase = new Purchase(itemType, purchaseData, signature);

				if (isConsumableProduct(purchase.getSku()))//This is exceptional case as there is some problem while consuming this product earlier.
				{
					try
					{
						consume(purchase);
					}
					catch (IabException e)
					{
						e.printStackTrace();
					}
					continue;
				}

				if (TextUtils.isEmpty(purchase.getToken()))
				{
					logWarn("BUG: empty/null token!");
					logDebug("Purchase data: " + purchaseData);
				}

				if (Security.verifyPurchase(mSignatureBase64, purchaseData, signature))
				{
					logDebug("Sku is owned: " + sku);

					// Record ownership and token
					purchase.setValidationState(Keys.Billing.Validation.SUCCESS);
					inv.addPurchase(purchase);

				}
				else
				{
					if (!isSandboxBillingProduct(sku))
					{
						logWarn("Purchase signature verification **FAILED**. Updating transactionValidation state with eBillingTransactionValidationState.FAILED");
					}
					logDebug("   Purchase data: " + purchaseData);
					logDebug("   Signature: " + signature);
					verificationFailed = true;

					//Update the validation state as eBillingTransactionValidationState.FAILED
					purchase.setValidationState(Keys.Billing.Validation.FAILED);
					inv.addPurchase(purchase);
				}
			}

			continueToken = ownedItems.getString(BillingResponseCodes.BILLING_INAPP_CONTINUATION_TOKEN);
			logDebug("Continuation token: " + continueToken);
		} while (!TextUtils.isEmpty(continueToken));

		return verificationFailed ? BillingResponseCodes.BILLING_VERIFICATION_FAILED : BillingResponseCodes.BILLING_RESPONSE_RESULT_OK;
	}

	int querySkuDetails(String itemType, Inventory inv, List<String> moreSkus) throws RemoteException, JSONException
	{
		logDebug("Querying SKU details.");
		ArrayList<String> skuList = new ArrayList<String>();
		skuList.addAll(inv.getAllOwnedSkus(itemType));
		if (moreSkus != null)
		{
			for (String sku : moreSkus)
			{
				if (!skuList.contains(sku))
				{
					skuList.add(sku);
				}
			}
		}

		if (skuList.size() == 0)
		{
			logDebug("queryPrices: nothing to do because there are no SKUs.");
			return BillingResponseCodes.BILLING_RESPONSE_RESULT_OK;
		}

		ArrayList<String> responseList = new ArrayList<String>();
		Bundle querySkus = new Bundle();

		for (int i = 0; i < skuList.size(); i += 20)
		{
			ArrayList<String> currentSubList = new ArrayList<String>(skuList.subList(i, ((i + 20) > skuList.size() ? skuList.size() : (i + 20))));

			querySkus.putStringArrayList(GET_SKU_DETAILS_ITEM_LIST, currentSubList);

			Bundle skuDetails = mService.getSkuDetails(3, mContext.getPackageName(), itemType, querySkus);

			if (!skuDetails.containsKey(BillingResponseCodes.BILLING_RESPONSE_GET_SKU_DETAILS_LIST))
			{
				int response = getResponseCodeFromBundle(skuDetails);
				if (response != BillingResponseCodes.BILLING_RESPONSE_RESULT_OK)
				{
					logDebug("getSkuDetails() failed: " + BillingResponseCodes.getResponseDesc(response));
					return response;
				}
				else
				{
					logError("getSkuDetails() returned a bundle with neither an error nor a detail list.");
					return BillingResponseCodes.BILLING_BAD_RESPONSE;
				}
			}

			responseList.addAll(skuDetails.getStringArrayList(BillingResponseCodes.BILLING_RESPONSE_GET_SKU_DETAILS_LIST));
		}

		for (String thisResponse : responseList)
		{
			SkuDetails d = new SkuDetails(itemType, thisResponse);
			logDebug("Got sku details: " + d);
			inv.addSkuDetails(d);
		}
		return BillingResponseCodes.BILLING_RESPONSE_RESULT_OK;
	}

	void consumeAsyncInternal(final List<Purchase> purchases, final IBillingEvents.IBillingConsumeFinishedListener singleListener, final IBillingEvents.IBillingConsumeMultiFinishedListener multiListener)
	{
		final Handler handler = new Handler();
		if (!flagStartAsync("consume"))
		{
			logError("Aborting this call request as another consume running currnelty");
			return;
		}

		(new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					final List<BillingResult> results = new ArrayList<BillingResult>();
					for (Purchase purchase : purchases)
					{
						try
						{
							consume(purchase);
							results.add(new BillingResult(BillingResponseCodes.BILLING_RESPONSE_RESULT_OK, "Successful consume of sku " + purchase.getSku()));
						}
						catch (IabException ex)
						{
							logError("Exception while consuming product : " + ex.getMessage());
							results.add(ex.getResult());
						}
					}

					flagEndAsync();

					if (!mDisposed && (singleListener != null))
					{
						handler.post(new Runnable()
							{
								@Override
								public void run()
								{
									singleListener.onBillingConsumeFinished(purchases.get(0), results.get(0));
								}
							});
					}
					if (!mDisposed && (multiListener != null))
					{
						handler.post(new Runnable()
							{
								@Override
								public void run()
								{
									multiListener.onBillingConsumeMultiFinished(purchases, results);
								}
							});
					}
				}
			})).start();
	}

	public boolean isConsumableProduct(String productId)
	{

		if (mConsumableProducts.size() == 0)
		{
			logWarn("No registered Consumable Products!");
			return false;
		}

		for (int i = 0; i < mConsumableProducts.size(); i++)
		{
			if (!StringUtility.isNullOrEmpty(productId) && mConsumableProducts.get(i).equals(productId))
			{
				return true;
			}
		}

		return false;
	}

	public void consumeIfAnyPendingConsumableProductsAsync(final IBillingPendingProductsConsumeFinishedListener listener)
	{
		(new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					Inventory inv = new Inventory();
					try
					{
						queryPurchases(inv, ITEM_TYPE_INAPP);
					}
					catch (Exception ex)
					{
						logError(ex.getMessage());
					}

					flagEndAsync();

					if (listener != null)
					{
						listener.onBillingPendingProductsConsumeFinished();
					}
				}
			})).start();
	}

	void logDebug(String msg)
	{
		if (mDebugLog)
		{
			Log.d(mDebugTag, msg);
		}
	}

	void logError(String msg)
	{
		Log.e(mDebugTag, "In-app billing error: " + msg);
	}

	void logWarn(String msg)
	{
		Log.w(mDebugTag, "In-app billing warning: " + msg);
	}
}
