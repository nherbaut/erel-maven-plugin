package net.erel.maven.plugins.domain.gitlab;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MergeRequest {

	private Integer id;
	private String target_branch;
	private String source_branch;
	private Integer project_id;
	private String title;
	private Boolean closed;
	private Boolean merged;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTarget_branch() {
		return target_branch;
	}

	public void setTarget_branch(String target_branch) {
		this.target_branch = target_branch;
	}

	public String getSource_branch() {
		return source_branch;
	}

	public void setSource_branch(String source_branch) {
		this.source_branch = source_branch;
	}

	public Integer getProject_id() {
		return project_id;
	}

	public void setProject_id(Integer project_id) {
		this.project_id = project_id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	public Boolean getMerged() {
		return merged;
	}

	public void setMerged(Boolean merged) {
		this.merged = merged;
	}

}
