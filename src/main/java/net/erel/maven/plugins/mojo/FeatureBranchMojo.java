package net.erel.maven.plugins.mojo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.IOException;
import java.nio.file.Paths;

import net.erel.maven.plugins.domain.maven.PGXVersion;
import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.service.bugtracking.BugTrackerService;
import net.erel.maven.plugins.service.git.GitLabRestFacade;
import net.erel.maven.plugins.service.git.PGXBranches;
import net.erel.maven.plugins.utils.ANSI_COLOR;
import net.erel.maven.plugins.utils.L18nHelper;
import net.erel.maven.plugins.utils.PreconditionChecker;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.storage.file.FileRepository;

/**
 * <div class="fr">
 * <p>
 * Ce mojo est utilisé pour créer une branche où effectuer un développement. La branche de départ est capitable pour
 * comprendre comment va fonctionner ce mojo.
 * <ul>
 * <li>Si vous partez de la branche develop, la branche créée sera une feature branch (prefixe: feature) et ne pourra
 * être automatiquement intégrée que dans la branche develop</li>
 * <li>Si vous partez de la branche master, la branche créée sera une branche hotfix qui sera uniquement réintégrée à la
 * branche master</li>
 * </ul>
 * </p>
 * </div>
 * 
 * @author nherbaut
 */
@Mojo(name = "feature-branch", aggregator = true, requiresProject = true)
public class FeatureBranchMojo
    extends PGXAbstractGitLabMojo {
	
	
  /**
   * <div class="fr">Numéro du ticket qui déclanche la création de la
   * branche</div>
   */
  @Parameter(property = "ticketNumber", defaultValue = "")
  protected String ticketNumber;

  /**
   * <div class="fr">indice pour vous rappeller de ce que contient la
   * branche</div>
   */
  @Parameter(property = "hint", defaultValue = "")
  protected String hint;

  @Parameter(property = "project", readonly = true)
  protected MavenProject project;

  @Parameter(property = "session", readonly = true)
  protected MavenSession session;

  @Component
  protected BuildPluginManager buildPluginManager;
  

  public void execute() throws MojoExecutionException, MojoFailureException {

    PreconditionChecker.checkNoModifiedFile(project);

    try {

      FileRepository repository = new FileRepository(Paths.get(project.getBasedir().toString(), ".git").toFile());

      BugTrackerService redmineService = getBugTrackingFactory().getBugTrackerService();

      if (ticketNumber == null || ticketNumber.isEmpty())
        ticketNumber = prompter.prompt(L18nHelper.t("please-indicate-ticket-number"),
            redmineService.getAssignedTicketsForCurrentUser());

      if (hint == null || hint.isEmpty())
        hint = prompter.prompt(L18nHelper.t("please-indicate-a-hint"),
            doSanitizeHint(redmineService.getDescriptionForIssue(ticketNumber)));

      hint = doSanitizeHint(hint);

      PGXBranches branchType = null;
      // from master or develop?
      for (PGXBranches branchTypeCandidate : PGXBranches.values()) {
        if (repository.getBranch().equals(branchTypeCandidate.getBranchName())) {

          branchType = branchTypeCandidate;
          break;

        }
      }

      if (branchType == null) {
        throw new MojoFailureException(L18nHelper.t("a-feature-branch-must-start-from-master-or-develop"));
      }

      String branchName = branchType.getFeatureBranchPrefix() + "-" + ticketNumber + "-" + hint;

      prompter.showMessage(L18nHelper.t("updating the working copy", ANSI_COLOR.YELLOW));
      __do("git pull origin");

      executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-scm-plugin"), version("1.8.1")),
          goal("branch"), configuration(element(name("basedir"), "."), element(name("branch"), branchName)),
          executionEnvironment(project, session, buildPluginManager));

      executeMojo(
          plugin(groupId("org.codehaus.mojo"), artifactId("exec-maven-plugin"), version("1.2.1")),
          goal("exec"),
          configuration(element(name("executable"), "git"), element(name("commandlineArgs"), "checkout " + branchName)),
          executionEnvironment(project, session, buildPluginManager));

      executeMojo(
          plugin(groupId("org.codehaus.mojo"), artifactId("exec-maven-plugin"), version("1.2.1")),
          goal("exec"),
          configuration(element(name("executable"), "git"),
              element(name("commandlineArgs"), "push origin " + branchName)),
          executionEnvironment(project, session, buildPluginManager));

      // if hotfix, ask new version
      if (branchType.equals(PGXBranches.MASTER)) {

        PGXVersion version = new PGXVersion(this.project.getVersion());
        version.setIncrement(version.getIncrement() + 1);
        version.setSnapshot(true);

        String newVersion = prompter.prompt(L18nHelper.t("please-indicate-what-is-the-new-release-number-default-is-X",
            version.toString()));
        if (newVersion.isEmpty()) {
          newVersion = version.toString();
        }

        executeMojo(plugin(groupId("org.codehaus.mojo"), artifactId("versions-maven-plugin"), version("2.0")),
            goal("set"), configuration(element(name("newVersion"), newVersion)),
            executionEnvironment(project, session, buildPluginManager));

        __do("git commit --allow-empty -am \"updated version for hotfix " + newVersion + "\"");

      }
      
      

      // refresh repo
      repository = new FileRepository(Paths.get(project.getBasedir().toString(), ".git").toFile());
      
      GitLabRestFacade gitLabfacade = new GitLabRestFacade(gitlabUrl, gitlabPrivateToken);
      
      String gitlabBranch = gitLabfacade.getBranchUrl(project.getScm().getConnection(), repository.getBranch());

      redmineService.updateIssueStartDev(this.ticketNumber, gitlabBranch, project.getName());

    } catch (PrompterException | IOException | NoWorkTreeException | DoException e) {
      throw new MojoExecutionException(e.getMessage());

    }

  }

  private String doSanitizeHint(String hint) {

    return hint.toLowerCase().replaceAll("[^a-zA-z]", "_").replace(" ", "-");

  }
}
