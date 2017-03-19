using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Opens the store link of given application.")]
	public class UtilityOpenStoreLink : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The string specifies application identifier in App Store.")]
		public		FsmString 	iosAppID;
		[RequiredField]
		[Tooltip("The string specifies application identifier in Play Store.")]
		public		FsmString 	androidAppID;

		#endregion
		
		#region Methods

		public override void Reset ()
		{
			// Setup properties
			iosAppID		= null;
			androidAppID	= null;
		}
		
		public override void OnEnter () 
		{
			NPBinding.Utility.OpenStoreLink(PlatformID.IOS(iosAppID.Value), PlatformID.Android(androidAppID.Value));

			Finish();
		}
		
		#endregion
	}
}