package ca.mestevens.unity;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import ca.mestevens.unity.utils.DependencyGatherer;
import ca.mestevens.unity.utils.ProcessRunner;

/**
 * Goal which builds the android project from unity
 *
 * @goal unity-android-build
 * 
 * @phase compile
 */
public class UnityAndroidBuildMojo extends AbstractMojo {
	
	/**
	 * @parameter property="unity.path" default-value="/Applications/Unity/Unity.app/Contents/MacOS/Unity"
	 * @readonly
	 * @required
	 */
	public String unity;
	
	/**
	 * @parameter property="android.project.target.directory" default-value="Assets/../target"
	 * @readonly
	 * @required
	 */
	public String androidTarget;
	
	/**
	 * @parameter
	 */
	public List<String> scenes;
	
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
	 * The project's remote repositories to use for the resolution of project
	 * dependencies.
	 * 
	 * @parameter default-value="${project.remoteProjectRepositories}"
	 * @readonly
	 */
	protected List<RemoteRepository> projectRepos;

	/**
	 * The entry point to Aether, i.e. the component doing all the work.
	 * 
	 * @component
	 */
	protected RepositorySystem repoSystem;

	/**
	 * The current repository/network configuration of Maven.
	 * 
	 * @parameter default-value="${repositorySystemSession}"
	 * @readonly
	 */
	protected RepositorySystemSession repoSession;

	public void execute() throws MojoExecutionException, MojoFailureException {	
		File scriptFile = null;
		try {
			InputStream scriptStream = this.getClass().getClassLoader().getResourceAsStream("AndroidBuildScript.cs");
			scriptFile = new File(project.getBasedir().getAbsolutePath() + "/Assets/Editor/AndroidBuildScript.cs");
			FileUtils.copyInputStreamToFile(scriptStream, scriptFile);
			scriptStream.close();
			ProcessRunner processRunner = new ProcessRunner(getLog());
			List<String> commandList = new ArrayList<String>();
			commandList.add(unity);
			commandList.add("-executeMethod");
			commandList.add("ca.mestevens.unity.AndroidBuildScript.GenerateStudioProject");
			if (scenes != null && !scenes.isEmpty()) {
				String scenesString = "-Dscenes=";
				for(String scene : scenes) {
					scenesString += scene + ",";
				}
				scenesString = scenesString.substring(0, scenesString.length() - 1);
				commandList.add(scenesString);
			}
			commandList.add("-DandroidProjectTargetDirectory=" + androidTarget);
			commandList.add("-batchmode");
			commandList.add("-quit");
			commandList.add("-logFile");
			int returnValue = processRunner.runProcess(null, commandList.toArray(new String[commandList.size()]));
			checkReturnValue(returnValue);
			
			InputStream pomStream = this.getClass().getClassLoader().getResourceAsStream("pom-template-android.xml");
			String pomString = IOUtils.toString(pomStream);
			File pomFile = new File(project.getBasedir().getAbsolutePath() + "/target/" + unityProjectName + "/pom.xml");
			
			String pomInfoString = "<groupId>" + project.getGroupId() + "</groupId>";
			pomInfoString += "<artifactId>" + unityProjectName + "</artifactId>";
			pomInfoString += "<version>" + project.getVersion() + "</version>";
			pomString = pomString.replace("<pomInfo></pomInfo>", pomInfoString);
			
			DependencyGatherer dependencyGatherer = new DependencyGatherer(project);
			String pomDependenciesString = dependencyGatherer.createPomDependencySection();
			pomString = pomString.replace("<pomDependencies></pomDependencies>", pomDependenciesString);
			
			String pomRepositoriesString = dependencyGatherer.createPomRepositoriesSection();
			pomString = pomString.replace("<pomRepositories></pomRepositories>", pomRepositoriesString);
			
			FileUtils.writeStringToFile(pomFile, pomString);
			
		} catch (Exception ex) {
			throw new MojoFailureException(ex.getMessage());
		} finally {
			if (scriptFile.exists()) {
				scriptFile.delete();
			}
		}
	}
	
	protected void checkReturnValue(int returnValue) throws MojoFailureException {
		if (returnValue != 0) {
			throw new MojoFailureException("Failed to build project.");
		}
	}

}
