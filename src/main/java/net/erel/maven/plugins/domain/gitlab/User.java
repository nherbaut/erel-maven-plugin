package net.erel.maven.plugins.domain.gitlab;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * domain object for gitlab, see
 * https://github.com/gitlabhq/gitlabhq/tree/master/doc/api
 * 
 * @author nherbaut
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class User extends Owner {

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getSkype() {
		return skype;
	}

	public void setSkype(String skype) {
		this.skype = skype;
	}

	public String getLinkedin() {
		return linkedin;
	}

	public void setLinkedin(String linkedin) {
		this.linkedin = linkedin;
	}

	public String getTwitter() {
		return twitter;
	}

	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}

	public Boolean getDark_scheme() {
		return dark_scheme;
	}

	public void setDark_scheme(Boolean dark_scheme) {
		this.dark_scheme = dark_scheme;
	}

	public String getExtern_uid() {
		return extern_uid;
	}

	public void setExtern_uid(String extern_uid) {
		this.extern_uid = extern_uid;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Integer getTheme_id() {
		return theme_id;
	}

	public void setTheme_id(Integer theme_id) {
		this.theme_id = theme_id;
	}

	private String bio;
	private String skype;
	private String linkedin;
	private String twitter;
	private Boolean dark_scheme;
	private String extern_uid;
	private String provider;
	private Integer theme_id;

}
