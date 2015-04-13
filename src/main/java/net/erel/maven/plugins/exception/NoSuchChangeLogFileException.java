package net.erel.maven.plugins.exception;

public class NoSuchChangeLogFileException
    extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -90571569314733393L;

  public NoSuchChangeLogFileException(String absolutePath) {
    super(absolutePath);
  }

}
