using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Forcefully show review prompt irrespective of Rate My App settings.")]
	public class UtilityAskForReviewNow : FsmStateAction 
	{
		#region Fields

		[ActionSection("Events")]

		[Tooltip("Event to send when failed to show review prompt.")]
		public 		FsmEvent 	failedEvent;

		#endregion

		#region Methods
		
		public override void Reset ()
		{	
			// Events properties
			failedEvent		= null;
		}

		public override void OnEnter () 
		{
			if (NPSettings.Utility.RateMyApp.IsEnabled)
			{
				NPBinding.Utility.RateMyApp.AskForReviewNow();
			}
			else
			{
				Fsm.Event(failedEvent);
			}

			Finish();
		}

		#endregion
	}
}