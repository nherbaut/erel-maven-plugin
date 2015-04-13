package net.erel.maven.plugins.service.nexus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;

import net.erel.maven.plugins.domain.generated.NexusArtifact;
import net.erel.maven.plugins.domain.generated.SearchResponse;
import net.erel.maven.plugins.domain.maven.PGXVersion;
import net.erel.maven.plugins.utils.L18nHelper;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class NexusServiceImpl {

  private Client client;
  private String host;

  private static final Logger LOGGER = LoggerFactory.getLogger(NexusServiceImpl.class);

  public NexusServiceImpl(String host, String user, String password) {

    ClientConfig cc = new DefaultClientConfig();
    cc.getClasses().add(MOXyJsonProvider.class);

    this.client = Client.create(cc);
    this.client.addFilter(new HTTPBasicAuthFilter(user, password));
    this.host = host;

  }

  private UriBuilder getUriRoot() {
    return UriBuilder.fromUri(this.host).path("service").path("local").path("lucene").path("search");
  }

  public List<NexusArtifact> getArtifact(String query) {
    return this.client.resource(this.getUriRoot().queryParam("q", query).build()).get(SearchResponse.class).getData()
        .getArtifact();
  }

  public NexusArtifact getLatestRC(String groupId, String artifactId, String packaging, final String versionMax,
      final boolean allowSnapshot) {
    Iterable<NexusArtifact> artifacts = this.client
        .resource(
            this.getUriRoot().queryParam("g", groupId).queryParam("a", artifactId).queryParam("p", packaging).build())
        .get(SearchResponse.class).getData().getArtifact();

    Ordering<NexusArtifact> o = new Ordering<NexusArtifact>() {
      @Override
      public int compare(NexusArtifact left, NexusArtifact right) {
        PGXVersion leftV = new PGXVersion(left.getVersion());
        PGXVersion rightV = new PGXVersion(right.getVersion());

        return leftV.compareTo(rightV);
      }
    };

    final PGXVersion erelVersionMax = new PGXVersion(versionMax);
    // filter out artifact for version > that the one specified

    artifacts = Iterables.filter(artifacts, new Predicate<NexusArtifact>() {

      @Override
      public boolean apply(@Nullable NexusArtifact input) {

        PGXVersion version = new PGXVersion(input.getVersion());

        if (!allowSnapshot && version.getSnapshot()) {
          return false;
        }

        if (versionMax != null) {
          boolean res = erelVersionMax.compareTo(version) >= 0;
          LOGGER.debug("{} =< {} = {}", input.getVersion(), erelVersionMax.toString(), res);
          return res;
        } else {
          return true;
        }

      }
    });

    return o.max(artifacts);
  }

  public List<Artifact> interactivelyUpgradeArtifacts(String basePackage, MavenProject project, Prompter prompter)
      throws PrompterException {
    @SuppressWarnings("unchecked")
	Set<Artifact> artifacts = project.getDependencyArtifacts();
    List<Artifact> res = new ArrayList<Artifact>();

    for (Artifact artifact : artifacts) {
      if (artifact.getGroupId().startsWith(basePackage)) {
        PGXVersion currentDepVersion = new PGXVersion(artifact.getVersion());
        // allow ugrating RC, but that's all
        currentDepVersion.setRc(null);
        currentDepVersion.setSnapshot(false);

        String newVersionAvailable = this.getLatestRC(artifact.getGroupId(), artifact.getArtifactId(),
            artifact.getType(), currentDepVersion.toString(), false).getVersion();

        if (!Strings.isNullOrEmpty(newVersionAvailable)) {
          String response = prompter.prompt(
              L18nHelper.t("new-version-found-for-X-Y-upgrade", artifact.getGroupId() + ":" + artifact.getArtifactId()
                  + ":" + artifact.getVersion(), newVersionAvailable), Arrays.asList("y", "n"));
          if (response.equals("y")) {
            artifact.setVersion(newVersionAvailable);
            res.add(artifact);
          }

        } else {
          prompter.showMessage(L18nHelper.t("no-new-version-for-X",
              artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion()));
        }

      }
    }

    return res;

  }
}
