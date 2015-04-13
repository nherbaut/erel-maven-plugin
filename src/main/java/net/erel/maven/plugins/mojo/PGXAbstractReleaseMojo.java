package net.erel.maven.plugins.mojo;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;

public abstract class PGXAbstractReleaseMojo extends CommandLineExecutorAbstractMojo {

	protected DefaultArtifactVersion defaultArtifactVersion;

	@Component
	protected MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		this.defaultArtifactVersion = new DefaultArtifactVersion(project.getVersion());
	}

	String getNextRCVersion() {
		return null;
	}

	String getNextVersion() {
		return null;
	}

}
