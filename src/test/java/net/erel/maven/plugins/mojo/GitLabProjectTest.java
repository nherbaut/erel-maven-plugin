package net.erel.maven.plugins.mojo;

import java.util.List;

import javax.xml.bind.JAXBException;

import net.erel.maven.plugins.domain.gitlab.GitLabProject;
import net.erel.maven.plugins.exception.NoSuchProjectException;
import net.erel.maven.plugins.service.git.GitLabRestFacade;

import org.junit.Assert;

public class GitLabProjectTest {

  // @Test
  public void testGitLabFormat() throws JAXBException {

    List<GitLabProject> projects = GitLabTestUtils.loadTestProjects();
    Assert.assertEquals(new Integer(4), projects.get(0).getId());
    Assert.assertEquals("Prototype CSPExplorer V2 - release", projects.get(0).getName());

  }

  // @Test
  public void testGitLabProject() throws JAXBException {
    GitLabRestFacade facade = new GitLabRestFacade("http://git.erel.net", "9fdTwV5eYcgaNZ49gAp1");

    List<GitLabProject> res = facade.getProjects();

    Assert.assertTrue(res != null);
    Assert.assertTrue(res.size() != 0);
  }

  // @Test
  public void testGitLabRepoUrl() throws NoSuchProjectException {

    {
      GitLabRestFacade facade = new GitLabRestFacade("http://git.erel.net", "9fdTwV5eYcgaNZ49gAp1");

      for (GitLabProject project : facade.getProjects()) {
        if (project.getId().equals(new Integer(4))) {
          String path = facade.getGitRepoUrl(project);

          Assert.assertEquals("git@git.erel.net:n.herbaut/prototype-cspexplorer-v2-release.git", path);
        }
      }
    }

    {
      GitLabRestFacade facade = new GitLabRestFacade("git.erel.net", "9fdTwV5eYcgaNZ49gAp1");

      for (GitLabProject project : facade.getProjects()) {
        if (project.getId().equals(new Integer(4))) {
          String path = facade.getGitRepoUrl(project);

          Assert.assertEquals("git@git.erel.net:n.herbaut/prototype-cspexplorer-v2-release.git", path);
        }
      }
    }

  }

}
