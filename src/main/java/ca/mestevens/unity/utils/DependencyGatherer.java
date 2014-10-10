package ca.mestevens.unity.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

//import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
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

public class DependencyGatherer {
	
	private MavenProject project;
	private Log log;
	

	protected List<RemoteRepository> projectRepos;


	protected RepositorySystem repoSystem;


	protected RepositorySystemSession repoSession;
	
	public DependencyGatherer(Log log, MavenProject project, List<RemoteRepository> projectRepos, RepositorySystem repoSystem,
			RepositorySystemSession repoSession) {
		this.log = log;
		this.project = project;
		this.projectRepos = projectRepos;
		this.repoSystem = repoSystem;
		this.repoSession = repoSession;
	}
	
	public String createAndroidPomDependencySection() throws MojoFailureException {
		String dependencies = "";
		List<ArtifactResult> resolvedArtifacts = resolveArtifacts();
		for (ArtifactResult resolvedArtifact : resolvedArtifacts) {
			Artifact artifact = resolvedArtifact.getArtifact();
			if (artifact.getProperty("type", "").equals("aar")) {
				dependencies += "<dependency>";
				dependencies += "<groupId>" + artifact.getGroupId() + "</groupId>";
				dependencies += "<artifactId>" + artifact.getArtifactId() + "</artifactId>";
				dependencies += "<version>" + artifact.getVersion() + "</version>";
				dependencies += "<type>" + artifact.getProperty("type", "") + "</type>";
				dependencies += "</dependency>";
			}
		}
		return dependencies;
	}
	
	public String createXcodePomDependencySection() throws MojoFailureException {
		String dependencies = "";
		List<ArtifactResult> resolvedArtifacts = resolveArtifacts();
		for (ArtifactResult resolvedArtifact : resolvedArtifacts) {
			Artifact artifact = resolvedArtifact.getArtifact();
			if (artifact.getProperty("type", "").equals("xcode-framework")) {
				dependencies += "<dependency>";
				dependencies += "<groupId>" + artifact.getGroupId() + "</groupId>";
				dependencies += "<artifactId>" + artifact.getArtifactId() + "</artifactId>";
				dependencies += "<version>" + artifact.getVersion() + "</version>";
				dependencies += "<type>" + artifact.getProperty("type", "") + "</type>";
				dependencies += "</dependency>";
			}
		}
		return dependencies;
	}
	
	public List<ArtifactResult> resolveArtifacts() throws MojoFailureException {
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
			log.error("Could not resolve dependencies");
			log.error(e.getMessage());
			throw new MojoFailureException("Could not resolve dependencies");
		}
		return resolvedArtifacts;
	}
	
	public String createPomRepositoriesSection() {
		String repositories = "<repositories>";
		for(ArtifactRepository repository : project.getRemoteArtifactRepositories()) {
			repositories += "<repository>";
			repositories += "<id>" + repository.getId() + "</id>";
			repositories += "<url>" + repository.getUrl() + "</url>";
			repositories += "</repository>";
		}
		
		repositories += "</repositories>";
		return repositories;
	}

}
