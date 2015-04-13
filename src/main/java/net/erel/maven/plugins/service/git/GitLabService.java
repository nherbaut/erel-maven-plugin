package net.erel.maven.plugins.service.git;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.erel.maven.plugins.domain.gitlab.GitLabProject;
import net.erel.maven.plugins.domain.gitlab.User;
import net.erel.maven.plugins.exception.NoSuchProjectException;
import net.erel.maven.plugins.service.bugtracking.BugTrackerService;
import net.erel.maven.plugins.utils.L18nHelper;
import net.erel.maven.plugins.utils.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Perform business operations on gitlab domain objects
 * 
 * @author nherbaut
 */
public class GitLabService {

  public GitLabService(String gitlabUrl, String gitlabPrivateToken) {
    this.gitlabUrl = gitlabUrl;
    this.gitlabPrivateToken = gitlabPrivateToken;
  }

  private static final Log LOG = LogFactory.getLog(GitLabService.class);

  public final static String ID = "id";
  public final static String SRC_BRANCH = "source_branch";
  public final static String TGT_BRANCH = "target_branch";
  public final static String ASS_ID = "assignee_id";
  public final static String TITLE = "title";
  public final static String NAME = "name";

  public GitLabService() {
    this.gitlabUrl = "";
    this.gitlabPrivateToken = "";
  }

  protected final String gitlabUrl;

  protected final String gitlabPrivateToken;

  /**
   * Issue a merge request, guessing the project id with the artifactId
   * 
   * @param artifactId
   * @param branch
   * @param users
   * @param projects
   * @return
   * @throws NoSuchProjectException
   */
  public Map<String, Object> issueMergeRequest(String scm, String comment, String branch,
      List<GitLabProject> projects,BugTrackerService btservice,User user) throws NoSuchProjectException {

    Map<String, Object> res = new HashMap<>();
    res.put(ASS_ID, user.getId());

    for (GitLabProject project : projects) {

    	//scm=scm:git:ssh://git@git.erel.net/integration/opflux-parent-legacy.git
    	//project.getPath_with_namespace()=integration/opflux-parent
    	//we remove the ".git part from the scm" and try to match
      if (scm.substring(0, scm.length()-4).endsWith(project.getPath_with_namespace()))  {

        res.put(GitLabService.ID, project.getId());

        break;
      }

    }

    if (!res.containsKey(GitLabService.ID))
      throw new NoSuchProjectException("fail to find project on gitlab");

    PGXBranches targetBranch = guessTargetBranch(branch);

    res.put(SRC_BRANCH, branch);
    res.put(TGT_BRANCH, targetBranch.getBranchName());
    if (branch.equals(PGXBranches.MASTER.getBranchName())) {
      res.put(TITLE, L18nHelper.t("backport-master-branch"));

    } else {
      res.put(TITLE, "integration du ticket " + btservice.getTicketFromBranchName(branch) + " / " + comment);
    }

    return res;

  }

  /**
   * use the name of the current branch to guess the name of the target branch
   * to issue the merge request
   * 
   * @param branch
   * @return
   */
  public static PGXBranches guessTargetBranch(String branch) {
    if (branch.startsWith("feature"))
      return PGXBranches.DEVELOP;
    else if (branch.startsWith("hotfix"))
      return PGXBranches.MASTER;
    else if (branch.equals(PGXBranches.MASTER)) { // backport
      return PGXBranches.DEVELOP;
    } else {
      LOG.error("current branch doesn't floow branch naming scheme (feature- hotfix-), assuming we want to merge on develop branch");
      return PGXBranches.DEVELOP;
    }
  }

  /**
   * issue a merge request knowing the project Id
   * 
   * @param artifactId
   * @param branch
   * @param users
   * @param projectId
   * @return
   */
  public Map<String, Object> issueMergeRequest(String artifactId, String comment, String branch, User user,
      Integer projectId) {

    Map<String, Object> res = new HashMap<>();

    res.put(GitLabService.ID, projectId);

    PGXBranches targetBranch = guessTargetBranch(branch);

    res.put(SRC_BRANCH, branch);
    res.put(TGT_BRANCH, targetBranch.getBranchName());
    res.put(ASS_ID, user.getId());
    res.put(TITLE, "integration du ticket " + branch.split("-")[0] + " " + comment);

    return res;

  }

}
