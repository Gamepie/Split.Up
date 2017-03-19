using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Deletes the Twitter user session from this app.")]
	public class TwitterLogout : FsmStateAction 
	{
		#region FSM Methods

		public override void OnEnter ()
		{
#if USES_TWITTER
			NPBinding.Twitter.Logout();
#endif

			Finish();
		}
		
		#endregion
	}
}