package net.erel.maven.plugins.utils;

import java.io.IOException;
import java.nio.file.Paths;

import net.erel.maven.plugins.domain.maven.PGXVersion;
import net.erel.maven.plugins.service.git.PGXBranches;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepository;

/**
 * a class with a collection of static method to be called at the begining of
 * every mojo to make sure that thay can be called
 * 
 * @author nherbaut
 * 
 */
public class PreconditionChecker {

	/**
	 * Check that no file is marked with the modidified state on this repo
	 * 
	 * @param project
	 *            the maven project as injected by plexus
	 * @throws MojoFailureException
	 *             if the precondition is not met
	 * @throws MojoExecutionException
	 *             if an internal error occurs
	 */
	public static void checkNoModifiedFile(MavenProject project) throws MojoFailureException, MojoExecutionException {

		FileRepository repository;
		try {
			repository = new FileRepository(Paths.get(project.getBasedir().toString(), ".git").toFile());

			Git git = new Git(repository);

			if (!git.status().call().getModified().isEmpty()) {

				throw new MojoFailureException("please-commit-your-local-changes-before-branching");
			}
		} catch (IOException | NoWorkTreeException | GitAPIException e) {
			e.printStackTrace();
			throw new MojoExecutionException("unexpected error " + e.getMessage());
		}

	}

	/**
	 * Check if the project snapshot status is in accordance with what we want
	 * 
	 * @param project
	 *            the maven project as injected by plexus
	 * @param isSnapshot
	 *            if true, the precondition will throw if the project is not a
	 *            snapshot. If false, the precondition will throw if the project
	 *            is a snapshot
	 * @throws MojoFailureException
	 */
	public static void checkSnapshotStatus(MavenProject project, boolean isSnapshot) throws MojoFailureException {

		PGXVersion version = new PGXVersion(project);

		if (version.getSnapshot()!=isSnapshot)
			throw new MojoFailureException("we-should" + (isSnapshot ? "" : "-not-") + "have-a-snapshot-at-this-step-check-your-project-version");

	}

	/**
	 * Check if the project snapshot status is in accordance with what we want
	 * 
	 * @param project
	 *            the maven project as injected by plexus
	 * @param isRC
	 *            if true, the precondition will throw if the project is not a
	 *            RC. If false, the precondition will throw if the project is a
	 *            RC
	 * @throws MojoFailureException
	 */
	public static void checkRCStatus(MavenProject project, boolean isRC) throws MojoFailureException {

		PGXVersion version = new PGXVersion(project);

		if (version.isRc() != isRC)
			throw new MojoFailureException("we-should" + (isRC ? "" : "-not-") + "have-a-RC-at-this-step-check-your-project-version");

	}

	/**
	 * Check if you're on the right branch
	 * 
	 * @param project
	 *            the maven project as injected by plexus
	 * @param branch
	 *            the branch you should be on
	 * @throws MojoFailureException
	 *             if the branch doesn't match
	 * @throws MojoExecutionException
	 *             in case of internal error
	 */
	public static void checkBranch(MavenProject project, PGXBranches branch) throws MojoFailureException, MojoExecutionException {
		FileRepository repository;
		try {
			repository = new FileRepository(Paths.get(project.getBasedir().toString(), ".git").toFile());
			if (!repository.getBranch().equals(branch.getBranchName())) {
				throw new MojoFailureException("bad branch " + repository.getBranch() + " you should be on " + branch.getBranchName()
						+ ". You should issue a git stash ; git checkout " + branch.getBranchName());
			}

		} catch (IOException | NoWorkTreeException e) {
			e.printStackTrace();
			throw new MojoExecutionException("unexpected error " + e.getMessage());
		}

	}

	/**
	 * make sure that the prefix of the branch is ok for the operation you want
	 * to do
	 * 
	 * @param project
	 *            the maven project as injected by plexus
	 * @param prefix
	 *            a string like feature or hotfix
	 * @throws MojoFailureException
	 *             if the prefix don't match the required one
	 * @throws MojoExecutionException
	 *             in case of internal error
	 */
	public static void checkBranchPrefix(MavenProject project, String... prefixes) throws MojoFailureException, MojoExecutionException {
		FileRepository repository;
		StringBuilder builder = new StringBuilder();
		try {
			repository = new FileRepository(Paths.get(project.getBasedir().toString(), ".git").toFile());
			boolean found = false;
			for (String prefix : prefixes) {
				builder.append(prefix).append(" ");
				if (repository.getBranch().startsWith(prefix)) {
					found = true;
				}
			}

			if (!found)
				throw new MojoFailureException("bad branch " + repository.getBranch() + " you should be on a branch starting with a valid prefix like " + builder.toString());

		} catch (IOException | NoWorkTreeException e) {
			e.printStackTrace();
			throw new MojoExecutionException("unexpected error " + e.getMessage());
		}

	}

	/**
	 * check that the tag doesn't exit
	 * 
	 * @param project
	 *            the project
	 * @param tagName
	 *            the tag that should not be there
	 * @throws MojoFailureException
	 *             if the tag already exist
	 * @throws MojoExecutionException
	 */
	public static void checkNoSuchTag(MavenProject project, String tagName) throws MojoFailureException, MojoExecutionException {
		try {
			FileRepository repository = new FileRepository(Paths.get(project.getBasedir().toString(), ".git").toFile());

			Git git = new Git(repository);

			for (Ref ref : git.tagList().call()) {
				if (ref.getName().endsWith(tagName)) {
					throw new MojoExecutionException("tag " + tagName + " already exist");
				}
			}

		} catch (IOException | GitAPIException e) {
			throw new MojoExecutionException("unexpected  error " + e.getMessage());
		}

	}

}
