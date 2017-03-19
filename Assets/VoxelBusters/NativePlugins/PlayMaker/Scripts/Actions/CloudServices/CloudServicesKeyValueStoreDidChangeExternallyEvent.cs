using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Registers to the event and notifies you when the value of one or more keys in the local key-value store changed due to incoming data pushed from cloud.")]
	public class CloudServicesKeyValueStoreDidChangeExternallyEvent : FsmStateAction 
	{
		#region Fields

		[ActionSection("Results")]
		
		[Tooltip("The count of keys whose values got changed.")]
		[UIHint(UIHint.Variable)]
		public	FsmInt		changedKeysCount;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when another instance of your app using same cloud service account, uploads a new value.")]
		public	FsmEvent	serverEvent;
		[Tooltip("Event to send when an attempt to write to key-value storage was discarded because an initial download from cloud server has not yet happened.")]
		public	FsmEvent	initialSyncEvent;
		[Tooltip("Event to send when your app’s key-value store has exceeded its space quota on the cloud server.")]
		public	FsmEvent	quotaViolationEvent; 
		[Tooltip("Event to send when user has changed the cloud service account. The keys and values in the local key-value store have been replaced with those from the new account.")]
		public	FsmEvent	storeAccountEvent; 

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			changedKeysCount	= new FsmInt {
				UseVariable = true
			};

			// Event properties
			serverEvent			= null;
			initialSyncEvent	= null;
			quotaViolationEvent	= null;
			storeAccountEvent	= null;
		}
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			CloudServices.KeyValueStoreDidChangeExternallyEvent	+= OnKeyValueStoreDidChangeExternally;
#endif
		}
		
		#endregion

		#region Callback Methods

#if USES_CLOUD_SERVICES
		private void OnKeyValueStoreDidChangeExternally (eCloudDataStoreValueChangeReason _reason, string[] _changedKeys)
		{
			// Update properties
			changedKeysCount				= _changedKeys.Length;
			CloudServicesUtils.changedKeys	= _changedKeys;

			// Send event
			switch (_reason)
			{
			case eCloudDataStoreValueChangeReason.SERVER:
				Fsm.Event(serverEvent);
				break;

			case eCloudDataStoreValueChangeReason.INITIAL_SYNC:
				Fsm.Event(initialSyncEvent);
				break;

			case eCloudDataStoreValueChangeReason.QUOTA_VIOLATION:
				Fsm.Event(quotaViolationEvent);
				break;
				
			case eCloudDataStoreValueChangeReason.STORE_ACCOUNT:
				Fsm.Event(storeAccountEvent);
				break;
			}
		}
#endif

		#endregion
	}
}