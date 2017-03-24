using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the name of the key whose value got changed externally.")]
	public class CloudServicesGetChangedExternallyKey : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[Tooltip("The index to retrieve the changed key from.")]
		public	FsmInt		atIndex;

		[ActionSection("Results")]
		
		[Tooltip("The name of the key whose value was changed.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	changedKey;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when action fails (likely index is out of range exception).")]
		public 	FsmEvent 	failedEvent;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			atIndex		= 0;
			
			// Results properties
			changedKey	= new FsmString {
				UseVariable = true
			};

			// Event properties
			failedEvent	= null;
		}
		
		public override void OnEnter ()
		{
			DoAction();

			Finish();
		}
		
		#endregion

		#region Methods

		private void DoAction ()
		{
			try
			{
				changedKey.Value	= CloudServicesUtils.changedKeys[atIndex.Value];
			}
			catch (System.Exception _exception)
			{
				Debug.Log(_exception.Message);

				Fsm.Event(failedEvent);

				return;
			}
		}

		#endregion
	}
}