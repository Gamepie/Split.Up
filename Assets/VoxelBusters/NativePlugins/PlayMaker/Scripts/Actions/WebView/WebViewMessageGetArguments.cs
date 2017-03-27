using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.NativePlugins.Internal;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the argument's key-value from the last message and stores it in variables.")]
	public class WebViewMessageGetArguments : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]

		[Tooltip("The index to retrieve the arguments from.")]
		public 	FsmInt	 	atIndex;

		[ActionSection("Results")]
		
		[Tooltip("The key available at specified index.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	argumentKey;
		[Tooltip("The value corresponding to the key.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	argumentValue;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when action fails (likely index is out of range exception).")]
		public 	FsmEvent 	failedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			atIndex				= 0;

			// Results properties
			argumentKey			= new FsmString {
				UseVariable		= true
			};
			argumentValue		= new FsmString {
				UseVariable		= true
			};

			// Event properties
			failedEvent			= null;
		}
		
		public override void OnEnter ()
		{
			DoAction();
		}

		#endregion

		#region Methods

		private void DoAction()
		{
#if USES_WEBVIEW
			try
			{
				WebViewMessage	_message	= WebViewUtils.message;

				if (_message == null || _message.Arguments == null)
				{
					OnActionDidFail();
					return;
				}

				// Fetch the value
				int 	_iter	= 0;

				foreach (KeyValuePair<string, string> _pair in _message.Arguments)
				{
					if (_iter == atIndex.Value)
					{
						// Update the properties
						argumentKey.Value	= _pair.Key;
						argumentValue.Value	= _pair.Value;
						
						// Invoke handler
						OnActionDidFinish();

						return;
					}

					// Increment index
					_iter++;
				}

				// Item is not found at given index
				OnActionDidFail();

				return;
			}
			catch (System.Exception _exception)
			{
				Debug.Log(_exception.Message);
				
				OnActionDidFail();
				
				return;
			}
#endif
		}

		private void OnActionDidFinish ()
		{
			Finish();
		}

		private void OnActionDidFail ()
		{
			Fsm.Event(failedEvent);
			Finish();
		}

		#endregion
	}
}