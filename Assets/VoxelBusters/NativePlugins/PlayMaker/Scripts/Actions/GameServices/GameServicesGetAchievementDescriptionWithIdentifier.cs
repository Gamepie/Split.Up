using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using VoxelBusters.NativePlugins.Internal;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the properties of the specified achievement and stores it in variable.")]
	public class GameServicesGetAchievementDescriptionWithIdentifier : FsmStateAction 
	{
		#region Fields

		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("A string used to identify the achievement.")]
		public	FsmString	identifier;
		[Tooltip("Indicates that specified identifier is unified value, which can identify achievement across all the supported platform. Works only if, achievement id collection was configured in NPSettings.")]
		public	FsmBool		isGlobalIdentifier;
		[Tooltip("If enabled, achievement's image is loaded from the disk.")]
		public FsmBool		loadImage;

		[ActionSection("Results")]
		
		[Tooltip("Localized title of the achievement.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	title;
		[Tooltip("Localized description of the achievement to be used upon achievement completion.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	achievedDescription;
		[Tooltip("Localized description of the achievement to be used when user has not completed achievement.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	unachievedDescription;
		[Tooltip("Indicates whether this achievement is initially visible to users.")]
		[UIHint(UIHint.Variable)]
		public 	FsmBool	 	isHidden;
		[Tooltip("The achievement's image.")]
		[UIHint(UIHint.Variable)]
		public	FsmTexture	image;

		[ActionSection("Events")]
		
		[Tooltip("Event to send when action fails to find specified achievement.")]
		public 	FsmEvent 	failedEvent;
		
		#endregion

		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			identifier				= null;
			isGlobalIdentifier		= true;
			
			// Results properties
			title					= new FsmString {
				UseVariable	= true
			};
			achievedDescription		= new FsmString {
				UseVariable	= true
			};
			unachievedDescription	= new FsmString {
				UseVariable	= true
			};
			isHidden				= new FsmBool {
				UseVariable	= true
			};
			
			// Events properties
			failedEvent				= null;
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
			try
			{
				// Find description object
				AchievementDescription _description		= null;

				if (isGlobalIdentifier.Value)
					_description			= AchievementHandler.GetAchievementDescriptionWithGlobalID(identifier.Value);
				else
					_description			= AchievementHandler.GetAchievementDescriptionWithID(identifier.Value);

				// Update properties
				title.Value					= _description.Title;
				achievedDescription.Value	= _description.AchievedDescription;
				unachievedDescription.Value	= _description.UnachievedDescription;
				isHidden.Value				= _description.IsHidden;

				// Check if image has to be download
				if (loadImage.Value)
				{
					_description.GetImageAsync((Texture2D _image, string _error) => {
						
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
				
				return;
			}
			catch (System.Exception _exception)
			{
				Debug.Log(_exception.Message);

				OnActionDidFail();

				return;
			}
#endif
		}
		
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