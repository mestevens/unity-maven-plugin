package ca.mestevens.unity;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;

import ca.mestevens.unity.utils.ProcessRunner;

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
	
	/**
	 * @component
	 */
	public MavenProjectHelper mavenProjectHelper;
	
	/**
	 * @parameter property="project.build.directory"
	 * @readonly
	 * @required
	 */
	public String targetDirectory;

	public void execute() throws MojoFailureException {
		File dllFile = new File(targetDirectory + "/" + unityDllName + ".dll");
		if (!dllFile.exists()) {
			throw new MojoFailureException("Dll file does not exist: " + "");
		}
		project.getArtifact().setFile(dllFile);
		File iOSFiles = new File(project.getBasedir().getAbsolutePath() + "/Assets/Plugins/iOS");
		if (iOSFiles.exists()) {
			File zippedFile = new File(targetDirectory + "/" + unityDllName);
			if (zippedFile.exists()) {
				try {
					FileUtils.deleteDirectory(zippedFile);
				} catch (IOException e) {
					getLog().error("Error deleting directory");
					getLog().error(e.getMessage());
					throw new MojoFailureException("Error deleting directory");
				}
			}
			File targetFiles = new File(targetDirectory + "/" + unityDllName + "-ios-plugin");
			try {
				FileUtils.copyDirectory(iOSFiles, targetFiles);
			} catch (IOException e) {
				getLog().error("Error copying directory");
				getLog().error(e.getMessage());
				throw new MojoFailureException("Error copying directory");
			}
			ProcessRunner processRunner = new ProcessRunner(getLog());
			int returnValue = processRunner.runProcess(targetDirectory + "/" + unityDllName + "-ios-plugin", "/bin/sh", "-c",
					"zip -r ../" + unityDllName + " * -x *.meta -x *.DS_Store");
			if (returnValue != 0) {
				getLog().error("Could not zip file: " + iOSFiles.getAbsolutePath());
				throw new MojoFailureException("Could not zip file: " + iOSFiles.getAbsolutePath());
			}
			mavenProjectHelper.attachArtifact(project, "ios-plugin", "ios-plugin", zippedFile);
		}
	}

}
