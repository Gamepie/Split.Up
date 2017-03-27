using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sets a Boolean value for the specified key in the cloud data store.")]
	public class CloudServicesSetBool : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("Case sensitive key under which to store the value.")]
		public	FsmString	key;
		[RequiredField]
		[Tooltip("The Boolean value to store.")]
		public	FsmBool		boolValue;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			key			= null;
			boolValue	= null;
		}
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			NPBinding.CloudServices.SetBool(key.Value, boolValue.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}