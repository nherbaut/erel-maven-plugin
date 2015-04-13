package net.erel.maven.plugins.service.git;

import static org.junit.Assert.*;

import net.erel.maven.plugins.service.git.GitLabRestFacade;

import org.junit.Test;

public class GitLabRestFacadeTest {

	@Test
	public void testSanitizeUrl() {

		assertEquals("google.fr/nicolas", GitLabRestFacade.getAuthority("http://google.fr/nicolas"));
		assertEquals("google.fr/nicolas", GitLabRestFacade.getAuthority("https://google.fr/nicolas"));
		assertEquals("google.fr/nicolas", GitLabRestFacade.getAuthority("google.fr/nicolas"));

	}
	
	@Test
	public void testgiturlcommit(){
		GitLabRestFacade fac = new GitLabRestFacade("http://git.erel.net", "gitlabPrivateToken");
		String res = fac.getBranchUrl("scm:git:ssh://git@git.erel.net/n.herbaut/a.git", "feature-CSP301-titi");
		
		assertEquals("http://git.erel.net/n.herbaut/a/commits/feature-CSP301-titi",res);
	}
	
	


}
