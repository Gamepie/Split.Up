using UnityEngine;
using System;
using HutongGames.PlayMaker;

namespace VoxelBusters.NativePlugins.PlayMaker
{
	public static class FSMUtils
	{
		#region Methods

		public static string[] ToStringList (this FsmString[] _fsmStringList)
		{
			if (_fsmStringList == null)
				return null;

			int			_count		= _fsmStringList.Length;
			string[] 	_stringList = new string[_count];
			
			for (int _iter = 0; _iter < _count; _iter++)
				_stringList[_iter] 	= (string)_fsmStringList.GetValue(_iter);

			return _stringList;
		}

		public static FsmString[] ToFsmStringList (this string[] _stringList)
		{
			if (_stringList == null)
				return null;
			
			int			_count			= _stringList.Length;
			FsmString[] _fsmStringList 	= new FsmString[_count];
			
			for (int _iter = 0; _iter < _count; _iter++)
				_fsmStringList[_iter] 	= _stringList[_iter];
			
			return _fsmStringList;
		}

		#endregion
	}
}