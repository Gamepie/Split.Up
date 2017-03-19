using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Cancels the delivery of all scheduled local notifications.")]
	public class NotificationServiceCancelAllLocalNotification : FsmStateAction 
	{
		#region FSM Methods

		public override void OnEnter ()
		{
#if USES_NOTIFICATION_SERVICE
			NPBinding.NotificationService.CancelAllLocalNotification();
#endif

			Finish();
		}
		
		#endregion
	}
}