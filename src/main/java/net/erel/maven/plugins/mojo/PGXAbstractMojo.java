package net.erel.maven.plugins.mojo;

import net.erel.maven.plugins.service.bugtracking.BugTrackingServiceFactory;
import net.erel.maven.plugins.service.bugtracking.BugTrackingServiceFactoryDefault;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.Prompter;

public abstract class PGXAbstractMojo extends AbstractMojo implements
		BugTrackerAwareMojo {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.maven.plugins.BugTrackerAwareMojo#getPrompter()
	 */
	@Override
	public Prompter getPrompter() {
		return prompter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.maven.plugins.BugTrackerAwareMojo#getRedmineAPIKey()
	 */
	@Override
	public String getRedmineAPIKey() {
		return redmineAPIKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.maven.plugins.BugTrackerAwareMojo#getRedmineHost()
	 */
	@Override
	public String getRedmineHost() {
		return redmineHost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.maven.plugins.BugTrackerAwareMojo#getNexusHost()
	 */
	@Override
	public String getNexusHost() {
		return nexusHost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.maven.plugins.BugTrackerAwareMojo#getNexusUser()
	 */
	@Override
	public String getNexusUser() {
		return nexusUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.maven.plugins.BugTrackerAwareMojo#getNexusPassword()
	 */
	@Override
	public String getNexusPassword() {
		return nexusPassword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.maven.plugins.BugTrackerAwareMojo#getJiraHost()
	 */
	@Override
	public String getJiraHost() {
		return jiraHost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.maven.plugins.BugTrackerAwareMojo#getJiraUser()
	 */
	@Override
	public String getJiraUser() {
		return jiraUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.maven.plugins.BugTrackerAwareMojo#getJiraPassword()
	 */
	@Override
	public String getJiraPassword() {
		return jiraPassword;
	}

	@Component
	protected Prompter prompter;

	/**
	 * redmine API Key, as found in, to put as a property of the settings.xml
	 * for your convenience
	 */
	@Parameter(property = "redmineAPIKey", required = true)
	protected String redmineAPIKey;
	/**
	 * url to the redmine host repository, to put as a property of the
	 * settings.xml for your convenience
	 */
	@Parameter(property = "redmineHost", required = false, defaultValue = "")
	protected String redmineHost;

	/**
	 * url to the nexus repo, to put as a property of the settings.xml for your
	 * convenience
	 */
	@Parameter(property = "nexusHost", required = false, defaultValue = "http://repo.erel.net")
	protected String nexusHost;

	/**
	 * user to use for nexus authentication, to put as a property of the
	 * settings.xml for your convenience
	 */
	@Parameter(property = "nexusUser", required = true)
	protected String nexusUser;

	/**
	 * password to use for nexus authentication, to put as a property of the
	 * settings.xml for your convenience
	 */
	@Parameter(property = "nexusPassword", required = true)
	protected String nexusPassword;

	@Parameter(property = "jiraHost", required = false, defaultValue = "")
	protected String jiraHost;

	@Parameter(property = "jiraUser", required = false, defaultValue = "")
	protected String jiraUser;

	@Parameter(property = "jiraPassword", required = false, defaultValue = "")
	protected String jiraPassword;

	public PGXAbstractMojo() {
		super();
	}

	public BugTrackingServiceFactory getBugTrackingFactory() {
		return new BugTrackingServiceFactoryDefault(jiraHost, jiraUser,
				jiraPassword, redmineAPIKey, redmineHost);
	}

}