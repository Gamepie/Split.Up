using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Registers to the event and notifies you when restoring old purchases is completed.")]
	public class BillingDidFinishRestoringPurchasesEvent : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Results")]
		
		[Tooltip("The count of transaction details received.")]
		[UIHint(UIHint.Variable)]
		public	FsmInt		count;
		[Tooltip("The description of the problem that occured.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	error;
		
		[ActionSection("Results")]
		
		[Tooltip("Event to send when transaction details are retrieved successfully.")]
		public	FsmEvent	successEvent;
		[Tooltip("Event to send when operation failed with an error.")]
		public	FsmEvent	failedEvent;

		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			count			= new FsmInt {
				UseVariable	= true
			};
			error			= new FsmString {
				UseVariable	= true
			};
			
			// Events properties
			successEvent	= null;
			failedEvent		= null;
		}
		
		public override void OnEnter ()
		{
#if USES_BILLING
			// Register for events
			Billing.DidFinishRestoringPurchasesEvent += OnDidFinishRestoringPurchases;
#endif
		}
		
		#endregion
		
		#region Callback Methods

#if USES_BILLING
		private void OnDidFinishRestoringPurchases (BillingTransaction[] _transactions, string _error)
		{
			if (string.IsNullOrEmpty(_error))
			{
				// Update properties
				BillingUtils.restoredPurchasesList	= _transactions;
				count.Value		= _transactions.Length;
				error.Value		= null;

				// Send event
				Fsm.Event(successEvent);
			}
			else
			{
				// Update properties
				BillingUtils.restoredPurchasesList	= null;
				count.Value		= 0;
				error.Value		= _error;
				
				// Send event
				Fsm.Event(failedEvent);
			}
		}
#endif
		
		#endregion
	}
}