package net.erel.maven.plugins.service.git;

import java.util.List;

import net.erel.maven.plugins.domain.gitlab.GitLabProject;
import net.erel.maven.plugins.exception.NoSuchProjectException;

/**
 * interface that abstracts scm provider to perform administrative task on a
 * scm. (Scm can be github, bitbucket, gitlab)
 * 
 * @author nherbaut
 * 
 */

public interface ScmProviderFacade {

	String ROLE = ScmProviderFacade.class.getName();

	/**
	 * return a list of projects from the scm
	 * 
	 * @return
	 */
	public List<GitLabProject> getProjects();

	/**
	 * Retreive the repository Url (used to clone the repository)
	 * 
	 * @param projectId
	 * @return
	 */
	public String getProjectRepoUrl(Integer projectId) throws NoSuchProjectException;

}
