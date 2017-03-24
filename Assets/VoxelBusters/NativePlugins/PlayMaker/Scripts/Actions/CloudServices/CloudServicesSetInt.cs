using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sets an integer value for the specified key in the cloud data store.")]
	public class CloudServicesSetInt : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("Case sensitive key under which to store the value.")]
		public	FsmString	key;
		[RequiredField]
		[Tooltip("The integer value to store.")]
		public	FsmInt		intValue;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			key			= null;
			intValue	= null;
		}
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			NPBinding.CloudServices.SetLong(key.Value, intValue.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}