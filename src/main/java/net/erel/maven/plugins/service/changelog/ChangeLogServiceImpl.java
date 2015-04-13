package net.erel.maven.plugins.service.changelog;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.erel.maven.plugins.domain.changelog.Action;
import net.erel.maven.plugins.domain.changelog.Author;
import net.erel.maven.plugins.domain.changelog.Body;
import net.erel.maven.plugins.domain.changelog.ChangesDocument;
import net.erel.maven.plugins.domain.changelog.ObjectFactory;
import net.erel.maven.plugins.domain.changelog.Properties;
import net.erel.maven.plugins.domain.changelog.Release;
import net.erel.maven.plugins.exception.ExistingReleaseException;
import net.erel.maven.plugins.exception.NoSuchReleaseException;
import net.erel.maven.plugins.service.bugtracking.BugTrackerService;
import net.erel.maven.plugins.utils.L18nHelper;

import org.apache.maven.project.MavenProject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * This class realizes operation for change log, that is create an entry in changes.xml file for each merged merge
 * request
 * 
 * @author nherbaut
 */
public class ChangeLogServiceImpl {

  private MavenProject project;

  private Unmarshaller unmarshaller;

  private Marshaller marshaller;

  private File changeDescriptor;

  private ObjectFactory factory;

  private String user;

  @SuppressWarnings("unused")
  private static Logger LOGGER = LoggerFactory.getLogger(ChangeLogServiceImpl.class);

  private BugTrackerService redmineService;

  public ChangeLogServiceImpl(MavenProject project, String user, String apiAccessKey, BugTrackerService btService)
      throws JAXBException {

    this(project, user, btService);

  }

  public ChangeLogServiceImpl(MavenProject project, String user, BugTrackerService redmineService) throws JAXBException {
    this.project = project;
    JAXBContext context = JAXBContext.newInstance("net.erel.maven.plugins.domain.changelog");
    this.unmarshaller = context.createUnmarshaller();
    this.marshaller = context.createMarshaller();

    this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    this.changeDescriptor = Paths.get(this.project.getBasedir().getAbsolutePath(), "src", "changes", "changes.xml")
        .toFile();

    this.factory = new ObjectFactory();

    this.user = user;

    this.redmineService = redmineService;

  }

  /**
   * @param versionName
   * @return a map containing both release and Changesdocument, in a map (key=class)
   * @throws NoSuchReleaseException
   */
  @SuppressWarnings("unchecked")
  private Map<Class<?>, Object> getRelease(String versionName) throws NoSuchReleaseException {

    Map<Class<?>, Object> res = new HashMap<Class<?>, Object>();
    try {
      if (this.changeDescriptor.exists()) {
        ChangesDocument changeDoc;

        changeDoc = ((JAXBElement<ChangesDocument>) this.unmarshaller.unmarshal(changeDescriptor)).getValue();

        for (Release rel : changeDoc.getBody().getRelease()) {
          if (rel.getVersion().equals(versionName)) {
            res.put(Release.class, rel);
            res.put(ChangesDocument.class, changeDoc);
            return res;

          }

        }

      }
      throw new NoSuchReleaseException();

    } catch (JAXBException e) {
      throw Throwables.propagate(e);
    }

  }

  /**
   * Get the unmarshalled change document
   * 
   * @return
   * @throws JAXBException
   */
  @SuppressWarnings("unchecked")
  private ChangesDocument getChangeDocument() throws JAXBException {

    ChangesDocument changeDoc = null;
    Body body = null;
    if (changeDescriptor.canRead()) {
      // file exist, we just update

      changeDoc = ((JAXBElement<ChangesDocument>) this.unmarshaller.unmarshal(changeDescriptor)).getValue();

      body = changeDoc.getBody();

    } else {
      // file doesn't exist, we create
      changeDescriptor.getParentFile().mkdirs();

      changeDoc = factory.createChangesDocument();
      Properties properties = factory.createProperties();
      Author author = factory.createAuthor();
      author.setEmail("dev@erel.net");
      author.getContent().add("erel");

      properties.setAuthor(author);
      properties.setTitle("Changelog pour " + project.getName());
      changeDoc.setProperties(properties);
      body = factory.createBody();
      changeDoc.setBody(body);
    }

    saveToDisk(changeDoc);
    return changeDoc;

  }

  /**
   * Marshall changes documents
   * 
   * @param changeDoc
   */
  private void saveToDisk(ChangesDocument changeDoc) {

    try {

      JAXBElement<?> doc = factory.createDocument(changeDoc);
      marshaller.marshal(doc, changeDescriptor);
    } catch (JAXBException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Make the current release as the official release for release name "New name"
   * 
   * @param newName the name of the release to be created
   * @throws NoSuchReleaseException
   */
  public void updateReleaseNewVersion(String newName, String rcName, String projectName) throws NoSuchReleaseException {

    Map<Class<?>, Object> res = getRelease("develop");
    ChangesDocument cd = (ChangesDocument) res.get(ChangesDocument.class);
    Release rel = (Release) res.get(Release.class);

    rel.setVersion(newName);
    saveToDisk(cd);

    updateIssueNewReleaseCreated(newName, rcName, projectName);

  }

  /**
   * look in the changelog for issue for this version and update them telling that a new version has been released
   * 
   * @param releaseName
   * @param rcName
   * @param projectName
   * @throws NoSuchReleaseException
   */
  public void updateIssueNewReleaseCreated(String releaseName, String rcName, String projectName)
      throws NoSuchReleaseException {

    Map<Class<?>, Object> res = getRelease(releaseName);

    Release rel = (Release) res.get(Release.class);

    for (Action action : rel.getAction()) {
      this.redmineService.updateIssueReleased(action, rcName, projectName);
    }

    rel.setDate(getNow());
    rel.setDescription(rcName);

  }

  public void addTicketToCurrentVersion(String ticket) {
    addTicketToVersion(ticket, "develop");

  }

  /**
   * Add the following ticket to the current version of the changelog. Connects to Redmine to get more info in the
   * ticket
   * 
   * @param ticket a string representing the ticket name
   */
  public void addTicketToVersion(String ticket, String version) {
    try {
      Release rel = null;
      ChangesDocument cd = null;
      try {
        Map<Class<?>, Object> res = getRelease(version);
        cd = (ChangesDocument) res.get(ChangesDocument.class);
        rel = (Release) res.get(Release.class);

      } catch (NoSuchReleaseException e) {

        createRelease(version);

        Map<Class<?>, Object> res = getRelease(version);
        cd = (ChangesDocument) res.get(ChangesDocument.class);
        rel = (Release) res.get(Release.class);

      }

      Action action = factory.createAction();

      action.setDate(getNow());
      action.setDev(user);
      action.setIssue(ticket);

      this.redmineService.populateActionWithTicketDate(action);

      rel.getAction().add(action);
      saveToDisk(cd);

    } catch (NoSuchReleaseException | ExistingReleaseException e1) {
      // cannot happen
    }

  }

  /**
   * @return the current time in string format, no question asked
   */
  private static String getNow() {
    DateTimeFormatter formatter = ISODateTimeFormat.date();

    return new DateTime().toString(formatter);
  }

  /**
   * Add a new verison info the change log file. If the file doesn't exist, it's created
   * 
   * @param releaseName
   * @throws JAXBException
   * @throws ExistingReleaseException
   */

  public void createRelease(String releaseName) throws ExistingReleaseException {

    try {
      getRelease(releaseName);
    } catch (NoSuchReleaseException e) {
      Release release = factory.createRelease();

      release.setDate(getNow());
      release.setVersion(releaseName);

      ChangesDocument changeDoc;
      try {
        changeDoc = getChangeDocument();
      } catch (JAXBException e1) {
        throw Throwables.propagate(e1);
      }
      changeDoc.getBody().getRelease().add(release);

      saveToDisk(changeDoc);
      return;
    }

    throw new ExistingReleaseException(releaseName);

  }

  /**
   * @return a list of all release in the changelog
   */
  public List<Release> getReleases() {
    try {
      return this.getChangeDocument().getBody().getRelease();
    } catch (JAXBException e) {
      return Collections.emptyList();
    }
  }

  /**
   * at promote time, change the date the release has been created to match today and set the description to reflext the
   * promotion
   * 
   * @param gaVersion
   * @param rcVersion
   * @throws NoSuchReleaseException
   */
  public void promoteRelease(String gaVersion, String rcVersion) throws NoSuchReleaseException {

    Map<Class<?>, Object> res = getRelease(gaVersion);
    ChangesDocument cd = (ChangesDocument) res.get(ChangesDocument.class);
    Release rel = (Release) res.get(Release.class);
    rel.setDate(getNow());
    rel.setDescription(L18nHelper.t("promoted-from-rc-X", rcVersion));

    this.saveToDisk(cd);

  }
}
