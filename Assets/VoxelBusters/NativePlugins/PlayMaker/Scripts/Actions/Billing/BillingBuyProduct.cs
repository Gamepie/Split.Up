using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Initiates purchase process for the specified billing product.")]
	public class BillingBuyProduct : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The string identifies the billing product registered in the Store.")]
		public	FsmString	productIdentifier;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			productIdentifier	= null;
		}
		
		public override void OnEnter ()
		{
			DoAction();

			Finish();
		}
		
		#endregion

		#region Methods

		private void DoAction ()
		{
#if USES_BILLING
			// Get the specified product
			BillingProduct	_product	= NPBinding.Billing.GetStoreProduct(productIdentifier.Value);

			// Start request to purchase product
			NPBinding.Billing.BuyProduct(_product);
#endif
		}

		#endregion
	}
}