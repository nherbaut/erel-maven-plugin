package net.erel.maven.plugins.domain.maven;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a wrapper around the version, because maven class that does it is not
 * expendable
 * 
 * @author nherbaut
 */
public class PGXVersion
    implements Comparable<PGXVersion> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PGXVersion.class);

  public void setMajor(Integer major) {
    this.major = major;
  }

  public void setMinor(Integer minor) {
    this.minor = minor;
  }

  public void setIncrement(Integer increment) {
    this.increment = increment;
  }

  public void setRc(Integer rc) {
    this.rc = rc;
  }

  public void setSnapshot(boolean snapshot) {
    this.snapshot = snapshot;
  }

  public Integer getMajor() {
    return major;
  }

  public Integer getMinor() {
    return minor;
  }

  public Integer getIncrement() {
    return increment;
  }

  public Integer getRc() {
    return rc;
  }

  public Boolean isRc() {
    return rc != null;
  }

  public boolean getSnapshot() {
    return snapshot != null ? snapshot : false;
  }

  public static Pattern getPgxversionpattern() {
    return erelVersionPattern;
  }

  protected Integer major = null;
  protected Integer minor = null;
  protected Integer increment = null;
  protected Integer rc = null;
  protected Boolean snapshot = null;

  private static final Pattern erelVersionPattern = Pattern
      .compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-RC(\\d+))?(?:-(SNAPSHOT))?");

  public PGXVersion(MavenProject project) {
    this(project, false);
  }

  public PGXVersion(String version) {

    this(version, false);

  }

  public PGXVersion(MavenProject project, boolean throwIfBadVersion) {
    this(project.getVersion(), throwIfBadVersion);
  }

  public PGXVersion(String version, boolean throwIfBadVersion) {

    try {
      init(version);

    } catch (IllegalArgumentException e) {
      if (throwIfBadVersion == true) {
        throw e;
      } else {
        init("0.0.0-SNAPSHOT");
      }
    }

  }

  private void init(String version) {

    Matcher matcher = erelVersionPattern.matcher(version);

    if (matcher.matches()) {
      this.major = new Integer(matcher.group(1));
      this.minor = new Integer(matcher.group(2));
      this.increment = new Integer(matcher.group(3));

      this.rc = matcher.group(4) != null ? new Integer(matcher.group(4)) : null;
      this.snapshot = matcher.group(5) != null && !matcher.group(5).isEmpty();
    } else {
      PGXVersion.LOGGER.trace("version with bad format: {}", version);
      throw new IllegalArgumentException(version);

    }

  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(this.major).append(".");
    sb.append(this.minor).append(".");
    sb.append(this.increment);
    if (this.rc != null) {
      sb.append("-RC").append(String.format("%02d", this.rc));
    }
    if (this.snapshot != null && snapshot) {
      sb.append("-SNAPSHOT");
    }

    return sb.toString();

  }

  public PGXVersion() {
  }

  @Override
  public int compareTo(PGXVersion o) {

    {
      int res = this.major.compareTo(o.getMajor());
      if (res != 0) {
        return res;
      }
    }

    {
      int res = this.minor.compareTo(o.getMinor());
      if (res != 0) {
        return res;
      }
    }

    {
      int res = this.increment.compareTo(o.getIncrement());
      if (res != 0) {
        return res;
      }
    }

    {

      if (rc == null && o.getRc() != null) {
        return 1;
      }

      if (rc != null && o.getRc() == null) {
        return -1;
      }

      if (!(rc == null && o.getRc() == null)) {
        int res = this.rc.compareTo(o.getRc());
        if (res != 0) {
          return res;
        }
      }

    }

    {

      int res = new Boolean(this.getSnapshot()).compareTo(o.getSnapshot());
      if (res != 0) {
        return res * -1;
      }
    }

    return 0;
  }

}