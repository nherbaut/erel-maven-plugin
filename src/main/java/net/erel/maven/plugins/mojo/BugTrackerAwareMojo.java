package net.erel.maven.plugins.mojo;

import org.codehaus.plexus.components.interactivity.Prompter;

public interface BugTrackerAwareMojo {

	public abstract Prompter getPrompter();

	public abstract String getRedmineAPIKey();

	public abstract String getRedmineHost();

	public abstract String getNexusHost();

	public abstract String getNexusUser();

	public abstract String getNexusPassword();

	public abstract String getJiraHost();

	public abstract String getJiraUser();

	public abstract String getJiraPassword();

}