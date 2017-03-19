using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.Utility;

// Conflict handlers
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;
using URLStruct	= VoxelBusters.Utility.URL;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Plays full screen video from specified URL. Video URL can be local as well as remote address.")]
	public class MediaLibraryPlayVideoFromURL : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The URL of the video.")]
		public 	FsmString	URL;
		
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
			URL					= null;

			// Events properties
			playbackEndedEvent	= null;
			playbackErrorEvent	= null;
			userExitedEvent		= null;
		}
		
		public override void OnEnter () 
		{
#if USES_MEDIA_LIBRARY
			NPBinding.MediaLibrary.PlayVideoFromURL(new URL(URL.Value), PlayVideoFinished);
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