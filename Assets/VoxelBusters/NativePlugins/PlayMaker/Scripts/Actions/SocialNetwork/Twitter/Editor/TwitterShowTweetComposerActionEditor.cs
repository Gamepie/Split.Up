using UnityEngine;
using System.Collections;
using UnityEditor;
using HutongGames.PlayMakerEditor;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[CustomActionEditor(typeof(TwitterShowTweetComposer))]
	public class TwitterShowTweetComposerActionEditor : CustomActionEditor 
	{
		#region GUI Methods
		
		public override bool OnGUI ()
		{
			TwitterShowTweetComposer _target = target as TwitterShowTweetComposer;
			
			EditField("text");
			EditField("URL");
			EditField("attachmentOption");
			
			if (_target.attachmentOption == TwitterShowTweetComposer.eAttachmentOption.AttachImage)
				EditField("image");
			else if (_target.attachmentOption == TwitterShowTweetComposer.eAttachmentOption.AttachImageAtPath)
				EditField("imagePath");
			
			EditField("doneEvent");
			EditField("cancelledEvent");

			return GUI.changed;
		}
		
		#endregion
	}
}