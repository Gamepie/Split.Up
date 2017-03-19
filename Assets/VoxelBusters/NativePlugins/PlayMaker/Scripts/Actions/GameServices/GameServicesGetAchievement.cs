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
	public class GameServicesGetAchievement : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[Tooltip("The index to retrieve the achievement from.")]
		public 	FsmInt	 	atIndex;
		[RequiredField]
		[Tooltip("Defines the text representation of date and time.")]
		public	FsmString	dateTimeFormat;

		[ActionSection("Results")]
		
		[Tooltip("An unified string internally used to identify the achievement across all the supported platforms.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	globalIdentifier;
		[Tooltip("A string used to identify the achievement in the current platform.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	identifier;
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
			atIndex			= 0;
			dateTimeFormat	= Constants.kPlayMakerDateTimeFormat;

			// Results properties
			globalIdentifier		= new FsmString {
				UseVariable	= true
			};
			identifier				= new FsmString {
				UseVariable	= true
			};
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
			failedEvent		= null;
		}

		public override void OnEnter ()
		{
			DoAction();

			Finish();
		}

		#endregion

		#region Methods

		private void DoAction ()
		{
#if USES_GAME_SERVICES
			try
			{
				Achievement _achievement 	= GameServicesUtils.achievementsList[atIndex.Value];

				// Update properties
				globalIdentifier.Value		= _achievement.GlobalIdentifier;
				identifier.Value			= _achievement.Identifier;
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