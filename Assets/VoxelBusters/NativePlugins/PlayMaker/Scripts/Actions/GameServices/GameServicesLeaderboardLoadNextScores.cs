using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sends a request to load next set of scores.")]
	public class GameServicesLeaderboardLoadNextScores : GameServicesLeaderboardLoadScoresBase 
	{
		#region MyRegion

		#endregion

		#region FSM Methods
		
		public override void OnEnter ()
		{
#if USES_GAME_SERVICES
			Leaderboard _leaderboard	= CreateLeaderboard();

			// Start request
			_leaderboard.LoadMoreScores(eLeaderboardPageDirection.NEXT, OnScoreLoadFinished);
#endif
		}
		
		#endregion
	}
}