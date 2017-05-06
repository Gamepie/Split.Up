using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the product with specified identifier and stores in variables.")]
	public class BillingGetStoreProductWithIdentifier : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The string that identifies the product to the Store.")]
		public	FsmString	productIdentifier;
		
		[ActionSection("Results")]
		
		[Tooltip("The name of the product.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	name;
		[Tooltip("The description of the product.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	description;
		[Tooltip("Bool value used to identify product type. Is it Consumable/Non-Consumable product?")]
		[UIHint(UIHint.Variable)]
		public	FsmBool		isConsumable;
		[Tooltip("The cost of the product in the local currency.")]
		[UIHint(UIHint.Variable)]
		public	FsmFloat	price;
		[Tooltip("The cost of the product prefixed with local currency symbol.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	localizedPrice;
		[Tooltip("The local currency code.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	currencyCode;
		[Tooltip("The local currency symbol.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	currencySymbol;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when action fails to find specified product.")]
		public 	FsmEvent 	failedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			productIdentifier	= null;

			// Results properties
			name				= new FsmString {
				UseVariable	= true
			};
			description			= new FsmString {
				UseVariable	= true
			};
			isConsumable		= new FsmBool {
				UseVariable	= true
			};
			price				= new FsmFloat {
				UseVariable	= true
			};
			localizedPrice		= new FsmString {
				UseVariable	= true
			};
			currencyCode		= new FsmString {
				UseVariable	= true
			};
			currencySymbol		= new FsmString {
				UseVariable	= true
			};

			// Events properties
			failedEvent			= null;
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
			BillingProduct _product		= NPBinding.Billing.GetStoreProduct(productIdentifier.Value);

			if (_product == null)
			{
				Fsm.Event(failedEvent);
				return;
			}
			else
			{
				// Update properties
				name.Value				= _product.Name;
				description.Value		= _product.Description;
				isConsumable.Value		= _product.IsConsumable;
				price.Value				= _product.Price;
				localizedPrice.Value	= _product.LocalizedPrice;
				currencyCode.Value		= _product.CurrencyCode;
				currencySymbol.Value	= _product.CurrencySymbol;
			}
#endif
		}
		
		#endregion
	}
}