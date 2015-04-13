package net.erel.maven.plugins.service.setup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.erel.maven.plugins.utils.SLF4JLogChute;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

public class SetupService
    extends SLF4JLogChute {

  private Template t;
  private VelocityContext context;

  private static final Logger LOGGER = LoggerFactory.getLogger(SetupService.class);

  public SetupService() throws MojoExecutionException {
    init("settings.xml.tpl");
  }

  protected SetupService(String template) throws MojoExecutionException {
    init(template);
  }

  protected void init(String template) throws MojoExecutionException {

    try {

      Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this);

      Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
      Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

      Velocity.init();

      context = new VelocityContext();

      t = Velocity.getTemplate(template);

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoExecutionException("failed to setup environement", e);
    }

  }

  protected String getTemplate() throws IOException {
    return CharStreams.toString(new InputStreamReader(this.getClass().getClassLoader()
        .getResourceAsStream("settings.xml.tpl"), Charsets.UTF_8));
  }

  public String getCurrentValue(String name) {

    try {

      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();
      DocumentBuilderFactory dBFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dB = dBFactory.newDocumentBuilder();
      Document doc = dB.parse(getSettingsXML());
      XPathExpression expr = xpath.compile("//" + name + "/text()");
      return (String) expr.evaluate(doc, XPathConstants.STRING);
    } catch (Exception e) {
      return "";
    }

  }

  @SuppressWarnings("unchecked")
public Collection<String> getNeededVariables() {

    String template;
    try {
      template = getTemplate();
    } catch (IOException e) {
      template = null;
    }
    if (!Strings.isNullOrEmpty(template)) {
      Set<String> res = new HashSet<String>();

      Pattern pattern = Pattern.compile(".*(?:\\$\\{(erel_.*)\\}).*");
      Matcher matcher = pattern.matcher(template);
      while (matcher.find()) {
        res.add(matcher.group(1).substring(4));
      }

      return res;
    } else {
      return Collections.EMPTY_SET;
    }

  }

  public void addToContext(String key, String value) {
    context.put("erel_" + key, value);

  }

  protected File getSettingsXML() {
    return (Paths.get(System.getProperty("user.home"), ".m2", "settings.xml")).toFile();
  }

  protected void buildSettingsXml(Writer w) throws MojoExecutionException {

    try {
      t.merge(context, w);
      w.flush();
    } catch (IOException e) {
      throw new MojoExecutionException("failed to save settings.xml", e);
    }

  }

  public void backupSettingsXml() {
    File settingsXml = getSettingsXML();
    if (settingsXml.exists()) {
      try {
        Files.move(settingsXml, new File(settingsXml, "-backup" + System.currentTimeMillis()));
      } catch (IOException e) {
        LOGGER.error("failed to backup current settings.xml");
      }
    }
  }

  public void buildSettingsXml() throws MojoExecutionException {
    FileWriter fw;
    try {
      fw = new FileWriter(getSettingsXML());
      buildSettingsXml(fw);
    } catch (IOException e) {
      throw new MojoExecutionException("failed to save settings.xml", e);
    }

  }

}
