package net.erel.maven.plugins;

import net.erel.maven.plugins.domain.maven.PGXVersion;
import net.erel.maven.plugins.domain.maven.PGXVersionBuilder;

import org.junit.Assert;
import org.junit.Test;

public class PGXVersionTest {

	@Test
	public void testAll() {

		PGXVersionBuilder builder = new PGXVersionBuilder();
		String res = builder.major(1).minor(2).increment(3).rc(4).snapshot(true).build();
		Assert.assertEquals("1.2.3-RC04-SNAPSHOT", res);

		PGXVersion version = new PGXVersion("04.05.06-RC7");
		Assert.assertEquals("4.5.6-RC07", version.toString());

	}

}
