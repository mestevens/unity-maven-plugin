package ca.mestevens.unity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

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
		File iOSFiles = new File(project.getBasedir().getAbsolutePath() + "/Plugins/iOS");
		if (iOSFiles.exists()) {
			File zippedFile = new File(targetDirectory + "/" + unityDllName + ".ios-plugin");
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
					"zip -r ../" + unityDllName + ".ios-plugin" + " * -x *.meta -x *.DS_Store");
			if (returnValue != 0) {
				getLog().error("Could not zip file: " + iOSFiles.getAbsolutePath());
				throw new MojoFailureException("Could not zip file: " + iOSFiles.getAbsolutePath());
			}
		}
		
		File androidFiles = new File(project.getBasedir().getAbsolutePath() + "/Plugins/Android");
		if (androidFiles.exists()) {
			File zippedFile = new File(targetDirectory + "/" + unityDllName + ".android-plugin");
			if (zippedFile.exists()) {
				try {
					FileUtils.deleteDirectory(zippedFile);
				} catch (IOException e) {
					getLog().error("Error deleting directory");
					getLog().error(e.getMessage());
					throw new MojoFailureException("Error deleting directory");
				}
			}
			File targetFiles = new File(targetDirectory + "/" + unityDllName + "-android-plugin");
			try {
				FileUtils.copyDirectory(androidFiles, targetFiles);
			} catch (IOException e) {
				getLog().error("Error copying directory");
				getLog().error(e.getMessage());
				throw new MojoFailureException("Error copying directory");
			}
			ProcessRunner processRunner = new ProcessRunner(getLog());
			int returnValue = processRunner.runProcess(targetDirectory + "/" + unityDllName + "-android-plugin", "/bin/sh", "-c",
					"zip -r ../" + unityDllName + ".android-plugin" + " * -x *.meta -x *.DS_Store");
			if (returnValue != 0) {
				getLog().error("Could not zip file: " + androidFiles.getAbsolutePath());
				throw new MojoFailureException("Could not zip file: " + androidFiles.getAbsolutePath());
			}
		}
		File unityLibraryFile = new File(targetDirectory + "/" + unityDllName + ".unity-library");
		List<String> unityLibraryZipCommand = new ArrayList<String>();
		unityLibraryZipCommand.add("zip");
		unityLibraryZipCommand.add("-rj");
		unityLibraryZipCommand.add(unityDllName + ".unity-library");
		unityLibraryZipCommand.add(unityDllName + ".dll");
		if (iOSFiles.exists()) {
			unityLibraryZipCommand.add(unityDllName + ".ios-plugin");
		}
		if (androidFiles.exists()) {
			unityLibraryZipCommand.add(unityDllName + ".android-plugin");
		}
		File sourceFile = new File(String.format("%s/.scm-commands", project.getBasedir().getAbsolutePath()));
		if (sourceFile.exists()) {
			//Replace ${project.version}
			String scmFile;
			try {
				scmFile = new String(Files.readAllBytes(Paths.get(sourceFile.getAbsolutePath())));
				scmFile = scmFile.replace("${project.version}", project.getVersion());
				Files.write(Paths.get(targetDirectory + "/" + sourceFile.getName()), scmFile.getBytes(), StandardOpenOption.CREATE);
				unityLibraryZipCommand.add(targetDirectory + "/" + sourceFile.getName());
			} catch (IOException ex) {
				throw new MojoFailureException(ex.getMessage());
			}
		}
		ProcessRunner processRunner = new ProcessRunner(getLog());
		int returnValue = processRunner.runProcess(targetDirectory, unityLibraryZipCommand.toArray(new String[unityLibraryZipCommand.size()]));
		if (returnValue != 0) {
			throw new MojoFailureException("Could not zip file: " + unityLibraryFile.getAbsolutePath());
		}
		project.getArtifact().setFile(unityLibraryFile);
	}

}
