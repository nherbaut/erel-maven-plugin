package net.erel.maven.plugins.mojo;

import java.util.Arrays;

import net.erel.maven.plugins.exception.DoException;
import net.erel.maven.plugins.utils.BigPrompt;
import net.erel.maven.plugins.utils.L18nHelper;
import net.erel.maven.plugins.utils.BigPrompt.Messages;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import com.google.common.base.Strings;

/**
 * Cette classe gère les dépendances des packets erel
 * 
 * @author nherbaut
 */
@Mojo(name = "dep", requiresProject = true, aggregator = true)
public class UpdateDependencyMojo extends CommandLineExecutorAbstractMojo {

	/**
	 * id of the server used to read the rule
	 */
	@Parameter(property = "serverID", required = false, defaultValue = "nexus")
	protected String serverId;

	/**
	 * uri of the rules file to use
	 */
	@Parameter(property = "rulesURI", required = false, defaultValue = "http://repo.erel.net/service/local/repositories/public/content/net/erel/utils/rules/rules/1.0.0/rules-1.0.0.xml")
	protected String rulesURI;

	/**
	 * the group id to include in the update
	 */
	@Parameter(property = "includes", required = false, defaultValue = "net.erel*")
	protected String includes;

	/**
	 * strategy when updating dependencies (WITH_SNAPSHOT, WITHOUT_SNAPSHOT or
	 * UPGRADE_RC)
	 */
	@Parameter(property = "strategy", required = false, defaultValue = "")
	protected String stategy;

	public enum UpdatedStrategy {
		WITH_SNAPSHOT("A"), WITHOUT_SNAPSHOT("B"), UPGRADE_RC("C"), ;

		private String value;

		private UpdatedStrategy(String value) {
			this.setValue(value);
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		BigPrompt.display(prompter, Messages.DEPENDENCIES);

		boolean majorAllowed = false;
		boolean minorAllowed = false;
		boolean incrementAllowed = false;
		boolean allowSnapshots= false;

		String res = null;

		try {

			if (!Strings.isNullOrEmpty(stategy)) {
				res = UpdatedStrategy.valueOf(stategy).getValue();
			}

			if (Strings.isNullOrEmpty(res)) {
				BigPrompt.display(prompter, Messages.DEPENDENCIES_HELP);
				res = prompter.prompt(L18nHelper
						.t("please-indicate-which-update-do-you-want"), Arrays
						.asList("A", "B", "C"), "A");
			}

			switch (res) {

			case "C":

				break;
			case "B":

				minorAllowed = true;
				incrementAllowed = true;

				break;

			case "A":
				majorAllowed = true;
				minorAllowed = true;
				incrementAllowed = true;
				allowSnapshots=true;
				break;

			}

			String commandUpdateRegular = "mvn -Dincludes=" + includes
					+ " -DallowMajorUpdates=" + majorAllowed
					+ " -DallowMinorUpdates=" + minorAllowed
					+ " -DallowIncrementalUpdates=" + incrementAllowed
					+ " -DallowSnapshots=" + allowSnapshots 
					+ " -Dmaven.version.rules.serverId=" + serverId
					+ " -Dmaven.version.rules=" + rulesURI
					+ "  org.codehaus.mojo:versions-maven-plugin:2.0-PGX:"
					+ "use-latest-versions";

			__do(commandUpdateRegular);

			//we don't update properties, since there's no way to restrict the update to erel project
			//we just show 'em
			
			String command = "mvn -Dincludes=" + includes
					+ " -Dmaven.version.rules.serverId=" + serverId
					+ " -Dmaven.version.rules=" + rulesURI
					+ "  org.codehaus.mojo:versions-maven-plugin:2.0-PGX:"
					+ "display-property-updates";


			__do(command);
			

		} catch (PrompterException | DoException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}
}
