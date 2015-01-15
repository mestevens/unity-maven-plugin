package ca.mestevens.unity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import ca.mestevens.unity.utils.ProcessRunner;
import ca.mestevens.unity.utils.UnityMenuCommands;

@Mojo(name = "unity-sync-mono-project")
public class UnitySyncMonoProjectMojo extends AbstractMojo {

	@Parameter(property = "project", readonly = true, required = true)
	public MavenProject project;
	
	@Parameter(property = "unity.path", defaultValue="/Applications/Unity/Unity.app/Contents/MacOS/Unity")
	public String unity;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		UnityMenuCommands menuCommands = new UnityMenuCommands(new ProcessRunner(getLog()), unity, project.getBasedir().getAbsolutePath());
		menuCommands.syncMonoDevelopProject();
	}

}
