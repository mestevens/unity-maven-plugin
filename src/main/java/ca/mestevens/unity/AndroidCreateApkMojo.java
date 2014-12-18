package ca.mestevens.unity;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import ca.mestevens.unity.utils.ProcessRunner;

/**
 * Goal which builds the android project from unity
 *
 * @goal unity-android-package-apk
 * 
 * @phase package
 */
public class AndroidCreateApkMojo extends AbstractMojo {
	
	/**
	 * @parameter property="unity.project.name" default-value="${project.artifactId}"
	 * @readonly
	 * @required
	 */
	public String unityProjectName;
	
	/**
	 * @parameter property="project"
	 * @readonly
	 * @required
	 */
	public MavenProject project;
	
	/**
	 * @parameter property="android.emulator.name" default-value="default"
	 * @readonly
	 * @required
	 */
	public String avdDeviceName;
	
	/**
	 * @parameter property="android.start.emulator" default-value="false"
	 * @readonly
	 * @required
	 */
	public boolean startEmulator;
	
	/**
	 * @parameter property="android.emulator.wait.time" default-value="60000"
	 * @readonly
	 * @required
	 */
	public String emulatorWaitTime;
	
	/**
	 * @parameter property="android.deploy.to.devices" default-value="false"
	 * @readonly
	 * @required
	 */
	public boolean deployToDevices;

	public void execute() throws MojoExecutionException, MojoFailureException {
		ProcessRunner processRunner = new ProcessRunner(getLog());
		String workingDirectory = project.getBasedir().getAbsolutePath() + "/target/" + unityProjectName;
		List<String> commandList = new ArrayList<String>();
		commandList.add("mvn");
		commandList.add("clean");
		commandList.add("package");
		if (startEmulator) {
			commandList.add("android:emulator-start");
			commandList.add("-Dandroid.emulator.avd=" + avdDeviceName);
			commandList.add("-Dandroid.emulator.wait=" + emulatorWaitTime);
		}
		if (deployToDevices) {
			commandList.add("android:deploy");
		}
		processRunner.killProcessWithName("Unity");
		int returnValue = processRunner.runProcess(workingDirectory, commandList.toArray(new String[commandList.size()]));
		processRunner.checkReturnValue(returnValue);
	}

}
