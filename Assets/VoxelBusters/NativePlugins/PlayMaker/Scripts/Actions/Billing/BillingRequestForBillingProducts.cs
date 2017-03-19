using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	[ActionCategory("VB - Cross Platform Native Plugins")]
	[Tooltip("Sends the request to retrieve localized information about billing products from the Store.")]
	public class BillingRequestForBillingProducts : FsmStateAction 
	{
		#region FSM Methods
		
		public override void OnEnter ()
		{
#if USES_BILLING
			NPBinding.Billing.RequestForBillingProducts();
#endif

			Finish();
		}
		
		#endregion
	}
}