using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sends a request to access current Twitter user account details.")]
	public class TwitterRequestAccountDetails : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Result")]
		
		[UIHint(UIHint.Variable)]
		[Tooltip("The ID of the Twitter User.")]
		public	FsmString 	userID;
		[UIHint(UIHint.Variable)]
		[Tooltip("The user’s name as it appears on their profile.")]
		public	FsmString 	name;
		[UIHint(UIHint.Variable)]
		[Tooltip("Indicates whether the user has been verified by Twitter.")]
		public	FsmBool 	isVerified;
		[UIHint(UIHint.Variable)]
		[Tooltip("Indicates whether the user is protected.")]
		public	FsmBool 	isProtected;
		[UIHint(UIHint.Variable)]
		[Tooltip("The URL of the user’s profile image.")]
		public	FsmString 	profileImageURL;
		[UIHint(UIHint.Variable)]
		[Tooltip("The description of the problem that occurred.")]
		public	FsmString 	error;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when user account details are retrieved.")]
		public	FsmEvent	successEvent;
		[Tooltip("Event to send when request failed with error.")]
		public	FsmEvent	failedEvent;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Results properties
			userID			= new FsmString {
				UseVariable	= true
			};
			name			= new FsmString {
				UseVariable	= true
			};
			isVerified		= new FsmBool {
				UseVariable	= true
			};
			isProtected		= new FsmBool {
				UseVariable	= true
			};
			profileImageURL	= new FsmString {
				UseVariable	= true
			};
			error			= new FsmString {
				UseVariable	= true
			};
			
			// Events properties
			successEvent	= null;
			failedEvent		= null;
		}
		
		public override void OnEnter ()
		{
#if USES_TWITTER
			NPBinding.Twitter.RequestAccountDetails(DidReceiveAccountDetailsResponse);
#endif
		}
		
		#endregion

		#region Callback Methods

#if USES_TWITTER
		private void DidReceiveAccountDetailsResponse (TwitterUser _user, string _error)
		{
			if (string.IsNullOrEmpty(_error))
			{
				// Update properties
				userID.Value			= _user.UserID;
				name.Value				= _user.Name;
				isVerified.Value		= _user.IsVerified;
				isProtected.Value		= _user.IsProtected;
				profileImageURL.Value	= _user.ProfileImageURL;

				// Send event
				Fsm.Event(successEvent);
			}
			else
			{
				// Update properties
				error.Value				= _error;

				// Send event
				Fsm.Event(failedEvent);
			}		

			Finish();
		}
#endif
		
		#endregion
	}
}