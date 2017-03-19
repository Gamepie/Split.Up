using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Shows the native alert dialog with specified buttons.")]
	public class UserInterfaceShowAlertDialog : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The string that appears in the title bar.")]
		public 	FsmString 	title;
		[RequiredField]
		[Tooltip("Descriptive text that provides more details than the title.")]
		public 	FsmString 	message;
		[RequiredField]
		[Tooltip("The button label for action buttons.")]
		public 	FsmString[] buttons;
		
		[ActionSection("Results")]
		
		[Tooltip("The button label of the button pressed by the user.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	pressedButton;

		[ActionSection("Events")]

		[Tooltip("Event to send when the user clicks a button from the alert dialog.")]
		public 	FsmEvent 	closedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			title			= null;
			message			= null;
			buttons			= new FsmString[0];

			// Results properties
			pressedButton	= new FsmString {
				UseVariable	= true
			};
			
			// Events properties
			closedEvent		= null;
		}
		
		public override void OnEnter() 
		{
			NPBinding.UI.ShowAlertDialogWithMultipleButtons(title.Value, 
			                                                message.Value, 
			                                                buttons.ToStringList(), 
			                                                AlertDialogPressed);
		}
		
		#endregion
		
		#region Callback Methods
		
		private void AlertDialogPressed (string _buttonPressed)
		{
			// Update values
			pressedButton.Value 	= _buttonPressed;
			
			// Send event
			Fsm.Event(closedEvent);
			Finish();
		}
		
		#endregion
	}
}