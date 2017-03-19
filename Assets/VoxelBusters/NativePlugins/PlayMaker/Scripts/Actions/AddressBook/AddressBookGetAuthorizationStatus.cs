using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the current authorization status to access the address book data.")]
	public class AddressBookGetAuthorizationStatus : FsmStateAction 
	{
		#region Fields

		[ActionSection("Events")]

		[Tooltip("Event to send if user has not made a choice whether the application can access address book data.")]
		public	FsmEvent	notDeterminedEvent;
		[Tooltip("Event to send if application is not authorized to access address book data.")]
		public	FsmEvent	restrictedEvent;
		[Tooltip("Event to send if user explicitly denied access to address book data for this application.")]
		public	FsmEvent	deniedEvent; 
		[Tooltip("Event to send if application is authorized to access address book data.")]
		public	FsmEvent	authorizedEvent; 

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			notDeterminedEvent	= null;
			restrictedEvent		= null;
			deniedEvent			= null;
			authorizedEvent		= null;
		}
		
		public override void OnEnter () 
		{
			DoAction();
			
			Finish();
		}

		#endregion

		#region Methods

		private void DoAction ()
		{
#if USES_ADDRESS_BOOK
			eABAuthorizationStatus 	_authStatus = NPBinding.AddressBook.GetAuthorizationStatus();

			switch(_authStatus)
			{
				case eABAuthorizationStatus.NOT_DETERMINED:
				Fsm.Event(notDeterminedEvent);
				break;

				case eABAuthorizationStatus.RESTRICTED:
				Fsm.Event(restrictedEvent);
				break;

				case eABAuthorizationStatus.DENIED:
				Fsm.Event(deniedEvent);
				break;

				case eABAuthorizationStatus.AUTHORIZED:
				Fsm.Event(authorizedEvent);
				break;

				default:
				LogError("[NativePlugins] Unhandled authorization status.");
				break;
			}
#endif
		}

		#endregion
	}
}