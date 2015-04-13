package net.erel.maven.plugins.mojo;

import net.erel.maven.plugins.service.setup.SetupService;
import net.erel.maven.plugins.utils.L18nHelper;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import com.google.common.base.Strings;

/**
 * Cette classe est utilisée pour configurer le plugin. Il permet de remplir
 * interactivement le settings.xml
 * 
 * @author nherbaut
 */
@Mojo(name = "setup", requiresProject = false, aggregator = true)
public class SetupMojo extends AbstractMojo {

	@Component
	protected Prompter prompter;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			SetupService service = new SetupService();

			for (String var : service.getNeededVariables()) {
				String currentVar = service.getCurrentValue(var);

				// if we have a current var, propose it, if not, propose a
				// default value is available
				String propozedValue = Strings.isNullOrEmpty(currentVar) ? L18nHelper
						.defaultValue(var) : currentVar;
				String newVar = null;
				if (Strings.isNullOrEmpty(propozedValue)) {

					newVar = this.prompter.prompt(L18nHelper.t(
							"please-provide-value-for-X", var)
							+ L18nHelper.help(var));
				}

				else {
					newVar = this.prompter.prompt(
							L18nHelper.t("please-provide-value-for-X", var)
									+ L18nHelper.help(var), propozedValue);
				}

				service.addToContext(var, newVar);
			}

			service.buildSettingsXml();

		} catch (PrompterException e) {
			throw new MojoExecutionException("", e);
		}

	}
}
