package net.erel.maven.plugins.service.bugtracking;

import org.apache.maven.plugin.MojoExecutionException;

public interface BugTrackingServiceFactory {

	
	public abstract BugTrackerService getBugTrackerService() throws MojoExecutionException;
		
}