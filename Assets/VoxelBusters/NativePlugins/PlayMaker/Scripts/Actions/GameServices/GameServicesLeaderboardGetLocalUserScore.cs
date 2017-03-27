using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.NativePlugins.Internal;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the local user score details from the leaderboard and stores it in variables.")]
	public class GameServicesLeaderboardGetLocalUserScore : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("A string used to identify the leaderboard.")]
		public	FsmString	identifier;
		[Tooltip("Indicates that specified identifier is unified value, which can identify leaderboard across all the supported platform. Works only if, leaderboard id collection was configured in NPSettings.")]
		public	FsmBool		isGlobalIdentifier;
		[RequiredField]
		[Tooltip("Defines the text representation of date and time.")]
		public	FsmString	dateTimeFormat;
		[Tooltip("If enabled, user's profile picture is loaded from the disk.")]
		public FsmBool		loadImage;

		[ActionSection("Results")]
		
		[Tooltip("A string assigned by game service to uniquely identify a user.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	userIdentifier;
		[Tooltip("A string chosen by the player to identify themselves to other players.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	userName;
		[Tooltip("The score earned by the user.")]
		[UIHint(UIHint.Variable)]
		public 	FsmInt	 	value;
		[Tooltip("The date and time when score was earned.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString	date;
		[Tooltip("The user’s score in localized format.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString	formattedValue;
		[Tooltip("The position of the score in leaderboard.")]
		[UIHint(UIHint.Variable)]
		public 	FsmInt	 	rank;
		[Tooltip("The profile picture of the user.")]
		[UIHint(UIHint.Variable)]
		public	FsmTexture	image;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when action fails to find local score details.")]
		public 	FsmEvent 	failedEvent;
		
		#endregion
		
		#region FSM Methods
		
		public override void Reset ()
		{
			// Setup properties
			identifier			= null;
			isGlobalIdentifier	= true;
			dateTimeFormat		= Constants.kPlayMakerDateTimeFormat;
			loadImage			= false;

			// Results properties
			userIdentifier		= new FsmString {
				UseVariable		= true
			};
			userName			= new FsmString {
				UseVariable		= true
			};
			value				= new FsmInt {
				UseVariable		= true
			};
			date				= new FsmString {
				UseVariable		= true
			};
			formattedValue		= new FsmString {
				UseVariable		= true
			};
			rank				= new FsmInt {
				UseVariable		= true
			};
			image				= new FsmTexture {
				UseVariable		= true
			};
			
			// Event properties
			failedEvent			= null;
		}
		
		public override void OnEnter ()
		{
#if USES_GAME_SERVICES
			try
			{
				Leaderboard _leaderboard	= GameServicesUtils.GetLeaderboard(identifier.Value, isGlobalIdentifier.Value);
				Score		_score			= _leaderboard.LocalUserScore;

				if (_score == null)
				{
					OnActionDidFail();
					
					return;
				}
				else
				{
					User	_user			= _score.User;
				
					// Update properties
					userIdentifier.Value	= _user.Identifier;
					userName.Value			= _user.Name;
					value.Value				= (int)_score.Value;
					date.Value				= _score.Date.ToString(dateTimeFormat.Value);
					formattedValue.Value	= _score.FormattedValue;
					rank.Value				= _score.Rank;
					
					// Check if image has to be download
					if (loadImage.Value)
					{
						_user.GetImageAsync((Texture2D _image, string _error) => {
							
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