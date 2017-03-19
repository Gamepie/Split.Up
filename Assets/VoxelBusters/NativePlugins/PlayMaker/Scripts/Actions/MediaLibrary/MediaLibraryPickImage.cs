using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Opens an user interface to pick an image from specified image source.")]
	public class MediaLibraryPickImage : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]
		
		[Tooltip("The source to use when picking an image.")]
		public 	eImageSource 	source;
		[Tooltip("Indicates whether to scale-up or scale-down selected image. Having value as 1.0f returns the image without any modification.")]
		public 	FsmFloat	 	scaleFactor;

		[ActionSection("Results")]
		[Tooltip("The image selected by the user.")]
		[UIHint(UIHint.Variable)]
		public	FsmTexture 		image;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when user selected an image from specified source.")]
		public	FsmEvent		selectedEvent;
		[Tooltip("Event to send when user cancelled selection.")]
		public	FsmEvent		cancelledEvent;
		[Tooltip("Event to send when action fails.")]
		public	FsmEvent		failedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			source			= eImageSource.BOTH;
			scaleFactor		= 1f;

			// Reset properties
			image			= new FsmTexture {
				UseVariable	= true
			};

			// Events properties
			selectedEvent	= null;
			cancelledEvent	= null;
			failedEvent		= null;
		}
		
		public override void OnEnter () 
		{
#if USES_MEDIA_LIBRARY
			NPBinding.MediaLibrary.PickImage(source, scaleFactor.Value, PickImageFinished);
#endif
		}
		
		#endregion

		#region Callback Methods

#if USES_MEDIA_LIBRARY
		private void PickImageFinished (ePickImageFinishReason _finishReason, Texture2D _image)
		{
			// Update properties
			image.Value	= _image;

			// Invoke appropriate event
			switch (_finishReason)
			{
			case ePickImageFinishReason.SELECTED:
				Fsm.Event(selectedEvent);
				break;

			case ePickImageFinishReason.CANCELLED:
				Fsm.Event(cancelledEvent);
				break;

			case ePickImageFinishReason.FAILED:
				Fsm.Event(failedEvent);
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