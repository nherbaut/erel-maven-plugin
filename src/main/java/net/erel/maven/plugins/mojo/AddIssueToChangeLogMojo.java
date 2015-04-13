package net.erel.maven.plugins.mojo;

import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;

import net.erel.maven.plugins.domain.bugTracker.IssueFacade;
import net.erel.maven.plugins.domain.changelog.Release;
import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.service.bugtracking.BugTrackerService;
import net.erel.maven.plugins.service.changelog.ChangeLogServiceImpl;
import net.erel.maven.plugins.utils.ANSI_COLOR;
import net.erel.maven.plugins.utils.BigPrompt;
import net.erel.maven.plugins.utils.L18nHelper;
import net.erel.maven.plugins.utils.BigPrompt.Messages;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.PrompterException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Ajoute une issue au changelog du projet
 * 
 * @author nherbaut
 */
@Mojo(name = "add-issue", aggregator = true, requiresProject = true)
public class AddIssueToChangeLogMojo
    extends CommandLineExecutorAbstractMojo {

  @Parameter(property = "project", readonly = true)
  protected MavenProject project;
  
  

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    BigPrompt.display(this.prompter, Messages.ADD_ISSUE);

    try {
      String issueNumber = this.prompter.prompt(L18nHelper.t("please-enter-issue-number"));

      BugTrackerService redmineService = getBugTrackingFactory().getBugTrackerService();
      ChangeLogServiceImpl changelogService = new ChangeLogServiceImpl(this.project, "", redmineService);

      IssueFacade issue = redmineService.getIssueById(issueNumber);

      this.prompter.showMessage(L18nHelper.t("adding-issue-X-with-description-Y", ANSI_COLOR.GREEN, "" + issue.getId(),
          issue.getSubject()));

      List<Release> releases = changelogService.getReleases();

      if (releases.isEmpty()) {
        this.prompter.showMessage(L18nHelper.t("no-release-is-defined", ANSI_COLOR.RED));
        return;
      }

      for (Release release : releases) {
        this.prompter.showMessage(L18nHelper.t("", ANSI_COLOR.GREEN, release.getVersion()));
      }

      String version = this.prompter.prompt(L18nHelper.t("please-indicate-on-which-version-to-attach-the-ticket"),
          Lists.transform(releases, new Function<Release, String>() {

            @Override
            public String apply(@Nullable Release input) {
              return input.getVersion();
            }
          }));

      changelogService.addTicketToVersion("" + issue.getId(), version);

      __do("git commit -am \"adding ticket " + issue.getId() + " to version " + version + " in changelog\"");
      this.prompter.showMessage(L18nHelper.t("changelog-commited-dont-forget-to-push", ANSI_COLOR.YELLOW));

    } catch (PrompterException | NumberFormatException | DoException | JAXBException e) {
      throw new MojoExecutionException("failed to execute mojo", e);
    }

  }
}
