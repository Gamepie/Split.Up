// (c) Copyright HutongGames, LLC 2010-2013. All rights reserved.

using UnityEngine;
using VoxelBusters.NativePlugins;



namespace HutongGames.PlayMaker.Actions
{
	namespace VoxelBusters.NativePlugins	{
		[ActionCategory("GameServices")]
	[Tooltip("Loads achievements")]
	public class GS_load_achievements : FsmStateAction

	{

			public override void OnEnter ()
			{
			
				NPBinding.GameServices.LoadAchievements((Achievement[] _achievements, string _error)=>{

                if (_achievements == null)
                {
                    Debug.Log("Couldn't load achievement list with error = " + _error);
                    return;
                }

                int        _achievementCount    = _achievements.Length;
                Debug.Log(string.Format("Successfully loaded achievement list. Count={0}.", _achievementCount));

                for (int _iter = 0; _iter < _achievementCount; _iter++)
                {
                    Debug.Log(string.Format("[Index {0}]: {1}", _iter, _achievements[_iter]));
                }
            });

			}



		}


	}
}
