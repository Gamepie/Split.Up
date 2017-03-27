using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Presents a view to share contents on WhatsApp.")]
	public class SharingShowWhatsAppShareComposer : FsmStateAction 
	{
		public enum eShareOption
		{
			ShareText,
			ShareScreenshot,
			ShareImage,
			ShareImageAtPath
		}

		#region Fields

		[ActionSection("Setup")]

		[Tooltip("Choose the option to share content.")]
		public		eShareOption	shareOption;
		[Tooltip("The text to be shared.")]
		public 		FsmString 		text;
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
			shareOption	= eShareOption.ShareText;
			text		= null;
			image		= null;
			imagePath	= null;

			// Events properties
			closedEvent	= null;
		}
		
		public override void OnEnter() 
		{
#if USES_SHARING
			// Create composer
			WhatsAppShareComposer 	_composer	= new WhatsAppShareComposer();

			switch (shareOption)
			{
			case eShareOption.ShareText:
				_composer.Text	= text.Value;
				break;
				
			case eShareOption.ShareScreenshot:
				_composer.AttachScreenShot();
				break;
				
			case eShareOption.ShareImage:
				_composer.AttachImage((Texture2D)image.Value);
				break;
				
			case eShareOption.ShareImageAtPath:
				_composer.AttachImageAtPath(imagePath.Value);
				break;
				
			default:
				Log("[Sharing] Unhandled option.");
				break;
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