// (c) Copyright HutongGames, LLC 2010-2013. All rights reserved.

using UnityEngine;
using VoxelBusters.NativePlugins;



namespace HutongGames.PlayMaker.Actions
{
	namespace VoxelBusters.NativePlugins	{
		[ActionCategory("GameServices")]
	[Tooltip("Authenticate")]
	public class GS_authenticate : FsmStateAction

	
	{

			[ActionSection("Results")]
	
		[Tooltip("The status of authentication.")]
		[UIHint(UIHint.Variable)]
		public	FsmBool		isAuthenticated;
		[Tooltip("The description of the problem that occured.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	error;
		
[ActionSection("Events")]
		
		[Tooltip("Event to send if local user is authenticated.")]
		public 	FsmEvent 	authenticatedEvent;
		[Tooltip("Event to send if local user is not authenticated.")]
		public 	FsmEvent 	notAuthenticatedEvent;
		


			public override void OnEnter ()
			{

				NPBinding.GameServices.LocalUser.Authenticate(OnAuthenticationFinished);

			}



			private void OnAuthenticationFinished (bool _success, string _error)
			{
				// Update properties
				isAuthenticated.Value	= _success;
				error.Value 			= _error;

				// Send event
				Fsm.Event(_success ? authenticatedEvent : notAuthenticatedEvent);
				Finish();
			}

		}

//		public override void OnEnter()
//		{
//				//Authenticate Local User
//NPBinding.GameServices.LocalUser.Authenticate((bool _success, string _error)=>{
//
//                if (_success)
//                {
//                    Debug.Log("Sign-In Successfully");
//                    Debug.Log("Local User Details : " + NPBinding.GameServices.LocalUser.ToString());
//                }
//                else
//                {
//                    Debug.Log("Sign-In Failed with error " + _error);
//                }
//            });
//
//		}

	}
}
