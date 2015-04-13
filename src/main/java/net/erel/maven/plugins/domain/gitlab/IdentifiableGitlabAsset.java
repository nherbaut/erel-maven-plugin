package net.erel.maven.plugins.domain.gitlab;

/**
 * tentative to interface gitlab assets
 * 
 * @author nherbaut
 * 
 */
public abstract class IdentifiableGitlabAsset {

	private Integer id;
	private String name;
	private Integer displayId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getDisplayId() {
		return displayId;
	}

	public void setDisplayId(Integer displayId) {
		this.displayId = displayId;
	}

}