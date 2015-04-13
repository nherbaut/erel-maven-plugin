package net.erel.maven.plugins.service.bugtracking;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.component.annotations.Component;

import com.google.common.base.Strings;

@Component(role = BugTrackingServiceFactory.class)
public class BugTrackingServiceFactoryDefault implements
		BugTrackingServiceFactory {

	private final String jiraHost;
	private final String jiraUser;
	private final String jiraPassword;
	private final String redmineAPIKey;
	private final String redmineHost;

	public BugTrackingServiceFactoryDefault(String jiraHost, String jiraUser,
			String jiraPassword, String redmineAPIKey, String redmineHost) {
		this.jiraHost = jiraHost;
		this.jiraPassword = jiraPassword;
		this.jiraUser = jiraUser;
		this.redmineAPIKey = redmineAPIKey;
		this.redmineHost = redmineHost;

	}

	public BugTrackerService getBugTrackerService()
			throws MojoExecutionException {
		try {

			if (Strings.isNullOrEmpty(jiraHost)
					|| Strings.isNullOrEmpty(jiraUser)
					|| Strings.isNullOrEmpty(jiraPassword)) {
				if (Strings.isNullOrEmpty(redmineAPIKey)
						|| Strings.isNullOrEmpty(redmineHost)) {
					throw new Exception(
							"Neither Jira nor Redmine is configured");
				} else {
					return new RedMineService(redmineAPIKey, redmineHost);
				}
			} else {
				return new JiraService(jiraHost, jiraUser, jiraPassword);
			}

		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}

	}

}
