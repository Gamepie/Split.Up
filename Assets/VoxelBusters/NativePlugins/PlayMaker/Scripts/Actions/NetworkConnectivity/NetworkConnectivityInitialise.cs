using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Initialises the network connectivity component. You need to call this method, to start checking if IP Address specified in the Network Connectivity Settings is reachable or not.")]
	public class NetworkConnectivityInitialise : FsmStateAction
	{
		#region FSM Methods

		public override void OnEnter() 
		{
#if USES_NETWORK_CONNECTIVITY
			NPBinding.NetworkConnectivity.Initialise();
#endif

			Finish();
		}

		#endregion
	}
}