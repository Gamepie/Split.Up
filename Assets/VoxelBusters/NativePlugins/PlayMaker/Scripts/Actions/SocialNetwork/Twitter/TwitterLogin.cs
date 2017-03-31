using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Starts authenticating the app user with Twitter.")]
	public class TwitterLogin : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[Tooltip("Bool specifies whether application needs access to email address information of the logged-in user.")]
		public 	FsmBool 	requiresEmailAccess;

		[ActionSection("Results")]

		[Tooltip("The description of the problem that occurred.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	error;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when authentication process finishes successfully.")]
		public	FsmEvent	successEvent;
		[Tooltip("Event to send when authentication failed with error.")]
		public	FsmEvent	failedEvent;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			requiresEmailAccess	= new FsmBool();

			// Results properties
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
			NPBinding.Twitter.Login(requiresEmailAccess.Value, OnLoginFinished);
#endif
		}
		
		#endregion

		#region Callback Methods

		private void OnLoginFinished (TwitterSession _session, string _error)
		{
			// Update properties
			error.Value	= _error;

			// Send events
			Fsm.Event(string.IsNullOrEmpty(_error) ? successEvent : failedEvent);
			Finish();
		}

		#endregion
	}
}