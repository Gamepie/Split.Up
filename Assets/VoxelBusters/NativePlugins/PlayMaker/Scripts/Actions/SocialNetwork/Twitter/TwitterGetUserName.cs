using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the username associated with the access token.")]
	public class TwitterGetUserName : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Result")]
		
		[UIHint(UIHint.Variable)]
		[Tooltip("The username associated with the access token.")]
		public		FsmString 	userName;
		
		#endregion
		
		#region Methods
		
		public override void Reset ()
		{
			// Results properties
			userName	= new FsmString {
				UseVariable	= true
			};
		}
		
		public override void OnEnter () 
		{
#if USES_TWITTER
			userName.Value = NPBinding.Twitter.GetUserName();
#endif

			Finish();
		}
		
		#endregion
	}
}