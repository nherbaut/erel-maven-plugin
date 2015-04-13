package net.erel.maven.plugins.mojo;

import javax.xml.bind.JAXBException;

import net.erel.maven.plugins.domain.maven.PGXVersion;
import net.erel.maven.plugins.domain.maven.PGXVersionBuilder;
import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.exception.NoSuchReleaseException;
import net.erel.maven.plugins.service.changelog.ChangeLogServiceImpl;
import net.erel.maven.plugins.utils.ANSI_COLOR;
import net.erel.maven.plugins.utils.BigPrompt;
import net.erel.maven.plugins.utils.L18nHelper;
import net.erel.maven.plugins.utils.PreconditionChecker;
import net.erel.maven.plugins.utils.BigPrompt.Messages;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * <div class="fr">Ce mojo doit être utilisé sur une branche de release afin de
 * générer la Release Candidate suivante. Il releasera la release candidate
 * correspondant à la version actuelle du snapshot, et commitera sur la branche
 * une nouvelle version snapshot d'une nouvelle release candidate</div>
 * 
 * @author nherbaut
 */
@Mojo(name = "release-next", requiresProject = true, aggregator = true)
public class ReleaseNextRCMojo
    extends PGXAbstractReleaseMojo {

  /**
   * Base package for dependency resolution
   */
  @Parameter(property = "basePackage", required = false, defaultValue = "net.erel")
  protected String basePackage;

  public ReleaseNextRCMojo() {
    super();
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    super.execute();

    BigPrompt.display(prompter, Messages.NEW_RELEASE_CANDIDATE);
    PreconditionChecker.checkRCStatus(project, true);
    PreconditionChecker.checkSnapshotStatus(project, true);
    PreconditionChecker.checkNoModifiedFile(project);
    PreconditionChecker.checkBranchPrefix(project, "release");

    try {

      PGXVersion v = new PGXVersion(this.project.getVersion());

      String nextRC = new PGXVersionBuilder().major(v.getMajor()).minor(v.getMinor()).increment(v.getIncrement())
          .rc(v.getRc()).snapshot(false).build();

      String nextRCDev = new PGXVersionBuilder().major(v.getMajor()).minor(v.getMinor()).increment(v.getIncrement())
          .rc(v.getRc() + 1).snapshot(true).build();

      String targetGA = new PGXVersionBuilder().major(v.getMajor()).minor(v.getMinor()).increment(v.getIncrement())
          .rc(null).snapshot(false).build();

      this.prompter
          .showMessage(L18nHelper.t("preparing-new-release-candidate-for-version-X", this.project.getVersion()));
      this.prompter.showMessage(L18nHelper.t("next-release-candidate-is-X", nextRC));
      this.prompter.showMessage(L18nHelper.t("next-developement-version-for-release-X-is-Y", new PGXVersionBuilder()
          .major(v.getMajor()).minor(v.getMinor()).increment(v.getIncrement()).build(), nextRCDev));

      prompter.showMessage(L18nHelper.t("updating-dependencies-looking-for-new-released-rc", ANSI_COLOR.GREEN));
      
      //experimental __do("mvn erel:dep -Dstrategy=UPGRADE_RC");

      __do("git commit --allow-empty -am \"updating pom depencies for release \"");

      __do("mvn --batch-mode -Dresume=false org.apache.maven.plugins:maven-release-plugin:2.5:prepare -DreleaseVersion="
          + nextRC + " -DdevelopmentVersion=" + nextRCDev + " -DscmCommentPrefix=\"[PGX:RELEASE]\"");

      __do("mvn release:perform  -DscmCommentPrefix=\"[PGX:RELEASE]\" -Perel-release-profile");

      try {
    	  //TODO:fix
        ChangeLogServiceImpl changeLogService = new ChangeLogServiceImpl(project, "000", null);

        changeLogService.updateIssueNewReleaseCreated(targetGA, nextRC, project.getName());

      } catch (JAXBException | NoSuchReleaseException e) {
        this.prompter.showMessage("failed to update changelog or  redmine...");
      }

    } catch (DoException | PrompterException e) {

      e.printStackTrace();
      throw new MojoExecutionException(e.getMessage());
    }

  }
}
