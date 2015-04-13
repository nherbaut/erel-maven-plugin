package net.erel.maven.plugins.service.nexus;

import net.erel.maven.plugins.domain.maven.PGXVersion;

import org.junit.Assert;
import org.junit.Test;

public class PGXVersionComparable {

  @Test
  public void testAll() {
    Assert.assertTrue(new PGXVersion("1.0.0").compareTo(new PGXVersion("2.0.0")) < 0);
    Assert.assertTrue(new PGXVersion("1.0.0-RC1").compareTo(new PGXVersion("1.0.0")) < 0);
    Assert.assertTrue(new PGXVersion("1.0.0-RC1-SNAPSHOT").compareTo(new PGXVersion("1.0.0-RC1")) < 0);
    Assert.assertTrue(new PGXVersion("1.0.0-RC2-SNAPSHOT").compareTo(new PGXVersion("1.0.0-RC3")) < 0);
    Assert.assertTrue(new PGXVersion("1.1.1").compareTo(new PGXVersion("1.1.2")) < 0);
    Assert.assertTrue(new PGXVersion("1.1.1").compareTo(new PGXVersion("1.2.1")) < 0);
    Assert.assertTrue(new PGXVersion("1.1.1").compareTo(new PGXVersion("2.1.1")) < 0);

  }

}
