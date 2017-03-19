using UnityEngine;
using System.Collections;
using UnityEditor;
using HutongGames.PlayMakerEditor;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[CustomActionEditor(typeof(SharingShowTwitterShareComposer))]
	public class SharingShowTwitterShareComposerActionEditor : CustomActionEditor 
	{
		#region GUI Methods

		public override bool OnGUI ()
		{
			SharingShowTwitterShareComposer _target = target as SharingShowTwitterShareComposer;

			EditField("text");
			EditField("URL");
			EditField("attachmentOption");

			if (_target.attachmentOption == SharingShowTwitterShareComposer.eAttachmentOption.AttachImage)
				EditField("image");
			else if (_target.attachmentOption == SharingShowTwitterShareComposer.eAttachmentOption.AttachImageAtPath)
				EditField("imagePath");

			EditField("closedEvent");

			return GUI.changed;
		}

		#endregion
	}
}