using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Determines whether the built-in camera is supported.")]
	public class MediaLibraryIsCameraSupported : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Results")]
		
		[Tooltip("Indicates whether the built-in camera is supported.")]
		[UIHint(UIHint.Variable)]
		public 	FsmBool 	isSupported;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send if built-in camera is supported.")]
		public	FsmEvent	supportedEvent;
		[Tooltip("Event to send if built-in camera is not supported.")]
		public	FsmEvent	notSupportedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			isSupported		= new FsmBool {
				UseVariable	= true
			};

			// Events properties
			supportedEvent		= null;
			notSupportedEvent	= null;
		}
		
		public override void OnEnter () 
		{
#if USES_MEDIA_LIBRARY
			isSupported.Value	= NPBinding.MediaLibrary.IsCameraSupported();
			
			Fsm.Event(isSupported.Value ? supportedEvent : notSupportedEvent);
#endif

			Finish();
		}
		
		#endregion
	}
}