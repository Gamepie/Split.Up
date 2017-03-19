using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Initialises the cloud services component. You need to call this method, before using any features.")]
	public class CloudServicesInitialise : FsmStateAction 
	{
		#region FSM Methods
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			NPBinding.CloudServices.Initialise();
#endif

			Finish();
		}
		
		#endregion
	}
}