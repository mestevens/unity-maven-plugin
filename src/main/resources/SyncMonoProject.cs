using UnityEngine;
using System.Collections;
using UnityEditor;

namespace ca.mestevens.unity
{

	public class SyncMonoProject
	{

		public static void SyncProject() {
			EditorApplication.ExecuteMenuItem ("Assets/Sync MonoDevelop Project");
		}

	}

}