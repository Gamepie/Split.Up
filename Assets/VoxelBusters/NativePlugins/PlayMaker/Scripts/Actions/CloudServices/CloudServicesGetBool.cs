using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the Boolean value associated with specified key.")]
	public class CloudServicesGetBool : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("A string used to uniquely identify the value stored in the cloud data store.")]
		public	FsmString	key;

		[ActionSection("Results")]
		
		[Tooltip("The Boolean value associated with specified key.")]
		[UIHint(UIHint.Variable)]
		public	FsmBool		boolValue;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			key			= null;

			// Results properties
			boolValue	= new FsmBool {
				UseVariable = true
			};
		}
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			boolValue.Value	= NPBinding.CloudServices.GetBool(key.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}