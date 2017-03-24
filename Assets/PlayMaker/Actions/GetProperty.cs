// (c) Copyright HutongGames, LLC 2010-2013. All rights reserved.

#if !UNITY_FLASH

using UnityEngine;

#if UNITY_EDITOR
using HutongGames.PlayMaker.Ecosystem.Utils;
#endif

namespace HutongGames.PlayMaker.Actions
{
	[ActionCategory(ActionCategory.UnityObject)]
    [ActionTarget(typeof(Component), "targetProperty")]
    [ActionTarget(typeof(GameObject), "targetProperty")]
	[Tooltip("Gets the value of any public property or field on the targeted Unity Object and stores it in a variable. E.g., Drag and drop any component attached to a Game Object to access its properties.")]
	public class GetProperty : FsmStateAction
	{
		public FsmProperty targetProperty;
		public bool everyFrame;

		#if UNITY_EDITOR
		public bool debug;
		#endif


		public override void Reset()
		{
			targetProperty = new FsmProperty { setProperty = false };
			everyFrame = false;

			#if UNITY_EDITOR
			debug = false;
			#endif
		}

		public override void OnEnter()
		{
			targetProperty.GetValue();

			#if UNITY_EDITOR
			if (debug || LinkerData.DebugAll )
			{
				
				UnityEngine.Debug.Log("<Color=blue>GetProperty</Color> on "+this.Fsm.GameObjectName+":"+this.Fsm.Name+"\n" +
				                      "<Color=red>TargetType</Color>\t\t"+ targetProperty.TargetTypeName+"\n" +
				                      "<Color=red>Assembly</Color>\t\t"+targetProperty.TargetType.Assembly.FullName+"\n" +
				                      "<Color=red>Property</Color>\t\t\t"+targetProperty.PropertyName+" <"+ targetProperty.PropertyType+">\n" );
				
				LinkerData.RegisterClassDependancy(targetProperty.TargetType,targetProperty.TargetTypeName);
			}
			#endif

			if (!everyFrame)
			{
				Finish();
			}
		}

		public override void OnUpdate()
		{
			targetProperty.GetValue();
		}

#if UNITY_EDITOR
        public override string AutoName()
        {
            var name = string.IsNullOrEmpty(targetProperty.PropertyName) ? "[none]" : targetProperty.PropertyName;
            return "Get Property: "+ name;
            //var value = ActionHelpers.GetValueLabel(targetProperty.GetVariable());
            //return string.Format("Get {0} to {1}", name, value);
        }
#endif
	}
}

#endif