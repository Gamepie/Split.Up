using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Determines whether game services feature is supported.")]
	public class GameServicesIsAvailable : FsmStateAction 
	{
		#region Fields

		[ActionSection("Results")]
	
		[Tooltip("The status of feature availablity.")]
		[UIHint(UIHint.Variable)]
		public	FsmBool		isAvailable;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send if game services is supported.")]
		public 	FsmEvent 	supportedEvent;
		[Tooltip("Event to send if game services is not supported.")]
		public 	FsmEvent 	notSupportedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			isAvailable			= new FsmBool {
				UseVariable	= true
			};

			// Events properties
			supportedEvent		= null;
			notSupportedEvent	= null;
		}
		
		public override void OnEnter ()
		{
#if USES_GAME_SERVICES
			// Update property
			isAvailable.Value	= NPBinding.GameServices.IsAvailable();

			// Send event
			Fsm.Event(isAvailable.Value ? supportedEvent : notSupportedEvent);
#endif

			Finish();
		}
		
		#endregion
	}
}