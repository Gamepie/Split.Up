using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.NativePlugins.Internal;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the restored purchase transaction details received from Store and stores in variables.")]
	public class BillingGetRestoredPurchaseTransaction : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("The index to retrieve the transaction details from.")]
		public	FsmInt		atIndex;
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

		[Tooltip("Event to send when action fails (likely index is out of range exception).")]
		public 	FsmEvent 	failedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			atIndex					= 0;
			dateTimeFormat			= Constants.kPlayMakerDateTimeFormat;

			// Results properties
			productIdentifier		= new FsmString {
				UseVariable	= true
			};
			transactionDate			= new FsmString {
				UseVariable	= true
			};
			transactionIdentifier	= new FsmString {
				UseVariable	= true
			};
			transactionReceipt		= new FsmString {
				UseVariable	= true
			};
			error					= new FsmString {
				UseVariable	= true
			};
			rawPurchaseData			= new FsmString {
				UseVariable	= true
			};

			// Events properties
			failedEvent				= null;
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
				BillingTransaction	_transaction	= BillingUtils.restoredPurchasesList[atIndex.Value];

				// Update properties
				productIdentifier.Value		= _transaction.ProductIdentifier;
				transactionDate.Value		= _transaction.TransactionDateLocal.ToString(dateTimeFormat.Value);
				transactionIdentifier.Value	= _transaction.TransactionIdentifier;
				transactionReceipt.Value	= _transaction.TransactionReceipt;
				error.Value					= _transaction.Error;
				rawPurchaseData.Value		= _transaction.RawPurchaseData;
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