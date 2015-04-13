package net.erel.maven.plugins.mojo;

import java.io.File;

import javax.xml.bind.JAXBException;

import net.erel.maven.plugins.domain.maven.PGXVersion;
import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.exception.NoSuchProjectException;
import net.erel.maven.plugins.exception.NoSuchReleaseException;
import net.erel.maven.plugins.service.bugtracking.BugTrackerService;
import net.erel.maven.plugins.service.changelog.ChangeLogServiceImpl;
import net.erel.maven.plugins.service.git.GitLabRestFacade;
import net.erel.maven.plugins.service.git.PGXBranches;
import net.erel.maven.plugins.service.prompt.ResourcePicker;
import net.erel.maven.plugins.utils.L18nHelper;
import net.erel.maven.plugins.utils.PreconditionChecker;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 * <div class="fr">Ce mojo effectue la promotion d'une release candidate. Il
 * peut être effectué en dehors de tout contexte de développement (pas besoin de
 * workspace). Le descripteur "RC" est abandonné, et la version est déployée sur
 * nexus. La branche master est fusionnée avec la branche de release et une
 * merge request est envoyée pour effectuer le backport</div>
 * 
 * @author nherbaut
 */
@Mojo(name = "promote", requiresProject = false, aggregator = true)
public class PromoteRCMojo
    extends PGXAbstractGitLabMojo {

  /**
   * <div class="fr">url de connection au gitlab</div>
   */
  @Parameter(property = "connectionUrl", defaultValue = "")
  protected String connectionUrl;

  @Component
  private MavenProjectBuilder mavenProjectBuilder;

  private static final Logger LOGGER = LoggerFactory.getLogger(PromoteRCMojo.class);

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    ResourcePicker picker = new ResourcePicker(prompter, gitlabUrl, gitlabPrivateToken);
    picker.askForProject(null, true);

    File tempDir = new File(Files.createTempDir(), picker.getProject().getPath());

    try {
      // get the project on the master branch
      __do("git clone -b master " + picker.getRepoUrl() + " " + tempDir.getAbsolutePath());

      ArtifactRepository repo = new DefaultArtifactRepository("repositoryId", "http://my.repository.url",
          new DefaultRepositoryLayout());

      // merge the tag to master
      __do("git merge " + picker.getTag().getName() + " -X theirs --no-commit", tempDir);

      // check that it compiles and test ok
      __do("mvn clean package", tempDir);

      MavenProject project = mavenProjectBuilder.build(new File(tempDir, "pom.xml").getAbsoluteFile(), repo, null);

      PGXVersion version = new PGXVersion(project.getVersion());
      String rcVersion = version.toString();
      version.setRc(null);
      version.setSnapshot(false);
      String tagName = version.toString();

      PreconditionChecker.checkNoSuchTag(project, tagName);

      // sync with repo
      __do("git push origin master", tempDir);
      
      //experimental prompter.showMessage(L18nHelper.t("updating-dependencies-looking-for-new-released-rc", ANSI_COLOR.GREEN));
      //experimental  __do("mvn erel:dep -Dstrategy=UPGRADE_RC", tempDir);

      // set the new version to the specified tagname, which is the
      // promoted version without RC or SNAPSHOT
      __do("mvn versions:set -DnewVersion=" + tagName, tempDir);

      try {
    	  //TODO:fix
        ChangeLogServiceImpl changelogService = new ChangeLogServiceImpl(project, "", null);

        changelogService.promoteRelease(tagName, rcVersion);

      } catch (JAXBException | NoSuchReleaseException e) {
        LOGGER.warn("failed to update changelog");
      }

      // put on master
      __do("git commit --allow-empty -am \"promoted " + tagName + " to GA\"", tempDir);
      __do("git push origin master", tempDir);

      // create the corresponding tag
      __do("git tag " + tagName, tempDir);
      __do("git push origin " + tagName, tempDir);

      // do the release with the release plugin
      __do("mvn release:perform  -DconnectionUrl=" + project.getScm().getConnection() + " -Dtag=" + tagName, tempDir);

      // creating the backport merge request
      prompter.showMessage(L18nHelper.t("promotion-is-ok-now-issuing-a-merge-request"));
      BugTrackerService bugtracker = getBugTrackingFactory().getBugTrackerService();
	  picker.askForUser();

      new GitLabRestFacade(gitlabUrl, gitlabPrivateToken).issueMergeRequest(project, "backport",
          PGXBranches.MASTER.getBranchName(),bugtracker,picker.getUser());

    } catch (DoException | ProjectBuildingException | PrompterException | NoSuchProjectException e) {
      throw new MojoExecutionException(e.getMessage());
    }

  }
}
