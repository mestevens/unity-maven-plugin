package ca.mestevens.unity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;

@Mojo(name = "open-solution")
public class UnityOpenSolutionMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.basedir}", property = "projectPath", required = true)
	private File projectPath;

	private final Desktop desktop;

	public UnityOpenSolutionMojo() {
		this.desktop = Desktop.getDesktop();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		this.getLog().info("Executing unity:open-solution");

		try {
			final File solution = this.projectPath.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(final File dir, final String name) {
					return name.endsWith(".sln");
				}
			})[0];

			this.desktop.open(solution);

		} catch (final Exception e) {
			throw new MojoExecutionException("Failed to open solution", e);
		}
	}

}
