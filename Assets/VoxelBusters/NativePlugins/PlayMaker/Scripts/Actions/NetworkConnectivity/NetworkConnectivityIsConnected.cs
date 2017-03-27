using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Determines whether system is currently connected to the network.")]
	public class NetworkConnectivityIsConnected : FsmStateAction 
	{
		#region Fields
				
		[ActionSection("Results")]

		[Tooltip("The current connectivity status.")]
		[UIHint(UIHint.Variable)]
		public 	FsmBool 	isConnected;

		[ActionSection("Events")]

		[Tooltip("Event to send if system is connected to the network.")]
		public	FsmEvent	connectedEvent;
		[Tooltip("Event to send if system is not connected to the network.")]
		public	FsmEvent	notConnectedEvent;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			isConnected		= new FsmBool {
				UseVariable	= true
			};
			
			// Events properties
			connectedEvent		= null;
			notConnectedEvent	= null;
		}
		
		public override void OnEnter () 
		{
#if USES_NETWORK_CONNECTIVITY
			isConnected.Value	= NPBinding.NetworkConnectivity.IsConnected;

			Fsm.Event(isConnected.Value ? connectedEvent : notConnectedEvent);
#endif

			Finish();
		}

		#endregion
	}
}