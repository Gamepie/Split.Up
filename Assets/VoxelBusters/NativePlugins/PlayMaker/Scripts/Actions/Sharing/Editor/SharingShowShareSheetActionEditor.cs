using UnityEngine;
using System.Collections;
using UnityEditor;
using HutongGames.PlayMakerEditor;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[CustomActionEditor(typeof(SharingShowShareSheet))]
	public class SharingShowShareSheetActionEditor : CustomActionEditor 
	{
		#region GUI Methods

		public override bool OnGUI ()
		{
			SharingShowShareSheet _target = target as SharingShowShareSheet;

			EditField("text");
			EditField("URL");
			EditField("attachmentOption");

			if (_target.attachmentOption == SharingShowShareSheet.eAttachmentOption.AttachImage)
				EditField("image");
			else if (_target.attachmentOption == SharingShowShareSheet.eAttachmentOption.AttachImageAtPath)
				EditField("imagePath");

			EditField("excludedOptions");
			EditField("closedEvent");

			return GUI.changed;
		}

		#endregion
	}
}