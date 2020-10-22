#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.EnvironmentPeeker

/**
 * Prints important GitLab environment information depending upon the context of the event (e.g. push, merge, forced
 * build, etc.).
 */
void printGitlabEnvDetails() {
  new EnvironmentPeeker(this).printGitLabEnvDetails()
}
