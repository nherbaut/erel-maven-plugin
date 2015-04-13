package net.erel.maven.plugins.exception;

public class ExistingReleaseException
    extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -6444530108097173881L;

  public ExistingReleaseException(String versionName) {
    super("already existing version: " + versionName);
  }

}
