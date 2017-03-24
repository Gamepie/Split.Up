using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class testing : MonoBehaviour {

	// Use this for initialization
	void Start () {
		bool _isAvailable = NPBinding.GameServices.IsAvailable();
		bool _isAuthenticated = NPBinding.GameServices.LocalUser.IsAuthenticated;
		Debug.Log ("booled");
		Debug.Log (_isAvailable);
		Debug.Log (_isAuthenticated);

		if (_isAvailable == true) {
			Debug.Log ("available");
			if (NPBinding.GameServices.LocalUser.IsAuthenticated == false) {
				Debug.Log ("authenticated");

				//Authenticate Local User
				NPBinding.GameServices.LocalUser.Authenticate ((bool _success, string _error) => {

					if (_success) {
						Debug.Log ("Sign-In Successfully");
						Debug.Log ("Local User Details : " + NPBinding.GameServices.LocalUser.ToString ());
					} else {
						Debug.Log ("Sign-In Failed with error " + _error);
					}
				});
			}
		}
	}
	
	// Update is called once per frame
	void Update () {
		
	}
}
