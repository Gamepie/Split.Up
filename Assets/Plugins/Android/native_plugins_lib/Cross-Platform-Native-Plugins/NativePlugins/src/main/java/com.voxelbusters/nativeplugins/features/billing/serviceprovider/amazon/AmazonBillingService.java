package com.voxelbusters.nativeplugins.features.billing.serviceprovider.amazon;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.PurchaseResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.features.billing.core.BasicBillingService;
import com.voxelbusters.nativeplugins.features.billing.core.IBillingEvents;
import com.voxelbusters.nativeplugins.features.billing.core.IBillingServiceListener;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.GoogleBillingActivity;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.BillingResult;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.IabException;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.IabHelper;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.Inventory;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.Purchase;
import com.voxelbusters.nativeplugins.features.billing.serviceprovider.google.util.SkuDetails;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AmazonBillingService
{

}
/*
public class AmazonBillingService extends BasicBillingService
{
    public static AmazonBillingService instance = null;
    IabHelper helper;
    boolean isSetupDone = false;

    boolean isBillingSupported = false;

    ArrayList<String> consumableProducts = null;
    ArrayList<String> nonConsumableProducts = null;
    ArrayList<PurchaseResponse> purchasedProducts = null;
    ArrayList<String> allProducts = new ArrayList<String>();
    boolean requestInventoryQueued = false;
    boolean requestRestorePurchasesQueued = false;

    boolean hasPublicKey = false;

    //Returns singleton instance
    public static AmazonBillingService getInstance() {
        if (instance == null) {
            instance = new AmazonBillingService();
        }

        return instance;
    }

    private AmazonBillingService() {
        super();
    }

    @Override
    public void init(String key, Context context, String[] consumableList) {
        if (StringUtility.isNullOrEmpty(key)) {
            hasPublicKey = false;
        } else {
            hasPublicKey = true;
        }

        consumableProducts = new ArrayList<String>(Arrays.asList(consumableList));
        isSetupDone = false;
        startSetup(context);
    }

    //If setup not done, just start setup
    void startSetup(Context context)
    {
        if (!isSetupDone)
        {
            PurchasingService.registerListener(context, new AmazonPurchasingListener());
            isSetupDone = true;
        }
    }

    @Override
    public void requestBillingProducts(String[] consumableProductIDs, String[] nonConsumableProductIDs) {
        if (!isSetupDone)
        {
            String error = "Please Call Init at start for billing setup";
            Debug.error(CommonDefines.BILLING_TAG, error);
            if (serviceListener != null) {
                serviceListener.onRequestProductsFinished(null, error);
            }
            return;
        }

        consumableProducts = new ArrayList<String>(Arrays.asList(consumableProductIDs));
        nonConsumableProducts = new ArrayList<String>(Arrays.asList(nonConsumableProductIDs));

        allProducts.clear();
        allProducts.addAll(consumableProducts);
        allProducts.addAll(nonConsumableProducts);

        if (isSetupDone)
        {
            // Request inventory
            requestInventoryInternal();
        }
        else
        {
            requestInventoryQueued = true;
        }
    }

    void requestInventoryInternal()
    {
        Set<String> set = new HashSet<String>(allProducts.size());
        set.addAll(allProducts);

        PurchasingService.getProductData(set);
        // getPurchaseUpdates(boolean reset)
        // getProductData(java.util.Set skus)
        // purchase(java.lang.String sku)
        // notifyFulfillment(java.lang.String receiptId, FulfillmentResult fulfillmentResult) - Consume purchase.
    }

    @Override
    public void buyProduct(String productID, Context context) {

        // Purchase call should be from Activity which will be triggered from an
        // activity
        if (isSetupDone)
        {
            PurchasingService.purchase(productID);
        }
        else
        {
            Debug.error(CommonDefines.BILLING_TAG, "Check if initialized or not." + "Billing supported :  " + isBillingSupported);
            reportFailedTransaction(productID, "Initialization not yet done. IsBillingSupported ? " + isBillingSupported);
        }

    }

    @Override
    public boolean isProductPurchased(String productID)
    {
        if (isSetupDone)
        {
            if (purchasedProducts != null) {
                for (int i = 0; i < purchasedProducts.size(); i++) {

                    PurchaseResponse eachPurchase = purchasedProducts.get(i);
                    if (eachPurchase.getReceipt().getSku().equals(productID)) {
                        return true;
                    }
                }
            }
            else
            {
                Debug.warning(CommonDefines.BILLING_TAG, "If restore purchases not yet done, Try restoring purchases");
            }
        }
        else
        {
            Debug.error(CommonDefines.BILLING_TAG, "Please Call Init at start for billing setup");
        }

        return false;
    }

    @Override
    public void restoreCompletedTransactions() {
        if (!isSetupDone) {
            requestRestorePurchasesQueued = true;
            return;
        }

        PurchasingService.getPurchaseUpdates(true);

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                boolean isSuccess = true;
                Debug.log(CommonDefines.BILLING_TAG, "Querying inventory for purchases");
                try
                {
                    purchasedProducts = (ArrayList<Purchase>) inv.getAllPurchases();
                }
                catch (IabException e)
                {
                    isSuccess = false;
                    e.printStackTrace();
                }

                return isSuccess;
            }

            @Override
            protected void onPostExecute(Boolean isSuccessful) {
                if (isSuccessful) {
                    onReceivingRestoredPurchasedProducts();
                } else {
                    String error = "Error retrieving restore purchases!";
                    Debug.error(CommonDefines.BILLING_TAG, error);
                    if (serviceListener != null) {
                        serviceListener.onRestoreTransactionFinished(null, error);
                    }
                }

            }

        }.execute(null, null, null);

    }

    void onReceivingRestoredPurchasedProducts() {
        ArrayList<JsonObject> list = new ArrayList<JsonObject>();
        for (int i = 0; i < purchasedProducts.size(); i++) {
            Purchase eachPurchase = purchasedProducts.get(i);

            int purchaseState = eachPurchase.getPurchaseState();

            JsonObject json = eachPurchase.getJsonObject();

            Debug.log(CommonDefines.BILLING_TAG, json.toString());

            //Update the key to restore purchases purchase state
            if (purchaseState == 2) {
                json.addProperty("purchaseState", 2); //2 means refunded
            } else {
                json.addProperty("purchaseState", 3); //3 means restored
            }

            list.add(json);
        }

        if (serviceListener != null) {
            serviceListener.onRestoreTransactionFinished(list, null);
        }
    }

    public IabHelper getHelper() {
        return helper;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Debug.log(CommonDefines.BILLING_TAG, "Disposing helper resources");

        if (helper != null) {
            helper.dispose();
        }
        helper = null;
    }

    // IBillingListeners implementations
    @Override
    public IBillingEvents.IBillingSetupFinishedListener getSetupFinishedListener() {
        IBillingEvents.IBillingSetupFinishedListener listener = new IBillingEvents.IBillingSetupFinishedListener() {
            @Override
            public void onBillingSetupFinished(BillingResult result) {

                if (!result.isSuccess()) {
                    Debug.error(CommonDefines.BILLING_TAG, "Sorry, Billing not supported!" + result);
                    isBillingSupported = false;

                    if (serviceListener != null) {
                        serviceListener.onSetupFinished(false);
                    }
                } else {
                    isSetupDone = true;

                    isBillingSupported = true;

                    if (requestInventoryQueued)//If its queued, finish  by calling it.
                    {
                        requestInventoryInternal();
                        requestInventoryQueued = false;
                    }

                    if (requestRestorePurchasesQueued) {
                        restoreCompletedTransactions();
                        requestRestorePurchasesQueued = false;
                    }

                    if (serviceListener != null) {
                        serviceListener.onSetupFinished(true);
                    }
                }

            }
        };

        return listener;
    }

    @Override
    public IBillingEvents.IBillingQueryInventoryFinishedListener getQueryInventoryListener() {
        IBillingEvents.IBillingQueryInventoryFinishedListener queryInventoryListener = new IBillingEvents.IBillingQueryInventoryFinishedListener() {
            @Override
            public void onBillingQueryInventoryFinished(BillingResult result, Inventory inventory) {
                if (result.isFailure()) {
                    String error = result.toString();
                    Debug.error(CommonDefines.BILLING_TAG, "OnQueryInventoryFailed! " + result.toString());
                    if (serviceListener != null) {
                        serviceListener.onRequestProductsFinished(null, error);
                    }
                } else {
                    // inventory.getSkuDetails
                    // Fetch all the results and send the message
                    ArrayList<JsonObject> detailsArray = new ArrayList<JsonObject>();

                    for (String eachProductID : allProducts) {
                        SkuDetails eachDetails = inventory.getSkuDetails(eachProductID);
                        if (eachDetails != null) {
                            JsonObject json = null;
                            String jsonString = eachDetails.getJsonString();

                            JsonParser jsonParser = new JsonParser();
                            json = (JsonObject) jsonParser.parse(jsonString);

                            String currencyCode = json.get(Keys.Billing.PRODUCT_CURRENCY_CODE).getAsString();
                            json.addProperty(Keys.Billing.PRODUCT_CURRENCY_SYMBOL, StringUtility.getCurrencySymbolFromCode(currencyCode));

                            detailsArray.add(json);
                        }
                    }

                    // Fetching all productID's of purchased products
                    purchasedProducts = (ArrayList<Purchase>) inventory.getAllPurchases();

                    if (serviceListener != null) {
                        serviceListener.onRequestProductsFinished(detailsArray, null);
                    }
                }
            }

        };
        return queryInventoryListener;
    }

    @Override
    public IBillingEvents.IBillingConsumeFinishedListener getConsumeFinishedListener() {
        IBillingEvents.IBillingConsumeFinishedListener consumeFinishedListener = new IBillingEvents.IBillingConsumeFinishedListener() {

            @Override
            public void onBillingConsumeFinished(Purchase purchaseInfo, BillingResult result) {
                if (result.isFailure()) {
                    Debug.error(CommonDefines.BILLING_TAG, "onBillingConsumeFailed!");
                    if (serviceListener != null) {

                        purchaseInfo = new Purchase(purchaseInfo.getItemType(), purchaseInfo.getSku());//creating a dummy purchase instance

                        String error = result.getMessage();
                        ArrayList<JsonObject> list = new ArrayList<JsonObject>();
                        list.add(getFailedPurchaseFormat(purchaseInfo, "onBillingConsumeFailed : " + result.getMessage()));

                        serviceListener.onPurchaseTransactionFinished(list, error);

                    }

                    return;
                } else {
                    if (serviceListener != null) {
                        ArrayList<JsonObject> list = new ArrayList<JsonObject>();
                        list.add(purchaseInfo.getJsonObject());
                        serviceListener.onPurchaseTransactionFinished(list, null);
                    }
                }

            }
        };

        return consumeFinishedListener;
    }

    JsonObject getFailedPurchaseFormat(Purchase purchaseInfo, String error) {
        JsonObject map = purchaseInfo.getJsonObject();

        map.addProperty(Keys.Billing.ERROR, error);

        return map;
    }

    void reportFailedTransaction(String productId, String error) {
        IBillingServiceListener listener = getListener();

        if (listener != null) {
            Purchase purchaseInfo = new Purchase(IabHelper.ITEM_TYPE_INAPP, productId);
            ArrayList<JsonObject> list = new ArrayList<JsonObject>();

            list.add(getFailedPurchaseFormat(purchaseInfo, error));//This appends error tag

            listener.onPurchaseTransactionFinished(list, error);
        }
    }

}
*/
