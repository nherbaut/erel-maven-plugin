package net.erel.maven.plugins.service.setup;

import java.io.File;
import java.io.StringWriter;
import java.net.URI;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import com.google.common.base.Throwables;

public class SetupServiceTest {

  @Test
  public void testAll() throws MojoExecutionException {

    SetupService service = new SetupService("test-template.vm");
    service.addToContext("salut", "salut");
    StringWriter writer = new StringWriter();
    service.buildSettingsXml(writer);
    org.junit.Assert.assertEquals("salut nounou!", writer.toString());

  }

  @Test
  public void testGetCurrentValue() throws MojoExecutionException {

    SetupService service = new SetupService() {
      @Override
      protected File getSettingsXML() {
        try {
          return new File(new URI(this.getClass().getClassLoader().getResource("settings.xml.tpl").toExternalForm()));
        } catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }
    };

    org.junit.Assert.assertEquals("http://192.168.0.72:8080/manager", service.getCurrentValue("deploy.server.url"));

  }
}
