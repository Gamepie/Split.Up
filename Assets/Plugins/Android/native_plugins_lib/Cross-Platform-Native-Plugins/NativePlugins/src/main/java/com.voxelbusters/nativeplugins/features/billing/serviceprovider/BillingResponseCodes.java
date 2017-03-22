package com.voxelbusters.nativeplugins.features.billing.serviceprovider;

public class BillingResponseCodes
{
	// Billing Result codes
	public static final int		BILLING_RESPONSE_RESULT_OK					= 0;
	public static final int		BILLING_RESPONSE_RESULT_USER_CANCELED		= 1;
	public static final int		BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE	= 3;
	public static final int		BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE	= 4;
	public static final int		BILLING_RESPONSE_RESULT_DEVELOPER_ERROR		= 5;
	public static final int		BILLING_RESPONSE_RESULT_ERROR				= 6;
	public static final int		BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED	= 7;
	public static final int		BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED		= 8;

	// Billing Error codes
	public static final int		BILLING_ERROR_BASE							= -1000;
	public static final int		BILLING_REMOTE_EXCEPTION					= -1001;
	public static final int		BILLING_BAD_RESPONSE						= -1002;
	public static final int		BILLING_VERIFICATION_FAILED					= -1003;
	public static final int		BILLING_SEND_INTENT_FAILED					= -1004;
	public static final int		BILLING_USER_CANCELLED						= -1005;
	public static final int		BILLING_UNKNOWN_PURCHASE_RESPONSE			= -1006;
	public static final int		BILLING_MISSING_TOKEN						= -1007;
	public static final int		BILLING_UNKNOWN_ERROR						= -1008;
	public static final int		BILLING_SUBSCRIPTIONS_NOT_AVAILABLE			= -1009;
	public static final int		BILLING_INVALID_CONSUMPTION					= -1010;

	// Keys for the responses from InAppBillingService
	public static final String	BILLING_RESPONSE_CODE						= "RESPONSE_CODE";
	public static final String	BILLING_RESPONSE_GET_SKU_DETAILS_LIST		= "DETAILS_LIST";
	public static final String	BILLING_RESPONSE_BUY_INTENT					= "BUY_INTENT";
	public static final String	BILLING_RESPONSE_INAPP_PURCHASE_DATA		= "INAPP_PURCHASE_DATA";
	public static final String	BILLING_RESPONSE_INAPP_SIGNATURE			= "INAPP_DATA_SIGNATURE";
	public static final String	BILLING_RESPONSE_INAPP_ITEM_LIST			= "INAPP_PURCHASE_ITEM_LIST";
	public static final String	BILLING_RESPONSE_INAPP_PURCHASE_DATA_LIST	= "INAPP_PURCHASE_DATA_LIST";
	public static final String	BILLING_RESPONSE_INAPP_SIGNATURE_LIST		= "INAPP_DATA_SIGNATURE_LIST";
	public static final String	BILLING_INAPP_CONTINUATION_TOKEN			= "INAPP_CONTINUATION_TOKEN";

	/**
	 * Returns a human-readable description for the given response code.
	 * 
	 * @param code
	 *            The response code
	 * @return A human-readable string explaining the result code. It also
	 *         includes the result code numerically.
	 */
	public static String getResponseDesc(int code)
	{
		String[] iab_msgs = ("0:OK/1:User Canceled/2:Unknown/" + "3:Billing Unavailable/4:Item unavailable/" + "5:Developer Error/6:Error/7:Item Already Owned/" + "8:Item not owned").split("/");
		String[] iabhelper_msgs = ("0:OK/-1001:Remote exception during initialization/" + "-1002:Bad response received/" + "-1003:Purchase signature verification failed/" + "-1004:Send intent failed/" + "-1005:User cancelled/" + "-1006:Unknown purchase response/" + "-1007:Missing token/" + "-1008:Unknown error/" + "-1009:Subscriptions not available/" + "-1010:Invalid consumption attempt")
						.split("/");

		if (code <= BillingResponseCodes.BILLING_ERROR_BASE)
		{
			int index = BillingResponseCodes.BILLING_ERROR_BASE - code;
			if ((index >= 0) && (index < iabhelper_msgs.length))
			{
				return iabhelper_msgs[index];
			}
			else
			{
				return String.valueOf(code) + ":Unknown Error";
			}
		}
		else if ((code < 0) || (code >= iab_msgs.length))
		{
			return String.valueOf(code) + ":Unknown";
		}
		else
		{
			return iab_msgs[code];
		}
	}

}
