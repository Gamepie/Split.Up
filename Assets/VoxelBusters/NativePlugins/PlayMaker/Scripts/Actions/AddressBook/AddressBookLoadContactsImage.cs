using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Loads the images (profile pictures) of all the specified contacts.")]
	public class AddressBookLoadContactsImage : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[Tooltip("Start loading images (profile pictures) starting from this index.")]
		public	FsmInt		startIndex;
		[Tooltip("Total number of images (profile pictures) to be loaded.")]
		public	FsmInt		count;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when the requested images were loaded successfully.")]
		public	FsmEvent	successEvent;
		[Tooltip("Event to send when the requested images couldn't be loaded.")]
		public	FsmEvent	failedEvent;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			startIndex		= 0;
			count			= 1;

			// Events properties
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
			int _startIndex			= startIndex.Value;
			int	_endIndex			= _startIndex + count.Value - 1;
			int	_totalContacts		= AddressBookUtils.GetContactsCount();
			int	_imagesToLoad		= count.Value;
			int	_loadedImagesCount	= 0;
			
			if (_startIndex < 0 || _startIndex >= _totalContacts)
			{
				OnActionDidFail();

				return;
			}
			
			if (_endIndex < _startIndex || _endIndex >= _totalContacts)
			{
				OnActionDidFail();

				return;
			}
			
			// Start downloading contacts image
			for (int _iter = _startIndex; _iter <= _endIndex; _iter++)
			{
				AddressBookContact _currentContact	= AddressBookUtils.contactsInfoList[_iter];

				_currentContact.GetImageAsync((Texture2D _image, string _error) => {

					// Update download completed count
					_loadedImagesCount++;

					// Check if we are done with downloading
					if (_loadedImagesCount == _imagesToLoad)
					{
						OnActionDidFinish();

						return;
					}
				});
			}
#endif
		}

		private void OnActionDidFail ()
		{
			Fsm.Event(failedEvent); 
			Finish();
		}

		private void OnActionDidFinish ()
		{
			Fsm.Event(successEvent);
			Finish();
		}

		#endregion
	}
}