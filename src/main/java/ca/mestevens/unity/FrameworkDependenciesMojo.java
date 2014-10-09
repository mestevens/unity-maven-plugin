package ca.mestevens.unity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;

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
		getLog().info("Starting execution");
		
		CollectRequest collectRequest = new CollectRequest();
		final Artifact mainArtifact = new DefaultArtifact(project.getArtifact().getId());
		collectRequest.setRoot(new Dependency(mainArtifact, JavaScopes.COMPILE));
		collectRequest.setRepositories(projectRepos);
		DependencyRequest dependencyRequest = new DependencyRequest().setCollectRequest(collectRequest);
		dependencyRequest.setFilter(new DependencyFilter() {

			public boolean accept(DependencyNode node,
					List<DependencyNode> parents) {
				Artifact nodeArtifact = node.getArtifact();
				
				if (nodeArtifact.getGroupId().equals(mainArtifact.getGroupId()) &&
						nodeArtifact.getArtifactId().equals(mainArtifact.getArtifactId())) {
					return false;
				}
				return true;
			}
			
		});
		List<ArtifactResult> resolvedArtifacts;
		try {
			
			resolvedArtifacts = repoSystem.resolveDependencies(repoSession, dependencyRequest).getArtifactResults();
		} catch (DependencyResolutionException e) {
			getLog().error("Could not resolve dependencies");
			getLog().error(e.getMessage());
			throw new MojoFailureException("Could not resolve dependencies");
		}
		
		File resultFile = new File(project.getBasedir() + "/Assets/Runtime/Plugins");
		try {
			if (resultFile.exists()) {
				FileUtils.deleteDirectory(resultFile);
			}
			FileUtils.mkdir(resultFile.getAbsolutePath());
		} catch (IOException e) {
			getLog().error("Problem deleting or creating plugin folder at: " + resultFile.getAbsolutePath());
			getLog().error(e.getMessage());
			throw new MojoFailureException("Problem deleting or creating plugin folder at: " + resultFile.getAbsolutePath());
		}
		
		for (ArtifactResult resolvedArtifact : resolvedArtifacts) {
			Artifact artifact = resolvedArtifact.getArtifact();
			for(String key : artifact.getProperties().keySet()) {
				getLog().info(key + artifact.getProperty(key, ""));
			}
			if (artifact.getProperty("type", "").equals("unity-library")) {
				
					// Get File from result artifact
					File file = artifact.getFile();
					try {
						FileUtils.copyFileToDirectory(file, resultFile);
						//File pluginFile = new File(resultFile.getAbsolutePath() + "/" + file.getName());
						//File renamedFile = new File(pluginFile.getAbsolutePath().replace(".unity-library", ".dll"));
						//FileUtils.rename(pluginFile, renamedFile);
					} catch (IOException e) {
						getLog().error("Problem copying dll " + file.getName() + " to " + resultFile.getAbsolutePath());
						getLog().error(e.getMessage());
						throw new MojoFailureException("Problem copying dll " + file.getName() + " to " + resultFile.getAbsolutePath());
					}
			}
		}

	}
}
