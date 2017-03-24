using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Removes the value associated with the specified key from the cloud data store.")]
	public class CloudServicesRemoveKey : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("The key corresponding to the value you want to remove.")]
		public	FsmString	key;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			key		= null;
		}
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			NPBinding.CloudServices.RemoveKey(key.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}