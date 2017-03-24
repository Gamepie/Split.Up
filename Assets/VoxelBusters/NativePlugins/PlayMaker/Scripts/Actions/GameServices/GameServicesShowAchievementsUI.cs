using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Opens standard view to display achievement progress  for the local player.")]
	public class GameServicesShowAchievementsUI : FsmStateAction 
	{
		#region Fields

		[ActionSection("Results")]
		
		[Tooltip("The description of the problem that occured.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	error;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when the view was closed by the user.")]
		public 	FsmEvent 	successEvent;
		[Tooltip("Event to send when the view failed to show.")]
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
			NPBinding.GameServices.ShowAchievementsUI(OnAchievementsUIViewClosed);
#endif
		}
		
		#endregion

		#region Callback Methods

		private void OnAchievementsUIViewClosed (string _error)
		{
			// Update properties
			error.Value = _error;

			// Send event
			Fsm.Event(string.IsNullOrEmpty(_error) ? successEvent : failedEvent);
			Finish();
		}

		#endregion
	}
}