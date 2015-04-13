package net.erel.maven.plugins.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.utils.ANSI_COLOR;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * Base class for erel mojos that allow interaction with client
 * 
 * @author nherbaut
 */
public abstract class CommandLineExecutorAbstractMojo
    extends PGXAbstractMojo {

  /**
   * display output from command, from
   * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
   * 
   * @author nherbaut
   */
  class StreamGobbler
      extends Thread {
    InputStream is;
    String type;
    Prompter p;
    String color;

    StreamGobbler(InputStream is, String type, ANSI_COLOR color, Prompter p) {
      this.is = is;
      this.type = type;
      this.p = p;
      this.color = color.getColorCode();
    }

    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null)
          p.showMessage(this.color + type + ANSI_COLOR.RESET.getColorCode() + ">" + line + "\n");
      } catch (IOException | PrompterException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  protected static List<String> tokenize(String input) {

    // tokenize the string => je vais bien "merci beaucoup" =>
    // [je,vais,bien,merci beaucoup] othersize we are screwed with commits
    List<String> tokenizedCommand = new ArrayList<String>();
    Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
    while (m.find())
      tokenizedCommand.add(m.group(1));

    return tokenizedCommand;

  }

  /**
   * do the command and use prompter to display its result
   * 
   * @param command
   * @return the exit value of the process. By convention, 0 indicates normal
   *         termination.
   * @throws IOException
   * @throws InterruptedException
   * @throws PrompterException
   */
  protected int _do(String command, File workingDirectory) throws IOException, InterruptedException, PrompterException {
    this.prompter.showMessage(ANSI_COLOR.YELLOW.getColorCode() + "executing:\t" + command
        + ANSI_COLOR.RESET.getColorCode());

    Runtime rt = Runtime.getRuntime();

    List<String> tokenizedCommand = tokenize(command);

    Process p = rt.exec(Arrays.copyOf(tokenizedCommand.toArray(), tokenizedCommand.size(), String[].class), null,
        workingDirectory);

    StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "2>", ANSI_COLOR.YELLOW, this.prompter);
    StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", ANSI_COLOR.GREEN, this.prompter);

    errorGobbler.start();
    outputGobbler.start();

    int res = p.waitFor();

    return res;
  }

  protected void __do(String command, File workingDirectory) throws DoException {
    try {
      if (_do(command, workingDirectory) != 0) {
        throw new DoException("bad outcome");
      }

    } catch (IOException | InterruptedException | PrompterException | DoException e) {
      throw new DoException(e.getMessage());
    }
  }

  /**
   * same a _do, but with exception typing insead
   * 
   * @param command
   * @throws DoException
   */
  protected void __do(String command) throws DoException {
    __do(command, new File("."));
  }

}
