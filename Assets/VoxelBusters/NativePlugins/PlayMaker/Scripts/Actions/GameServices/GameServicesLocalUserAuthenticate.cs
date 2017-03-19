using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Authenticates the local user on the device.")]
	public class GameServicesLocalUserAuthenticate : FsmStateAction 
	{
		#region Fields

		[ActionSection("Results")]
	
		[Tooltip("The status of authentication.")]
		[UIHint(UIHint.Variable)]
		public	FsmBool		isAuthenticated;
		[Tooltip("The description of the problem that occured.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	error;

		[ActionSection("Events")]
		
		[Tooltip("Event to send if local user is authenticated.")]
		public 	FsmEvent 	authenticatedEvent;
		[Tooltip("Event to send if local user is not authenticated.")]
		public 	FsmEvent 	notAuthenticatedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			isAuthenticated		= new FsmBool {
				UseVariable	= true
			};
			error				= new FsmString {
				UseVariable	= true
			};

			// Events properties
			authenticatedEvent		= null;
			notAuthenticatedEvent	= null;
		}
		
		public override void OnEnter ()
		{
#if USES_GAME_SERVICES
			NPBinding.GameServices.LocalUser.Authenticate(OnAuthenticationFinished);
#endif
		}
		
		#endregion

		#region Callback Methods

		private void OnAuthenticationFinished (bool _success, string _error)
		{
			// Update properties
			isAuthenticated.Value	= _success;
			error.Value 			= _error;
			
			// Send event
			Fsm.Event(_success ? authenticatedEvent : notAuthenticatedEvent);
			Finish();
		}

		#endregion
	}
}