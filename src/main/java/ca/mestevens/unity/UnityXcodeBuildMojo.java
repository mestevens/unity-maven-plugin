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
import ca.mestevens.unity.utils.UnityMenuCommands;

/**
 * Goal which builds the android project from unity
 *
 * @goal unity-xcode-build
 * 
 * @phase compile
 */
public class UnityXcodeBuildMojo extends AbstractMojo {
	
	/**
	 * @parameter property="unity.path" default-value="/Applications/Unity/Unity.app/Contents/MacOS/Unity"
	 * @readonly
	 * @required
	 */
	public String unity;
	
	/**
	 * @parameter property="xcode.project.ouput.directory" default-value="target"
	 * @readonly
	 * @required
	 */
	public String xcodeTarget;
	
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
		File scriptMetaFile = null;
		UnityMenuCommands menuCommands = new UnityMenuCommands(new ProcessRunner(getLog()), unity, project.getBasedir().getAbsolutePath());
		try {
			InputStream scriptStream = this.getClass().getClassLoader().getResourceAsStream("IOSBuildScript.cs");
			scriptFile = new File(project.getBasedir().getAbsolutePath() + "/Assets/Editor/IOSBuildScript.cs");
			scriptMetaFile = new File(project.getBasedir().getAbsolutePath() + "/Assets/Editor/IOSBuildScript.cs.meta");
			FileUtils.copyInputStreamToFile(scriptStream, scriptFile);
			scriptStream.close();
			ProcessRunner processRunner = new ProcessRunner(getLog());
			menuCommands.syncMonoDevelopProject();
			List<String> commandList = new ArrayList<String>();
			commandList.add(unity);
			commandList.add("-projectPath");
			commandList.add(project.getBasedir().getAbsolutePath());
			commandList.add("-executeMethod");
			commandList.add("ca.mestevens.unity.IOSBuildScript.GenerateXcodeProject");
			if (scenes != null && !scenes.isEmpty()) {
				String scenesString = "-Dscenes=";
				for(String scene : scenes) {
					scenesString += scene + ",";
				}
				scenesString = scenesString.substring(0, scenesString.length() - 1);
				commandList.add(scenesString);
			}
			commandList.add("-DiosProjectTargetDirectory=" + xcodeTarget + "/" + unityProjectName + "-ios");
			commandList.add("-batchmode");
			commandList.add("-quit");
			commandList.add("-logFile");
			
			File targetDirectory = new File(xcodeTarget);
			if (!targetDirectory.exists()) {
				FileUtils.forceMkdir(targetDirectory);
			}
			
			processRunner.killProcessWithName("Unity");
			int returnValue = processRunner.runProcess(null, commandList.toArray(new String[commandList.size()]));
			processRunner.checkReturnValue(returnValue);
			
			InputStream pomStream = this.getClass().getClassLoader().getResourceAsStream("pom-template-xcode.xml");
			String pomString = IOUtils.toString(pomStream);
			File pomFile = new File(project.getBasedir().getAbsolutePath() + "/target/" + unityProjectName + "-ios/pom.xml");
			
			String pomInfoString = "<groupId>" + project.getGroupId() + "</groupId>";
			pomInfoString += "<artifactId>" + unityProjectName + "</artifactId>";
			pomInfoString += "<version>" + project.getVersion() + "</version>";
			pomInfoString += "<packaging>xcode-application</packaging>";
			pomString = pomString.replace("<pomInfo></pomInfo>", pomInfoString);
			
			DependencyGatherer dependencyGatherer = new DependencyGatherer(getLog(), project, projectRepos, repoSystem, repoSession);
			String pomDependenciesString = dependencyGatherer.createXcodePomDependencySection();
			pomString = pomString.replace("<pomDependencies></pomDependencies>", pomDependenciesString);
			
			String pomRepositoriesString = dependencyGatherer.createPomRepositoriesSection();
			pomString = pomString.replace("<pomRepositories></pomRepositories>", pomRepositoriesString);
			
			FileUtils.writeStringToFile(pomFile, pomString);
			
			processRunner.runProcess(xcodeTarget + "/" + unityProjectName + "-ios", "mvn", "clean", "initialize", "-Dxcode.add.dependencies", "-Dxcode.project.path=" + xcodeTarget + "/Unity-iPhone.xcodeproj");
			
		} catch (Exception ex) {
			throw new MojoFailureException(ex.getMessage());
		} finally {
			if (scriptFile != null && scriptFile.exists()) {
				scriptFile.delete();
			}
			if (scriptMetaFile != null && scriptMetaFile.exists()) {
				scriptMetaFile.delete();
			}
			menuCommands.syncMonoDevelopProject();
		}
	}

}
