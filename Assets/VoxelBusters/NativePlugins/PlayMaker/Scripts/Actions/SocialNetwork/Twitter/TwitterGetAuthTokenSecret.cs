using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the authorization token secret of the current user session.")]
	public class TwitterGetAuthTokenSecret : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Results")]
		
		[UIHint(UIHint.Variable)]
		[Tooltip("The authentication token secret.")]
		public		FsmString 	authTokenSecret;
		
		#endregion
		
		#region Methods
		
		public override void Reset ()
		{
			// Results properties
			authTokenSecret	= new FsmString {
				UseVariable	= true
			};
		}
		
		public override void OnEnter () 
		{
#if USES_TWITTER
			authTokenSecret.Value = NPBinding.Twitter.GetAuthTokenSecret();
#endif

			Finish();
		}
		
		#endregion
	}
}