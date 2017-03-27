// (c) Copyright HutongGames, LLC 2010-2013. All rights reserved.

using UnityEngine;
using VoxelBusters.NativePlugins;





namespace HutongGames.PlayMaker.Actions
{
	namespace VoxelBusters.NativePlugins	{
		[ActionCategory("GameServices")]
	[Tooltip("report achievements")]
	public class GS_report_achievements : FsmStateAction

	{
			private double PROGRESS_PERCENTAGE = 100d;
			[Tooltip("A string used to identify the achievement.")]

			private Achievement	m_achievement;
			public string identifier;
			public	FsmString	identifieri;


			public	FsmString	dateTimeFormat;

			[ActionSection("Events")]
			public 	FsmEvent 	successEvent;
			public 	FsmEvent 	failedEvent;

			[ActionSection("Results")]
			public 	FsmBool	 	completed;
			public 	FsmString 	lastReportedDate;
			public 	FsmString 	error;
			[Tooltip("Indicates whether the current user has completed this achievement.")]
			[UIHint(UIHint.Variable)]

			public override void OnEnter ()
			{

				m_achievement	= NPBinding.GameServices.CreateAchievementWithGlobalID(identifier);
				m_achievement.ReportProgress (OnReportProgressFinished);
				NPBinding.GameServices.ReportProgressWithGlobalID (identifier, PROGRESS_PERCENTAGE, OnReportProgressFinished);

			}

			private void OnReportProgressFinished (bool _success, string _error){

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

		}
	}
}