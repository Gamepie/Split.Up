using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Starts a request to retrieve contact info from device Address Book.")]
	public class AddressBookReadContacts : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[Tooltip("If enabled, the image (profile picture) of the contacts is loaded from the disk.")]
		public	FsmBool		loadImages;

		[ActionSection("Results")]

		[Tooltip("The total number of contacts info retrieved.")]
		[UIHint(UIHint.Variable)]
		public 	FsmInt 		count;

		[ActionSection("Events")]

		[Tooltip("Event to send when request retrieves all the contacts info saved in address book database.")]
		public	FsmEvent	successEvent;
		[Tooltip("Event to send when request fails to retrieve contacts info.")]
		public	FsmEvent	failedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			loadImages		= false;

			// Results properties
			count			= new FsmInt {
				UseVariable = true
			};

			// Event properties
			successEvent	= null;
			failedEvent		= null;
		}
		
		public override void OnEnter () 
		{
			DoAction();
		}
		
		#endregion

		#region Methods

		private void DoAction ()
		{
#if USES_ADDRESS_BOOK
			NPBinding.AddressBook.ReadContacts(OnReadContactsFinished);
#endif
		}
		
		private void OnActionDidFinish ()
		{
			// Send event
			Fsm.Event(successEvent);
			Finish();
		}
		
		private void OnActionDidFail ()
		{
			// Send event
			Fsm.Event(failedEvent);
			Finish();
		}

		#endregion
		
		#region Callback Methods
		
#if USES_ADDRESS_BOOK
		private void OnReadContactsFinished (eABAuthorizationStatus _authorizationStatus, AddressBookContact[] _contactsList)
		{
			if (_authorizationStatus == eABAuthorizationStatus.AUTHORIZED)
			{
				// Update property values
				int			_totalContacts			= _contactsList.Length;

				AddressBookUtils.contactsInfoList	= _contactsList;
				count.Value 						= _totalContacts;

				// Check if we have any items to download
				if (!loadImages.Value || _totalContacts == 0)
				{
					OnActionDidFinish();
					
					return;
				}
				else
				{
					int		_loadedImagesCount		= 0;

					foreach (AddressBookContact _currentContact in _contactsList)
					{
						_currentContact.GetImageAsync((Texture2D _image, string _error) => {
							
							// Update counter
							_loadedImagesCount++;
							
							// Check if we are done with downloading
							if (_loadedImagesCount == _totalContacts)
							{
								OnActionDidFinish();
								
								return;
							}
						});
					}				
				}
			}
			else
			{
				// Update property values
				AddressBookUtils.contactsInfoList	= null;
				count.Value 						= 0;

				OnActionDidFail();

				return;
			}
		}
#endif
		
		#endregion
	}
}