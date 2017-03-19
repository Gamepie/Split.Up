using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sets a string value for the specified key in the cloud data store.")]
	public class CloudServicesSetString : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("Case sensitive key under which to store the value.")]
		public	FsmString	key;
		[RequiredField]
		[Tooltip("The string value to store.")]
		public	FsmString	stringValue;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			key			= null;
			stringValue	= null;
		}
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			NPBinding.CloudServices.SetString(key.Value, stringValue.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}