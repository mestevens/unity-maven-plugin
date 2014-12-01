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
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.io.IOException;
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

			if (typePropertyValue.equals(UNITY_LIBRARY) || typePropertyValue.equals(DLL)) {
				this.copyArtifact(artifact, resultFile);
			}

			if (typePropertyValue.equals(UNITY_LIBRARY)) {

				try {
					Artifact ab = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "ios-plugin",
							"ios-plugin", artifact.getVersion());
					ArtifactRequest request = new ArtifactRequest(ab, projectRepos, null);
					ArtifactResult artifactResult = repoSystem.resolveArtifact(repoSession, request);
					Artifact a = artifactResult.getArtifact();
					File zippedFile = a.getFile();
					File iOSPluginsFolder = new File(project.getBasedir() + "/Assets/Plugins/iOS");
					ProcessRunner processRunner = new ProcessRunner(getLog());
					processRunner.runProcess(null, "unzip", "-uo", zippedFile.getAbsolutePath(), "-d", iOSPluginsFolder.getAbsolutePath());
				} catch (ArtifactResolutionException e1) {
					this.getLog().error("Failed to get iOS artifact", e1);
				}
				try {
					Artifact ab = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "android-plugin",
							"android-plugin", artifact.getVersion());
					ArtifactRequest request = new ArtifactRequest(ab, projectRepos, null);
					ArtifactResult artifactResult = repoSystem.resolveArtifact(repoSession, request);
					Artifact a = artifactResult.getArtifact();
					File zippedFile = a.getFile();
					File AndroidPluginsFolder = new File(project.getBasedir() + "/Assets/Plugins/Android");
					ProcessRunner processRunner = new ProcessRunner(getLog());
					processRunner.runProcess(null, "unzip", "-uo", zippedFile.getAbsolutePath(), "-d", AndroidPluginsFolder.getAbsolutePath());
				} catch (ArtifactResolutionException e1) {
					this.getLog().error("Failed to get android artifact", e1);
				}
			}
		}

	}

	private void copyArtifact(final Artifact artifact, final File resultFile) throws MojoFailureException {

		final File file = artifact.getFile();

		try {
			FileUtils.copyFileToDirectory(file, resultFile);
		} catch (final IOException e) {
			this.getLog().error("Problem copying dll " + file.getName() + " to " + resultFile.getAbsolutePath());
			this.getLog().error(e.getMessage());
			throw new MojoFailureException("Problem copying dll " + file.getName() + " to " + resultFile.getAbsolutePath());
		}

	}

}
