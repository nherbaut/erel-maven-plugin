package net.erel.maven.plugins.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import net.erel.maven.plugins.domain.gitlab.GitLabProject;
import net.erel.maven.plugins.domain.gitlab.User;
import net.erel.maven.plugins.exception.NoSuchProjectException;
import net.erel.maven.plugins.service.bugtracking.BugTrackingServiceFactoryDefault;
import net.erel.maven.plugins.service.git.GitLabService;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Assert;
import org.junit.Test;

public class GitLabServiceTest {

  @Test
  public void getProjectTestAll() throws JAXBException {

    

    List<GitLabProject> projects = GitLabTestUtils.loadTestProjects();

    Assert.assertTrue(projects.size() == 6);

  }

  @Test
  public void issueMR() throws MojoFailureException, NoSuchProjectException, MojoExecutionException {

    GitLabService service = new GitLabService();

    List<User> users = new ArrayList<User>();
    List<GitLabProject> projects = new ArrayList<GitLabProject>();

    for (int i = 0; i < 10; ++i) {
      User user = new User();
      user.setId(i);
      user.setName("user" + i);
      users.add(user);
    }

    for (int i = 0; i < 10; ++i) {
      GitLabProject project = new GitLabProject();
      project.setId(100 + i);
      project.setName("project" + (100 + i));
      project.setPath("n.herbaut/project" + (100 + i));
      projects.add(project);
    }

    Map<String, Object> map = service.issueMergeRequest("scm:git:ssh://git@git.erel.net/n.herbaut/project104.git",
        "comment", "feature-987-comment", projects, new BugTrackingServiceFactoryDefault("","","","test","test").getBugTrackerService(),users.get(0) );

    assertEquals((Integer) 104, (Integer) map.get(GitLabService.ID));
    assertEquals("feature-987-comment", (String) map.get(GitLabService.SRC_BRANCH));
    assertEquals("develop", map.get(GitLabService.TGT_BRANCH));
    assertTrue(((String) map.get(GitLabService.TITLE)).contains("987"));

  }
}
