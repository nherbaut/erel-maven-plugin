package net.erel.maven.plugins.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.DefaultModelWriter;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

  @Test
  public void testHandleBadPom() throws IOException, IllegalArgumentException, IllegalAccessException {

    DefaultModelReader reader = new DefaultModelReader();
    Model model = reader.read(this.getClass().getClassLoader().getResourceAsStream("badpom.xml"), null);

    Utils.cleanUpModel(model, "/tmp/1370511369655-0/moulinette-test10");

    Assert.assertTrue(model.getBuild().getSourceDirectory() == null);

    DefaultModelWriter writer = new DefaultModelWriter();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    writer.write(bos, null, model);

    Assert.assertFalse(bos.toString().contains("1370511369655"));

  }

}
