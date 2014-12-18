package ca.mestevens.unity;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import ca.mestevens.unity.utils.ProcessRunner;

@Mojo(name = "unity-sync-mono-project")
public class UnitySyncMonoProjectMojo extends AbstractMojo {

	@Parameter(property = "project", readonly = true, required = true)
	public MavenProject project;
	
	@Parameter(property = "unity.path", defaultValue="/Applications/Unity/Unity.app/Contents/MacOS/Unity")
	public String unity;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File scriptFile = null;
		File scriptMetaFile = null;
		try {
			InputStream scriptStream = this.getClass().getClassLoader().getResourceAsStream("SyncMonoProject.cs");
			scriptFile = new File(project.getBasedir().getAbsolutePath() + "/Assets/Editor/SyncMonoProject.cs");
			scriptMetaFile = new File(project.getBasedir().getAbsolutePath() + "/Assets/Editor/SyncMonoProject.cs.meta");
			FileUtils.copyInputStreamToFile(scriptStream, scriptFile);
			scriptStream.close();
			ProcessRunner processRunner = new ProcessRunner(getLog());
			List<String> commandList = new ArrayList<String>();
			commandList.add(unity);
			commandList.add("-projectPath");
			commandList.add(project.getBasedir().getAbsolutePath());
			commandList.add("-executeMethod");
			commandList.add("ca.mestevens.unity.SyncMonoProject.SyncProject");
			commandList.add("-batchmode");
			commandList.add("-quit");
			commandList.add("-logFile");
			processRunner.killProcessWithName("Unity");
			int returnValue = processRunner.runProcess(null, commandList.toArray(new String[commandList.size()]));
			processRunner.checkReturnValue(returnValue);
		} catch (Exception ex) {
			throw new MojoFailureException(ex.getMessage());
		} finally {
			if (scriptFile != null && scriptFile.exists()) {
				scriptFile.delete();
			}
			if (scriptMetaFile != null && scriptMetaFile.exists()) {
				scriptMetaFile.delete();
			}
		}
		
	}

}
