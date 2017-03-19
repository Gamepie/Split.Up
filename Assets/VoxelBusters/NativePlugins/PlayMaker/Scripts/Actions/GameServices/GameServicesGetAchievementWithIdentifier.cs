using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.NativePlugins.Internal;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the properties of the previously reported achievement and stores it in variable.")]
	public class GameServicesGetAchievementWithIdentifier : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("A string used to identify the achievement.")]
		public	FsmString	identifier;
		[Tooltip("Indicates that specified identifier is unified value, which can identify achievement across all the supported platform. Works only if, achievement id collection was configured in NPSettings.")]
		public	FsmBool		isGlobalIdentifier;
		[RequiredField]
		[Tooltip("Defines the text representation of date and time.")]
		public	FsmString	dateTimeFormat;

		[ActionSection("Results")]

		[Tooltip("Describes how far the user has progressed on this achievement.")]
		[UIHint(UIHint.Variable)]
		public 	FsmFloat 	percentageCompleted;
		[Tooltip("Indicates whether the current user has completed this achievement.")]
		[UIHint(UIHint.Variable)]
		public 	FsmBool	 	completed;
		[Tooltip("The last time that progress on the achievement was successfully reported to game server.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	lastReportedDate;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when action fails (likely index is out of range exception).")]
		public 	FsmEvent 	failedEvent;
		
		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			identifier			= null;
			isGlobalIdentifier	= true;
			dateTimeFormat		= Constants.kPlayMakerDateTimeFormat;

			// Results properties
			percentageCompleted	= new FsmFloat {
				UseVariable	= true
			};
			completed			= new FsmBool {
				UseVariable	= true
			};
			lastReportedDate	= new FsmString {
				UseVariable	= true
			};

			// Events properties
			failedEvent			= null;
		}

		public override void OnEnter ()
		{
			DoAction();
		}

		#endregion

		#region Methods

		private void DoAction ()
		{
#if USES_GAME_SERVICES
			try
			{
				Achievement _achievement	= null;

				if (isGlobalIdentifier.Value)
				{
					_achievement			= System.Array.Find<Achievement>(GameServicesUtils.achievementsList, (Achievement _currentAchievement) => {
						return _currentAchievement.GlobalIdentifier.Equals(identifier.Value);
					});
				}
				else
				{
					_achievement			= System.Array.Find<Achievement>(GameServicesUtils.achievementsList, (Achievement _currentAchievement) => {
						return _currentAchievement.Identifier.Equals(identifier.Value);
					});
				}

				// Update properties
				percentageCompleted.Value	= (float)_achievement.PercentageCompleted;
				completed.Value				= _achievement.Completed;
				lastReportedDate.Value		= _achievement.LastReportedDate.ToString(dateTimeFormat.Value);

				OnActionDidFinish();

				return;
			}
			catch (System.Exception _exception)
			{
				Debug.Log(_exception.Message);

				OnActionDidFail();

				return;
			}
#endif
		}
		
		private void OnActionDidFinish ()
		{
			Finish();
		}
		
		private void OnActionDidFail ()
		{
			Fsm.Event(failedEvent);
			Finish();
		}
		
		#endregion
	}
}