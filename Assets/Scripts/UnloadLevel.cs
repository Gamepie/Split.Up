// (c) Copyright HutongGames, LLC 2010-2013. All rights reserved.

using UnityEngine;
using VoxelBusters.NativePlugins;
using UnityEngine.SceneManagement;



namespace HutongGames.PlayMaker.Actions
{
		[ActionCategory("Scenemanager")]
	[Tooltip("Unload")]
	public class UnloadLevel : FsmStateAction

	
	{
		


		public override void OnEnter ()
			{

			SceneManager.UnloadSceneAsync (SceneManager.GetActiveScene().name);
			Debug.Log ("unloaded");
			Finish();

			}
				

		}
		
}
