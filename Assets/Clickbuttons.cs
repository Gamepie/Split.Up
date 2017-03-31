using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class Clickbuttons : MonoBehaviour {
	public GameObject Rate;
	public GameObject Play;
	public GameObject Settings;
	public GameObject Noads;
	public GameObject rateobj;
	private Button bu;
	private GameObject Twitter;
	private GameObject Leaderboard;
	private GameObject Replay;
	private GameObject No_Ads;
	private BoxCollider2D rate;
	private BoxCollider2D play;
	private BoxCollider2D settings;
	private BoxCollider2D noads;
	private BoxCollider2D twitter;
	private BoxCollider2D leaderboard;
	private BoxCollider2D replay;
	private BoxCollider2D no_ads;

	// Use this for initialization
	void Start () {
		rate = Rate.GetComponent (typeof(BoxCollider2D)) as BoxCollider2D;
		play = Play.GetComponent (typeof(BoxCollider2D)) as BoxCollider2D;
		settings = Settings.GetComponent (typeof(BoxCollider2D)) as BoxCollider2D;
		noads = Noads.GetComponent (typeof(BoxCollider2D)) as BoxCollider2D;
		bu = rateobj.GetComponent (typeof(Button)) as Button;


		bu.onClick.AddListener (fetchobj);
	}

	// Update is called once per frame
	void Update () {
		
	}

	public void activate2dafterdelay(){
		
		Invoke ("activate", 0.2f);
			}

	void activate () {
		if (Rate == null || Play == null || Settings == null || Noads == null){
		}
		else{
		rate.enabled = true;
		play.enabled = true;
		settings.enabled = true;
		noads.enabled = true;

		}
		if (Twitter == null || twitter == null || Replay == null || Leaderboard == null || No_Ads == null ) {
		} 
		else {
			

			twitter.enabled = true;
			leaderboard.enabled = true;
			replay.enabled = true;
			no_ads.enabled = true;
		}
			}
	public void fetchobj (){
		Twitter = GameObject.Find ("Twitter");
		twitter = Twitter.GetComponent (typeof(BoxCollider2D)) as BoxCollider2D;
		Leaderboard = GameObject.Find ("Leaderboard");
		leaderboard = Leaderboard.GetComponent (typeof(BoxCollider2D)) as BoxCollider2D;
		Replay = GameObject.Find ("Replay");
		replay = Replay.GetComponent (typeof(BoxCollider2D)) as BoxCollider2D;
		No_Ads = GameObject.Find ("No Ads");
		no_ads = No_Ads.GetComponent (typeof(BoxCollider2D)) as BoxCollider2D;
		rateobj = GameObject.FindGameObjectWithTag ("Neutral");
		bu = rateobj.GetComponent (typeof(Button)) as Button;
	}

}
