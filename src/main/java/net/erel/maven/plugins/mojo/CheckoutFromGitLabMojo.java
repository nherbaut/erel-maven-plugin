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

import java.io.File;
import java.util.Arrays;

import net.erel.maven.plugins.domain.maven.MockMavenProject;
import net.erel.maven.plugins.service.prompt.ResourcePicker;
import net.erel.maven.plugins.utils.ANSI_COLOR;
import net.erel.maven.plugins.utils.L18nHelper;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * <div class="fr">Offre une interface interactive permettant de récupérer la
 * branche par défaut de n'importe quel projet gitlab auquel vous avez
 * accès</div>
 * 
 * @author nherbaut
 */
@Mojo(name = "checkout", aggregator = true, requiresProject = false)
public class CheckoutFromGitLabMojo
    extends PGXAbstractGitLabMojo {

  /**
   * <div class="fr">l'endroit où vous souhaitez que le clonage soit
   * fait</div>
   */
  @Parameter(defaultValue = ".", required = false)
  protected File destination;

  @Parameter(property = "session", readonly = true)
  protected MavenSession session;

  /**
   * <div class="fr">l'url de connection au server gitlab</div>
   */
  @Parameter(property = "connectionUrl", defaultValue = "")
  protected String connectionUrl;

  @Component
  protected BuildPluginManager buildPluginManager;

  public final String YES = L18nHelper.t("yes");
  public final String NO = L18nHelper.t("no");

  public void execute() throws MojoExecutionException, MojoFailureException {

    try {

      ResourcePicker picker = new ResourcePicker(prompter, gitlabUrl, gitlabPrivateToken);
      picker.askForProject(null, false);

      File existingRepo = new File(picker.getProject().getPath_with_namespace());
      if (existingRepo.exists()) {
        String override = this.prompter.prompt(L18nHelper.t(
            "folder-X-already-exist-would-you-like-to-backit-up-and-clone-again-a-fresh-copy", picker.getProject()
                .getPath_with_namespace()), Arrays.asList(YES, NO), YES);
        if (override.equals(YES)) {
          File newRepoName = new File(picker.getProject().getPath_with_namespace() + "-old-"
              + System.currentTimeMillis());
          existingRepo.renameTo(newRepoName);
          this.prompter.showMessage(L18nHelper.t("your-old-repo-has-been-renamed-as", ANSI_COLOR.YELLOW,
              newRepoName.toString()));
        } else {
          return;
        }

      }

      executeMojo(
          plugin(groupId("org.apache.maven.plugins"), artifactId("maven-scm-plugin"), version("1.8.1")),
          goal("checkout"),

          configuration(element(name("basedir"), "."), element(name("connectionUrl"), picker.getScmConnection()),
              element(name("checkoutDirectory"), picker.getProject().getPath_with_namespace()),
              element(name("scmVersion"), picker.getProject().getDefault_branch()),
              element(name("scmVersionType"), "branch")

          ),

          executionEnvironment(new MockMavenProject(), session, buildPluginManager));

      this.prompter.showMessage(L18nHelper.t("checkout-succeed", ANSI_COLOR.GREEN));
    } catch (PrompterException e) {
      throw new MojoExecutionException(e.getMessage());
    }

  }
}
