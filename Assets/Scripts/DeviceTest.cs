// (c) Copyright HutongGames, LLC 2010-2013. All rights reserved.

using UnityEngine;

namespace HutongGames.PlayMaker.Actions
{
	[ActionCategory(ActionCategory.Device)]
	[Tooltip("Set a String value for the Platform you're on")]
	public class DeviceString : FsmStateAction
	{
		[UIHint(UIHint.Variable)]
		[Tooltip("Android,iOS or UnityEditor")]
		public FsmString deviceIs;

		public override void OnEnter()
		{
			if (Application.platform == RuntimePlatform.Android)
			{
				deviceIs.Value = "Android";
				Finish();
			}
			if(Application.platform == RuntimePlatform.IPhonePlayer)
			{
				deviceIs.Value = "iOS";
				Finish();
			}	
			if(Application.platform == RuntimePlatform.OSXEditor)
			{
				deviceIs.Value = "Unity";
				Finish();
			}

		}
	}
}