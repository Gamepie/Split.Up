using UnityEngine;
using System.Collections;
using UnityEditor;
using HutongGames.PlayMakerEditor;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[CustomActionEditor(typeof(SharingShowWhatsAppShareComposer))]
	public class SharingShowWhatsAppShareComposerActionEditor : CustomActionEditor 
	{
		#region GUI Methods

		public override bool OnGUI ()
		{
			SharingShowWhatsAppShareComposer _target = target as SharingShowWhatsAppShareComposer;

			EditField("shareOption");

			if (_target.shareOption == SharingShowWhatsAppShareComposer.eShareOption.ShareText)
				EditField("text");
			else if (_target.shareOption == SharingShowWhatsAppShareComposer.eShareOption.ShareImage)
				EditField("image");
			else if (_target.shareOption == SharingShowWhatsAppShareComposer.eShareOption.ShareImageAtPath)
				EditField("imagePath");

			EditField("closedEvent");

			return GUI.changed;
		}

		#endregion
	}
}