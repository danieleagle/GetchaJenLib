#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.agents.SonarQubeAgent

/**
 * Waits for the SonarQube scan results by polling the SonarQube server for a success or failure.
 * @param currentJobInstWorkspacePath The workspace path where the current job instance data is stored.
 * @param sonarQubeRootUrl The root URL of SonarQube (e.g. https://sonarqube.internal.example.com).
 * @param sonarQubeAccessTokenId The ID of the access token used by SonarQube to authenticate.
 * @param queryIntervalSecs The seconds between queries.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void waitForResults(final String currentJobInstWorkspacePath, final String sonarQubeRootUrl, final String sonarQubeAccessTokenId,
                    final int queryIntervalSecs) throws IllegalArgumentException {
  if (currentJobInstWorkspacePath && sonarQubeRootUrl && sonarQubeAccessTokenId && queryIntervalSecs) {
    new SonarQubeAgent(this).waitForResults(currentJobInstWorkspacePath, sonarQubeRootUrl, sonarQubeAccessTokenId,
      queryIntervalSecs)
  } else {
    throw new IllegalArgumentException("The argument passed to the sonarQubeScanner.waitForResults step is " +
      "invalid. It could be empty or null.") as Throwable
  }
}
