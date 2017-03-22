// (c) Copyright HutongGames, LLC 2010-2013. All rights reserved.

using UnityEngine;
using VoxelBusters.NativePlugins;



namespace HutongGames.PlayMaker.Actions
{
	namespace VoxelBusters.NativePlugins	{
		[ActionCategory("GameServices")]
	[Tooltip("Authenticated?")]
	public class GS_isauthenticated : FsmStateAction

	
	{
		[UIHint(UIHint.Variable)]
		[Tooltip("authenticated or not")]
		public FsmBool _isAuthenticated;
		       [Tooltip("Event to send if the Bool variable is True.")]
		public FsmEvent isTrue;

        [Tooltip("Event to send if the Bool variable is False.")]
		public FsmEvent isFalse;

		public override void OnEnter()
		{
				FsmBool _isAuthenticated = NPBinding.GameServices.LocalUser.IsAuthenticated;
			Fsm.Event(_isAuthenticated.Value ? isTrue : isFalse);

		}
		public override void Reset()
		{
			_isAuthenticated = null;
			isTrue = null;
			isFalse = null;
		}
		
		public override void OnUpdate()
		{
			Fsm.Event(_isAuthenticated.Value ? isTrue : isFalse);
		}
	}
}
}