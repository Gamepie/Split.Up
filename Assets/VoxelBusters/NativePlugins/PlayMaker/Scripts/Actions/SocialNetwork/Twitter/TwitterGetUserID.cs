using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the user ID associated with the access token.")]
	public class TwitterGetUserID : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Results")]
		
		[UIHint(UIHint.Variable)]
		[Tooltip("The user ID associated with the access token.")]
		public		FsmString 	userID;
		
		#endregion
		
		#region Methods

		public override void Reset ()
		{
			// Results properties
			userID	= new FsmString {
				UseVariable	= true
			};
		}
		
		public override void OnEnter () 
		{
#if USES_TWITTER
			userID.Value = NPBinding.Twitter.GetUserID();
#endif

			Finish();
		}
		
		#endregion
	}
}