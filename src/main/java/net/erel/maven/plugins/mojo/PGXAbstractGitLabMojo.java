package net.erel.maven.plugins.mojo;

import org.apache.maven.plugins.annotations.Parameter;

public abstract class PGXAbstractGitLabMojo
    extends CommandLineExecutorAbstractMojo {

  /**
   * url to the gitlab repository
   */
  @Parameter(property = "gitlabUrl", defaultValue = "http://git.erel.net/")
  protected String gitlabUrl;

  /**
   * your private gitlab token
   */
  @Parameter(property = "gitlabPrivateToken", required = true)
  protected String gitlabPrivateToken;

 
}
