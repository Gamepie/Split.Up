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
	[Tooltip("Registers your preferred options for notifying the user, on receiving a new notification.")]
	public class NotificationServiceRegisterNotificationTypes : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[Tooltip("The notification types that your app uses to alert user.")]
		[EnumMaskField(typeof(NotificationType))]
		public	NotificationType	types;

		#endregion

		#region FSM Methods

		public override void OnEnter ()
		{
#if USES_NOTIFICATION_SERVICE
			NPBinding.NotificationService.RegisterNotificationTypes(types);
#endif

			Finish();
		}
		
		#endregion
	}
}