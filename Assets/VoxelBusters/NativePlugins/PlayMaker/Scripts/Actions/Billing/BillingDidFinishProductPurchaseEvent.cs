using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.NativePlugins.Internal;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Registers to the event and notifies you when product payment response is received from the Store.")]
	public class BillingDidFinishProductPurchaseEvent : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("Defines the text representation of date and time.")]
		public 	FsmString 	dateTimeFormat;

		[ActionSection("Results")]
		
		[Tooltip("The string used to identify the product that was purchased.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	productIdentifier;
		[Tooltip("The local date and time, when user initiated product purchase.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	transactionDate;
		[Tooltip("The string that uniquely identifies a payment transaction.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	transactionIdentifier;
		[Tooltip("Signed receipt that records all information about a payment transaction. ")]
		[UIHint(UIHint.Variable)]
		public	FsmString	transactionReceipt;
		[Tooltip("Description of the problem that occured.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	error;
		[Tooltip("The purchase data in JSON format.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	rawPurchaseData;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when product was purchased successfully.")]
		public 	FsmEvent 	purchasedEvent;
		[Tooltip("Event to send when product was restored successfully.")]
		public 	FsmEvent 	restoredEvent;
		[Tooltip("Event to send when transaction was refunded back to the user.")]
		public 	FsmEvent 	refundedEvent;
		[Tooltip("Event to send when transaction failed.")]
		public 	FsmEvent 	failedEvent;

		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			dateTimeFormat		= Constants.kPlayMakerDateTimeFormat;
			
			// Results properties
			productIdentifier	= new FsmString {
				UseVariable	= true
			};
			transactionDate		= new FsmString {
				UseVariable	= true
			};
			transactionIdentifier	= new FsmString {
				UseVariable	= true
			};
			transactionReceipt	= new FsmString {
				UseVariable	= true
			};
			error				= new FsmString {
				UseVariable	= true
			};
			rawPurchaseData		= new FsmString {
				UseVariable	= true
			};

			// Events properties
			purchasedEvent		= null;
			restoredEvent		= null;
			refundedEvent		= null;
			failedEvent			= null;
		}
		
		public override void OnEnter ()
		{
#if USES_BILLING
			// Register for events
			Billing.DidFinishProductPurchaseEvent += OnDidFinishProductPurchase;
#endif
		}
		
		#endregion
		
		#region Callback Methods
		
#if USES_BILLING
		private void OnDidFinishProductPurchase (BillingTransaction _transaction)
		{
			// Update properties
			productIdentifier.Value		= _transaction.ProductIdentifier;
			transactionDate.Value		= _transaction.TransactionDateLocal.ToString(dateTimeFormat.Value);
			transactionIdentifier.Value	= _transaction.TransactionIdentifier;
			transactionReceipt.Value	= _transaction.TransactionReceipt;
			error.Value					= _transaction.Error;
			rawPurchaseData.Value		= _transaction.RawPurchaseData;
			
			// Send event
			if (_transaction.VerificationState == eBillingTransactionVerificationState.FAILED)
			{
				Fsm.Event(failedEvent);

				return;
			}
			else if (_transaction.VerificationState == eBillingTransactionVerificationState.SUCCESS)
			{
				switch (_transaction.TransactionState)
				{
				case eBillingTransactionState.FAILED:
					Fsm.Event(failedEvent);
					break;
					
				case eBillingTransactionState.PURCHASED:
					Fsm.Event(purchasedEvent);
					break;
					
				case eBillingTransactionState.RESTORED:
					Fsm.Event(restoredEvent);
					break;
					
				case eBillingTransactionState.REFUNDED:
					Fsm.Event(refundedEvent);
					break;
					
				default:
					LogWarning("[Billing] Unhandled transactions state.");
					break;
				}

				return;
			}
		}
#endif
		
		#endregion
	}
}