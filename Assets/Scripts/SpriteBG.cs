using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SpriteBG : MonoBehaviour {

	public Sprite HighScore;
	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

	void putsprite () {
		this.GetComponent<SpriteRenderer> ().sprite = HighScore;
		
	}

}
