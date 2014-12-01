package ca.mestevens.unity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Binds to initialize phase.
 * Creates all required directories including:
 * - Plugins directory
 * - Test plugins directory
 * - iOS plugins directory
 * - Android plugins directory
 */
@Mojo(name = "unity-initialize")
public class InitializeMojo extends AbstractMojo {

	@Parameter(property = "project", readonly = true, required = true)
	public MavenProject project;

	@Parameter(property = "unity.plugins.directory", defaultValue = "Assets/Runtime/Plugins", readonly = true, required = true)
	public String pluginsDirectory;

	@Parameter(property = "unity.test.plugins.directory", defaultValue = "Assets/Editor/Plugins", readonly = true, required = true)
	public String testPluginsDirectory;

	@Parameter(property = "unity.ios.plugins.directory", defaultValue = "Assets/Plugins/iOS", readonly = true, required = true)
	public String iosPlusinsDirectory;

	@Parameter(property = "unity.android.plugins.directory", defaultValue = "Assets/Plugins/Android", readonly = true, required = true)
	public String androidPluginsDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		this.getLog().info("Executing unity:unity-initialize");

		final File pluginsDirectoryFile = new File(String.format("%s/%s", this.project.getBasedir(), this.pluginsDirectory));
		final File testPluginsDirectoryFile = new File(String.format("%s/%s", this.project.getBasedir(), this.testPluginsDirectory));
		final File iosPluginsDirectoryFile = new File(String.format("%s/%s", this.project.getBasedir(), this.iosPlusinsDirectory));
		final File androidPluginsDirectoryFile = new File(String.format("%s/%s", this.project.getBasedir(), this.androidPluginsDirectory));

		try {
			if (pluginsDirectoryFile.exists()) {
				FileUtils.deleteDirectory(pluginsDirectoryFile);
			}

			if (testPluginsDirectoryFile.exists()) {
				FileUtils.deleteDirectory(testPluginsDirectoryFile);
			}

			if (iosPluginsDirectoryFile.exists()) {
				FileUtils.deleteDirectory(iosPluginsDirectoryFile);
			}

			if (androidPluginsDirectoryFile.exists()) {
				FileUtils.deleteDirectory(androidPluginsDirectoryFile);
			}
		} catch (final IOException e) {
			this.getLog().error("Failed to clean up existing plugins directories", e);
			throw new MojoFailureException("Failed to clean up existing plugins directories", e);
		}

		FileUtils.mkdir(pluginsDirectoryFile.getAbsolutePath());
		FileUtils.mkdir(testPluginsDirectoryFile.getAbsolutePath());
		FileUtils.mkdir(iosPluginsDirectoryFile.getAbsolutePath());
		FileUtils.mkdir(androidPluginsDirectoryFile.getAbsolutePath());

	}

}
