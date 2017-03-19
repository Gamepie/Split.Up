using System.Net;

namespace HutongGames.PlayMaker.Actions
{
	[ActionCategory(ActionCategory.Device)]
	[Tooltip("Check for internet connection.")]
	public class CheckForInternet : FsmStateAction
	{
		[RequiredField]
		[UIHint(UIHint.Variable)]

        [Tooltip("Send event if internet is available.")]
		public FsmEvent internetIsOn;

        [Tooltip("Send event if internet is not available.")]
		public FsmEvent internetIsOff;

		public override void Reset()
		{
			internetIsOn = null;
			internetIsOff = null;
		}

		public override void OnEnter()
		{
			try
			{
				Dns.GetHostEntry("www.google.com");
				Fsm.Event(internetIsOn);
				Finish();
			}
			catch
			{
				Fsm.Event(internetIsOff);
				Finish();
			}
		}	
	}
}