using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Presents a view to compose and share message.")]
	public class SharingShowMessageShareComposer : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[Tooltip("The initial content of the message.")]
		public 		FsmString 		body;
		[Tooltip("An array of strings containing the initial recipients of the message. This property can have multiple values joined using \';\' delimiter.")]
		public 		FsmString 		recipients;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when view is closed.")]
		public 		FsmEvent 		closedEvent;
		
		#endregion

		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			body		= null;
			recipients	= null;

			// Events properties
			closedEvent	= null;
		}
		
		public override void OnEnter() 
		{
#if USES_SHARING
			MessageShareComposer	_composer	= new MessageShareComposer();
			_composer.Body						= body.Value;
			_composer.ToRecipients 				= recipients.IsNone ? null : recipients.Value.Split(';');
			
			// Show message composer
			NPBinding.Sharing.ShowView(_composer, FinishedSharing);
#endif
		}

		#endregion

		#region Callback Methods
		
		private void FinishedSharing (eShareResult _result)
		{
			// Send event
			Fsm.Event(closedEvent);
			Finish();
		}
		
		#endregion
	}
}