package com.voxelbusters.nativeplugins.features.billing.serviceprovider.amazon;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.UserDataResponse;

/**
 * Created by ayyappa on 25/03/16.
 */
public class AmazonPurchasingListener implements PurchasingListener
{
    AmazonPurchasingListener()
    {

    }

    @Override
    public void onUserDataResponse(UserDataResponse userDataResponse) {

    }

    @Override
    public void onProductDataResponse(ProductDataResponse productDataResponse) {

    }

    @Override
    public void onPurchaseResponse(PurchaseResponse purchaseResponse) {

    }

    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {

    }
}
