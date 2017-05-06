using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Gets the title of specified leaderboard. This property is invalid until a call to load scores is completed.")]
	public class GameServicesLeaderboardGetTitle : FsmStateAction 
	{
		#region Fields
		
		[ActionSection("Setup")]
		
		[RequiredField]
		[Tooltip("A string used to identify the leaderboard.")]
		public	FsmString	identifier;
		[Tooltip("Indicates that specified identifier is unified value, which can identify leaderboard across all the supported platform. Works only if, leaderboard id collection was configured in NPSettings.")]
		public	FsmBool		isGlobalIdentifier;

		[ActionSection("Results")]
		
		[Tooltip("The localized title of the leaderboard.")]
		[UIHint(UIHint.Variable)]
		public 	FsmString 	title;
		
		#endregion
		
		#region FSM Methods

		public override void Reset ()
		{
			// Setup properties
			identifier			= null;
			isGlobalIdentifier	= true;

			// Results properties
			title				= new FsmString {
				UseVariable	= true
			};
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
#if USES_GAME_SERVICES
			Leaderboard	_leaderboard	= GameServicesUtils.GetLeaderboard(identifier.Value, isGlobalIdentifier.Value);
			
			if (_leaderboard == null)
			{
				Log("[GameServices] Leaderboard is null.");
				title.Value	= null;
			}
			else
			{
				title.Value	= _leaderboard.Title;
			}
#endif
		}

		#endregion
	}
}