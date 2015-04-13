package net.erel.maven.plugins.service.bugtracking;

import java.util.List;

import net.erel.maven.plugins.domain.bugTracker.IssueFacade;
import net.erel.maven.plugins.domain.changelog.Action;



public interface BugTrackerService {

	public abstract List<String> getAssignedTicketsForCurrentUser();

	public abstract String getDescriptionForIssue(String issue);

	public abstract void updateIssueStartDev(String ticket, String branchUrl,
			String project);

	public abstract void updateIssueDoneDev(String ticket, String project,String urlMergeRequest);

	public abstract void populateActionWithTicketDate(Action action);

	public abstract void updateIssueReleased(Action action, String version,
			String projectName);

	public abstract IssueFacade getIssueById(String issueNumber);

	public abstract String getTicketFromBranchName(String branch);

}