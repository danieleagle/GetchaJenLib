#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.agents

import com.danieleagle.GetchaJenLib.components.FileSystemHandler
import com.danieleagle.GetchaJenLib.exceptions.InvalidDirOrFileNameException
import com.danieleagle.GetchaJenLib.exceptions.GenericToolsException
import groovy.json.JsonSlurperClassic

/**
 * Provides functionality to coordinate with a running SonarQube server. Requires the SonarQube Plugin.
 */
class SonarQubeAgent implements Serializable {
  /**
   * Steps object for using steps such as echo, sh, etc. and accessing environment variables.
   */
  private final def steps

  /**
   * Sets the steps object to allow for accessing steps such as echo, sh, etc. and accessing environment variables.
   * @param steps The object accessible in the context of the running job instance allowing access to all available steps,
   *              environment variables, etc.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  SonarQubeAgent(final def steps) throws IllegalArgumentException {
    if (steps) {
      this.steps = steps
    } else {
      throw new IllegalArgumentException("The argument passed to the SonarQubeAgent constructor is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Retrieves the SonarQube task ID used to get the quality gate status.
   * @param currentJobInstWorkspacePath The workspace path where the current job instance data is stored.
   * @return The SonarQube task URL.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the specified directory contains invalid characters.
   */
  String getTaskId(final String currentJobInstWorkspacePath) throws IllegalArgumentException, InvalidDirOrFileNameException {
    String sonarQubeTaskId = ""

    if (currentJobInstWorkspacePath) {
      FileSystemHandler fileSystemHandler = new FileSystemHandler(steps)

      if (new FileSystemHandler(steps).isValidDirectoryName(currentJobInstWorkspacePath)) {
        sonarQubeTaskId = fileSystemHandler.readFileAsString("${currentJobInstWorkspacePath}/target/sonar",
          "report-task.txt", "ceTaskId").substring(9) // use substring() method to remove ceTaskId= from string
      } else {
        throw new InvalidDirOrFileNameException("The current job instance workspace path contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the SonarQubeAgent getTaskId method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return sonarQubeTaskId
  }

  /**
   * Gets the SonarQube task data by contacting the SonarQube task URL and parsing the returned JSON.
   * @param sonarQubeRootUrl The root URL of SonarQube (e.g. https://sonarqube.internal.example.com).
   * @param taskId The SonarQube task ID.
   * @param accessTokenId The ID of the access token used by SonarQube to authenticate.
   * @return The task data map.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  Map getTaskData(final String sonarQubeRootUrl, final String taskId, final String accessTokenId)
      throws IllegalArgumentException {
    Map taskData = [:]

    if (sonarQubeRootUrl && taskId && accessTokenId) {
      steps.withCredentials([steps.string(credentialsId: accessTokenId, variable: "accessToken")]) {
        String jsonResult = steps.sh(script: "curl -u ${steps.accessToken}: --connect-timeout 5 " +
          "${sonarQubeRootUrl}/api/ce/task?id=${taskId} || true", returnStdout: true).trim()
        def jsonObject = new JsonSlurperClassic().parseText(jsonResult)
        taskData = (jsonObject.get("task")) ? jsonObject.get("task") : [:]
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the SonarQubeAgent getTaskData method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return taskData
  }

  /**
   * Gets the SonarQube quality gate status by contacting the SonarQube quality gate status URL and parsing the returned
   * JSON.
   * @param sonarQubeRootUrl The root URL of SonarQube (e.g. https://sonarqube.internal.example.com).
   * @param accessTokenId The ID of the access token used by SonarQube to authenticate.
   * @param analysisId The SonarQube analysis ID.
   * @return The quality gate status.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  String getQualityGateStatus(final String sonarQubeRootUrl, final String accessTokenId, final String analysisId)
      throws IllegalArgumentException {
    String qualityGateStatus = ""

    if (sonarQubeRootUrl && accessTokenId && analysisId) {
      steps.withCredentials([steps.string(credentialsId: accessTokenId, variable: "apiToken")]) {
        String jsonResult =
          steps.sh(script: "curl -u ${steps.apiToken}: --connect-timeout 5 ${sonarQubeRootUrl}/api/qualitygates" +
            "/project_status?analysisId=${analysisId} || true", returnStdout: true).trim()
        Map projectStatusInfo = new JsonSlurperClassic().parseText(jsonResult)
        qualityGateStatus = (projectStatusInfo.get("projectStatus") && projectStatusInfo.get("projectStatus").get("status"))
          ? projectStatusInfo.get("projectStatus").get("status") : ""
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the SonarQubeAgent getQualityGateStatus method " +
        "is invalid. It could be empty or null.") as Throwable
    }

    return qualityGateStatus
  }

  /**
   * Waits for the SonarQube scan results by polling the SonarQube server for a success or failure.
   * @param currentJobInstWorkspacePath The workspace path where the current job instance data is stored.
   * @param sonarQubeRootUrl The root URL of SonarQube (e.g. https://sonarqube.internal.example.com).
   * @param accessTokenId The ID of the access token used by SonarQube to authenticate.
   * @param queryIntervalSecs The seconds between queries.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws GenericToolsException when unable to retrieve the SonarQube task analysis ID from the server.
   * @throws GenericToolsException when the SonarQube task URL and task data retrieved from the server are invalid.
   * @throws GenericToolsException when the SonarQube quality gate didn't pass.
   * @throws InvalidDirOrFileNameException when the specified directory contains invalid characters.
   */
  void waitForResults(final String currentJobInstWorkspacePath, final String sonarQubeRootUrl, final String accessTokenId,
                      final int queryIntervalSecs)
                      throws IllegalArgumentException, InvalidDirOrFileNameException, GenericToolsException {
    if (currentJobInstWorkspacePath && sonarQubeRootUrl && accessTokenId && queryIntervalSecs) {
      if (new FileSystemHandler(steps).isValidDirectoryName(currentJobInstWorkspacePath)) {
        String sonarTaskId = getTaskId(currentJobInstWorkspacePath)
        Map sonarQubeTask = getTaskData(sonarQubeRootUrl, sonarTaskId, accessTokenId)

        // if data is valid, continue
        if (sonarTaskId && sonarQubeTask && sonarQubeTask.get("status")) {
          while (sonarQubeTask.get("status") == "PENDING" || sonarQubeTask.get("status") == "IN_PROGRESS") {
            steps.echo "Waiting for the SonarQube scan results..."

            // wait N seconds (based on referenced environment variable) before polling the server for updated data
            steps.sleep(time: queryIntervalSecs, unit: "SECONDS")

            // get latest data and update status
            steps.echo "Polling the SonarQube server for the latest task data..."
            sonarQubeTask = getTaskData(sonarQubeRootUrl, sonarTaskId, accessTokenId)
          }

          // fail the Jenkins job if the status is CANCELED or FAILED
          if (sonarQubeTask.get("status") == "CANCELED" || sonarQubeTask.get("status") == "FAILED") {
            throw new Exception("The SonarQube scan was either canceled or has failed.") as Throwable
          }
          // otherwise, check the SonarQube quality gate status
          else {
            // if the analysis ID is present, continue
            if (sonarQubeTask.get("analysisId")) {
              steps.echo "Polling the SonarQube server for the updated quality gate status..."
              String sonarQualityGateStatus = getQualityGateStatus(sonarQubeRootUrl, accessTokenId,
                "${sonarQubeTask.get('analysisId')}")

              // if the SonarQube quality gate status is valid, check the status
              if (sonarQualityGateStatus) {
                // if the quality gate status is reported as ERROR (it didn't pass), fail the pipeline
                if (sonarQualityGateStatus == "ERROR") {
                  throw new GenericToolsException("The SonarQube quality gate didn't pass.") as Throwable
                }
                // otherwise, if the quality gate status is reported as WARN, don't fail the pipeline and instead notify
                // the user
                else if (sonarQualityGateStatus == "WARN") {
                  steps.echo "The SonarQube quality gate has warnings that need to be reviewed."
                }
                // otherwise, notify the user that it passed (quality gate status is reported as OK)
                else {
                  steps.echo "The SonarQube quality gate passed."
                }
              }
            } else {
              throw new GenericToolsException("Retrieving the SonarQube task analysis ID from the server " +
                "failed.") as Throwable
            }
          }
        } else {
          throw new GenericToolsException("The SonarQube task URL and task data retrieved from the server are " +
            "invalid.") as Throwable
        }
      } else {
        throw new InvalidDirOrFileNameException("The current job instance workspace path contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the SonarQubeAgent waitForResults method is " +
        "invalid. It could be empty or null.") as Throwable
    }
  }
}
