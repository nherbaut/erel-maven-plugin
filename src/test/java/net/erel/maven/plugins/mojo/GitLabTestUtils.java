package net.erel.maven.plugins.mojo;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import net.erel.maven.plugins.domain.gitlab.GitLabProject;
import net.erel.maven.plugins.mojo.PGXAbstractReleaseMojo;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.junit.Assert;
import org.junit.Test;

public class GitLabTestUtils {

	public static List<GitLabProject> loadTestProjects() throws JAXBException {
		InputStream is = GitLabProjectTest.class.getClassLoader().getResourceAsStream("gitlab-project.json");

		JAXBContext context = JAXBContext.newInstance(GitLabProject.class);

		Unmarshaller u = context.createUnmarshaller();
		u.setProperty(JAXBContextProperties.MEDIA_TYPE, "application/json");
		u.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, false);

		@SuppressWarnings("unchecked")
		List<GitLabProject> projects = (List<GitLabProject>) u.unmarshal(new StreamSource(is), GitLabProject.class).getValue();

		return projects;
	}

	@Test
	public void testTokenizer() {
		
		{
			List<String> res = PGXAbstractReleaseMojo.tokenize("git salut \"je_vais_bien\"");

			int i = 0;
			Assert.assertTrue(res.get(i++).equals("git"));
			Assert.assertTrue(res.get(i++).equals("salut"));
			Assert.assertTrue(res.get(i++).equals("\"je_vais_bien\""));
		}
		
		{
			List<String> res = PGXAbstractReleaseMojo.tokenize("git salut \"je vais bien\"");

			int i = 0;
			Assert.assertTrue(res.get(i++).equals("git"));
			Assert.assertTrue(res.get(i++).equals("salut"));
			Assert.assertTrue(res.get(i++).equals("\"je vais bien\""));
		}

		

	}

}
