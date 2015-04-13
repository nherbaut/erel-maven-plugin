package net.erel.maven.plugins.utils;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import net.erel.maven.plugins.mojo.CommandLineExecutorAbstractMojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class L18nHelper {

  private static Properties help = new Properties();
  private static Properties defaultValues = new Properties();

  private final static Logger LOGGER = LoggerFactory.getLogger(L18nHelper.class);

  static {

    try {
      help.load(L18nHelper.class.getClassLoader().getResourceAsStream("setup-help.properties"));
      defaultValues.load(L18nHelper.class.getClassLoader().getResourceAsStream("setup-default.properties"));
    } catch (IOException e) {
      LOGGER.error("failed to load help file", e);
    }
  }

  /**
   * translate the key, fallback, with color
   * 
   * @param key
   * @param color
   * @param args
   * @return
   */
  public static String t(String key, ANSI_COLOR color, String... args) {
    return L18nHelper.t(color.getColorCode() + key + ANSI_COLOR.RESET.colorCode, args);
  }

  /**
   * translate the key, fallback if not found
   * 
   * @param key
   * @return
   */
  public static String t(String key, String... args) {

    try {

      return String.format(L18nHelper.getResourceBundle(Locale.FRENCH).getString(key), (Object[]) args) + "\n";

    } catch (Exception e) {
      StringBuilder res = new StringBuilder(key);
      for (String arg : args) {
        res.append("\t").append(arg);
      }
      res.append("\n");
      return res.toString();
    }

  }

  public static String help(String key) {
    if (help.containsKey(key)) {
      return ANSI_COLOR.BLUE + help.getProperty(key) + ANSI_COLOR.RESET;
    } else
      return "";
  }
  
  
  public static String defaultValue(String key) {
	    if (defaultValues.containsKey(key)) {
	      return defaultValues.getProperty(key);
	    } else
	      return "";
	  }

  /**
   * @return the resource bundle associated with the mojo
   */
  static ResourceBundle getResourceBundle(Locale locale) {
    return ResourceBundle.getBundle("erel-messages", locale, CommandLineExecutorAbstractMojo.class.getClassLoader());
  }

}
