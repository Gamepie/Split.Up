using UnityEngine;
using System.Collections;
using Heyzap;
public class AdsManager : MonoBehaviour {

public static bool showOnce = false;

	// Use this for initializationd 


	void starheyzap () {
		HeyzapAds.Start ("35848a20ffa97036bf2d562bcc9641ce", HeyzapAds.FLAG_DISABLE_AUTOMATIC_FETCHING);
	}

	
	void FetchAdGameOver () {
		HZInterstitialAd.Fetch("GameOver");

	}
	
	void FetchAdMainMenu () {
		HZInterstitialAd.Fetch("MainMenu");

	}
	
	void ShowAdBanner () {
		HZBannerShowOptions showOptions = new HZBannerShowOptions ();
		showOptions.Position = HZBannerShowOptions.POSITION_BOTTOM;
		showOptions.SelectedAdMobSize = HZBannerShowOptions.AdMobSize.SMART_BANNER;
		showOptions.Tag = "Banner";
		HZBannerAd.ShowWithOptions (showOptions);

	}
	
	void DestroyAdBanner () {
		HZBannerAd.Destroy ();

	}
	void HideAdBanner () {
		HZBannerAd.Hide ();
	}
	
	void ShowAdMenu () {
			if (HZInterstitialAd.IsAvailable("MainMenu")) {
				if (!showOnce) {
				//HZShowOptions showOptions = new HZShowOptions();
				//showOptions.Tag = "MainMenu";
				//HZInterstitialAd.ShowWithOptions(showOptions);
				HZInterstitialAd.Show();

		}
		showOnce = true;
			}
	}
	void ShowAdGameOver () {
			if (HZInterstitialAd.IsAvailable("GameOver")) {
					
				//HZShowOptions showOptions = new HZShowOptions();
				//showOptions.Tag = "GameOver";
				//HZInterstitialAd.ShowWithOptions(showOptions);
				HZInterstitialAd.Show();

		}
	}
	
	void AdTestSuite () {
		HeyzapAds.ShowMediationTestSuite();
	}
}
