using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Loads the webpage contents from specified file.")]
	public class WebViewLoadFile : FsmStateAction 
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
		[Tooltip("Path of the target file.")]
		public	FsmString		filePath;
		[Tooltip("The MIME type of the content.")]
		public	FsmString		mimeType;
		[Tooltip("The content's character encoding name.")]
		public	FsmString		textEncoding;
		[Tooltip("The base URL for the content.")]
		public	FsmString		baseURL;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			gameObject		= null;
			filePath		= null;
			mimeType		= null;
			textEncoding	= null;
			baseURL			= null;
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
			
			_webView.LoadFile(filePath.Value, mimeType.Value, textEncoding.Value, baseURL.Value);
#endif
		}
		
		#endregion
	}
}