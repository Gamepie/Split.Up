using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.Utility;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;
using URLStruct	= VoxelBusters.Utility.URL;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Presents a view to compose and share Twitter post.")]
	public class TwitterShowTweetComposer : FsmStateAction 
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
		public 	FsmString 		text;
		[Tooltip("The URL to be shared.")]
		public 	FsmString 		URL;
		[Tooltip("Choose the attachment you want to add.")]
		public	eAttachmentOption	attachmentOption;
		[Tooltip("The image to be shared.")]
		public 	FsmTexture	 	image;
		[Tooltip("The absolute path of the image to be shared.")]
		public	FsmString		imagePath;

		[ActionSection("Events")]
		
		[Tooltip("Event to send if composer view is dismissed and the message is being sent in the background.")]
		public	FsmEvent		doneEvent;
		[Tooltip("Event to send if composer view is dismissed without sending the Tweet.")]
		public	FsmEvent		cancelledEvent;

#pragma warning disable			
		private	byte[]			m_imageData;
#pragma warning restore

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
			
			// Events properties
			doneEvent			= null;
			cancelledEvent		= null;

			// Misc.
			m_imageData			= null;
		}

		public override void OnEnter ()
		{
			if (attachmentOption != eAttachmentOption.None)
			{
				if (attachmentOption == eAttachmentOption.AttachScreenshot)
				{
					TextureExtensions.TakeScreenshot((Texture2D _image) => {

						m_imageData	= _image.EncodeToPNG();
						
						OnShareDataAvailable();
					});

					return;
				}
				else if (attachmentOption == eAttachmentOption.AttachImageAtPath)
				{
					DownloadAsset _request	= new DownloadAsset(new URLStruct(imagePath.Value), true);
					_request.OnCompletion	= (WWW _www, string _error) => {

						if (_error == null)
							m_imageData		= _www.bytes;

						OnShareDataAvailable();
					};

					return;
				}
				else
				{
					m_imageData		= ((Texture2D)image.Value).EncodeToPNG();

					OnShareDataAvailable();
					return;
				}
			}
			else
			{
				OnShareDataAvailable();
			}
		}

		private void OnShareDataAvailable ()
		{
#if USES_TWITTER
			NPBinding.Twitter.ShowTweetComposer(text.Value, 
			                                    URL.Value, 
			                                    m_imageData, 
			                                    OnTweetComposerClosed);
#endif
		}
		
		#endregion

		#region Callback Methods

#if USES_TWITTER
		private void OnTweetComposerClosed (eTwitterComposerResult _result)
		{
			Fsm.Event(_result == eTwitterComposerResult.DONE ? doneEvent : cancelledEvent);
			Finish();
		}
#endif

		#endregion
	}
}