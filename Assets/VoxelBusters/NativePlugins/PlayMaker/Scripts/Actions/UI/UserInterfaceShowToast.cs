using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Shows the toast view. A toast is a view containing a quick little message for the user.")]
	public class UserInterfaceShowToast : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The message used in Toast view.")]
		public 		FsmString 				message;
		[Tooltip("Toast display time length.")]
		public		eToastMessageLength		length;

		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			message	= null;
			length	= eToastMessageLength.SHORT;
		}
		
		public override void OnEnter () 
		{
			NPBinding.UI.ShowToast(message.Value, length);

			Finish();
		}
		
		#endregion
	}
}