package net.erel.maven.plugins.service.git;

/**
 * enum and properties for default erel branches
 * 
 * @author nherbaut
 */
public enum PGXBranches {

  DEVELOP("feature"),
  MASTER("hotfix");

  private String topicBranchPrefix;

  private PGXBranches(String prefixForTopicBranch) {
    this.topicBranchPrefix = prefixForTopicBranch;

  }

  public String getBranchName() {
    return super.name().toLowerCase();
  }

  public String getFeatureBranchPrefix() {
    return this.topicBranchPrefix;
  }

  public static PGXBranches getTargetBranch(String featureBranchName) {
    for (PGXBranches branch : PGXBranches.values()) {
      if (featureBranchName.startsWith(branch.getFeatureBranchPrefix())) {
        return branch;
      }
    }

    throw new IllegalArgumentException("No Such PGXBranch prefix");

  }

 

}
