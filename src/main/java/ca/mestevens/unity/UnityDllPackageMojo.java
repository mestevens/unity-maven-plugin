package ca.mestevens.unity;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which generates your framework dependencies in the target directory.
 *
 * @goal unity-package-dll
 * 
 * @phase package
 */
public class UnityDllPackageMojo extends AbstractMojo {
	
	/**
	 * @parameter property="project"
	 * @readonly
	 * @required
	 */
	public MavenProject project;
	
	/**
	 * @parameter property="unity.dll.name" default-value="${project.artifactId}-${project.version}"
	 * @readonly
	 * @required
	 */
	public String unityDllName;

	public void execute() throws MojoFailureException {
		File dllFile = new File(project.getBasedir().getAbsolutePath() + "/target/" + unityDllName + ".dll");
		if (!dllFile.exists()) {
			throw new MojoFailureException("Dll file does not exist: " + "");
		}
		project.getArtifact().setFile(dllFile);
	}

}
