using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the bundle identifier of this application.")]
	public class UtilityGetBundleIdentifier : FsmStateAction 
	{
		#region Fields

		[ActionSection("Result")]

		[UIHint(UIHint.Variable)]
		[Tooltip("The string identifies your application to the system.")]
		public		FsmString 	bundleIdentifier;

		#endregion

		#region Methods
		
		public override void Reset ()
		{
			// Results properties
			bundleIdentifier	= new FsmString {
				UseVariable	= true
			};
		}

		public override void OnEnter () 
		{
			bundleIdentifier.Value = NPBinding.Utility.GetBundleIdentifier();

			Finish();
		}
		
		#endregion
	}
}