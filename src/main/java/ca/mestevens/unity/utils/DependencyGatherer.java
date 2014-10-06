package ca.mestevens.unity.utils;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;

public class DependencyGatherer {
	
	private MavenProject project;
	
	public DependencyGatherer(MavenProject project) {
		this.project = project;
	}
	
	public String createPomDependencySection() {
		String dependencies = "<dependencies>";
		for(Artifact artifact : project.getDependencyArtifacts()) {
			if (artifact.getType().equals("jar") || artifact.getType().equals("aar")) {
				dependencies += "<dependency>";
				dependencies += "<groupId>" + artifact.getGroupId() + "</groupId>";
				dependencies += "<artifactId>" + artifact.getArtifactId() + "</artifactId>";
				dependencies += "<version>" + artifact.getVersion() + "</version>";
				dependencies += "<type>" + artifact.getType() + "</type>";
				dependencies += "</dependency>";
			}
		}
		dependencies += "</dependencies>";
		return dependencies;
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
