using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sets the web view frame to desired position and size.")]
	public class WebViewSetFrame : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]

		[RequiredField]
#if USES_WEBVIEW
		[CheckForComponent(typeof(WebView))]
#endif
		[Tooltip("The gameObject with the web view component.")]
		public	FsmOwnerDefault	gameObject;
		[Tooltip("The new position and size for web view.")]
		public	FsmRect	 		frame;
		[Tooltip("Use normalized screen coordinates (0-1)")]
		public	FsmBool	 		normalized;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			gameObject	= null;
			frame		= new Rect(0f, 0f, 1f, 1f);
			normalized	= true;
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
#if USES_WEBVIEW
			GameObject	_webViewGO	= Fsm.GetOwnerDefaultTarget(gameObject);
			
			if (_webViewGO == null)
			{
				LogWarning(string.Format("[WebView] Game object is null."));
				return;
			}
			
			WebView		_webView	= _webViewGO.GetComponent<WebView>();
			
			if (_webView == null)
			{
				LogWarning(string.Format("[WebView] WebView component not found in game object: {0}.", _webViewGO.name));
				return;
			}
			
			// Update webview property value
			Rect		_newRect	= frame.Value;

			if (normalized.Value)
			{
				_newRect.x			= frame.Value.x * Screen.width;
				_newRect.y			= frame.Value.y * Screen.height;
				_newRect.width		= frame.Value.width * Screen.width;
				_newRect.height		= frame.Value.height * Screen.height;
			}

			_webView.Frame			= _newRect;
#endif
		}

		#endregion
	}
}