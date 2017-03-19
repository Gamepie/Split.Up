using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Set the badge of the app icon.")]
	public class UtilitySetApplicationBadgeCount : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The value to be set as the badge of the app icon.")]
		public		FsmInt 		badgeCount;
		
		#endregion
		
		#region Methods

		public override void Reset ()
		{
			// Setup properties
			badgeCount		= 0;
		}
		
		public override void OnEnter () 
		{
			NPBinding.Utility.SetApplicationIconBadgeNumber(badgeCount.Value);
			
			Finish();
		}
		
		#endregion
	}
}