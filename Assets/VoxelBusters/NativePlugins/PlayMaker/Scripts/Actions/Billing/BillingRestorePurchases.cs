using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sends a request to restore completed purchases.")]
	public class BillingRestorePurchases : FsmStateAction 
	{
		#region FSM Methods
		
		public override void OnEnter ()
		{
#if USES_BILLING
			NPBinding.Billing.RestorePurchases();
#endif

			Finish();
		}
		
		#endregion
	}
}