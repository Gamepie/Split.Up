using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.NativePlugins.Internal;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	public abstract class NotificationServiceReceivedNotificationEventBase : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("Defines the text representation of date and time.")]
		public 	FsmString 	dateTimeFormat;

		[ActionSection("Results")]
		
		[Tooltip("The message displayed in the notification alert.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	alertBody;
		[Tooltip("The date and time when the system should deliver the notification.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	fireDate;
		[Tooltip("The name of the sound file to play when an alert is displayed.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	soundName;
		[Tooltip("A string used to uniquely identify the notification.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	notificationID;
		[Tooltip("The title of the action button or slider. This property is valid only on iOS.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	alertAction;
		[Tooltip("Indicates whether the alert action is visible or not. This property is valid only on iOS.")]
		[UIHint(UIHint.Variable)]
		public 	FsmBool	 	hasAction;
		[Tooltip("The number to display as the application's icon badge. This property is valid only on iOS.")]
		[UIHint(UIHint.Variable)]
		public 	FsmInt	 	badgeCount;
		[Tooltip("The image used as the launch image when the user taps the action button. This property is valid only on iOS.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	launchImage;
		[Tooltip("The first line of text in the notification. This property is valid only on Android.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	contentTitle;
		[Tooltip("The ticker text. This property is valid only on Android.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	tickerText;
		[Tooltip("The tag of the notification. This property is valid only on Android.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	tag;
		[Tooltip("The image used as the large icon for notification. This property is valid only on Android.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	largeIcon;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when notification is received.")]
		public 	FsmEvent 	receivedEvent;

		#endregion

		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			dateTimeFormat	= Constants.kPlayMakerDateTimeFormat;

			// Results properties
			alertBody		= new FsmString {
				UseVariable	= true
			};
			fireDate		= new FsmString {
				UseVariable	= true
			};
			soundName		= new FsmString {
				UseVariable	= true
			};
			notificationID	= new FsmString {
				UseVariable	= true
			};
			alertAction		= new FsmString {
				UseVariable	= true
			};
			hasAction		= new FsmBool {
				UseVariable	= true
			};
			badgeCount		= new FsmInt {
				UseVariable	= true
			};
			launchImage		= new FsmString {
				UseVariable	= true
			};
			contentTitle	= new FsmString {
				UseVariable	= true
			};
			tickerText		= new FsmString {
				UseVariable	= true
			};
			tag				= new FsmString {
				UseVariable	= true
			};
			largeIcon		= new FsmString {
				UseVariable	= true
			};
			
			// Events properties
			receivedEvent	= null;
		}

		public override void OnEnter ()
		{}

		public override void OnExit ()
		{}
		
		#endregion

		#region Callback Methods

#if USES_NOTIFICATION_SERVICE
		protected void OnNotificationReceived (CrossPlatformNotification _notification)
		{
			// Update properties
			alertBody.Value			= _notification.AlertBody;
			fireDate.Value			= _notification.FireDate.ToString(dateTimeFormat.Value);
			soundName.Value			= _notification.SoundName;
			notificationID.Value	= _notification.GetNotificationID();

			// Update iOS properties
			CrossPlatformNotification.iOSSpecificProperties	_iOSProperties	= _notification.iOSProperties;

			if (_iOSProperties != null)
			{
				alertAction.Value	= _iOSProperties.AlertAction;
				hasAction.Value		= _iOSProperties.HasAction;
				badgeCount.Value	= _iOSProperties.BadgeCount;
				launchImage.Value	= _iOSProperties.LaunchImage;
			}

			CrossPlatformNotification.AndroidSpecificProperties	_androidProperties	= _notification.AndroidProperties;

			if (_androidProperties != null)
			{
				contentTitle.Value	= _androidProperties.ContentTitle;
				tickerText.Value	= _androidProperties.TickerText;
				tag.Value			= _androidProperties.Tag;
				largeIcon.Value		= _androidProperties.LargeIcon;
			}

			Fsm.Event(receivedEvent);
		}
#endif

		#endregion
	}
}