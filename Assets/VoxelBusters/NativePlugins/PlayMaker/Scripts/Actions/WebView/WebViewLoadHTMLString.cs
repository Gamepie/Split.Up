using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Loads the webpage contents.")]
	public class WebViewLoadHTMLString : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]

		[RequiredField]
#if USES_WEBVIEW
		[CheckForComponent(typeof(WebView))]
#endif
		[Tooltip("The gameObject with the web view component.")]
		public	FsmOwnerDefault	gameObject;
		[RequiredField]
		[TextArea]
		[Tooltip("The contents of the webpage.")]
		public	FsmString		htmlString;
		[Tooltip("The base URL for the content.")]
		public	FsmString		baseURL;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			gameObject	= null;
			htmlString	= null;
			baseURL		= null;
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
			
			_webView.LoadHTMLString(htmlString.Value, baseURL.Value);
#endif
		}
		
		#endregion
	}
}