using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Saves the specified image to gallery.")]
	public class MediaLibrarySaveImageToGallery : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The image to be saved.")]
		public	FsmTexture	image;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when image is saved to gallery.")]
		public	FsmEvent	savedEvent;
		[Tooltip("Event to send when image couldn't be saved to gallery.")]
		public	FsmEvent	notSavedEvent;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			image			= null;

			// Event properties
			savedEvent		= null;
			notSavedEvent	= null;
		}
		
		public override void OnEnter () 
		{
#if USES_MEDIA_LIBRARY
			NPBinding.MediaLibrary.SaveImageToGallery((Texture2D)image.Value, SaveImageToGalleryFinished);
#endif
		}
		
		#endregion

		#region Callback Methods

		private void SaveImageToGalleryFinished (bool _saved)
		{
			// Send events
			Fsm.Event(_saved ? savedEvent : notSavedEvent);
			Finish();
		}

		#endregion
	}
}