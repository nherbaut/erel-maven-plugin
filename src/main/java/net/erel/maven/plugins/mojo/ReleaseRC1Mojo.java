package net.erel.maven.plugins.mojo;

import net.erel.maven.plugins.domain.maven.PGXVersion;
import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.service.changelog.ChangeLogServiceImpl;
import net.erel.maven.plugins.service.git.PGXBranches;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <div class="fr">Ce mojo est utilisé à partir de la branche develop pour créer
 * la première release candidate d'une nouvelle version planifiée. Une nouvelle
 * branche release est créee, une RC1 est déployée sur nexus, une RC2-SNAPSHOT
 * est commitée sur la branche de release</div>
 * 
 * @author nherbaut
 */
@Mojo(name = "release-rc1", requiresProject = true, aggregator = true)
public class ReleaseRC1Mojo
    extends PGXAbstractReleaseMojo {

  /**
   * <div class="fr">Nom de la version à releaser</div>
   */
  @Parameter(property = "newVersion", defaultValue = "")
  protected String newVersion;

  /**
   * <div class="fr">Nom de la nouvelle version de la branche develop</div>
   */
  @Parameter(property = "newDevVersion", defaultValue = "")
  protected String newDevVersion;

  /**
   * <div class="fr">@BETA indique d'une résolution automatique des
   * dépendances snapshot doit être effectuée</div>
   */
  @Parameter(property = "autoResolveDependency", defaultValue = "false")
  protected Boolean autoResolveDependency;

  protected ChangeLogServiceImpl changeLogService;

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseRC1Mojo.class);

  /**
   * 1- start from develop (mandatory) with a snapshot version 2- using mvn
   * release:branch, change develop version to next snapshot version then
   * create the release branch with a RC release version 3- perform the
   * release, move to
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    BigPrompt.display(prompter, Messages.NEW_RELEASE);
    PreconditionChecker.checkNoModifiedFile(project);
    PreconditionChecker.checkRCStatus(project, false);
    PreconditionChecker.checkSnapshotStatus(project, true);
    PreconditionChecker.checkBranch(project, PGXBranches.DEVELOP);

    PGXVersion newVersionOnReleaseBranch = new PGXVersion(project.getVersion());
    PGXVersion newVersionOnDevelop = new PGXVersion(project.getVersion());
    PGXVersion nextVersionOnReleaseBranch = new PGXVersion(project.getVersion());
    final PGXVersion currentVersion = new PGXVersion(project.getVersion());
    PGXVersion branchName = new PGXVersion(project.getVersion());
    branchName.setSnapshot(false);
    branchName.setRc(null);

    if (this.newVersion == null || this.newVersion.isEmpty()) {

      newVersionOnReleaseBranch.setSnapshot(false);
      newVersionOnReleaseBranch.setRc(1);

      this.newVersion = newVersionOnReleaseBranch.toString();
    }

    if (this.newDevVersion == null || this.newDevVersion.isEmpty()) {
      newVersionOnDevelop.setSnapshot(true);
      newVersionOnDevelop.setMinor(newVersionOnDevelop.getMinor() + 1);
      this.newDevVersion = newVersionOnDevelop.toString();
    }

    try {

      this.prompter.showMessage(L18nHelper.t("current-version", currentVersion.toString()));
      this.prompter.showMessage(L18nHelper.t("release-version", this.newVersion));
      this.prompter.showMessage(L18nHelper.t("next-dev-version", this.newDevVersion));
      
      
      prompter.showMessage(L18nHelper.t("updating-dependencies-looking-for-new-released-rc", ANSI_COLOR.GREEN));
      
      //experimental __do("mvn erel:dep -Dstrategy=UPGRADE_RC");

      String groupAndArtifactId = this.project.getGroupId() + ":" + this.project.getArtifactId();

      try {
    	  //TODO:fix
        this.changeLogService = new ChangeLogServiceImpl(project, "000", null);
        this.changeLogService.updateReleaseNewVersion(branchName.toString(), this.newVersion, project.getName());

      } catch (Exception e1) {
        this.prompter.showMessage(L18nHelper.t("something-went-wrong-with-changelog-it-wont-be-available"));
      }

      try {

        __do("git commit -am \"pre-release info commited for " + branchName.toString() + "\"");
        __do("git push origin develop");
      } catch (DoException e) {
        // exception should not be taken into account because if no
        // latest version is detected, there's nothing to commit
        // and git commit returns 1
      }

      // maybe we should clean up workspace before?
      //__do("git clean package -f ");

      __do("git pull origin develop");

      final String releaseBranchName = "release-" + branchName.toString();

      __do("mvn -Dresume=false --batch-mode org.apache.maven.plugins:maven-release-plugin:2.5:branch -DbranchName="
          + releaseBranchName + " -DupdateBranchVersions=true  -Dproject.dev." + groupAndArtifactId + "="
          + newDevVersion + " -Dproject.rel." + groupAndArtifactId + "=" + newVersion + "-SNAPSHOT "
          + " -DscmCommentPrefix=\"[PGX:RELEASE]\"  -DautoVersionSubmodules=true");

      __do("git checkout " + releaseBranchName);

      nextVersionOnReleaseBranch.setRc(2);
      nextVersionOnReleaseBranch.setSnapshot(true);

      __do("mvn --batch-mode -Dresume=false org.apache.maven.plugins:maven-release-plugin:2.5:prepare -DreleaseVersion=" + newVersionOnReleaseBranch.toString()
          + " -DdevelopmentVersion=" + nextVersionOnReleaseBranch.toString() + " -DscmCommentPrefix=\"[PGX:RELEASE]\"");

      __do("mvn org.apache.maven.plugins:maven-release-plugin:2.5:perform -DscmCommentPrefix=\"[PGX:RELEASE]\"");

    } catch (DoException | PrompterException e) {
      throw new MojoExecutionException(e.getMessage());
    }

  }
}
