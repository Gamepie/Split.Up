using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using VoxelBusters.NativePlugins.Internal;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	public class AddressBookUtils
	{
		#region Static Fields
		
		public	static	AddressBookContact[]	contactsInfoList	= null;

		#endregion

		#region Static Methods
		
		public static int GetContactsCount ()
		{
			if (contactsInfoList == null)
				return 0;
			
			return contactsInfoList.Length;
		}
		
		#endregion
	}
}