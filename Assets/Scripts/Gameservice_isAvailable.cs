// (c) Copyright HutongGames, LLC 2010-2013. All rights reserved.

using UnityEngine;
using VoxelBusters.NativePlugins;



namespace HutongGames.PlayMaker.Actions
{
	namespace VoxelBusters.NativePlugins	{
		[ActionCategory("GameServices")]
	[Tooltip("Bool is service is available")]
	public class GS_isavailable : FsmStateAction
	
	{
		[UIHint(UIHint.Variable)]
		[Tooltip("Available or not")]
		public FsmBool _isAvailable;

		public override void OnEnter()
		{
				_isAvailable.Value = NPBinding.GameServices.IsAvailable();
				Finish();
		}
	}
}
}