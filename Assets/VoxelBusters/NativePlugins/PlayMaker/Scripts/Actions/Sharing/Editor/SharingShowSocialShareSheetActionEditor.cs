using UnityEngine;
using System.Collections;
using UnityEditor;
using HutongGames.PlayMakerEditor;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[CustomActionEditor(typeof(SharingShowSocialShareSheet))]
	public class SharingShowSocialShareSheetActionEditor : CustomActionEditor 
	{
		#region GUI Methods

		public override bool OnGUI ()
		{
			SharingShowSocialShareSheet _target = target as SharingShowSocialShareSheet;

			EditField("text");
			EditField("URL");
			EditField("attachmentOption");

			if (_target.attachmentOption == SharingShowSocialShareSheet.eAttachmentOption.AttachImage)
				EditField("image");
			else if (_target.attachmentOption == SharingShowSocialShareSheet.eAttachmentOption.AttachImageAtPath)
				EditField("imagePath");

			EditField("closedEvent");

			return GUI.changed;
		}

		#endregion
	}
}