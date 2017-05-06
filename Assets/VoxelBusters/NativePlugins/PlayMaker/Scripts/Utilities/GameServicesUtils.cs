using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using VoxelBusters.NativePlugins.Internal;
using GameServicesFeatureUtils = VoxelBusters.NativePlugins.Internal.GameServicesUtils;


namespace VoxelBusters.NativePlugins.PlayMaker
{
	public class GameServicesUtils
	{
		#region Static Fields

		public	static	Achievement[]					achievementsList		= null;
		private	static 	Dictionary<string, Leaderboard> leaderboardCollection	= new Dictionary<string, Leaderboard>();

		#endregion

		#region Static Methods
		
		public static Leaderboard GetLeaderboard (string _identifier, bool _isGlobalID)
		{
			string		_globalID		= _isGlobalID ? _identifier : GameServicesFeatureUtils.GetLeaderboardGID(_identifier);
			Leaderboard	_leaderboard	= null;
			
			if (_globalID != null)
				leaderboardCollection.TryGetValue(_globalID, out _leaderboard);
			
			return _leaderboard;
		}
		
		public static void AddLeaderboardToCollection (Leaderboard _newLeaderboard)
		{
			leaderboardCollection[_newLeaderboard.GlobalIdentifier]	= _newLeaderboard;
		}

		#endregion
	}
}