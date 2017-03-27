using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Cancels the delivery of the specified scheduled local notification.")]
	public class NotificationServiceCancelLocalNotification : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("A string used to uniquely identify the notification")]
		public	FsmString	notificationID;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			notificationID	= null;
		}

		public override void OnEnter ()
		{
#if USES_NOTIFICATION_SERVICE
			NPBinding.NotificationService.CancelLocalNotification(notificationID.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}