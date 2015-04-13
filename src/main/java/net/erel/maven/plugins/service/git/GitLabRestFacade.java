package net.erel.maven.plugins.service.git;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;


import net.erel.maven.plugins.domain.gitlab.GitLabBranch;
import net.erel.maven.plugins.domain.gitlab.GitLabProject;
import net.erel.maven.plugins.domain.gitlab.GitLabTag;
import net.erel.maven.plugins.domain.gitlab.MergeRequest;
import net.erel.maven.plugins.domain.gitlab.User;
import net.erel.maven.plugins.exception.NoSuchProjectException;
import net.erel.maven.plugins.service.bugtracking.BugTrackerService;

import org.apache.maven.project.MavenProject;
import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * This class performs WS calls to gitlab api, delegating business to
 * GitLabService
 * 
 * @See(GitLabService.class)
 * @author nherbaut
 */
public class GitLabRestFacade {

  private static final class FindProjectByName
      implements Predicate<GitLabProject> {
    private final String projectName;

    private FindProjectByName(String projectName) {
      this.projectName = projectName;
    }

    @Override
    public boolean apply(@Nullable GitLabProject input) {
      return input.getName().equals(projectName);

    }
  }

  private GitLabService service;

  public GitLabRestFacade(String gitlabUrl, String gitlabPrivateToken) {
    ClientConfig cc = new DefaultClientConfig();
    cc.getClasses().add(MOXyJsonProvider.class);

    this.client = Client.create(cc);

    gitlabUrl = GitLabRestFacade.getAuthority(gitlabUrl);

    this.gitLabUri = UriBuilder.fromUri(GIT_LAB_SCHEME + "://" + gitlabUrl).build();
    this.gitlabPrivateToken = gitlabPrivateToken;
    this.service = new GitLabService();
  }

  protected static String getAuthority(String url) {

    URI uri = UriBuilder.fromUri(url).build();
    return (uri.getAuthority() != null ? uri.getAuthority() : "").concat(uri.getPath());
  }

  protected final URI gitLabUri;

  protected final String gitlabPrivateToken;

  protected final Client client;

  // //////////////////////// CONSTANTS
  private final static String PRIVATE_TOKEN = "private_token";
  private final static String PROJECTS = "projects";
  private final static String USERS = "users";
  private final static String MERGE_REQUEST = "merge_requests";
  private final static String GIT_LAP_API_VERSION = "v3";
  private final static String GIT_LAB_SCHEME = "http";
  private final static String REPOSITORY = "repository";
  private final static String BRANCHES = "branches";
  private final static String TAGS = "tags";
  private final static String NAME = "name";
  private final static String DESCRIPTION = "description";
  private final static String DEFAULT_BRANCH = "default_branch";
  private final static String ISSUE_ENABLED = "issues_enabled";
  private final static String WALL_ENABLED = "wall_enabled";
  private final static String MERGE_REQUEST_ENABLED = "merge_requests_enabled";
  private final static String WIKI_ENABLED = "wiki_enabled";

  // //////////////

  private UriBuilder getGitlabRestAPIRoot() {
    return UriBuilder.fromUri(this.gitLabUri).path("api").path(GIT_LAP_API_VERSION)
        .queryParam(PRIVATE_TOKEN, gitlabPrivateToken);
  }

  /**
   * @return all the projects form gitlab which are merge_requestable
   */
  public List<GitLabProject> getProjects() {

    List<GitLabProject> result = new ArrayList<GitLabProject>();

    int page = 0;
    int size = 0;
    do {

      size = result.size();

      URI uri = this.getGitlabRestAPIRoot().path(PROJECTS).queryParam("page", ++page).build();

      WebResource resource = client.resource(uri);

      result.addAll(resource.accept("application/json").get(new GenericType<List<GitLabProject>>() {
      }));

    } while (result.size() - size > 0);

    return result;

  }

  /**
   * @return all the users defined in the system
   */
  public List<User> getUsers() {
    URI uri = this.getGitlabRestAPIRoot().path(USERS).build();
    WebResource resource = client.resource(uri);

    return resource.accept("application/json").get(new GenericType<List<User>>() {
    });

  }

  /**
   * case where we try to gess the project id from artifact id
   * 
   * @param mavenProject
   * @param branch
   * @return newly created merge request
   * @throws NoSuchProjectException
   */
  public MergeRequest issueMergeRequest(MavenProject mavenProject, String comment, String branch,BugTrackerService btservice,User user)
      throws NoSuchProjectException {

    
    List<GitLabProject> projects = getProjects();

    Map<String, Object> args = this.service.issueMergeRequest(mavenProject.getScm().getConnection(), comment, branch,
        projects,btservice,user);

    return createMergeRequest(args);

  }

  /**
   * case where we know the project id
   * 
   * @param project
   * @param branch
   * @param projectId
   * @return newly created merge request
   */
  public MergeRequest issueMergeRequest(MavenProject project, String comment, String branch, Integer projectId) {
    List<User> users = getUsers();

    Map<String, Object> args = this.service.issueMergeRequest(project.getArtifactId(), comment, branch, 
        users.get(0),projectId);

    return createMergeRequest(args);

  }

  /**
   * Perform WS call
   * 
   * @param args
   *        a map of parameters
   * @return newly created merge request
   */
  private MergeRequest createMergeRequest(Map<String, Object> args) {
    URI uri = this.getGitlabRestAPIRoot().path(PROJECTS).path("" + args.get(GitLabService.ID)).path(MERGE_REQUEST)
        .queryParam(GitLabService.SRC_BRANCH, args.get(GitLabService.SRC_BRANCH))
        .queryParam(GitLabService.TGT_BRANCH, args.get(GitLabService.TGT_BRANCH))
        .queryParam(GitLabService.TITLE, args.get(GitLabService.TITLE))
        .queryParam(GitLabService.ASS_ID, args.get(GitLabService.ASS_ID))

        .build();
    WebResource resource = client.resource(uri);

    return resource.accept("application/json").post(MergeRequest.class);
  }

  /**
   * giver a project and the façade configuration, return an url to clone the
   * project
   * 
   * @param project
   * @return the url to use to clone the project
   */
  public String getGitRepoUrl(GitLabProject project) {

    return "git@" + this.gitLabUri.getAuthority() + ":" + project.getPath_with_namespace() + ".git";

  }

  public List<GitLabBranch> getBranchesForProject(Integer projectId) {
    URI uri = this.getGitlabRestAPIRoot().path(PROJECTS).path("" + projectId).path(REPOSITORY).path(BRANCHES).build();
    WebResource resource = client.resource(uri);
    return resource.accept("application/json").get(new GenericType<List<GitLabBranch>>() {
    });
  }

  public List<GitLabTag> getTagsForProject(Integer projectId) {
    URI uri = this.getGitlabRestAPIRoot().path(PROJECTS).path("" + projectId).path(REPOSITORY).path(TAGS).build();
    WebResource resource = client.resource(uri);
    return resource.accept("application/json").get(new GenericType<List<GitLabTag>>() {
    });
  }

  public GitLabProject createProject(final String projectName, final String namespace) {
    URI uri = this.getGitlabRestAPIRoot().path(PROJECTS).queryParam(NAME, projectName)
        .queryParam(DESCRIPTION, "moulinette pour le projet " + projectName).queryParam(DEFAULT_BRANCH, "master")
        .queryParam(ISSUE_ENABLED, "false").queryParam(WALL_ENABLED, "false")
        .queryParam(MERGE_REQUEST_ENABLED, "false").queryParam(WIKI_ENABLED, "false").build();
    client.resource(uri).post();

    return Iterables.find(this.getProjects(), new FindProjectByName(projectName), null);

  }

public String getBranchUrl(String pomScmUri,String branch) {
	//example URI: scm:git:ssh://git@git.erel.net/n.herbaut/a.git
	Pattern pat = Pattern.compile(".*\\/\\/.*@.*/(.*/.*).git");
	Matcher mat = pat.matcher(pomScmUri);
	if(mat.matches()){
		return UriBuilder.fromUri(this.gitLabUri).path(mat.group(1)).path("commits").path(branch).build().toASCIIString();	
	}
	else{
		return branch;
	}
	
	
	
}


public String getMergeRequest(String pomScmUri,String id) {
	//example URI: scm:git:ssh://git@git.erel.net/n.herbaut/a.git
	Pattern pat = Pattern.compile(".*\\/\\/.*@.*/(.*/.*).git");
	Matcher mat = pat.matcher(pomScmUri);
	if(mat.matches()){
		return UriBuilder.fromUri(this.gitLabUri).path(mat.group(1)).path("merge_requests").path(id).build().toASCIIString();	
	}
	else{
		return id;
	}
	
	
	
}
}
