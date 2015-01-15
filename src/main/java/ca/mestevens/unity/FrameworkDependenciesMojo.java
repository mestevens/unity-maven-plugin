package ca.mestevens.unity;

import ca.mestevens.unity.utils.DependencyGatherer;
import ca.mestevens.unity.utils.ProcessRunner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Goal which generates your framework dependencies in the target directory.
 *
 * @goal unity-library-dependencies
 * 
 * @phase initialize
 */
public class FrameworkDependenciesMojo extends AbstractMojo {

	private static final String UNITY_LIBRARY = "unity-library";

	private static final String DLL = "dll";

	/**
	 * @parameter property="project"
	 * @readonly
	 * @required
	 */
	public MavenProject project;

	/**
	 * @parameter property="unity.plugins.directory" default-value="Assets/Runtime/Plugins"
	 * @readonly
	 * @required
	 */
	public String pluginsDirectory;

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
		getLog().info("Starting execution");
		
		DependencyGatherer dependencyGatherer = new DependencyGatherer(getLog(), project, projectRepos, repoSystem, repoSession);
		List<ArtifactResult> resolvedArtifacts = dependencyGatherer.resolveArtifacts();
		
		File resultFile = new File(String.format("%s/%s", this.project.getBasedir(), this.pluginsDirectory));
		this.getLog().info(String.format("Resolved [%s] artifacts, copying to plugins directory [%s]", resolvedArtifacts.size(), resultFile.getAbsolutePath()));
		
		for (ArtifactResult resolvedArtifact : resolvedArtifacts) {
			Artifact artifact = resolvedArtifact.getArtifact();

			final String typePropertyValue = artifact.getProperty("type", "");

			this.getLog().info(String.format("Copying artifact [%s:%s:%s:%s] to plugins directory [%s]", artifact.getGroupId(),
					artifact.getArtifactId(), artifact.getVersion(), typePropertyValue, resultFile.getAbsolutePath()));
			
			if (typePropertyValue.equals(DLL)) {
				this.copyFile(artifact.getFile(), resultFile);
			} else if (typePropertyValue.equals(UNITY_LIBRARY)) {
				File targetDirectory = new File(String.format("%s/target/unity-libraries", this.project.getBasedir()));
				this.copyFile(artifact.getFile(), targetDirectory);
				File unityLibraryFile = new File(String.format("%s/target/unity-libraries/%s", this.project.getBasedir(), artifact.getFile().getName()));
				File unityLibraryResultFile = new File(String.format("%s/target/unity-libraries/%s", this.project.getBasedir(), artifact.getArtifactId()));
				ProcessRunner processRunner = new ProcessRunner(getLog());
				processRunner.runProcess(null, "unzip", unityLibraryFile.getAbsolutePath(), "-d", unityLibraryResultFile.getAbsolutePath());
				boolean cloneSource = project.getProperties().containsKey(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":clone-source");
				if (cloneSource) {
					File scmFile = new File(String.format("%s/.scm-commands", unityLibraryResultFile.getAbsolutePath()));
					try {
						String scmFileString = new String(Files.readAllBytes(Paths.get(scmFile.getAbsolutePath())));
						String[] commands = scmFileString.split("\n");
						for (String command : commands) {
							String[] commandTokens = command.split(" ");
							FileUtils.mkdir(resultFile.getAbsolutePath() + "/" + project.getArtifactId());
							if (commandTokens.length > 2) {
								if (commandTokens[0].equals("bash") && commandTokens[1].equals("-c")) {
									String[] bashCommand = new String[3];
									bashCommand[0] = commandTokens[0];
									bashCommand[1] = commandTokens[1];
									String thirdCommand = "";
									for (int i = 2; i < commandTokens.length; i++) {
										thirdCommand += commandTokens[i] + " ";
									}
									thirdCommand = thirdCommand.trim();
									bashCommand[2] = thirdCommand;
									processRunner.runProcess(resultFile.getAbsolutePath() + "/" + project.getArtifactId(), bashCommand);
								}
							}
							processRunner.runProcess(resultFile.getAbsolutePath() + "/" + project.getArtifactId(), commandTokens);
						}
					} catch (IOException e) {
						throw new MojoFailureException(String.format("Could not read .scm-commands file [%s]", scmFile.getAbsolutePath()));
					}
				} else {
					File dllFile = new File(String.format("%s/%s", unityLibraryResultFile.getAbsolutePath(), artifact.getFile().getName().replace(".unity-library", ".dll")));
					this.copyFile(dllFile, resultFile);
				}
				try {
					File zippedFile = new File(String.format("%s/target/unity-libraries/%s/%s", this.project.getBasedir(), artifact.getArtifactId(), artifact.getFile().getName().replace(".unity-library", ".ios-plugin")));
					if (zippedFile.exists()) {
						File iOSPluginsFolder = new File(project.getBasedir() + "/Assets/Plugins/iOS");
						this.getLog().info(String.format("Extracting iOS plugins at [%s] to iOS plugins directory [%s]", zippedFile.getAbsolutePath(),
								iOSPluginsFolder.getAbsolutePath()));
						processRunner.runProcess(null, "unzip", "-uo", zippedFile.getAbsolutePath(), "-d", iOSPluginsFolder.getAbsolutePath());
					}
				} catch (MojoFailureException ex) {
					this.getLog().error("Failed to copy iOS artifact", ex);
				}
				try {
					File zippedFile = new File(String.format("%s/target/unity-libraries/%s/%s", this.project.getBasedir(), artifact.getArtifactId(), artifact.getFile().getName().replace(".unity-library", ".android-plugin")));
					if (zippedFile.exists()) {
						File androidPluginsFolder = new File(project.getBasedir() + "/Assets/Plugins/Android");
						this.getLog().info(String.format("Extracting iOS plugins at [%s] to iOS plugins directory [%s]", zippedFile.getAbsolutePath(),
								androidPluginsFolder.getAbsolutePath()));
						processRunner.runProcess(null, "unzip", "-uo", zippedFile.getAbsolutePath(), "-d", androidPluginsFolder.getAbsolutePath());
					}
				} catch (MojoFailureException ex) {
					this.getLog().error("Failed to copy android artifact", ex);
				}
			}
		}

	}

	private void copyFile(final File file, final File resultFile) throws MojoFailureException {
		try {
			FileUtils.copyFileToDirectory(file, resultFile);
		} catch (final IOException e) {
			this.getLog().error("Problem copying dll " + file.getName() + " to " + resultFile.getAbsolutePath());
			this.getLog().error(e.getMessage());
			throw new MojoFailureException("Problem copying dll " + file.getName() + " to " + resultFile.getAbsolutePath());
		}
	}

}
