using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Discards all the received notifications.")]
	public class NotificationServiceClearNotifications : FsmStateAction 
	{
		#region FSM Methods

		public override void OnEnter ()
		{
#if USES_NOTIFICATION_SERVICE
			NPBinding.NotificationService.ClearNotifications();
#endif

			Finish();
		}
		
		#endregion
	}
}