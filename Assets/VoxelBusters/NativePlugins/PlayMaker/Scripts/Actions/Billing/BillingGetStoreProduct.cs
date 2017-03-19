using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the product at specified index and stores in variables.")]
	public class BillingGetStoreProduct : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]
		
		[Tooltip("The index to retrieve the product from.")]
		[UIHint(UIHint.Variable)]
		public	FsmInt		atIndex;
		
		[ActionSection("Results")]

		[Tooltip("The string that identifies the product to the Store.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	productIdentifier;
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
		
		[Tooltip("Event to send when action fails (likely index is out of range exception).")]
		public 	FsmEvent 	failedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			atIndex				= 0;

			// Results properties
			productIdentifier	= new FsmString {
				UseVariable	= true
			};
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
			try
			{
				BillingProduct _product	= BillingUtils.productsList[atIndex.Value];

				// Update properties
				productIdentifier.Value	= _product.ProductIdentifier;
				name.Value				= _product.Name;
				description.Value		= _product.Description;
				isConsumable.Value		= _product.IsConsumable;
				price.Value				= _product.Price;
				localizedPrice.Value	= _product.LocalizedPrice;
				currencyCode.Value		= _product.CurrencyCode;
				currencySymbol.Value	= _product.CurrencySymbol;
			}
			catch (System.Exception _exception)
			{
				Debug.Log(_exception.Message);

				Fsm.Event(failedEvent);

				return;
			}
#endif
		}
		
		#endregion
	}
}