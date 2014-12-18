package ca.mestevens.unity;

import ca.mestevens.unity.utils.ProcessRunner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "open")
public class UnityOpenMojo extends AbstractMojo {

	@Parameter(property = "project", readonly = true, required = true)
	public MavenProject project;

	@Parameter(property = "unity.path", defaultValue = "/Applications/Unity/Unity.app/Contents/MacOS/Unity", readonly = true, required = true)
	public String unity;

	private final ProcessRunner processRunner;

	public UnityOpenMojo() {
		this.processRunner = new ProcessRunner(this.getLog());
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		this.getLog().info("Executing unity:open");
		try {
			processRunner.killProcessWithName("Unity");
			this.processRunner.runProcessAsync(this.unity, "-projectPath", this.project.getBasedir().getAbsolutePath());
		} catch (final Exception e) {
			this.getLog().error("Failed to open unity project", e);
		}
	}

}
