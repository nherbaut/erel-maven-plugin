package net.erel.maven.plugins.domain.gitlab;

/**
 * domain object for gitlab, see https://github.com/gitlabhq/gitlabhq/tree/master/doc/api
 * @author nherbaut
 *
 */
public class GitLabTag {

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Commit getCommit() {
		return commit;
	}

	public void setCommit(Commit commit) {
		this.commit = commit;
	}

	private String name;
	private Commit commit;

}
