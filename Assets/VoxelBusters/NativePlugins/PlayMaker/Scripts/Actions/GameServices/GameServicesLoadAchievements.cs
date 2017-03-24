using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Loads the achievements previously reported to game server.")]
	public class GameServicesLoadAchievements : FsmStateAction 
	{
		#region Fields

		[ActionSection("Results")]

		[Tooltip("The count of achievement objects retrieved.")]
		[UIHint(UIHint.Variable)]
		public	FsmInt		count;
		[Tooltip("The description of the problem that occurred.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	error;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when achievement objects are retrieved from game server.")]
		public 	FsmEvent 	successEvent;
		[Tooltip("Event to send when request failed with error.")]
		public 	FsmEvent 	failedEvent;
		
		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			count			= new FsmInt {
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
#if USES_GAME_SERVICES
			NPBinding.GameServices.LoadAchievements(OnLoadAchievementsFinished);
#endif
		}

		#endregion

		#region Callback Methods

#if USES_GAME_SERVICES
		private void OnLoadAchievementsFinished (Achievement[] _achievements, string _error)
		{
			if (string.IsNullOrEmpty(_error))
			{
				// Update properties
				GameServicesUtils.achievementsList	= _achievements;
				count.Value		= _achievements.Length;
				error.Value		= null;

				// Send event
				Fsm.Event(successEvent);
			}
			else
			{
				// Update properties
				GameServicesUtils.achievementsList	= null;
				count.Value		= 0;
				error.Value		= _error;

				// Send event
				Fsm.Event(failedEvent);
			}

			Finish();
		}
#endif

		#endregion
	}
}