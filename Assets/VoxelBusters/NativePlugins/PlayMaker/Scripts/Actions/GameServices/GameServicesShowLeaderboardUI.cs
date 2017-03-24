using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Opens standard view to display leaderboard scores corresponding to the given identifier.")]
	public class GameServicesShowLeaderboardUI : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("A string used to identify the leaderboard.")]
		public	FsmString	identifier;
		[Tooltip("Indicates that specified identifier is unified value, which can identify leaderboard across all the supported platform. Works only if, leaderboard id collection was configured in NPSettings.")]
		public	FsmBool		isGlobalIdentifier;
		[Tooltip("A filter used to restrict the search to scores that were posted within a specific period of time.")]
		public	eLeaderboardTimeScope timeScope;

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
			// Setup properties
			identifier			= null;
			isGlobalIdentifier	= true;
			timeScope			= eLeaderboardTimeScope.ALL_TIME;

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
			if (isGlobalIdentifier.Value)
			{
				NPBinding.GameServices.ShowLeaderboardUIWithGlobalID(identifier.Value, 
				                                                     timeScope, 
				                                                     OnLeaderboardUIViewClosed);
			}
			else
			{
				NPBinding.GameServices.ShowLeaderboardUIWithID(identifier.Value, 
				                                               timeScope, 
				                                               OnLeaderboardUIViewClosed);
			}
#endif
		}
		
		#endregion

		#region Callback Methods

		private void OnLeaderboardUIViewClosed (string _error)
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