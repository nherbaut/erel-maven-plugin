package net.erel.maven.plugins.mojo;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.erel.maven.plugins.domain.gitlab.GitLabProject;
import net.erel.maven.plugins.domain.gitlab.IdentifiableGitlabAsset;
import net.erel.maven.plugins.domain.gitlab.MergeRequest;
import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.exception.NoSuchProjectException;
import net.erel.maven.plugins.service.bugtracking.BugTrackerService;
import net.erel.maven.plugins.service.changelog.ChangeLogServiceImpl;
import net.erel.maven.plugins.service.git.GitLabRestFacade;
import net.erel.maven.plugins.service.prompt.ResourcePicker;
import net.erel.maven.plugins.utils.ANSI_COLOR;
import net.erel.maven.plugins.utils.L18nHelper;
import net.erel.maven.plugins.utils.PreconditionChecker;
import net.erel.maven.plugins.utils.Utils;

import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.eclipse.jgit.storage.file.FileRepository;

import com.google.common.base.Function;
import com.google.common.base.Nullable;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;

/**
 * <div class="fr">Ce Mojo est utilisé pour demandé la réintégration du travail
 * dans sa branche d'origine, master (dans le cas d'un hotfix) ou develop (dans
 * le cas d'une feature branch)</div>
 * 
 * @author nherbaut
 */
@Mojo(name = "merge-request", aggregator = true)
public class MergeRequestMojo
    extends PGXAbstractGitLabMojo {

  /**
   * <div class="fr">commentaire utilisé pour le titre dans la change request
   * en complément du numéro de ticket</div> <div class="en">hint used along
   * branch title to describe merge request</div>
   */
  @Parameter(property = "comment", defaultValue = "")
  protected String comment;

  @Component
  protected BuildPluginManager buildPluginManager;
  
	
  @Parameter(property = "project", readonly = true)
  protected MavenProject project;

  final private static Function<GitLabProject, String> PRETTY_PRINT_PROJECT = new Function<GitLabProject, String>() {

    public String apply(@Nullable GitLabProject from) {
      return from.getId().toString();
    }

  };

  public void execute() throws MojoExecutionException, MojoFailureException {

    PreconditionChecker.checkSnapshotStatus(project, true);
    PreconditionChecker.checkNoModifiedFile(project);
    ResourcePicker picker = new ResourcePicker(this.prompter, this.gitlabUrl, this.gitlabPrivateToken);

    try {

      GitLabRestFacade facade = new GitLabRestFacade(gitlabUrl, gitlabPrivateToken);
      // confirmation
      if (comment == null || comment.isEmpty()) {
        this.comment = prompter.prompt(L18nHelper.t("please-write-a-comment-for-this-merge-request"));
      }
      picker.askForUser();
      String res = prompter.prompt(L18nHelper.t("are-you-sure-you-want-to-issue-merge-request-for-branch"),
          Arrays.asList("y", "n"));

      BugTrackerService bugtracker = this.getBugTrackingFactory().getBugTrackerService();

      if (res.equals("n")) {
        prompter.showMessage(L18nHelper.t("merge-request-cancelled"));
        return;
      }

      FileRepository repository = new FileRepository(Paths.get(project.getBasedir().toString(), ".git").toFile());
      String ticket = bugtracker.getTicketFromBranchName(repository.getBranch());
      if(Strings.isNullOrEmpty(ticket)){
    	  ticket=prompter.prompt(L18nHelper.t("fail-to-infer-ticket-please-enter-ticket-number"));
      }

      MergeRequest mr;
      try {
        // try with branch name from

        try {
          ChangeLogServiceImpl changeLog = new ChangeLogServiceImpl(project, repository.getConfig().getString("user",
              null, "name"),bugtracker);
          changeLog.addTicketToCurrentVersion(ticket);
          __do("git add " + Paths.get("src", "changes", "changes.xml").toFile().getAbsolutePath());
          __do("git commit -am \"updated change report\"");
          __do("git push origin " + repository.getBranch());
        } catch (JAXBException e) {
          this.prompter.showMessage(L18nHelper.t("there-is-an-issue-with-change-report-please-inform-release-manager"));
        }
        mr = facade.issueMergeRequest(project, comment, repository.getBranch(),bugtracker,picker.getUser());
      } catch (NoSuchProjectException e) {

        try {

          getLog().warn(L18nHelper.t("failed-to-gess-project-name"));
          this.prompter.showMessage(L18nHelper.t("available-projects"));

          List<GitLabProject> projects = facade.getProjects();

          for (IdentifiableGitlabAsset project : projects)
            this.prompter.showMessage(ANSI_COLOR.RED.getColorCode() + project.getId() + ANSI_COLOR.RESET.getColorCode()
                + "\t(" + project.getName() + ")\n");

          Integer projectId = new Integer(this.prompter.prompt(L18nHelper.t("pick-project-to-issue-a-merge-request-on"),
              Arrays.asList(Collections2.transform(projects, PRETTY_PRINT_PROJECT).toArray())));

          mr = facade.issueMergeRequest(project, comment, repository.getBranch(), projectId);

          

        } catch (PrompterException ee) {
          e.printStackTrace();
          throw new MojoExecutionException(e.getMessage());
        }

      }
      prompter.showMessage(ANSI_COLOR.GREEN.getColorCode()
          + L18nHelper.t("your-merge-request-has-been-sent", mr.getTitle(), mr.getId().toString())
          + ANSI_COLOR.RESET.getColorCode());
      
      String url = facade.getMergeRequest(project.getScm().getConnection(), mr.getId().toString());
      bugtracker.updateIssueDoneDev(ticket, project.getName(),url);

    } catch (PrompterException | IOException | DoException e) {
      throw new MojoExecutionException(e.getMessage());
    }

  }
}
