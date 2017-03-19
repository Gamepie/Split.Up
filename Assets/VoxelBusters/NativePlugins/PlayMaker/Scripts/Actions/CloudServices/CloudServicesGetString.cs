using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the string value associated with specified key.")]
	public class CloudServicesGetString : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("A string used to uniquely identify the value stored in the cloud data store.")]
		public	FsmString	key;

		[ActionSection("Results")]
		
		[Tooltip("The string value associated with specified key.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	stringValue;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			key			= null;
			
			// Results properties
			stringValue	= new FsmString {
				UseVariable = true
			};
		}
		
		public override void OnEnter ()
		{
#if USES_CLOUD_SERVICES
			stringValue.Value	= NPBinding.CloudServices.GetString(key.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}