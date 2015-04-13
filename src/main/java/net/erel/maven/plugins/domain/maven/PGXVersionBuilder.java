package net.erel.maven.plugins.domain.maven;


/**
 * tool to build version easily
 * 
 * @author nherbaut
 * 
 */
public class PGXVersionBuilder {

	private PGXVersion version = new PGXVersion();

	public PGXVersionBuilder major(Integer major) {
		this.version.setMajor(major);
		return this;
	}

	public PGXVersionBuilder minor(Integer minor) {
		this.version.setMinor(minor);
		return this;
	}

	public PGXVersionBuilder increment(Integer increment) {
		this.version.setIncrement(increment);
		return this;
	}

	public PGXVersionBuilder rc(Integer rc) {
		this.version.setRc(rc);
		return this;

	}

	public PGXVersionBuilder snapshot(boolean bool) {
		this.version.setSnapshot(bool);
		return this;
	}

	public String build() {
		return this.version.toString();

	}
}