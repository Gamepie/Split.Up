using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Registers to the event and notifies you when your app has received a remote notification.")]
	public class NotificationServiceDidReceiveRemoteNotificationEvent : NotificationServiceReceivedNotificationEventBase 
	{
		#region FSM Methods

		public override void OnEnter ()
		{
			base.OnEnter();

#if USES_NOTIFICATION_SERVICE
			NotificationService.DidReceiveRemoteNotificationEvent += OnNotificationReceived;
#endif
		}
		
		#endregion
	}
}