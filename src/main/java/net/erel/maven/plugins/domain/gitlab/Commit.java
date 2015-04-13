package net.erel.maven.plugins.domain.gitlab;

/**
 * domain object for gitlab, see https://github.com/gitlabhq/gitlabhq/tree/master/doc/api
 * @author nherbaut
 *
 */
public class Commit {

	private String id;
	private String message;
	private String committed_date;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

}
