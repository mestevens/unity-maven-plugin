package ca.mestevens.unity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import ca.mestevens.unity.utils.ProcessRunner;

/**
 * Goal which builds the android project from unity
 *
 * @goal unity-build-dll
 * 
 * @phase compile
 */
public class UnityBuildDllMojo extends AbstractMojo {
	
	/**
	 * @parameter property="xbuild.location" default-value="/Applications/Unity/Unity.app/Contents/Frameworks/MonoBleedingEdge/bin/xbuild"
	 * @readonly
	 * @required
	 */
	public String xbuildLocation;
	
	/**
	 * @parameter property="unity.solution.name" default-value="${project.artifactId}.sln"
	 * @readonly
	 * @required
	 */
	public String unitySolutionName;
	
	/**
	 * @parameter property="unity.dll.name" default-value="${project.artifactId}-${project.version}"
	 * @readonly
	 * @required
	 */
	public String unityDllName;
	
	/**
	 * @parameter property="project"
	 * @readonly
	 * @required
	 */
	public MavenProject project;

	public void execute() throws MojoExecutionException, MojoFailureException {
		ProcessRunner processRunner = new ProcessRunner(getLog());
		int returnValue = processRunner.runProcess(project.getBasedir().getAbsolutePath(), xbuildLocation, unitySolutionName, "/p:OutputPath=target",
				"/p:AssemblyName=" + unityDllName);
		processRunner.checkReturnValue(returnValue);
	}

}
