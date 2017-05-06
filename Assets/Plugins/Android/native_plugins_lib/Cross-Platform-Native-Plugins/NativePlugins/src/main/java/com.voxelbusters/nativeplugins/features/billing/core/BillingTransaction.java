package com.voxelbusters.nativeplugins.features.billing.core;

import com.google.gson.JsonObject;

/**
 * Created by ayyappa on 28/03/16.
 */
public class BillingTransaction
{
    public String productIdentifier;
    public long transactionDate;
    public String transactionIdentifier;
    public String transactionReceipt;
    public int transactionState;
    public int verificationState;
    public String error;
    public String rawPurchaseJson; // This should be the original json

    public JsonObject getJsonObject()
    {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("product-identifier",productIdentifier);
        jsonObject.addProperty("transaction-date",transactionDate);
        jsonObject.addProperty("transaction-identifier",transactionIdentifier);
        jsonObject.addProperty("transaction-receipt",transactionReceipt);
        jsonObject.addProperty("transaction-state",transactionState);
        jsonObject.addProperty("verification-state",verificationState);
        jsonObject.addProperty("error",error);
        jsonObject.addProperty("raw-purchase-json",rawPurchaseJson);

        return  jsonObject;
    }
}
