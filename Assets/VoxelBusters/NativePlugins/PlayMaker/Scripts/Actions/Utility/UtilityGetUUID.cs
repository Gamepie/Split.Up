using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the unique identifier.")]
	public class UtilityGetUUID : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Result")]

		[UIHint(UIHint.Variable)]
		[Tooltip("The string holds the generated unique identifier value.")]
		public	FsmString 	UUID;
		
		#endregion
		
		#region Methods
		
		public override void Reset ()
		{
			// Results properties
			UUID	= new FsmString {
				UseVariable	= true
			};
		}
		
		public override void OnEnter () 
		{
			UUID.Value = NPBinding.Utility.GetUUID();
			
			Finish();
		}
		
		#endregion
	}
}