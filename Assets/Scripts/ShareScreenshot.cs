using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using VoxelBusters.NativePlugins;
using VoxelBusters.Utility;


public class ShareScreenshot : MonoBehaviour {

	public 	string			m_shareMessage		= "message...";
	public  bool share_done = false;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

	void ShareScreenShotUsingShareSheet ()
	{
		// Create share sheet
		ShareSheet _shareSheet     = new ShareSheet();    
		_shareSheet.Text        = m_shareMessage;


		// Attaching screenshot here
		_shareSheet.AttachScreenShot();

		// Show composer
		NPBinding.UI.SetPopoverPointAtLastTouchPosition();
		NPBinding.Sharing.ShowView (_shareSheet, FinishedSharing);
	}

	void FinishedSharing (eShareResult _result)
	{
		Debug.Log("Finished sharing");
		Debug.Log("Share Result = " + _result);
		share_done = true;
	}

	void resetsharebool ()
	{
		share_done = false;
	}


}
