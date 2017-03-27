using UnityEngine;

namespace HutongGames.PlayMaker.Actions
{
	[ActionCategory(ActionCategory.Device)]
	[Tooltip("Sets the target frame rate")]
	public class TargetFrameRate : FsmStateAction {
	
	    [RequiredField]
		public FsmInt intValue;
	
		public override void Reset()
		{
			intValue = 30;
		}

	// Code that runs on entering the state.
	public override void OnEnter()
	{
		
		Application.targetFrameRate = intValue.Value;
		Finish();
	}


}
}