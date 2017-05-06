using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Shows native dialog requesting user to enter login details.")]
	public class UserInterfaceShowLoginPromptDialog : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The string that appears in the title bar.")]
		public 	FsmString 	title;
		[RequiredField]
		[Tooltip("Descriptive text that provides more details than the title.")]
		public 	FsmString 	message;
		[Tooltip("The string that is displayed when there is no other text in the username textfield.")]
		public 	FsmString 	usernamePlaceHolder;
		[Tooltip("The string that is displayed when there is no other text in the password textfield.")]
		public 	FsmString 	passwordPlaceHolder;
		[RequiredField]
		[Tooltip("The button label for action buttons.")]
		public 	FsmString[] buttons;
		
		[ActionSection("Results")]
		
		[Tooltip("The button label of the button pressed by the user.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	pressedButton;
		[Tooltip("Text entered by the user in username textfield.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	inputUsername;
		[Tooltip("Text entered by the user in password textfield.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	inputPassword;

		[ActionSection("Events")]

		[Tooltip("Event to send when the user clicks a button from the prompt dialog.")]
		public 	FsmEvent 	closedEvent;

		#endregion

		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			title			= null;
			message			= null;
			usernamePlaceHolder	= null;
			passwordPlaceHolder	= null;
			buttons			= new FsmString[0];
			
			// Results properties
			pressedButton	= new FsmString {
				UseVariable	= true
			};
			inputUsername	= new FsmString {
				UseVariable	= true
			};
			inputPassword	= new FsmString {
				UseVariable	= true
			};
			
			// Events properties
			closedEvent		= null;
		}
		
		public override void OnEnter() 
		{
			NPBinding.UI.ShowLoginPromptDialog(title.Value, 
			                                   message.Value, 
			                                   usernamePlaceHolder.Value, 
			                                   passwordPlaceHolder.Value, 
			                                   buttons.ToStringList(), 
			                                   LoginPromptDialogClosed);
		}

		#endregion

		#region Callback Methods

		private void LoginPromptDialogClosed (string _buttonPressed, string _username, string _password)
		{
			// Update values
			pressedButton.Value 	= _buttonPressed;
			inputUsername.Value 	= _username;
			inputPassword.Value 	= _password;
			
			// Send event
			Fsm.Event(closedEvent);
			Finish();
		}

		#endregion
	}
}