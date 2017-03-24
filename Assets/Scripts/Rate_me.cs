using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using PaperPlaneTools;

public class Rate_me : MonoBehaviour {

		public int minSessionCount = 0;

		public int minCustomEventsCount = 0;

		public float delayAfterInstallInHours = 0f;

		public float delayAfterLaunchInHours = 0f;

		public float postponeCooldownInHours = 22f;

		public bool requireInternetConnection = true;

		public string title = "Like the game?";

		public string message = "Take a moment to rate us!";

		public string rateButton = "Rate";

		public string postponeButton = "Later";

		public string rejectButton = "";

		public string appStoreAppId = "";

		public string playMarketAppBundleId = "";

		public GameObject customUIWindow;
		
		public bool clearOnStart = false;
		
		public bool logDebugMessages = true;
		
		
		
		





	// Use this for initialization
	void Start () {
		string rateUrl = RateBox.GetStoreUrl (appStoreAppId, playMarketAppBundleId); 
		//Debug settings are only allowed inside development environment
			#if (UNITY_EDITOR)
				if (clearOnStart) {
					RateBox.Instance.ClearStatistics(); 
				}
				RateBox.Instance.DebugMode = logDebugMessages;
			#else
				RateBox.Instance.DebugMode = false;
			#endif
		var rejectButtonTrimmed = rejectButton.Trim ();
					RateBox.Instance.Init (
				rateUrl, 
				new RateBoxConditions() {
					MinSessionCount = minSessionCount,
					MinCustomEventsCount = minCustomEventsCount,
					DelayAfterInstallInSeconds = Mathf.CeilToInt(delayAfterInstallInHours * 3600),
					DelayAfterLaunchInSeconds = Mathf.CeilToInt(delayAfterLaunchInHours * 3600),
					PostponeCooldownInSeconds = Mathf.CeilToInt(postponeCooldownInHours * 3600),
					RequireInternetConnection = requireInternetConnection
				},
				new RateBoxTextSettings() {
					Title = title.Trim(),
					Message = message.Trim(),
					RateButtonTitle = rateButton.Trim(),
					PostponeButtonTitle = postponeButton.Trim(),
					RejectButtonTitle = rejectButtonTrimmed.Length > 0 ? rejectButtonTrimmed.Trim() : null
				}
			);
			
			IAlertPlatformAdapter alertAdapter = null;
			if (customUIWindow != null) {
				customUIWindow.SetActive(false);
				alertAdapter = customUIWindow.GetComponent<IAlertPlatformAdapter>();
			}
		RateBox.Instance.AlertAdapter = alertAdapter;

	}
	
	// Update is called once per frame
	void Update () {
		
	}

	void rate () {
		RateBox.Instance.ForceShow ();
	}

	void ratenonf () {
		RateBox.Instance.Show ();
	}

	public void incrementcounter()
	{
		RateBox.Instance.IncrementCustomCounter (); 
		
	}


}
