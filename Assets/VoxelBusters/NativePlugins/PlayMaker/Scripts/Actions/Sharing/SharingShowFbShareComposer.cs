using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Presents a view to compose and share Facebook post.")]
	public class SharingShowFbShareComposer : FsmStateAction 
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

		[Tooltip("The URL to be shared.")]
		public 		FsmString 		URL;
		[Tooltip("Choose the attachment you want to add.")]
		public		eAttachmentOption	attachmentOption;
		[Tooltip("The image to be shared.")]
		public 		FsmTexture	 	image;
		[Tooltip("The absolute path of the image to be shared.")]
		public		FsmString		imagePath;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when view is closed.")]
		public 		FsmEvent 		closedEvent;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			attachmentOption	= eAttachmentOption.None;
			URL					= null;
			image				= null;
			imagePath			= null;
			
			// Events properties
			closedEvent		= null;
		}
		
		public override void OnEnter() 
		{
#if USES_SHARING
			// Create composer
			FBShareComposer _composer	= new FBShareComposer();

			if (!URL.IsNone)
				_composer.URL	= URL.Value;

			// Add attachment
			if (attachmentOption != eAttachmentOption.None)
			{
				switch (attachmentOption)
				{
				case eAttachmentOption.AttachScreenshot:
					_composer.AttachScreenShot();
					break;
					
				case eAttachmentOption.AttachImage:
					_composer.AttachImage((Texture2D)image.Value);
					break;
					
				case eAttachmentOption.AttachImageAtPath:
					_composer.AttachImageAtPath(imagePath.Value);
					break;
					
				default:
					Log("[Sharing] Unhandled option.");
					break;
				}
			}
			
			// Show share view
			NPBinding.Sharing.ShowView(_composer, FinishedSharing);	
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