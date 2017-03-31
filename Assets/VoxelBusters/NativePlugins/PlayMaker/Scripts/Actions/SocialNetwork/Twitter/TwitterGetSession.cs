using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the session information of active user.")]
	public class TwitterGetSession : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Results")]
		
		[UIHint(UIHint.Variable)]
		[Tooltip("The authentication token.")]
		public		FsmString 	authToken;
		[UIHint(UIHint.Variable)]
		[Tooltip("The authentication token secret.")]
		public		FsmString 	authTokenSecret;
		[UIHint(UIHint.Variable)]
		[Tooltip("The ID of the Twitter User.")]
		public		FsmString 	userID;

		[ActionSection("Events")]

		[Tooltip("Event to send when saved session information is not found.")]
		public		FsmEvent	failedEvent;
		
		#endregion
		
		#region Methods
		
		public override void Reset ()
		{
			// Results properties
			authToken		= new FsmString {
				UseVariable	= true
			};
			authTokenSecret	= new FsmString {
				UseVariable	= true
			};
			userID			= new FsmString {
				UseVariable	= true
			};

			// Events properties
			failedEvent		= null;
		}
		
		public override void OnEnter () 
		{
#if USES_TWITTER
			TwitterAuthSession _session = NPBinding.Twitter.GetSession();
			if (_session != null)
			{
				authToken.Value			= _session.AuthToken;
				authTokenSecret.Value	= _session.AuthTokenSecret;
				userID.Value			= _session.UserID;
			}
			else
			{
				Fsm.Event(failedEvent);
			}
#endif

			Finish();
		}
		
		#endregion
	}
}