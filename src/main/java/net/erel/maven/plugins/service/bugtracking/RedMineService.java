package net.erel.maven.plugins.service.bugtracking;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import net.erel.maven.plugins.domain.bugTracker.IssueFacade;
import net.erel.maven.plugins.domain.changelog.Action;
import net.erel.maven.plugins.utils.L18nHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;

class RedMineService implements BugTrackerService  {

  private String api;
  private String host;

  public RedMineService(String api, String host) {
    this.api = api;
    this.host = host;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(RedMineService.class);

  /* (non-Javadoc)
 * @see net.erel.maven.plugins.service.bugtracking.BugTrackerService#getAssignedTicketsForCurrentUser()
 */
@Override
@SuppressWarnings("unchecked")
  public List<String> getAssignedTicketsForCurrentUser() {

    try {

      RedmineManager mgr = new RedmineManager(host, api);

      Map<String, String> issueREquest = new HashMap<String, String>();
      issueREquest.put("assigned_to_id", mgr.getCurrentUser().getId().toString());

      List<Issue> issues = mgr.getIssues(issueREquest);

      return Lists.transform(issues, new Function<Issue, String>() {

        @Override
        public String apply(@Nullable Issue input) {
          return input.getId().toString();
        }
      });

    } catch (RedmineException e) {
      LOGGER.warn("issue with redmine, failed to get assigned ticket", e);
      return Collections.EMPTY_LIST;
    }

  }

  /* (non-Javadoc)
 * @see net.erel.maven.plugins.service.bugtracking.BugTrackerService#getDescriptionForIssue(java.lang.String)
 */
@Override
public String getDescriptionForIssue(String issue) {
    try {
      RedmineManager mgr = new RedmineManager(host, api);

      return mgr.getIssueById(Integer.valueOf(issue)).getSubject();
    } catch (NumberFormatException | RedmineException e) {
      LOGGER.warn("failed to get issue description");
      return "";
    }

  }

  /* (non-Javadoc)
 * @see net.erel.maven.plugins.service.bugtracking.BugTrackerService#updateIssueStartDev(java.lang.String, java.lang.String, java.lang.String)
 */
@Override
public void updateIssueStartDev(String ticket, String branchUrl, String project) {
    try {
      RedmineManager mgr = new RedmineManager(host, api);

      Issue issue = mgr.getIssueById(Integer.valueOf(ticket));

      issue.setNotes(L18nHelper.t("work-started-on-branch-X-for-project-Y", branchUrl, project));

      mgr.update(issue);

    } catch (NumberFormatException | RedmineException e) {
      LOGGER.warn("failed to update issue");

    }

  }

  /* (non-Javadoc)
 * @see net.erel.maven.plugins.service.bugtracking.BugTrackerService#updateIssueDoneDev(java.lang.String, java.lang.String)
 */
@Override
public void updateIssueDoneDev(String ticket, String project,String mrUrl) {
    try {
      RedmineManager mgr = new RedmineManager(host, api);

      Issue issue = mgr.getIssueById(Integer.valueOf(ticket));

      issue.setNotes(L18nHelper.t("task-done-pending-integration-for-projet-X-follow-merge-request-here", project,mrUrl));

      issue.setAssignee(issue.getAuthor());

      mgr.update(issue);

    } catch (RedmineException | IllegalArgumentException e) {
      LOGGER.warn("failed to update issue");

    }

  }

  /* (non-Javadoc)
 * @see net.erel.maven.plugins.service.bugtracking.BugTrackerService#populateActionWithTicketDate(net.erel.maven.plugins.domain.changelog.Action)
 */
@Override
public void populateActionWithTicketDate(Action action) {
    try {
      RedmineManager mgr = new RedmineManager(host, api);
      Issue issue;

      issue = mgr.getIssueById(Integer.parseInt(action.getIssue()));

      action.setDueTo(issue.getAuthor().getFullName());
      action.setDueToEmail(issue.getAuthor().getMail());
      action.setType(trackerNameToChangeLogName(issue.getTracker().getName()));
      action.getContent().add(issue.getSubject());
    } catch (RedmineException | IllegalArgumentException e) {
      LOGGER.warn("failed to populate action with ticket data", e);
    }

  }

  /**
   * quick and dirty mapping for tracker, this should change
   * 
   * @param trackerName
   * @return
   */
  private static String trackerNameToChangeLogName(String trackerName) {
    if (Strings.isNullOrEmpty(trackerName))
      return "fix";
    else if (trackerName.contains("Incident client")) {
      return "fix";
    } else if (trackerName.contains("evolution")) {
      return "add";
    } else
      return "fix";

  }

  /* (non-Javadoc)
 * @see net.erel.maven.plugins.service.bugtracking.BugTrackerService#updateIssueReleased(net.erel.maven.plugins.domain.changelog.Action, java.lang.String, java.lang.String)
 */
@Override
public void updateIssueReleased(Action action, String version, String projectName) {
    try {
      RedmineManager mgr = new RedmineManager(host, api);
      Issue issue;

      issue = mgr.getIssueById(Integer.parseInt(action.getIssue()));

      issue.setNotes(L18nHelper.t("ticket-released-in-version-X-for-project-Y", version, projectName));

      mgr.update(issue);

    } catch (RedmineException | IllegalArgumentException e) {
      LOGGER.warn("failed to populate action with ticket data", e);
    }

  }

  /* (non-Javadoc)
 * @see net.erel.maven.plugins.service.bugtracking.BugTrackerService#getIssueById(java.lang.String)
 */
@Override
public IssueFacade getIssueById(String issueNumber) {
    try {
      RedmineManager mgr = new RedmineManager(host, api);
      Issue issue;

      issue = mgr.getIssueById(Integer.parseInt(issueNumber));
      
      IssueFacade facade = new IssueFacade();
      facade.setId(""+issue.getId());
      facade.setSubject(issue.getSubject());
      return facade;
    } catch (RedmineException | IllegalArgumentException e) {
      LOGGER.warn("failed to use Redmine API", e);
      return null;
    }
  }

@Override
public String getTicketFromBranchName(String branch) {
	Pattern pat = Pattern.compile(".*-(\\d+)-.*");
	Matcher mat = pat.matcher(branch);
	if(mat.matches()){
		return mat.group();
	}
	else
		return null;
}
}
