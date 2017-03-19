using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Presents a view that offers various share services that your app user can choose to share.")]
	public class SharingShowShareSheet : FsmStateAction 
	{
		public enum eAttachmentOption
		{
			None,
			AttachScreenshot,
			AttachImage,
			AttachImageAtPath
		}
		
		#region Fields
		
		[ActionSection("Setup")]

		[Tooltip("The initial text to be shared.")]
		public 		FsmString 		text;
		[Tooltip("The URL to be shared.")]
		public 		FsmString 		URL;
		[Tooltip("Choose the attachment you want to add.")]
		public		eAttachmentOption	attachmentOption;
		[Tooltip("The image to be shared.")]
		public 		FsmTexture	 	image;
		[Tooltip("The absolute path of the image to be shared.")]
		public		FsmString		imagePath;
		[Tooltip("The share services to be excluded from the view.")]
		public 		eShareOptions[]	excludedOptions	= new eShareOptions[0];
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when view is closed.")]
		public 		FsmEvent 		closedEvent;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			text				= null;
			URL					= null;
			attachmentOption	= eAttachmentOption.None;
			image				= null;
			imagePath			= null;
			excludedOptions		= new eShareOptions[0];
			
			// Events properties
			closedEvent			= null;
		}
		
		public override void OnEnter() 
		{
#if USES_SHARING
			// Create composer
			ShareSheet	_shareSheet	= new ShareSheet();	

			if (!text.IsNone)
				_shareSheet.Text	= text.Value;
			
			if (!URL.IsNone)
				_shareSheet.URL		= URL.Value;

			if (excludedOptions.Length > 0)
				_shareSheet.ExcludedShareOptions	= excludedOptions;

			// Add attachment
			if (attachmentOption != eAttachmentOption.None)
			{
				switch (attachmentOption)
				{
				case eAttachmentOption.AttachScreenshot:
					_shareSheet.AttachScreenShot();
					break;
					
				case eAttachmentOption.AttachImage:
					_shareSheet.AttachImage((Texture2D)image.Value);
					break;
					
				case eAttachmentOption.AttachImageAtPath:
					_shareSheet.AttachImageAtPath(imagePath.Value);
					break;
					
				default:
					Log("[Sharing] Unhandled option.");
					break;
				}
			}
			
			// Show share view
			NPBinding.Sharing.ShowView(_shareSheet, FinishedSharing);	
#endif
		}
		
		#endregion
		
		#region Callback Methods
		
		private void FinishedSharing (eShareResult _result)
		{
			// Send event
			Fsm.Event(closedEvent);
			Finish();
		}
		
		#endregion
	}
}