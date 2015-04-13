package net.erel.maven.plugins.domain.maven;

import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Repository;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * class that is used to mimic mavenProject (as mojo executor need one to
 * operate, and we are in a requiresProject = false context
 * 
 * @author nherbaut
 * 
 */
public class MockMavenProject extends MavenProject {
	@SuppressWarnings("unchecked")
	public List<Repository> getRepositories() {
		return Collections.EMPTY_LIST;
	}

	@SuppressWarnings("unchecked")
	public List<RemoteRepository> getRemotePluginRepositories() {
		return Collections.EMPTY_LIST;
	}
}