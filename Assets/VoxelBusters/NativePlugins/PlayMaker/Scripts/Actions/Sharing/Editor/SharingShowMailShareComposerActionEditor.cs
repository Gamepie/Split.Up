using UnityEngine;
using System.Collections;
using UnityEditor;
using HutongGames.PlayMakerEditor;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[CustomActionEditor(typeof(SharingShowMailShareComposer))]
	public class SharingShowMailShareComposerActionEditor : CustomActionEditor 
	{
		#region GUI Methods

		public override bool OnGUI ()
		{
			SharingShowMailShareComposer _target = target as SharingShowMailShareComposer;

			EditField("subject");
			EditField("body");
			EditField("isHtmlBody");
			EditField("toRecipients");
			EditField("ccRecipients");
			EditField("bccRecipients");
			EditField("attachmentOption");

			if (_target.attachmentOption == SharingShowMailShareComposer.eAttachmentOption.AttachImage)
			{
				EditField("image");
			}
			else if (_target.attachmentOption == SharingShowMailShareComposer.eAttachmentOption.AttachFileAtPath)
			{
				EditField("filePath");
				EditField("mimeType");
			}

			EditField("closedEvent");

			return GUI.changed;
		}

		#endregion
	}
}