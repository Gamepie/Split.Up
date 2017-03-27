using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sets a float value for the specified key in the cloud data store.")]
	public class CloudServicesSetFloat : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("Case sensitive key under which to store the value.")]
		public	FsmString	key;
		[RequiredField]
		[Tooltip("The float value to store.")]
		public	FsmFloat	floatValue;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			key			= null;
			floatValue	= null;
		}
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			NPBinding.CloudServices.SetDouble(key.Value, floatValue.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}