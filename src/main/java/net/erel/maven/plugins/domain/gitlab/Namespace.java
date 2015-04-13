package net.erel.maven.plugins.domain.gitlab;

/**
 * domain object for gitlab, see
 * https://github.com/gitlabhq/gitlabhq/tree/master/doc/api
 * 
 * @author nherbaut
 *
 */
public class Namespace extends IdentifiableGitlabAsset implements
		Comparable<Namespace> {

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public Integer getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(Integer owner_id) {
		this.owner_id = owner_id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	private String created_at;

	private Integer owner_id;
	private String path;
	private String updated_at;

	@Override
	public int compareTo(Namespace o) {
		return this.getId().compareTo(o.getId());
	}

}
