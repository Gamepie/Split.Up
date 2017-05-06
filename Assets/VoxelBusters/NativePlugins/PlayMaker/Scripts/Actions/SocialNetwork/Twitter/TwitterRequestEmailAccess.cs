using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sends a request to access email address associated with current Twitter user.")]
	public class TwitterRequestEmailAccess : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Result")]
		
		[UIHint(UIHint.Variable)]
		[Tooltip("The logged in Twitter user's email address.")]
		public	FsmString 	emailID;
		[UIHint(UIHint.Variable)]
		[Tooltip("The description of the problem that occurred.")]
		public	FsmString 	error;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when email access request completes successfully.")]
		public	FsmEvent	successEvent;
		[Tooltip("Event to send when email access request failed with error.")]
		public	FsmEvent	failedEvent;
		
		#endregion

		#region FSM Methods
		
		public override void Reset ()
		{
			// Results properties
			emailID			= new FsmString {
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
#if USES_TWITTER
			NPBinding.Twitter.RequestEmailAccess(DidReceiveEmailAccessResponse);		
#endif
		}
		
		#endregion

		#region Callback Methods

		private void DidReceiveEmailAccessResponse (string _email, string _error)
		{
			// Update properties
			emailID.Value	= _email;
			error.Value		= _error;
			
			// Send event
			Fsm.Event(string.IsNullOrEmpty(_error) ? successEvent : failedEvent);
			Finish();
		}

		#endregion
	}
}