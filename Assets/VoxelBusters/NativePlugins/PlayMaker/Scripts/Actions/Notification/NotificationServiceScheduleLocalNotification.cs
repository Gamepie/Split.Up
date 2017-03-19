using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.NativePlugins.Internal;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Schedules a local notification for delivery at specified date and time.")]
	public class NotificationServiceScheduleLocalNotification : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[Tooltip("The message displayed in the notification alert.")]
		public 	FsmString 	alertBody;
		[Tooltip("Starting from now, after how many minutes the system should deliver the notification.")]
		public 	FsmInt	 	fireAfterMinutes;
		[Tooltip("The interval at which the notification has to be rescheduled.")]
		public 	eNotificationRepeatInterval repeatInterval;
		[Tooltip("The name of the sound file to play when an alert is displayed.")]
		public 	FsmString 	soundName;
		[Tooltip("The title of the action button or slider. This property is valid only on iOS.")]
		public 	FsmString 	alertAction;
		[Tooltip("Indicates whether the alert action is visible or not. This property is valid only on iOS.")]
		public 	FsmBool	 	hasAction;
		[Tooltip("The number to display as the application's icon badge. This property is valid only on iOS.")]
		public 	FsmInt	 	badgeCount;
		[Tooltip("The image used as the launch image when the user taps the action button. This property is valid only on iOS.")]
		public 	FsmString 	launchImage;
		[Tooltip("The first line of text in the notification. This property is valid only on Android.")]
		public 	FsmString 	contentTitle;
		[Tooltip("The ticker text. This property is valid only on Android.")]
		public 	FsmString 	tickerText;
		[Tooltip("The tag of the notification. This property is valid only on Android.")]
		public 	FsmString 	tag;
		[Tooltip("The image used as the large icon for notification. This property is valid only on Android.")]
		public 	FsmString 	largeIcon;
		
		[ActionSection("Results")]
		
		[Tooltip("A string used to uniquely identify the scheduled notification.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	notificationID;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			alertBody			= null;
			fireAfterMinutes	= 1;
			repeatInterval		= eNotificationRepeatInterval.NONE;
			soundName			= null;
			alertAction			= null;
			hasAction			= false;
			badgeCount			= 0;
			launchImage			= null;
			contentTitle		= null;
			tickerText			= null;
			tag					= null;
			largeIcon			= null;

			// Results properties
			notificationID		= new FsmString {
				UseVariable	= true
			};
		}

		public override void OnEnter ()
		{
#if USES_NOTIFICATION_SERVICE
			CrossPlatformNotification	_notification	= new CrossPlatformNotification();

			if (!alertBody.IsNone)
				_notification.AlertBody		= alertBody.Value;

			_notification.FireDate			= System.DateTime.Now.AddMinutes(fireAfterMinutes.Value);
			_notification.RepeatInterval	= repeatInterval;

			if (!soundName.IsNone)
				_notification.SoundName		= soundName.Value;

			// Set iOS specific notification properties
			CrossPlatformNotification.iOSSpecificProperties _iosSpecificProperties	= new CrossPlatformNotification.iOSSpecificProperties();

			if (!alertAction.IsNone)
				_iosSpecificProperties.AlertAction	= alertAction.Value;

			if (!hasAction.IsNone)
				_iosSpecificProperties.HasAction	= hasAction.Value;
			
			if (!badgeCount.IsNone)
				_iosSpecificProperties.BadgeCount	= badgeCount.Value;
			
			if (!launchImage.IsNone)
				_iosSpecificProperties.LaunchImage	= launchImage.Value;

			// Set android specific notification properties
			CrossPlatformNotification.AndroidSpecificProperties _androidSpecificProperties	= new CrossPlatformNotification.AndroidSpecificProperties();

			if (!contentTitle.IsNone)
				_androidSpecificProperties.ContentTitle	= contentTitle.Value;
			
			if (!tickerText.IsNone)
				_androidSpecificProperties.TickerText	= tickerText.Value;
			
			if (!tag.IsNone)
				_androidSpecificProperties.Tag			= tag.Value;
			
			if (!largeIcon.IsNone)
				_androidSpecificProperties.LargeIcon	= largeIcon.Value;

			// Set platform specific properties
			_notification.iOSProperties			= _iosSpecificProperties;
			_notification.AndroidProperties		= _androidSpecificProperties;

			// Schedules a new notification
			notificationID.Value				= NPBinding.NotificationService.ScheduleLocalNotification(_notification);
#endif

			Finish();
		}
		
		#endregion
	}
}