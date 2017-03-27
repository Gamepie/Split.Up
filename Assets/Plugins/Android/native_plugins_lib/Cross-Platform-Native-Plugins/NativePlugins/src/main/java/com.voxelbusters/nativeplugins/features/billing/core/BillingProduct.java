package com.voxelbusters.nativeplugins.features.billing.core;

import com.google.gson.JsonObject;

/**
 * Created by ayyappa on 28/03/16.
 */
public class BillingProduct
{
    public String name;
    public String description;
    public String product_identifier;
    public long price_amount_micros;
    public String localised_price;
    public String currency_code;
    public String currency_symbol;


    public  JsonObject GetJsonObject()
    {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name",name);
        jsonObject.addProperty("description",description);
        jsonObject.addProperty("product_id",product_identifier);
        jsonObject.addProperty("price_amount_micros",price_amount_micros);
        jsonObject.addProperty("localised_price",localised_price);
        jsonObject.addProperty("currency_code",currency_code);
        jsonObject.addProperty("currency_symbol",currency_symbol);

        return  jsonObject;
    }
}


