using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Registers to the event and notifies you when network connectivity status changes.")]
	public class NetworkConnectivityStateChangedEvent : FsmStateAction 
	{
		#region Fields

		[ActionSection("Events")]

		[Tooltip("Event to send when system connects to the network.")]
		public	FsmEvent	connectedEvent;
		[Tooltip("Event to send when network connectivity is lost.")]
		public	FsmEvent	disconnectedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Event properties
			connectedEvent		= null;
			disconnectedEvent	= null;
		}
		
		public override void OnEnter () 
		{
#if USES_NETWORK_CONNECTIVITY
			// Register event
			NetworkConnectivity.NetworkConnectivityChangedEvent += OnNetworkConnectivityChanged;
#endif
		}
		
		#endregion
		
		#region Callback Methods
		
		private void OnNetworkConnectivityChanged (bool _isConnected)
		{
			Fsm.Event(_isConnected ? connectedEvent : disconnectedEvent);
		}
		
		#endregion
	}
}