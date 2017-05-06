using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace VoxelBusters.NativePlugins {
public class authenticate : MonoBehaviour {

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

	public void authenticateplz(){
	//Authenticate Local User
	NPBinding.GameServices.LocalUser.Authenticate((bool _success, string _error)=>{

		if (_success)
		{
			Debug.Log("Sign-In Successfully");
			Debug.Log("Local User Details : " + NPBinding.GameServices.LocalUser.ToString());
		}
		else
		{
			Debug.Log("Sign-In Failed with error " + _error);
		}
	});
	}
}
}
