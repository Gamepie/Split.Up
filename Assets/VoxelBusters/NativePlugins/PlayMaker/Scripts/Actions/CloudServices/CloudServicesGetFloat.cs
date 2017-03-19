using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the float value associated with specified key.")]
	public class CloudServicesGetFloat : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("A string used to uniquely identify the value stored in the cloud data store.")]
		public	FsmString	key;

		[ActionSection("Results")]
		
		[Tooltip("The float value associated with specified key.")]
		[UIHint(UIHint.Variable)]
		public	FsmFloat	floatValue;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			key			= null;
			
			// Results properties
			floatValue	= new FsmFloat {
				UseVariable = true
			};
		}
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			floatValue.Value	= (float)NPBinding.CloudServices.GetDouble(key.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}