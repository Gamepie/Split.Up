using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Determines if specified product was previously purchased.")]
	public class BillingIsProductPurchased : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("The string identifies the billing product registered in the Store.")]
		public	FsmString	productIdentifier;
		
		[ActionSection("Results")]

		[Tooltip("The product purchase status.")]
		[UIHint(UIHint.Variable)]
		public	FsmBool		isPurchased;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send if product was already purchased.")]
		public 	FsmEvent 	purchasedEvent;
		[Tooltip("Event to send if product was not purchased.")]
		public 	FsmEvent 	notPurchasedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			productIdentifier	= null;

			// Results properties
			isPurchased			= new FsmBool {
				UseVariable	= true
			};

			// Events properties
			purchasedEvent		= null;
			notPurchasedEvent	= null;
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
			BillingProduct	_product		= NPBinding.Billing.GetStoreProduct(productIdentifier.Value);
			bool 			_isPurchased	= NPBinding.Billing.IsProductPurchased(_product);

			Fsm.Event(_isPurchased ? purchasedEvent : notPurchasedEvent);
#endif
		}
		
		#endregion
	}
}