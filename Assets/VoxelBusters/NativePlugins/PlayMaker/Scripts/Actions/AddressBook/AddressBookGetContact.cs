using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the contact from details received from database and stores in variables.")]
	public class AddressBookGetContact : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[Tooltip("The index to retrieve the contact from.")]
		public	FsmInt		atIndex;
		[Tooltip("If enabled, the image (profile picture) of the contact is loaded from the disk.")]
		public	FsmBool		loadImage;

		[ActionSection("Results")]

		[Tooltip("The first name of the contact.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	firstName;
		[Tooltip("The last name of the contact.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	lastName;
		[Tooltip("An array of phone numbers for the contact. This property can have multiple values joined using \';\' delimiter.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString	phoneNumberList;
		[Tooltip("An array of email addresses for the contact. This property can have multiple values joined using \';\' delimiter.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString	emailList;
		[Tooltip("The profile picture of the contact.")]
		[UIHint(UIHint.Variable)]
		public	FsmTexture 	image;

		[ActionSection("Events")]

		[Tooltip("Event to send when action fails (likely index is out of range exception).")]
		public 	FsmEvent 	failedEvent;

		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			atIndex			= 0;
			loadImage		= false;

			// Results properties
			firstName		= new FsmString {
				UseVariable = true
			};
			lastName		= new FsmString {
				UseVariable = true
			};
			phoneNumberList	= new FsmString {
				UseVariable = true
			};
			emailList		= new FsmString {
				UseVariable = true
			};
			image			= new FsmTexture {
				UseVariable = true
			};

			// Events properties
			failedEvent		= null;
		}

		public override void OnEnter() 
		{
#if USES_ADDRESS_BOOK
			try
			{
				AddressBookContact 	_contactInfo	= AddressBookUtils.contactsInfoList[atIndex.Value];
				
				// Update properties
				firstName.Value			= _contactInfo.FirstName;
				lastName.Value			= _contactInfo.LastName;
				phoneNumberList.Value	= (_contactInfo.PhoneNumberList == null) ? null : string.Join(";", _contactInfo.PhoneNumberList);
				emailList.Value			= (_contactInfo.EmailIDList == null) ? null : string.Join(";", _contactInfo.EmailIDList);

				// If required, download the image
				if (loadImage.Value)
				{
					_contactInfo.GetImageAsync((Texture2D _image, string _error)=>{

						// Update image property value
						image.Value	= _image;

						OnActionDidFinish();
						
						return;
					});
				}
				else
				{
					// Update image property value
					image.Value	= null;

					OnActionDidFinish();
					
					return;
				}
			}
			catch (System.Exception _exception)
			{
				Debug.Log(_exception.Message);

				OnActionDidFail();

				return;
			}
#endif
		}

		#endregion

		#region Methods

		private void OnActionDidFinish ()
		{
			Finish();
		}

		private void OnActionDidFail ()
		{
			Fsm.Event(failedEvent);
			Finish();
		}

		#endregion
	}
}