using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Determines whether local user is currently signed in to game service.")]
	public class GameServicesGetLocalUser : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[Tooltip("If enabled, user's profile picture is loaded from the disk.")]
		public FsmBool		loadImage;

		[ActionSection("Results")]
	
		[Tooltip("A string assigned by game service to uniquely identify a user.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	identifier;
		[Tooltip("A string chosen by the player to identify themselves to other players.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	name;
		[Tooltip("The profile picture of the user.")]
		[UIHint(UIHint.Variable)]
		public	FsmTexture	image;
		[Tooltip("The status of authentication.")]
		[UIHint(UIHint.Variable)]
		public	FsmBool		isAuthenticated;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send if local user is authenticated.")]
		public 	FsmEvent 	authenticatedEvent;
		[Tooltip("Event to send if local user is not authenticated.")]
		public 	FsmEvent 	notAuthenticatedEvent;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Results properties
			identifier				= new FsmString {
				UseVariable	= true
			};
			name					= new FsmString {
				UseVariable	= true
			};
			image					= new FsmTexture {
				UseVariable	= true
			};
			isAuthenticated			= new FsmBool {
				UseVariable	= true
			};

			// Events properties
			authenticatedEvent		= null;
			notAuthenticatedEvent	= null;
		}
		
		public override void OnEnter ()
		{
			DoAction();
		}
		
		#endregion

		#region Methods

		private void DoAction ()
		{
#if USES_GAME_SERVICES
			LocalUser _localUser	= NPBinding.GameServices.LocalUser;

			if (_localUser.IsAuthenticated)
			{
				// Update properties
				identifier.Value	= _localUser.Identifier;
				name.Value			= _localUser.Name;

				// Check if image has to be download
				if (loadImage.Value)
				{
					_localUser.GetImageAsync((Texture2D _image, string _error) => {
						
						// Update the image property
						image.Value	= _image;
						
						OnActionDidFinish();
						
						return;
					});
				}
				else
				{
					// Update the image property
					image.Value	= null;
					
					OnActionDidFinish();
					
					return;
				}
			}
			else
			{
				OnActionDidFail();
			}
#endif
		}

		private void OnActionDidFinish ()
		{
			Fsm.Event(authenticatedEvent);
			Finish();
		}

		private void OnActionDidFail ()
		{
			Fsm.Event(notAuthenticatedEvent);
			Finish();
		}

		#endregion
	}
}