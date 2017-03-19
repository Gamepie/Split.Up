using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Initialises the Twitter SDK with the credentials set in NPSettings.")]
	public class TwitterInitialise : FsmStateAction 
	{
		#region FSM Methods
		
		public override void OnEnter() 
		{
#if USES_TWITTER
			NPBinding.Twitter.Initialise();
#endif

			Finish();
		}
		
		#endregion
	}
}