package net.erel.maven.plugins.mojo;

import net.erel.maven.plugins.domain.maven.PGXVersion;
import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.exception.NoSuchProjectException;
import net.erel.maven.plugins.service.bugtracking.BugTrackerService;
import net.erel.maven.plugins.service.git.GitLabRestFacade;
import net.erel.maven.plugins.service.git.PGXBranches;
import net.erel.maven.plugins.service.prompt.ResourcePicker;
import net.erel.maven.plugins.utils.BigPrompt;
import net.erel.maven.plugins.utils.L18nHelper;
import net.erel.maven.plugins.utils.PreconditionChecker;
import net.erel.maven.plugins.utils.BigPrompt.Messages;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * <div class="fr">Ce mojo doit être utilisé lors de la release d'un hotfix sur
 * master</div>
 * 
 * @author nherbaut
 */
@Mojo(name = "release-hotfix", requiresProject = true, aggregator = true)
public class ReleaseHotfix
    extends PGXAbstractReleaseMojo {

  /**
   * url to the gitlab repository
   */
  @Parameter(property = "gitlabUrl", defaultValue = "http://git.erel.net/")
  protected String gitlabUrl;

  /**
   * your private gitlab token
   */
  @Parameter(property = "gitlabPrivateToken", required = true)
  protected String gitlabPrivateToken;

  public ReleaseHotfix() {
    super();
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    super.execute();

    BigPrompt.display(prompter, Messages.HOTFIX);

    PGXVersion version = new PGXVersion(project);
    version.setSnapshot(false);

    PreconditionChecker.checkBranch(project, PGXBranches.MASTER);
    PreconditionChecker.checkSnapshotStatus(project, true);
    PreconditionChecker.checkNoSuchTag(project, version.toString());

    ResourcePicker picker = new ResourcePicker(this.prompter, this.gitlabUrl, this.gitlabPrivateToken);
    picker.askForUser();
    try {
      __do("mvn versions:set -DnewVersion=" + version.toString());

      __do("mvn clean package javadoc:javadoc source:jar deploy");

      __do("git commit -am \"updated version for hotfix " + version.toString() + " \"");

      __do("git push origin master");

      __do("git tag " + version.toString());

      __do("git push origin " + version.toString());

      BugTrackerService bugtracker = getBugTrackingFactory().getBugTrackerService();
      new GitLabRestFacade(gitlabUrl, gitlabPrivateToken).issueMergeRequest(project, "backport",
          PGXBranches.MASTER.getBranchName(),bugtracker,picker.getUser());

    } catch (DoException | NoSuchProjectException e) {
      throw new MojoFailureException(e.getMessage() + L18nHelper.t("did-you-pulled-latest-master-modification"));
    }

    ;

  }
}
