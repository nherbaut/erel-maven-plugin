package net.erel.maven.plugins.service.bugtracking;

import org.junit.Test;

import com.taskadapter.redmineapi.RedmineException;

public class RedminServiceTest {


	@Test
  public void testRedmin() throws RedmineException {

    BugTrackerService service = new RedMineService("faf7d2718204a68622dbb829e1f85103fa68138f", "http://redmine.erel.net");

    for (String issueId : service.getAssignedTicketsForCurrentUser()) {
      System.out.println(issueId);
    }

  }

}
