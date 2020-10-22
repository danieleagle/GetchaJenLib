#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.GlobalsManager
import com.danieleagle.GetchaJenLib.components.GitManager
import com.danieleagle.GetchaJenLib.components.FileSystemHandler
import com.danieleagle.GetchaJenLib.exceptions.JobDataException

/**
* Validates that the required artifacts exist and contain the necessary contents based upon the specified global
* variables. Requires the following globals: JENKINS_CHILD_WORKSPACE_CONTAINER_PATH, GITLAB_INTERNAL_ROOT_URL,
* GITLAB_USER_CRED_ID, GITLAB_API_CURL_CRED_ID, CICD_ADMIN_NAME, CICD_ADMIN_EMAIL, GITLAB_CONFIG_FILES_INTERNAL_PATH_URL,
* GITLAB_GIT_CONFIG_FILES_SUB_PATH, and GIT_FILE_DOWNLOAD_REF.
* @throws JobDataException when missing the required global variables.
* @throws JobDataException when the required .editorconfig file is either missing or empty.
* @throws JobDataException when the required .gitignore file is either missing or empty.
* @throws JobDataException when the Dockerfile is empty or doesn't contain the required Trivy image scanning RUN command
*                          as the last RUN command in the file.
* @throws JobDataException when the Dockerfile doesn't contain any RUN commands.
* @throws JobDataException when the global variable TRIVY_DOCKER_IMAGE_RUN_COMMAND hasn't been set.
* @throws JobDataException when the global variable JENKINS_CHILD_WORKSPACE_CONTAINER_PATH hasn't been set.
*/
void checkArtifacts() throws JobDataException {
  FileSystemHandler fileSystemHandler = new FileSystemHandler(this)

  if (GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH")
      && GlobalsManager.instance.get("GITLAB_INTERNAL_ROOT_URL") && GlobalsManager.instance.get("GITLAB_USER_CRED_ID")
      && GlobalsManager.instance.get("GITLAB_API_CURL_CRED_ID") && GlobalsManager.instance.get("CICD_ADMIN_NAME")
      && GlobalsManager.instance.get("CICD_ADMIN_EMAIL") && GlobalsManager.instance.get("GITLAB_CONFIG_FILES_INTERNAL_PATH_URL")
      && GlobalsManager.instance.get("GITLAB_GIT_CONFIG_FILES_SUB_PATH") && GlobalsManager.instance.get("GIT_FILE_DOWNLOAD_REF")) {
    Map gitServerCredentials = [
      "gitServerCredentialsId" : GlobalsManager.instance.get("GITLAB_USER_CRED_ID"),
      "gitServerApiTokenCredId": GlobalsManager.instance.get("GITLAB_API_CURL_CRED_ID")
    ]

    Map userInfo = [
      "userFullName"    : GlobalsManager.instance.get("CICD_ADMIN_NAME"),
      "userEmailAddress": GlobalsManager.instance.get("CICD_ADMIN_EMAIL")
    ]

    GitManager gitManager =
      new GitManager(this, GlobalsManager.instance.get("GITLAB_INTERNAL_ROOT_URL"), gitServerCredentials, userInfo)

    // ensure the .gitignore file exists with the required contents
    if (fileSystemHandler.doesFileExist(GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH"), ".gitignore")) {
      echo "Found the required .gitignore file in the local repository."

      String gitIgnore = fileSystemHandler.readFileAsString(GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH"),
        ".gitignore")

      if (gitIgnore) {
        echo "The .gitignore file isn't empty."
      } else {
        throw new JobDataException("The .gitignore file is empty.") as Throwable
      }
    } else {
      throw new JobDataException("Couldn't find the required .gitignore file in the local repository.") as Throwable
    }
  } else {
    throw new JobDataException("Problem found in the validator.checkArtifacts step. One or more of the following " +
      "globals weren't defined: JENKINS_CHILD_WORKSPACE_CONTAINER_PATH, GITLAB_INTERNAL_ROOT_URL, GITLAB_USER_CRED_ID, " +
      "GITLAB_API_CURL_CRED_ID, CICD_ADMIN_NAME, CICD_ADMIN_EMAIL, GITLAB_CONFIG_FILES_INTERNAL_PATH_URL, " +
      "GITLAB_GIT_CONFIG_FILES_SUB_PATH, and GIT_FILE_DOWNLOAD_REF. Be sure these are all set using the " +
      "globals.set step.") as Throwable
  }

  if (GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH")) {
    // regardless of SCM type, ensure .editorconfig file exists
    if (fileSystemHandler.doesFileExist(GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH"),
        ".editorconfig")) {
      echo "Found the required .editorconfig file in the local repository."

      String editorConfig =
        fileSystemHandler.readFileAsString(GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH"),
          ".editorconfig")

      if (editorConfig) {
        echo "The .editorconfig file isn't empty."
      } else {
        throw new JobDataException("The .editorconfig file is empty.") as Throwable
      }
    } else {
      throw new JobDataException("Couldn't find the required .editorconfig file in the local repository.") as Throwable
    }

    if (GlobalsManager.instance.get("TRIVY_DOCKER_IMAGE_RUN_COMMAND")) {
      // if Dockerfile exists, ensure it's not empty and that it contains the Trivy scanning command in the appropriate
      // location (i.e. the last RUN command in the file)
      if (fileSystemHandler.doesFileExist(GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH"),
          "Dockerfile")) {
        String dockerfile =
          fileSystemHandler.readFileAsString(GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH"),
            "Dockerfile")

        List runCommands = dockerfile.split("RUN")

        if (runCommands) {
          String lastRunCommand = runCommands.get(runCommands.size() - 1)

          if (lastRunCommand.contains(GlobalsManager.instance.get("TRIVY_DOCKER_IMAGE_RUN_COMMAND").substring(3))) {
            echo "The Dockerfile contains the required Trivy image scanning RUN command and is the last RUN command in the " +
              "file which is necessary for proper scanning. For more information on Trivy, " +
              "see https://github.com/aquasecurity/trivy."
          } else {
            throw new JobDataException("The Dockerfile is empty or doesn't contain the required Trivy image scanning " +
              "RUN command as the last RUN command in the file. In order to obtain proper scanning results, it must be " +
              "the last RUN command. Please see https://github.com/aquasecurity/trivy for more details.") as Throwable
          }
        } else {
          throw new JobDataException("The Dockerfile doesn't contain any RUN commands. At least one RUN command with " +
            "the Trivy image scanning command is required. If multiple RUN commands exist, the Trivy image scanning " +
            "run command must be last which is necessary for proper scanning. Please see " +
            "https://github.com/aquasecurity/trivy for more details.") as Throwable
        }
      } else {
        echo "The Dockerfile doesn't exist. If any stages require it to function, they will fail."
      }
    } else {
      throw new JobDataException("Problem found in the validator.checkArtifacts step. The global " +
        "TRIVY_DOCKER_IMAGE_RUN_COMMAND hasn't been set and is needed if a Dockerfile is detected in the repository. " +
        "Please ensure it is set using the globals.set step.") as Throwable
    }
  } else {
    throw new JobDataException("Problem found in the validator.checkArtifacts step. The global " +
      "JENKINS_CHILD_WORKSPACE_CONTAINER_PATH hasn't been set. Please ensure it is set using the globals.set " +
      "step.") as Throwable
  }
}
