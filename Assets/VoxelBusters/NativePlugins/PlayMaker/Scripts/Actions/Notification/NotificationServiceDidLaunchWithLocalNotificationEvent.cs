using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Registers to the event and notifies you when your app was launched as a result of local notification.")]
	public class NotificationServiceDidLaunchWithLocalNotificationEvent : NotificationServiceReceivedNotificationEventBase 
	{
		#region FSM Methods

		public override void OnEnter ()
		{
			base.OnEnter();

#if USES_NOTIFICATION_SERVICE
			NotificationService.DidLaunchWithLocalNotificationEvent += OnNotificationReceived;
#endif
		}
		
		#endregion
	}
}