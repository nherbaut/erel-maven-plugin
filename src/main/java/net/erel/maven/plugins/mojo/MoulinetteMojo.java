package net.erel.maven.plugins.mojo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.erel.maven.plugins.domain.gitlab.GitLabProject;
import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.service.git.GitLabRestFacade;
import net.erel.maven.plugins.utils.ANSI_COLOR;
import net.erel.maven.plugins.utils.BigPrompt;
import net.erel.maven.plugins.utils.BigPrompt.Messages;
import net.erel.maven.plugins.utils.L18nHelper;
import net.erel.maven.plugins.utils.Utils;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.ModelWriter;
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

import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * Ce mojo crée une moulinette
 * 
 * @author nherbaut
 */
@Mojo(name = "moulinette", requiresProject = false, aggregator = true)
public class MoulinetteMojo
    extends PGXAbstractGitLabMojo {

  /**
   * Url du gestionnaire de catalogue maven
   */
  @Parameter(property = "archetypeCatalog", defaultValue = "http://repo.erel.net/content/repositories/public")
  protected String archetypeCatalog;

  /**
   * Base de l'uri des repo à mettre dans le scm des projets maven
   */
  @Parameter(property = "baseRepoUri", defaultValue = " git@git.erel.net")
  protected String baseRepoUri;

  /**
   * 
   */
  @Parameter(property = "moulinetteArtifactId")
  protected String moulinetteArtifactId;

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(MoulinetteMojo.class);

  @Component
  private MavenProjectBuilder mavenProjectBuilder;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    BigPrompt.display(prompter, Messages.MOULINETTE);

    GitLabRestFacade facade = new GitLabRestFacade(this.gitlabUrl, this.gitlabPrivateToken);

    try {
      if (Strings.isNullOrEmpty(moulinetteArtifactId)) {

        moulinetteArtifactId = this.prompter.prompt(L18nHelper.t("please-indicate-the-artifact-id-of-your-moulinette"));

      }

      moulinetteArtifactId.replace(" ", "-");
      if (!moulinetteArtifactId.contains("moulinette")) {
        moulinetteArtifactId = "moulinette-" + moulinetteArtifactId;
      }

      File tempDir = Files.createTempDir();

      __do(
          "mvn archetype:generate -DarchetypeCatalog="
              + archetypeCatalog
              + "  -DgroupId=net.erel.moulinette -DarchetypeVersion=RELEASE -Dversion=1.0.0-SNAPSHOT -DartifactId="
              + this.moulinetteArtifactId
          + " -DarchetypeArtifactId=moulinette -DarchetypeGroupId=net.erel.maven.archetypes -DinteractiveMode=false -Dname="
          + moulinetteArtifactId , tempDir);

      tempDir = new File(tempDir, this.moulinetteArtifactId);

      __do("git init", tempDir);

      ArtifactRepository repo = new DefaultArtifactRepository("repositoryId", "http://my.repository.url",
          new DefaultRepositoryLayout());

      File pomFile = new File(tempDir, "pom.xml");
      MavenProject project = mavenProjectBuilder.build(pomFile.getAbsoluteFile(), repo, null);

      GitLabProject gitLabProject = facade.createProject(moulinetteArtifactId, "moulinette");
      String pathWithNamespace = gitLabProject.getPath_with_namespace();
      Scm scm = project.getScm();
      if (scm == null) {
        scm = new Scm();
        project.setScm(scm);
      }

      scm.setConnection("scm:git:ssh://" + this.baseRepoUri + "/" + pathWithNamespace);

      ModelWriter modelWritter = new DefaultModelWriter();
      Model model = project.getModel();
      Utils.cleanUpModel(model, tempDir.getAbsolutePath());
      modelWritter.write(pomFile, null, model);

      __do("git add -A", tempDir);
      __do("git commit -am \"initalization\"", tempDir);
      __do("git remote add origin " + baseRepoUri + ":" + pathWithNamespace + ".git", tempDir);

      boolean pushIsASuccess = false;
      for (int i = 0; i < 4; i++) {
        try {
          __do("git push  -u origin master", tempDir);
          pushIsASuccess = true;
          break;
        } catch (DoException doe) {
          prompter.showMessage(L18nHelper.t("gitlab-is-no-ready-lets-try-again", ANSI_COLOR.YELLOW));
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {

          }
        }
      }

      if (!pushIsASuccess) {
        throw new MojoExecutionException("something when wrong when pushing the project");
      }

    }

    catch (PrompterException | DoException | IOException | ProjectBuildingException e) {
      throw new MojoExecutionException("something went wrong when building the new moulinette project", e);
    }
  }
}
