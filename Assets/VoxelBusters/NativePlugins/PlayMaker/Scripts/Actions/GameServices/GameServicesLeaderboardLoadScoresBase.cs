using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	public abstract class GameServicesLeaderboardLoadScoresBase : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[Tooltip("A string used to identify the leaderboard.")]
		public	FsmString	identifier;
		[Tooltip("Indicates that specified identifier is unified value, which can identify leaderboard across all the supported platform. Works only if, leaderboard id collection was configured in NPSettings.")]
		public	FsmBool		isGlobalIdentifier;
		[Tooltip("A filter used to restrict the search to a subset of the users on game server.")]
		public	eLeaderboardUserScope	userScope;
		[Tooltip("A filter used to restrict the search to scores that were posted within a specific period of time.")]
		public	eLeaderboardTimeScope	timeScope;
		[Tooltip("The value indicates maximum entries that has to be fetched from search.")]
		public	FsmInt		maxResults;
		[Tooltip("If enabled, user profile pictures are loaded from disk.")]
		public	FsmBool		loadUserImages;		

		[ActionSection("Results")]
		
		[Tooltip("The count of score objects retrieved.")]
		[UIHint(UIHint.Variable)]
		public	FsmInt		count;
		[Tooltip("The description of the problem that occured.")]
		[UIHint(UIHint.Variable)]
		public	FsmString	error;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when request is completed successfully.")]
		public 	FsmEvent 	successEvent;
		[Tooltip("Event to send when request failed with error.")]
		public 	FsmEvent 	failedEvent;
		
		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			identifier			= null;
			isGlobalIdentifier	= true;
			userScope			= eLeaderboardUserScope.GLOBAL;
			timeScope			= eLeaderboardTimeScope.ALL_TIME;
			maxResults			= 20;
			loadUserImages		= false;

			// Results properties	
			count			= new FsmInt {
				UseVariable	= true
			};
			error				= new FsmString {
				UseVariable	= true
			};
			
			// Event properties
			successEvent		= null;
			failedEvent			= null;
		}

		#endregion

		#region Methods

#if USES_GAME_SERVICES
		protected Leaderboard CreateLeaderboard ()
		{
			// Create instance
			Leaderboard	_newLeaderboard	= null;
			
			if (isGlobalIdentifier.Value)
				_newLeaderboard	= NPBinding.GameServices.CreateLeaderboardWithGlobalID(identifier.Value);
			else
				_newLeaderboard	= NPBinding.GameServices.CreateLeaderboardWithID(identifier.Value);

			// Set properties
			_newLeaderboard.UserScope	= userScope;
			_newLeaderboard.TimeScope	= timeScope;
			_newLeaderboard.MaxResults	= maxResults.Value;

			// Add new instance to the collection
			GameServicesUtils.AddLeaderboardToCollection(_newLeaderboard);

			return _newLeaderboard;
		}
#endif

		private void OnActionDidFinish ()
		{
			Fsm.Event(successEvent);
			Finish();
		}

		private void OnActionDidFail ()
		{
			Fsm.Event(failedEvent);
			Finish();
		}

		#endregion

		#region Callback Methods

		protected void OnScoreLoadFinished (Score[] _scores, Score _localUserScore, string _error)
		{
			if (string.IsNullOrEmpty(_error))
			{
				int		_totalScores	= (_scores == null) ? 0 : _scores.Length;

				// Update properties
				count.Value				= _totalScores;
				error.Value				= _error;

				// Check if we have any items to download
				if (!loadUserImages.Value || _totalScores == 0)
				{
					OnActionDidFinish();

					return;
				}
				else
				{
					int	_loadedImagesCount	= 0;
					
					foreach (Score _score in _scores)
					{
						_score.User.GetImageAsync((Texture2D _image, string _downloadError) => {

							// Update download completed count
							_loadedImagesCount++;
							
							// Check if we are done with downloading
							if (_loadedImagesCount == _totalScores)
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
				// Update properties
				count.Value		= 0;
				error.Value		= _error;

				OnActionDidFail();
	
				return;
			}
		}

		#endregion
	}
}