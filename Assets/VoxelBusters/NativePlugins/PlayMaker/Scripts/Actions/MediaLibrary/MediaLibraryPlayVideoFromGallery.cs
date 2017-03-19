using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Plays full screen video from gallery selected by the user.")]
	public class MediaLibraryPlayVideoFromGallery : FsmStateAction 
	{
		#region Fields

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
			// Events properties
			playbackEndedEvent	= null;
			playbackErrorEvent	= null;
			userExitedEvent		= null;
		}
		
		public override void OnEnter () 
		{
#if USES_MEDIA_LIBRARY
			NPBinding.MediaLibrary.PlayVideoFromGallery(PickVideoFinished, PlayVideoFinished);
#endif
		}
		
		#endregion
		
		#region Callback Methods

#if USES_MEDIA_LIBRARY
		private void PickVideoFinished (ePickVideoFinishReason _finishReason)
		{
			if (_finishReason != ePickVideoFinishReason.SELECTED)
			{
				// Send event
				Fsm.Event(playbackErrorEvent);
				Finish();

				return;
			}
		}
		
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