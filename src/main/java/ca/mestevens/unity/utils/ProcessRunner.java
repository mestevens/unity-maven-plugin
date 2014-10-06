package ca.mestevens.unity.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public class ProcessRunner {

	private Log log;

	public ProcessRunner(Log log) {
		this.log = log;
	}

	public int runProcess(String workingDirectory, String... strings) throws MojoFailureException {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(strings);
			if (workingDirectory != null) {
				processBuilder.directory(new File(workingDirectory));
			}
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			BufferedReader input = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				log.info(line);
			}
			return process.waitFor();
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new MojoFailureException(e.getMessage());
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			throw new MojoFailureException(e.getMessage());
		}
	}

}
