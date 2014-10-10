using UnityEditor;
using System.IO;

namespace ca.mestevens.unity
{
	
	public class IOSBuildScript
	{
		
		static void GenerateXcodeProject()
		{
			EditorApplication.ExecuteMenuItem("Assets/Sync MonoDevelop Project");
			
			string[] arguments = System.Environment.GetCommandLineArgs ();
			string[] scenes = null;
			string targetDirectory = "Assets/../target";
			foreach (string str in arguments)
			{
				if (str.StartsWith ("-D")) {
					string tempStr = str.Substring(2);
					string[] keyValue = tempStr.Split('=');
					if (keyValue[0].Equals("scenes")) {
						scenes = keyValue[1].Split(',');
					}
					if (keyValue[0].Equals("iosProjectTargetDirectory")) {
						targetDirectory = keyValue[1];
					}
				}
			}
			
			if (scenes == null) {
				scenes = Directory.GetFiles ("Assets", "*.unity", System.IO.SearchOption.AllDirectories);
			}
			
			BuildPipeline.BuildPlayer (scenes, targetDirectory, BuildTarget.iPhone, BuildOptions.None);
		}
		
	}
	
}