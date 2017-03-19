using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Report a score to game server leaderboard.")]
	public class GameServicesLeaderboardReportScore : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("A string used to identify the leaderboard.")]
		public	FsmString	identifier;
		[Tooltip("Indicates that specified identifier is unified value, which can identify leaderboard across all the supported platform. Works only if, leaderboard id collection was configured in NPSettings.")]
		public	FsmBool		isGlobalIdentifier;
		[Tooltip("The score earned by the local user.")]
		public	FsmInt		pointsScored;

		[ActionSection("Results")]
		
		[Tooltip("The description of the problem that occured.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	error;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when score was reported successfully.")]
		public 	FsmEvent 	successEvent;
		[Tooltip("Event to send when score reporting failed with errors.")]
		public 	FsmEvent 	failedEvent;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			identifier			= null;
			isGlobalIdentifier	= true;
			pointsScored		= 0;
			
			// Results properties
			error				= new FsmString {
				UseVariable	= true
			};
			
			// Events properties
			successEvent		= null;
			failedEvent			= null;
		}
		
		public override void OnEnter ()
		{
#if USES_GAME_SERVICES
			if (isGlobalIdentifier.Value)
			{
				NPBinding.GameServices.ReportScoreWithGlobalID(identifier.Value,
				                                               pointsScored.Value,
				                                               OnReportProgressFinished);
			}
			else
			{
				NPBinding.GameServices.ReportScoreWithID(identifier.Value,
				                                         pointsScored.Value,
				                                         OnReportProgressFinished);
			}
#endif
		}

		public void OnReportProgressFinished (bool _success, string _error)
		{
			// Update properties
			error.Value = _error;
			
			// Send event
			Fsm.Event(_success ? successEvent : failedEvent);
			Finish();
		}
		
		#endregion
	}
}