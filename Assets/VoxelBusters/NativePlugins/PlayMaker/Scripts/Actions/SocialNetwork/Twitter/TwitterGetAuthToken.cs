using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the authorization token of the current user session.")]
	public class TwitterGetAuthToken : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Results")]
		
		[UIHint(UIHint.Variable)]
		[Tooltip("The authentication token.")]
		public		FsmString 	authToken;
		
		#endregion
		
		#region Methods
		
		public override void Reset ()
		{
			// Results properties
			authToken	= new FsmString {
				UseVariable	= true
			};
		}
		
		public override void OnEnter () 
		{
#if USES_TWITTER
			authToken.Value = NPBinding.Twitter.GetAuthToken();
#endif

			Finish();
		}
		
		#endregion
	}
}