using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Plays embedded video using WebView.")]
	public class MediaLibraryPlayEmbeddedVideo : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]
		
		[Tooltip("The embedded HTML string to be loaded into the WebView.")]
		public 	TextAsset	embeddedHtmlText;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when video playback ended.")]
		public	FsmEvent	playbackEndedEvent;
		[Tooltip("Event to send when error was encountered while playing video.")]
		public	FsmEvent	playbackErrorEvent;
		[Tooltip("Event to send when user exited without playing the video completely.")]
		public	FsmEvent	userExitedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			embeddedHtmlText	= null;

			// Events properties
			playbackEndedEvent	= null;
			playbackErrorEvent	= null;
			userExitedEvent		= null;
		}
		
		public override void OnEnter () 
		{
#if USES_MEDIA_LIBRARY
			if (embeddedHtmlText != null)
			{
				NPBinding.MediaLibrary.PlayEmbeddedVideo(embeddedHtmlText.text, PlayVideoFinished);
			}
			else
			{
				Fsm.Event(playbackErrorEvent);
				Finish();
			}
#endif
		}
		
		#endregion
		
		#region Callback Methods
		
#if USES_MEDIA_LIBRARY
		private void PlayVideoFinished (ePlayVideoFinishReason _finishReason)
		{
			switch (_finishReason)
			{
			case ePlayVideoFinishReason.PLAYBACK_ENDED:
				Fsm.Event(playbackEndedEvent);
				break;
				
			case ePlayVideoFinishReason.PLAYBACK_ERROR:
				Fsm.Event(playbackErrorEvent);
				break;
				
			case ePlayVideoFinishReason.USER_EXITED:
				Fsm.Event(userExitedEvent);
				break;
				
			default:
				Log("[MediaLibrary] Unhandled reason.");
				break;
			}

			Finish();
		}
#endif
		
		#endregion
	}
}