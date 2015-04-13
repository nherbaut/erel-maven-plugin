package net.erel.maven.plugins.domain.gitlab;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * domain object for gitlab, see https://github.com/gitlabhq/gitlabhq/tree/master/doc/api
 * @author nherbaut
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GitLabProject extends IdentifiableGitlabAsset implements Comparable<GitLabProject>  {

	public String getDefault_branch() {
		return default_branch;
	}

	public void setDefault_branch(String default_branch) {
		this.default_branch = default_branch;
	}

	public String getPath_with_namespace() {
		return path_with_namespace;
	}

	public void setPath_with_namespace(String path_with_namespace) {
		this.path_with_namespace = path_with_namespace;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public void setNamespace(Namespace namespace) {
		this.namespace = namespace;
	}


	public void setMerge_requests_enabled(Boolean merge_requests_enabled) {
		this.merge_requests_enabled = merge_requests_enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.scm.domain.gitlab.Projecet#getId()
	 */
	/* (non-Javadoc)
	 * @see net.erel.scm.domain.gitlab.IdentifiableGitlabAsset#getId()
	 */
	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.erel.scm.domain.gitlab.Projecet#getName()
	 */
	/* (non-Javadoc)
	 * @see net.erel.scm.domain.gitlab.IdentifiableGitlabAsset#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDefaultBranch() {
		return default_branch;
	}

	public void setDefaultBranch(String defaultBranch) {
		this.default_branch = defaultBranch;
	}

	private Integer id;
	private String name;
	private String description;

	private String default_branch;

	private String path_with_namespace;

	private Owner owner;

	public Boolean getMerge_requests_enabled() {
		return merge_requests_enabled;
	}

	private Namespace namespace;

	private Boolean merge_requests_enabled;

	public String getPath() {
		return path_with_namespace;
	}

	public void setPath(String path) {
		this.path_with_namespace = path;
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
  }

  @Override
  public int compareTo(GitLabProject o) {
    return this.getName().compareTo(o.getName());
	}

}
