using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Unregister from receiving remote notifications sent via Push Notification service.")]
	public class NotificationServiceUnregisterForRemoteNotifications : FsmStateAction 
	{
		#region FSM Methods

		public override void OnEnter ()
		{
#if USES_NOTIFICATION_SERVICE
			NPBinding.NotificationService.UnregisterForRemoteNotifications();
#endif

			Finish();
		}
		
		#endregion
	}
}