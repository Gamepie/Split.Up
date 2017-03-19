using UnityEngine;
using System.Collections;
using UnityEditor;
using HutongGames.PlayMakerEditor;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[CustomActionEditor(typeof(SharingShowFbShareComposer))]
	public class SharingShowFbShareComposerActionEditor : CustomActionEditor 
	{
		#region GUI Methods

		public override bool OnGUI ()
		{
			SharingShowFbShareComposer _target = target as SharingShowFbShareComposer;

			EditField("URL");
			EditField("attachmentOption");

			if (_target.attachmentOption == SharingShowFbShareComposer.eAttachmentOption.AttachImage)
				EditField("image");
			else if (_target.attachmentOption == SharingShowFbShareComposer.eAttachmentOption.AttachImageAtPath)
				EditField("imagePath");

			EditField("closedEvent");

			return GUI.changed;
		}

		#endregion
	}
}