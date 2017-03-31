using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sets the value indicating whether the user is allowed to edit a selected image.")]
	public class MediaLibrarySetAllowsImageEditing : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("The bool value indicates whether user can edit image selected from media library.")]
		public	FsmBool		canEdit;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			canEdit	= null;
		}
		
		public override void OnEnter ()
		{
#if USES_MEDIA_LIBRARY
			NPBinding.MediaLibrary.SetAllowsImageEditing(canEdit.Value);
#endif

			Finish();
		}
		
		#endregion
	}
}