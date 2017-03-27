using UnityEngine;
using System.Collections;
using HutongGames.PlayMaker;
using HutongGames.PlayMaker.Actions;
using Tooltip	= HutongGames.PlayMaker.TooltipAttribute;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	public abstract class SharingIsShareServiceAvailableBase : FsmStateAction 
	{
		#region Fields

		[ActionSection("Results")]
		
		[Tooltip("Indicates whether service is available for use.")]
		[UIHint(UIHint.Variable)]
		public 		FsmBool 		isAvailable;

		[ActionSection("Events")]

		[Tooltip("Event to send when service is available.")]
		public 		FsmEvent 		availableEvent;
		[Tooltip("Event to send when service is not available.")]
		public 		FsmEvent 		notAvailableEvent;

		protected	eShareOptions 	m_shareOption;

		#endregion

		#region FSM Methods
		
		public override void Reset() 
		{
			// Results properties
			isAvailable		= new FsmBool {
				UseVariable	= true
			};

			// Events properties
			availableEvent		= null;
			notAvailableEvent	= null;
		}
		
		public override void OnEnter() 
		{
			DoAction();

			Finish();
		}

		#endregion

		#region Methods

		private void DoAction ()
		{
#if USES_SHARING
			bool _isAvailable 	= false;

			switch (m_shareOption)
			{
			case eShareOptions.MAIL:
				_isAvailable	= NPBinding.Sharing.IsMailServiceAvailable();
				break;

			case eShareOptions.MESSAGE:
				_isAvailable	= NPBinding.Sharing.IsMessagingServiceAvailable();
				break;

			case eShareOptions.FB:
				_isAvailable	= NPBinding.Sharing.IsFBShareServiceAvailable();
				break;
				
			case eShareOptions.TWITTER:
				_isAvailable	= NPBinding.Sharing.IsTwitterShareServiceAvailable();
				break;

			case eShareOptions.WHATSAPP:
				_isAvailable	= NPBinding.Sharing.IsWhatsAppServiceAvailable();
				break;

			default:
				Log("[Sharing] Unhandled service.");
				break;
			}

			// Update property
			isAvailable.Value	= _isAvailable;

			// Send event
			Fsm.Event(_isAvailable ? availableEvent : notAvailableEvent);
#endif
		}

		#endregion
	}
}