package net.erel.maven.plugins.service.prompt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Nullable;

import net.erel.maven.plugins.domain.gitlab.GitLabBranch;
import net.erel.maven.plugins.domain.gitlab.GitLabProject;
import net.erel.maven.plugins.domain.gitlab.GitLabTag;
import net.erel.maven.plugins.domain.gitlab.IdentifiableGitlabAsset;
import net.erel.maven.plugins.domain.gitlab.Namespace;
import net.erel.maven.plugins.domain.gitlab.User;
import net.erel.maven.plugins.mojo.CommandLineExecutorAbstractMojo;
import net.erel.maven.plugins.service.git.GitLabRestFacade;
import net.erel.maven.plugins.utils.ANSI_COLOR;
import net.erel.maven.plugins.utils.L18nHelper;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/***
 * interactively ask user for information about repo, projects and branches
 * 
 * @author nherbaut
 * 
 */

public class ResourcePicker extends CommandLineExecutorAbstractMojo {

	private User user;

	// ///////////////////////// GUAVA FUNCTORS
	class AssetToIntegerAdapter implements
			Function<IdentifiableGitlabAsset, Integer> {

		int count = 0;

		public AssetToIntegerAdapter() {

		};

		@Override
		public Integer apply(@Nullable IdentifiableGitlabAsset input) {
			return count++;
		}
	};

	// ///////////////////////// GUAVA FUNCTORS
	class AssetToStringAdapter implements
			Function<IdentifiableGitlabAsset, String> {

		int count = 0;

		@Override
		public String apply(@Nullable IdentifiableGitlabAsset input) {
			return "" + count++;
		}
	};

	private final static Function<GitLabTag, String> TAG_AS_STRING = new Function<GitLabTag, String>() {

		private Integer index = 0;

		@Override
		public String apply(@Nullable GitLabTag input) {
			return "" + index++;
		}
	};

	private static final class ProjectGetNamespaceFunction implements
			Function<GitLabProject, Namespace> {
		private int counter = 1;

		@Override
		public Namespace apply(@Nullable GitLabProject input) {
			Namespace ns = input.getNamespace();
			ns.setDisplayId(counter++);
			return ns;
		}
	}

	private static final class TagAsStringFunction implements
			Function<GitLabTag, String> {
		private Integer index = 0;

		@Override
		public String apply(@Nullable GitLabTag input) {
			return "" + index++;
		}
	}

	private static final class AssetAsStringFunction implements
			Function<IdentifiableGitlabAsset, String> {
		private int counter = 1;

		@Override
		public String apply(@Nullable IdentifiableGitlabAsset input) {
			input.setDisplayId(counter++);
			return "" + input.getDisplayId();
		}
	}

	private static final class AssetAsIntegerFunction implements
			Function<IdentifiableGitlabAsset, Integer> {
		private int counter = 1;

		@Override
		public Integer apply(@Nullable IdentifiableGitlabAsset input) {
			input.setDisplayId(counter++);
			return input.getDisplayId();
		}
	}

	/**
	 * add scm:checkout mojo specific formatting for scm
	 * 
	 * @param repoUrl
	 * @return
	 */
	public String getScmConnection() {

		return "scm:git:ssh://" + gitRepoUrl.replace(":", "/");

	}

	public String getRepoUrl() {
		return this.gitRepoUrl;
	}

	public GitLabProject getProject() {
		return project;
	}

	public GitLabBranch getBranch() {
		return branch;
	}

	public GitLabTag getTag() {
		return tag;
	}

	public User getUser() {
		return this.user;
	}

	private String gitRepoUrl;

	private GitLabProject project = null;

	private GitLabBranch branch = null;

	private GitLabTag tag = null;

	private GitLabRestFacade facade;

	// ////////////////////////////////////////

	public void askForUser() {
		final Map<String, User> usersMap = Maps.uniqueIndex(
				this.facade.getUsers(), new AssetToStringAdapter());
		try {
			for (Entry<String, User> entry : usersMap.entrySet())
				this.prompter.showMessage(ANSI_COLOR.RED.getColorCode()
						+ entry.getKey() + ANSI_COLOR.RESET.getColorCode()
						+ "\t(" + entry.getValue().getName() + ")\n");

			String userId = this.prompter.prompt(
					L18nHelper.t("pick-recipient-for-merge-request"),
					Lists.newArrayList(usersMap.keySet()), "0");

			this.user = usersMap.get(userId);

		} catch (PrompterException e) {
			throw Throwables.propagate(e);
		}

	}

	public ResourcePicker(Prompter prompter, String gitlabUrl,
			String gitlabPrivateToken) throws MojoExecutionException {

		this.prompter = prompter;
		this.facade = new GitLabRestFacade(gitlabUrl, gitlabPrivateToken);

	}

	public void askForProject(String connectionUrl, boolean askForTag)
			throws MojoExecutionException {

		try {

			GitLabProject project = null;

			// first, is connectionUrl is provided, try to find the project with
			// it
			if (connectionUrl != null && !connectionUrl.isEmpty()) {

				for (GitLabProject identifiableProject : facade.getProjects()) {
					if (connectionUrl.contains(identifiableProject
							.getPath_with_namespace())) {
						project = identifiableProject;
						break;
					}

				}

				if (project == null) {
					this.prompter.showMessage(L18nHelper
							.t("failed-to-find-project-from-url"));

				}
			}

			if (project == null) {
				// interactive mode, since project hasn't been found

				// get all the projects
				final List<GitLabProject> projects = facade.getProjects();

				// ask which namespace to use
				this.prompter.showMessage(L18nHelper.t("available-namespace"));

				final Set<Namespace> namespaces = new TreeSet<Namespace>(
						new Comparator<Namespace>() {

							@Override
							public int compare(Namespace o1, Namespace o2) {
								return o1.getName().compareTo(o2.getName());
							}
						});

				namespaces.addAll(Collections2.transform(projects,
						new ProjectGetNamespaceFunction()));

				final Map<Integer, Namespace> namespaceMap = Maps.uniqueIndex(
						namespaces.iterator(), new AssetAsIntegerFunction());

				for (Namespace namespace : namespaceMap.values())
					this.prompter.showMessage(ANSI_COLOR.RED.getColorCode()
							+ namespace.getDisplayId()
							+ ANSI_COLOR.RESET.getColorCode() + "\t("
							+ namespace.getName() + ")\n");

				String namespaceId = this.prompter.prompt(
						L18nHelper.t("pick-a-namespace"),
						Arrays.asList(Collections2.transform(namespaces,
								new AssetAsStringFunction()).toArray()));

				final Namespace namespace = namespaceMap.get(new Integer(
						namespaceId));

				// filter projects according to namespace
				final List<GitLabProject> filteredProjects = new ArrayList<GitLabProject>(
						Collections2.filter(projects,
								new Predicate<GitLabProject>() {

									@Override
									public boolean apply(
											@Nullable GitLabProject arg0) {
										return arg0.getNamespace().compareTo(
												namespace) == 0;
									}
								}));

				Collections.sort(filteredProjects,
						new Comparator<GitLabProject>() {

							@Override
							public int compare(GitLabProject o1,
									GitLabProject o2) {
								return o1.getName().compareTo(o2.getName());
							}
						});

				final Map<Integer, GitLabProject> filteredProjectsMap = Maps
						.uniqueIndex(filteredProjects.iterator(),
								new AssetAsIntegerFunction());

				for (IdentifiableGitlabAsset identifiableProject : filteredProjectsMap
						.values())
					this.prompter.showMessage(ANSI_COLOR.RED.getColorCode()
							+ identifiableProject.getDisplayId()
							+ ANSI_COLOR.RESET.getColorCode() + "\t("
							+ identifiableProject.getName() + ")\n");

				// ask use which project to checkout
				String projectId = this.prompter.prompt(
						L18nHelper.t("pick-project-to-branch"),
						Arrays.asList(Collections2.transform(
								filteredProjectsMap.values(),
								new AssetAsStringFunction()).toArray()));

				project = filteredProjectsMap.get(new Integer(projectId));

				// do we need to know something about tags ?
				if (askForTag) {
					List<GitLabTag> tags = facade.getTagsForProject(project
							.getId());
					Collections.sort(tags, new Comparator<GitLabTag>() {

						@Override
						public int compare(GitLabTag o1, GitLabTag o2) {
							return -(o1.getCommit().getCommitted_date()
									.compareTo(o2.getCommit()
											.getCommitted_date()));

						}
					});

					final Map<String, GitLabTag> tagMap = Maps.uniqueIndex(
							tags.iterator(), new TagAsStringFunction());

					for (Entry<String, GitLabTag> entry : tagMap.entrySet()) {

						GitLabTag tag = entry.getValue();
						String index = entry.getKey();
						this.prompter.showMessage(ANSI_COLOR.RED.getColorCode()
								+ index + ANSI_COLOR.RESET.getColorCode()
								+ ": " + tag.getName() + "\t("
								+ ANSI_COLOR.BLUE.getColorCode()
								+ tag.getCommit().getCommitted_date()
								+ ANSI_COLOR.RESET + "\t" + ANSI_COLOR.CYAN
								+ tag.getCommit().getMessage()
								+ ANSI_COLOR.RESET + ")\n");
					}

					List<String> tagList = new ArrayList<>();
					tagList.addAll(tagMap.keySet());
					tagList.add("");
					String promotedTagName = this.prompter.prompt(
							L18nHelper.t("pick-tag-to-promote-default-X", "0"),
							tagList);

					if (promotedTagName.isEmpty()) {
						this.tag = tagMap.get(0);
					} else
						this.tag = (tagMap.get(promotedTagName));

				}

			}

			this.gitRepoUrl = facade.getGitRepoUrl(project);
			this.project = project;
		} catch (PrompterException e) {
			throw new MojoExecutionException(e.getMessage());
		}

	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// do nothing, as we are not a mojo TODO: FIXIT

	}

}
