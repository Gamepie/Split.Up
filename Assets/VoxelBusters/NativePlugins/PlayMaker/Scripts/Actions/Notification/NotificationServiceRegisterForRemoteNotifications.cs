using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Registers to receive remote notifications via Push Notification service.")]
	public class NotificationServiceRegisterForRemoteNotifications : FsmStateAction 
	{
		#region FSM Methods

		public override void OnEnter ()
		{
#if USES_NOTIFICATION_SERVICE
			NPBinding.NotificationService.RegisterForRemoteNotifications();
#endif

			Finish();
		}
		
		#endregion
	}
}