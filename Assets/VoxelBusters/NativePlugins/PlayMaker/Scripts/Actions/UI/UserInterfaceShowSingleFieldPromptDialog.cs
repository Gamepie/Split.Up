using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Shows native dialog requesting user to enter text.")]
	public class UserInterfaceShowSingleFieldPromptDialog : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]

		[RequiredField]
		[Tooltip("The string that appears in the title bar.")]
		public 	FsmString 	title;
		[RequiredField]
		[Tooltip("Descriptive text that provides more details than the title.")]
		public 	FsmString 	message;
		[Tooltip("The string that is displayed when there is no other text in the textfield.")]
		public 	FsmString 	placeHolder;
		[Tooltip("If enabled, text in the textfield is obscured.")]
		public 	FsmBool 	secureTextEntry;
		[RequiredField]
		[Tooltip("The button label for action buttons.")]
		public 	FsmString[] buttons;
		
		[ActionSection("Results")]

		[Tooltip("The button label of the button pressed by the user.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	pressedButton;
		[Tooltip("Text entered by the user in the textfield.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	inputText;

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
			placeHolder		= null;
			secureTextEntry	= false;
			buttons			= new FsmString[0];
			
			// Results properties
			pressedButton	= new FsmString {
				UseVariable	= true
			};
			inputText		= new FsmString {
				UseVariable	= true
			};
			
			// Events properties
			closedEvent		= null;
		}
		
		public override void OnEnter() 
		{
			if (secureTextEntry.Value)
			{
				NPBinding.UI.ShowSingleFieldPromptDialogWithSecuredText(title.Value, 
				                                                        message.Value, 
				                                                        placeHolder.Value, 
				                                                        buttons.ToStringList(), 
				                                                        SingleFieldPromptDialogClosed);
			}
			else
			{
				NPBinding.UI.ShowSingleFieldPromptDialogWithPlainText(title.Value, 
				                                                      message.Value,
				                                                      placeHolder.Value, 
				                                                      buttons.ToStringList(), 
				                                                      SingleFieldPromptDialogClosed);			
			}
		}
		
		#endregion
		
		#region Callback Methods
		
		private void SingleFieldPromptDialogClosed (string _buttonPressed, string _input)
		{
			// Update values
			pressedButton.Value 	= _buttonPressed;
			inputText.Value			= _input;
			
			// Send event
			Fsm.Event(closedEvent);
			Finish();
		}
		
		#endregion
	}
}