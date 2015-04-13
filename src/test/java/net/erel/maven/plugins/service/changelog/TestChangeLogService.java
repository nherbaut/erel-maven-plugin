package net.erel.maven.plugins.service.changelog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import net.erel.maven.plugins.domain.changelog.ChangesDocument;
import net.erel.maven.plugins.exception.ExistingReleaseException;
import net.erel.maven.plugins.service.bugtracking.BugTrackerService;

import org.apache.maven.project.MavenProject;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;

public class TestChangeLogService {

  @SuppressWarnings("unchecked")
  @Test
  public void testCreateChangelog() throws IOException, JAXBException, ExistingReleaseException {

    EasyMockSupport support = new EasyMockSupport();

    File randomFile = Files.createTempDir();

    MavenProject mavenProject = support.createNiceMock(MavenProject.class);
    EasyMock.expect(mavenProject.getVersion()).andReturn("1.0.0-RC01").anyTimes();
    EasyMock.expect(mavenProject.getName()).andReturn("MockProjet").anyTimes();
    EasyMock.expect(mavenProject.getBasedir()).andReturn(randomFile).anyTimes();

    support.replayAll();

    ChangeLogServiceImpl service = new ChangeLogServiceImpl(mavenProject, "629", "", null);

    File resultingFile = Paths.get(randomFile.getAbsolutePath(), "src", "changes", "changes.xml").toFile();

    Assert.assertFalse(resultingFile.exists());
    service.createRelease("1.2.3");

    Assert.assertTrue(resultingFile.exists());

    JAXBContext context = JAXBContext.newInstance("net.erel.maven.plugins.domain.changelog");
    JAXBElement<ChangesDocument> doc = (JAXBElement<ChangesDocument>) context.createUnmarshaller().unmarshal(
        resultingFile);

    Assert.assertTrue(doc.getValue().getBody().getRelease().get(0).getVersion().equals("1.2.3"));

  }

  @Test
  public void testAddTicket() throws IOException, JAXBException, ExistingReleaseException {

    EasyMockSupport support = new EasyMockSupport();

    File randomFile = Files.createTempDir();
    File resultingFile = Paths.get(randomFile.getAbsolutePath(), "src", "changes", "changes.xml").toFile();

    MavenProject mavenProject = support.createNiceMock(MavenProject.class);
    EasyMock.expect(mavenProject.getVersion()).andReturn("1.0.0-RC01").anyTimes();
    EasyMock.expect(mavenProject.getName()).andReturn("MockProjet").anyTimes();
    EasyMock.expect(mavenProject.getBasedir()).andReturn(randomFile).anyTimes();
    
    BugTrackerService btservice = support.createNiceMock(BugTrackerService.class);

    support.replayAll();

    ChangeLogServiceImpl service = new ChangeLogServiceImpl(mavenProject, "nherbaut", "", btservice);
    service.addTicketToCurrentVersion("629");

    resultingFile.canRead();

  }

}
