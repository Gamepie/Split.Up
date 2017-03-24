// (c) Copyright HutongGames, LLC 2010-2013. All rights reserved.

using UnityEngine;
using VoxelBusters.NativePlugins;





namespace HutongGames.PlayMaker.Actions
{
	namespace VoxelBusters.NativePlugins	{
		[ActionCategory("GameServices")]
	[Tooltip("report achievements")]
	public class GS_fetch_achievements : FsmStateAction

	{
			
			[Tooltip("A string used to identify the achievement.")]


			public override void OnEnter ()
			{

				NPBinding.GameServices.LoadAchievementDescriptions((AchievementDescription[] _descriptions, string _error)=>{

                if (_descriptions == null)
                {
                    Debug.Log("Couldn't load achievement description list with error = " + _error);
                    return;
                }

                int        _descriptionCount    = _descriptions.Length;
                Debug.Log(string.Format("Successfully loaded achievement description list. Count={0}.", _descriptionCount));

                for (int _iter = 0; _iter < _descriptionCount; _iter++)
                {
                    Debug.Log(string.Format("[Index {0}]: {1}", _iter, _descriptions[_iter]));
                }
            });
			}


		}
	}
}