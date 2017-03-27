using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Registers to the event and notifies you when your app completes registration with Push Notification service.")]
	public class NotificationServiceDidFinishRegisterForRemoteNotificationEvent : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Results")]
		
		[Tooltip("A token that identifies the device to Push Notification service.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	deviceToken;
		[Tooltip("The description of the problem that occurred.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	error;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when registration is completed.")]
		public 	FsmEvent 	successEvent;
		[Tooltip("Event to send when registration failed with errors.")]
		public 	FsmEvent 	failedEvent;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			deviceToken		= new FsmString {
				UseVariable = true
			};
			error		= new FsmString {
				UseVariable = true
			};
			
			// Events properties
			successEvent	= null;
			failedEvent		= null;
		}

		public override void OnEnter ()
		{
#if USES_NOTIFICATION_SERVICE
			// Register for event
			NotificationService.DidFinishRegisterForRemoteNotificationEvent += OnRegistrationFinished;
#endif
		}
		
		#endregion

		#region Callback Methods

		private void OnRegistrationFinished (string _deviceToken, string _error)
		{
			// Update properties
			deviceToken.Value	= _deviceToken;
			error.Value			= _error;

			// Send event
			Fsm.Event(string.IsNullOrEmpty(_error) ? successEvent : failedEvent);
		}

		#endregion
	}
}