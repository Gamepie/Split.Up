using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Loads the achievement descriptions from game server.")]
	public class GameServicesLoadAchievementDescriptions : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]

		[Tooltip("If enabled, achievement's images are loaded from disk.")]
		public 	FsmBool		loadImages;

		[ActionSection("Results")]
		
		[Tooltip("The count of description objects retrieved.")]
		[UIHint(UIHint.Variable)]
		public	FsmInt		count;
		[Tooltip("The description of the problem that occurred.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	error;
		
		[ActionSection("Events")]
		
		[Tooltip("Event to send when achievement descriptions are retrieved from game server.")]
		public 	FsmEvent 	successEvent;
		[Tooltip("Event to send when request failed with error.")]
		public 	FsmEvent 	failedEvent;
		
		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			loadImages		= false;

			// Results properties
			count			= new FsmInt {
				UseVariable	= true
			};
			error			= new FsmString {
				UseVariable	= true
			};
			
			// Events properties
			successEvent	= null;
			failedEvent		= null;
		}

		public override void OnEnter ()
		{
#if USES_GAME_SERVICES
			NPBinding.GameServices.LoadAchievementDescriptions(OnLoadDescriptionsFinished);
#endif
		}

		#endregion

		#region Methods
		
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

#if USES_GAME_SERVICES
		private void OnLoadDescriptionsFinished (AchievementDescription[] _descriptions, string _error)
		{
			if (string.IsNullOrEmpty(_error))
			{
				int	_totalDescriptions		= _descriptions.Length;

				// Update properties
				count.Value					= _totalDescriptions;
				error.Value					= _error;

				// Check if we have any items to download
				if (!loadImages.Value || _totalDescriptions == 0)
				{
					OnActionDidFinish();
					
					return;
				}
				else
				{
					int	_loadedImagesCount	= 0;

					foreach (AchievementDescription _description in _descriptions)
					{
						_description.GetImageAsync((Texture2D _image, string _downloadError) => {
							
							// Update download completed count
							_loadedImagesCount++;
							
							// Check if we are done with downloading
							if (_loadedImagesCount == _totalDescriptions)
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
#endif

		#endregion
	}
}