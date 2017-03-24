using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Explicitly synchronizes in-memory data with those stored on disk.")]
	public class CloudServicesSynchronise : FsmStateAction 
	{
		#region FSM Methods
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			NPBinding.CloudServices.Synchronise();
#endif

			Finish();
		}
		
		#endregion
	}
}