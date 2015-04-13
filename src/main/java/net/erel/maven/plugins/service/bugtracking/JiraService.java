package net.erel.maven.plugins.service.bugtracking;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.erel.maven.plugins.domain.bugTracker.IssueFacade;
import net.erel.maven.plugins.domain.changelog.Action;
import net.erel.maven.plugins.utils.L18nHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

class JiraService implements BugTrackerService {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JiraService.class);

	private final JiraRestClientFactory factory;
	private final JiraRestClient client;

	public JiraService(String jiraHost, String jiraUser, String jiraPassword)
			throws URISyntaxException {
		factory = new AsynchronousJiraRestClientFactory();
		client = factory.createWithBasicHttpAuthentication(new URI(jiraHost),
				jiraUser, jiraPassword);
	}

	@Override
	public List<String> getAssignedTicketsForCurrentUser() {

		Promise<SearchResult> promise = client
				.getSearchClient()
				.searchJql(
						"status in (\"A faire\", \"En cours\") AND assignee in (currentUser())",1000,0);
		SearchResult result = promise.claim();
		
		System.out.println(Lists.newArrayList(result.getIssues()).size());
		return Lists.transform(Lists.newArrayList(result.getIssues()),
				new Function<BasicIssue, String>() {

					@Override
					public String apply(BasicIssue input) {
						return input.getKey();
					}
				});

	}

	@Override
	public String getDescriptionForIssue(String issue) {
		Promise<com.atlassian.jira.rest.client.domain.Issue> promise = client
				.getIssueClient().getIssue(issue);
		com.atlassian.jira.rest.client.domain.Issue jiraIssue = promise.claim();
		return jiraIssue.getSummary();
	}

	@Override
	public void updateIssueStartDev(String ticket, String branchUrl,
			String project) {

		Issue issue = client.getIssueClient().getIssue(ticket).claim();

		Comment comment = Comment.valueOf(L18nHelper.t(
				"work-started-on-branch-X-for-project-Y", branchUrl, project));
		client.getIssueClient().addComment(issue.getCommentsUri(), comment);
		

	}

	@Override
	public void updateIssueDoneDev(String ticket, String project,String url) {

		Issue issue = client.getIssueClient().getIssue(ticket).claim();

		Comment comment = Comment.valueOf(L18nHelper.t(
				"task-done-pending-integration-for-projet-X-follow-merge-request-here", project,url));
		client.getIssueClient().addComment(issue.getCommentsUri(), comment);

	}

	@Override
	public void populateActionWithTicketDate(Action action) {

		Issue issue = client.getIssueClient().getIssue(action.getIssue())
				.claim();
		action.setDueTo(issue.getReporter().getName());
		action.getContent().add(issue.getSummary());

	}

	@Override
	public void updateIssueReleased(Action action, String version,
			String projectName) {

		String comStr = L18nHelper.t(
				"ticket-released-in-version-X-for-project-Y", version,
				projectName);
		Issue issue = client.getIssueClient().getIssue(action.getIssue())
				.claim();

		Comment comment = Comment.valueOf(comStr);
		client.getIssueClient().addComment(issue.getCommentsUri(), comment);
	}

	@Override
	public IssueFacade getIssueById(String issueNumber) {
		Promise<Issue> promise = client.getIssueClient().getIssue(issueNumber);
		Issue issue = promise.claim();

		IssueFacade res = new IssueFacade();
		res.setId(issue.getKey());
		res.setSubject(issue.getSummary());
		return res;
	}

	@Override
	public String getTicketFromBranchName(String branch) {
		Pattern pat = Pattern.compile(".*-(.*-\\d+).*");
		Matcher mat = pat.matcher(branch);
		if(mat.matches()){
			return mat.group(1);
		}
		else
			return null;
	}

}
