using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Determines whether user is currently logged into Twitter.")]
	public class TwitterIsLoggedIn : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Results")]
		
		[Tooltip("The current login status.")]
		[UIHint(UIHint.Variable)]
		public 	FsmBool 	isLoggedIn;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send if user is logged into Twitter.")]
		public	FsmEvent	loggedInEvent;
		[Tooltip("Event to send if user is not logged into Twitter.")]
		public	FsmEvent	notLoggedInEvent;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Results properties
			isLoggedIn			= new FsmBool {
				UseVariable	= true
			};

			// Events properties
			loggedInEvent		= null;
			notLoggedInEvent	= null;
		}
		
		public override void OnEnter () 
		{
#if USES_TWITTER
			isLoggedIn.Value	= NPBinding.Twitter.IsLoggedIn();
			
			Fsm.Event(isLoggedIn.Value ? loggedInEvent : notLoggedInEvent);
#endif

			Finish();
		}
		
		#endregion
	}
}