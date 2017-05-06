using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.Utility;

// Conflict handlers
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the default system sound.")]
	public class NotificationServiceCrossPlatformNotificationGetDefaultSoundName : FsmStateAction 
	{
		#region Fields

		[ActionSection("Results")]
		
		[Tooltip("The default system sound.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	defaultSoundName;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			defaultSoundName	= new FsmString {
				UseVariable	= true
			};
		}

		public override void OnEnter ()
		{
#if USES_NOTIFICATION_SERVICE
			defaultSoundName	= CrossPlatformNotification.kDefaultSoundName;
#endif

			Finish();
		}
		
		#endregion
	}
}