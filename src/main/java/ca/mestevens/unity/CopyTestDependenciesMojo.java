package ca.mestevens.unity;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "unity-copy-test-dependencies", requiresDependencyResolution = ResolutionScope.TEST)
public class CopyTestDependenciesMojo extends AbstractMojo {

	@Component
	private MavenProject mavenProject;

	@Component
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	@Parameter(property = "project.build.directory", required = true, readonly = true)
	private String projectBuildDirectory;

	@Parameter(property = "unity.plugins.directory", defaultValue = "Assets/Editor/Plugins", required = true, readonly = true)
	private String testPluginsDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		executeMojo(
				plugin(
						groupId("org.apache.maven.plugins"),
						artifactId("maven-dependency-plugin"),
						version("2.0")
				),
				goal("copy-dependencies"),
				configuration(
						element(name("outputDirectory"), "${project.build.directory}/test-dependency"),
						element(name("excludeScope"), "compile")
				),
				executionEnvironment(
						mavenProject, mavenSession, pluginManager));

		final File testDependencyDirectoryFile = new File(String.format("%s/test-dependency", this.projectBuildDirectory));
		final File testPluginsDirectoryFile = new File(String.format("%s/%s", this.mavenProject.getBasedir(), this.testPluginsDirectory));

		if (!testDependencyDirectoryFile.exists()) {
			this.getLog().info("No test dependencies detected");
			return;
		}

		try {
			final FileFilter fileFilter = new WildcardFileFilter("*.dll");
			final List<File> testDependencies = Lists.newArrayList(testDependencyDirectoryFile.listFiles(fileFilter));
			for (final File dependency : testDependencies) {
				this.getLog().info(String.format("Copying [%s] to [%s]", dependency.getName(), this.testPluginsDirectory));
				FileUtils.copyFileToDirectory(dependency, testPluginsDirectoryFile);
			}
		} catch (final IOException e) {
			this.getLog().error("Failed to copy dependencies", e);
			throw new MojoFailureException("Failed to copy dependencies", e);
		}

	}

}
