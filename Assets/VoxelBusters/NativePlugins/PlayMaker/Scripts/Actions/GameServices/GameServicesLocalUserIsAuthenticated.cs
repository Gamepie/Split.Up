using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Determines whether local user is currently signed in to game service.")]
	public class GameServicesLocalUserIsAuthenticated : FsmStateAction 
	{
		#region Fields

		[ActionSection("Results")]
	
		[Tooltip("The status of authentication.")]
		[UIHint(UIHint.Variable)]
		public	FsmBool		isAuthenticated;
		
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
			isAuthenticated			= new FsmBool {
				UseVariable	= true
			};

			// Events properties
			authenticatedEvent		= null;
			notAuthenticatedEvent	= null;
		}
		
		public override void OnEnter ()
		{
#if USES_GAME_SERVICES
			// Update property
			isAuthenticated.Value	= NPBinding.GameServices.LocalUser.IsAuthenticated;

			// Send event
			Fsm.Event(isAuthenticated.Value ? authenticatedEvent : notAuthenticatedEvent);
#endif

			Finish();
		}
		
		#endregion
	}
}