using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Presents a view that offers various social share services that your app user can choose to share.")]
	public class SharingShowSocialShareSheet : SharingShowShareSheet 
	{
		#region FSM Methods
		
		public override void Reset () 
		{
			base.Reset();

#if USES_SHARING
			// Set default option
			excludedOptions	= new eShareOptions[] {
				eShareOptions.MAIL,
				eShareOptions.MESSAGE,
				eShareOptions.WHATSAPP
			};
#endif
		}
		
		#endregion
	}
}