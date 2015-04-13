package net.erel.maven.plugins.domain.gitlab;

/**
 * domain object for gitlab, see https://github.com/gitlabhq/gitlabhq/tree/master/doc/api
 * @author nherbaut
 *
 */
public class GitLabBranch {

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCommitted_date() {
		return committed_date;
	}

	public void setCommitted_date(String committed_date) {
		this.committed_date = committed_date;
	}

	String name;
	String message;
	String committed_date;

}
