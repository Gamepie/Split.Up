using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Presents a view to compose and send an email message.")]
	public class SharingShowMailShareComposer : FsmStateAction 
	{
		public enum eAttachmentOption
		{
			None,
			AttachScreenshot,
			AttachImage,
			AttachFileAtPath
		}
		
		#region Fields
		
		[ActionSection("Setup")]

		[Tooltip("The initial text for the subject line of the email.")]
		public 		FsmString 		subject;
		[Tooltip("The initial body text to include in the email.")]
		public 		FsmString		body;
		[Tooltip("Specify YES if the body parameter contains HTML content or specify NO if it contains plain text.")]
		public 		FsmBool			isHtmlBody;
		[Tooltip("The initial recipients to include in the email’s “To” field. This property can have multiple values joined using \';\' delimiter.")]
		public 		FsmString	 	toRecipients;
		[Tooltip("The initial recipients to include in the email’s “Cc” field. This property can have multiple values joined using \';\' delimiter.")]
		public 		FsmString 		ccRecipients;
		[Tooltip("The initial recipients to include in the email’s “Bcc” field. This property can have multiple values joined using \';\' delimiter.")]
		public 		FsmString	 	bccRecipients;
		[Tooltip("Choose the attachment that you want to append to this mail.")]
		public		eAttachmentOption	attachmentOption;
		[Tooltip("The image to be shared.")]
		public 		FsmTexture	 	image;
		[Tooltip("The absolute path of the file to be attached.")]
		public		FsmString		filePath;
		[Tooltip("The MIME type of the attachment.")]
		public		FsmString		mimeType;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when view is closed.")]
		public 		FsmEvent 		closedEvent;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			subject				= null;
			body				= null;
			isHtmlBody			= null;
			toRecipients		= null;
			ccRecipients		= null;
			bccRecipients		= null;
			attachmentOption	= eAttachmentOption.None;
			image				= null;
			filePath			= null;
			mimeType			= null;
			
			// Events properties
			closedEvent			= null;
		}
		
		public override void OnEnter() 
		{
#if USES_SHARING
			// Create composer
			MailShareComposer	_composer	= new MailShareComposer();
			_composer.Subject				= subject.Value;
			_composer.Body					= body.Value;
			_composer.IsHTMLBody			= isHtmlBody.Value;
			_composer.ToRecipients			= toRecipients.IsNone ? null : toRecipients.Value.Split(';');
			_composer.CCRecipients			= ccRecipients.IsNone ? null : ccRecipients.Value.Split(';');
			_composer.BCCRecipients			= bccRecipients.IsNone ? null : bccRecipients.Value.Split(';');

			// Add attachments
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
					
				case eAttachmentOption.AttachFileAtPath:
					_composer.AddAttachmentAtPath(filePath.Value, mimeType.Value);
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