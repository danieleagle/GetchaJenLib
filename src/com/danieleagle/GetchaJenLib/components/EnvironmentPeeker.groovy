#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components

/**
 * Provides functionality for displaying environment variables exposed to the current Jenkins job instance. Requires the
 * Git and GitLab plugins.
 */
class EnvironmentPeeker implements Serializable {
  /**
   * Steps object for using steps such as echo, sh, etc. and accessing environment variables.
   */
  private final def steps

  /**
   * Sets the steps object to allow for accessing steps such as echo, sh, etc., and accessing environment variables.
   * @param steps The object accessible in the context of the running job instance allowing access to all available steps,
   *              environment variables, etc.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  EnvironmentPeeker(final def steps) throws IllegalArgumentException {
    if (steps) {
      this.steps = steps
    } else {
      throw new IllegalArgumentException("The argument passed to the EnvironmentPeeker constructor is invalid. It " +
        "could be empty or null.") as Throwable
    }
  }

  /**
   * Prints important GitLab environment information depending upon the context of the event (e.g. push, merge, forced
   * build, etc.).
   */
  void printGitLabEnvDetails() {
    // only print environment details if a GitLab action is defined
    if (steps.env.gitlabActionType) {
      steps.echo "The GitLab ${steps.env.gitlabActionType} action has occurred. Please see below for GitLab environment " +
        "information."
      steps.echo "-----------------------------------------------------\n" +
        "GitLab Branch: ${steps.env.gitlabBranch}\n" +
        "GitLab Source Branch: ${steps.env.gitlabSourceBranch}\n" +
        "GitLab Action Type: ${steps.env.gitlabActionType}\n" +
        "GitLab Username: ${steps.env.gitlabUserName}\n" +
        "GitLab User Email: ${steps.env.gitlabUserEmail}\n" +
        "GitLab Source Repo Homepage: ${steps.env.gitlabSourceRepoHomepage}\n" +
        "GitLab Source Repo Name: ${steps.env.gitlabSourceRepoName}\n" +
        "GitLab Source Namespace: ${steps.env.gitlabSourceNamespace}\n" +
        "GitLab Source Repo URL: ${steps.env.gitlabSourceRepoURL}\n" +
        "GitLab Source Repo SSH URL: ${steps.env.gitlabSourceRepoSshUrl}\n" +
        "GitLab Source Repo HTTP URL: ${steps.env.gitlabSourceRepoHttpUrl}\n" +
        "GitLab Merge Request Title: ${steps.env.gitlabMergeRequestTitle}\n" +
        "GitLab Merge Request ID: ${steps.env.gitlabMergeRequestId}\n" +
        "GitLab Merge Request State: ${steps.env.gitlabMergeRequestState}\n" +
        "GitLab Merge Request Last Commit: ${steps.env.gitlabMergeRequestLastCommit}\n" +
        "GitLab Merge Request Target Project ID: ${steps.env.gitlabMergeRequestTargetProjectId}\n" +
        "GitLab Target Branch: ${steps.env.gitlabTargetBranch}\n" +
        "GitLab Target Repo Name: ${steps.env.gitlabTargetRepoName}\n" +
        "GitLab Target Namespace: ${steps.env.gitlabTargetNamespace}\n" +
        "GitLab Target Repo SSH URL: ${steps.env.gitlabTargetRepoSshUrl}\n" +
        "GitLab Target Repo HTTP URL: ${steps.env.gitlabTargetRepoHttpUrl}\n" +
        "GitLab Trigger Phrase: ${steps.env.gitlabTriggerPhrase}\n"
        "-----------------------------------------------------"
    } else {
      steps.echo "No GitLab action has been defined. Skipped printing GitLab details."
    }
  }
}
