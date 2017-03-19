using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Signs out the local user from the game service.")]
	public class GameServicesLocalUserSignOut : FsmStateAction 
	{
		#region Fields

		[ActionSection("Results")]
	
		[Tooltip("The description of the problem that occured.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	error;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when user is signed out from the game service.")]
		public 	FsmEvent 	successEvent;
		[Tooltip("Event to send when operation failed to complete.")]
		public 	FsmEvent 	failedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
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
#if USES_GAME_SERVICES
			NPBinding.GameServices.LocalUser.SignOut(OnSignOutFinished);
#endif
		}
		
		#endregion

		#region Callback Methods

		private void OnSignOutFinished (bool _success, string _error)
		{
			// Update properties
			error.Value	= _error;
			
			// Send event
			Fsm.Event(_success ? successEvent : failedEvent);
			Finish();
		}

		#endregion
	}
}