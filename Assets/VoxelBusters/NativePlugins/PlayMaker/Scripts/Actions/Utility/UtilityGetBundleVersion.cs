using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the bundle version of this application.")]
	public class UtilityGetBundleVersion : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Result")]

		[UIHint(UIHint.Variable)]
		[Tooltip("The string specifies the build version.")]
		public		FsmString 	bundleVersion;
		
		#endregion
		
		#region Methods
		
		public override void Reset ()
		{	
			// Results properties
			bundleVersion	= new FsmString {
				UseVariable	= true
			};
		}
		
		public override void OnEnter () 
		{
			bundleVersion.Value = NPBinding.Utility.GetBundleVersion();
			
			Finish();
		}
		
		#endregion
	}
}