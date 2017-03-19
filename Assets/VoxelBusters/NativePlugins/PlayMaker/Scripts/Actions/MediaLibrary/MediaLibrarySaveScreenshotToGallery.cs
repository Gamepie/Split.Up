using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Captures screenshot and saves the image to gallery.")]
	public class MediaLibrarySaveScreenshotToGallery : FsmStateAction 
	{
		#region Fields

		[ActionSection("Events")]
		
		[Tooltip("Event to send when image is saved to gallery.")]
		public	FsmEvent	savedEvent;
		[Tooltip("Event to send when image couldn't be saved to gallery.")]
		public	FsmEvent	notSavedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Event properties
			savedEvent		= null;
			notSavedEvent	= null;
		}
		
		public override void OnEnter () 
		{
#if USES_MEDIA_LIBRARY
			NPBinding.MediaLibrary.SaveScreenshotToGallery(SaveImageToGalleryFinished);
#endif
		}
		
		#endregion

		#region Callback Methods

		private void SaveImageToGalleryFinished (bool _saved)
		{
			// Send event
			Fsm.Event(_saved ? savedEvent : notSavedEvent);
			Finish();
		}

		#endregion
	}
}