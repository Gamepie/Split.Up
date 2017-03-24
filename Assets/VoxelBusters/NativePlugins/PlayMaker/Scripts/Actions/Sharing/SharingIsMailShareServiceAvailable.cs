using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Determines whether mail share service is available.")]
	public class SharingIsMailShareServiceAvailable : SharingIsShareServiceAvailableBase 
	{
		#region FSM Methods
		
		public override void Reset ()
		{
			base.Reset();

#if USES_SHARING
			// Set default option
			m_shareOption	= eShareOptions.MAIL;
#endif
		}
		
		#endregion
	}
}