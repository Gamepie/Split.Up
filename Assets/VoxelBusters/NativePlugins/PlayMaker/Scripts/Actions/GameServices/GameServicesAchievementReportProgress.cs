using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.NativePlugins.Internal;
using VoxelBusters.NativePlugins.PlayMaker;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Reports the local user's achievement progress to game server.")]
	public class GameServicesAchievementReportProgress : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("A string used to identify the achievement.")]
		public	FsmString	identifier;
		[Tooltip("Indicates that specified identifier is unified value, which can identify achievement across all the supported platform. Works only if, achievement id collection was configured in NPSettings.")]
		public	FsmBool		isGlobalIdentifier;
		[Tooltip("Indicates how far the player has progressed.")]
		public	FsmFloat	percentageCompleted;
		[RequiredField]
		[Tooltip("Defines the text representation of date and time.")]
		public	FsmString	dateTimeFormat;

		[ActionSection("Results")]
		
		[Tooltip("Indicates whether the current user has completed this achievement.")]
		[UIHint(UIHint.Variable)]
		public 	FsmBool	 	completed;
		[Tooltip("The last time that progress on the achievement was successfully reported to game server.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	lastReportedDate;
		[Tooltip("The description of the problem that occured.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	error;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when progress was reported successfully.")]
		public 	FsmEvent 	successEvent;
		[Tooltip("Event to send when progress reporting failed with errors.")]
		public 	FsmEvent 	failedEvent;

		private Achievement	m_achievement;
		
		#endregion

		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			identifier			= null;
			isGlobalIdentifier	= true;
			percentageCompleted	= 0;
			dateTimeFormat		= Constants.kPlayMakerDateTimeFormat;

			// Results properties
			completed			= new FsmBool {
				UseVariable	= true
			};
			lastReportedDate	= new FsmString {
				UseVariable	= true
			};
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
			// Create achievement instance
			if (isGlobalIdentifier.Value)
				m_achievement	= NPBinding.GameServices.CreateAchievementWithGlobalID(identifier.Value);
			else
				m_achievement	= NPBinding.GameServices.CreateAchievementWithID(identifier.Value);
			
			// Update properties
			m_achievement.PercentageCompleted	= percentageCompleted.Value;
			
			// Report progress
			m_achievement.ReportProgress(OnReportProgressFinished);
#endif
		}

		#endregion

		#region Callback Methods

#if USES_GAME_SERVICES
		private void OnReportProgressFinished (bool _success, string _error)
		{
			if (_success)
			{
				// Update properties
				completed.Value				= m_achievement.Completed;
				lastReportedDate.Value		= m_achievement.LastReportedDate.ToString(dateTimeFormat.Value);
				error.Value					= null;

				// Send event
				Fsm.Event(successEvent);
			}
			else
			{
				// Update properties
				error.Value					= _error;

				// Send event
				Fsm.Event(failedEvent);
			}

			Finish();
		}
#endif

		#endregion
	}
}