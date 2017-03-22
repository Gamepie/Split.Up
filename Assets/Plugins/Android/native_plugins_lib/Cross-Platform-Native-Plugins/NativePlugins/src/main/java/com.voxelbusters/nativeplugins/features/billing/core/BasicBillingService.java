package com.voxelbusters.nativeplugins.features.billing.core;

import android.content.Context;

import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.utilities.Debug;

public class BasicBillingService implements IBillingService
{

	private final String				EXTEND_FOR_IMPLEMENTATION_MESSAGE	= "Extend this class to provide your billing service implementation";
	protected boolean					isBillingSupported;

	protected IBillingServiceListener	serviceListener;

	public BasicBillingService()
	{

	}

	@Override
	public void init(String key, Context context, String[] consumableList)
	{
		Debug.warning(CommonDefines.BILLING_TAG, EXTEND_FOR_IMPLEMENTATION_MESSAGE);
	}

	@Override
	public void requestBillingProducts(String[] consumableProductIDs, String[] nonConsumableProductIDs)
	{
		Debug.warning(CommonDefines.BILLING_TAG, EXTEND_FOR_IMPLEMENTATION_MESSAGE);
	}

	@Override
	public void buyProduct(String productID, Context context)
	{
		Debug.warning(CommonDefines.BILLING_TAG, EXTEND_FOR_IMPLEMENTATION_MESSAGE);

	}

	@Override
	public boolean isProductPurchased(String productID)
	{
		Debug.warning(CommonDefines.BILLING_TAG, EXTEND_FOR_IMPLEMENTATION_MESSAGE);
		return false;
	}

	@Override
	public void restoreCompletedTransactions()
	{
		Debug.warning(CommonDefines.BILLING_TAG, EXTEND_FOR_IMPLEMENTATION_MESSAGE);
	}

	//Register service listener here 
	@Override
	public void setListener(IBillingServiceListener listener)
	{
		serviceListener = listener;
	}

	public IBillingServiceListener getListener()
	{
		return serviceListener;
	}

}
