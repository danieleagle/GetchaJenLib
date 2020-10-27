#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.GlobalsManager
import com.danieleagle.GetchaJenLib.components.GitManager
import com.danieleagle.GetchaJenLib.components.Housekeeper
import com.danieleagle.GetchaJenLib.components.NotificationSystem
import com.danieleagle.GetchaJenLib.exceptions.JobDataException

/**
 * Finalizes the job instance based upon the defined globals. Requires the following globals:
 * EMAIL_NOTIFICATION_RECIPIENTS, EMAIL_NOTIFICATION_REPLY_TO_ADDRESSES, SONARQUBE_EXTERNAL_PROJECT_URL,
 * JENKINS_PARENT_WORKSPACE_CONTAINER_PATH, JENKINS_CHILD_WORKSPACE_CONTAINER_PATH, and
 * JENKINS_JOB_INSTANCE_COLLECTION_FOLDER. The following variable is optional: EMAIL_FILE_ATTACHMENT_PATTERN.
 * @throws JobDataException when missing the required globals.
 */
void invoke() throws JobDataException {
  try {
    if (GlobalsManager.instance.get("EMAIL_NOTIFICATION_RECIPIENTS")
        && GlobalsManager.instance.get("EMAIL_NOTIFICATION_REPLY_TO_ADDRESSES")
        && GlobalsManager.instance.get("SONARQUBE_EXTERNAL_PROJECT_URL")
        && GlobalsManager.instance.get("JENKINS_PARENT_WORKSPACE_CONTAINER_PATH")
        && GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH")
        && GlobalsManager.instance.get("JENKINS_JOB_INSTANCE_COLLECTION_FOLDER")
        && GlobalsManager.instance.get("PRODUCTION_BRANCH_REGEX")
        && GlobalsManager.instance.get("DEVELOPMENT_BRANCH_REGEX")
        && GlobalsManager.instance.get("MR_LOCKED_BRANCHES_JENKINS_CONTAINER_PATH")
        && GlobalsManager.instance.get("MR_LOCKED_BRANCHES_FILE_NAME")
        && GlobalsManager.instance.get("VALID_BRANCHES")) {
      String emailFileAttachmentPattern = (GlobalsManager.instance.get("EMAIL_FILE_ATTACHMENT_PATTERN"))
        ? GlobalsManager.instance.get("EMAIL_FILE_ATTACHMENT_PATTERN") : ""

      new Housekeeper(this).cleanup(
        GlobalsManager.instance.get("JENKINS_PARENT_WORKSPACE_CONTAINER_PATH"),
        GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH"),
        GlobalsManager.instance.get("JENKINS_JOB_INSTANCE_COLLECTION_FOLDER")
      )

      if (GlobalsManager.instance.get("BUILD_RESULT") != "FAILURE") {
        updateGitlabCommitStatus(name: "build", state: "success")

        if (env.gitlabActionType && (env.gitlabActionType.toString() == "MERGE" || env.gitlabActionType.toString() == "NOTE")) {
          addGitLabMRComment(comment: "The Jenkins job ${JOB_NAME} with build ${BUILD_NUMBER} was successful. Please visit " +
            "${BUILD_URL} for more details.")

          if (env.gitlabSourceBranch && env.gitlabTargetBranch
              && GitManager.isValidRef(env.gitlabSourceBranch.toString(), GlobalsManager.instance.get("PRODUCTION_BRANCH_REGEX"))
              && GitManager.isValidRef(env.gitlabTargetBranch.toString(), GlobalsManager.instance.get("DEVELOPMENT_BRANCH_REGEX"))) {
            acceptGitLabMR(useMRDescription: true, removeSourceBranch: true) // accept MR after sending success to GitLab
                                                                             // or 405 error will occur
            echo "Unlocking merge request target branches since the job was successful and the source branch is production and " +
              "the target branch is development..."
            merReqAuthorizer.unlockTargetBranches(GlobalsManager.instance.get("MR_LOCKED_BRANCHES_JENKINS_CONTAINER_PATH"),
              GlobalsManager.instance.get("MR_LOCKED_BRANCHES_FILE_NAME"), GlobalsManager.instance.get("VALID_BRANCHES"))
          } else {
            echo "Skipped unlocking merge request target branches since the source branch isn't production and " +
              "the target branch isn't development."
          }
        }

        new NotificationSystem(this).send(
          NotificationSystem.NotificationType.SUCCESS,
          GlobalsManager.instance.get("EMAIL_NOTIFICATION_RECIPIENTS"),
          GlobalsManager.instance.get("EMAIL_NOTIFICATION_REPLY_TO_ADDRESSES"),
          emailFileAttachmentPattern,
          GlobalsManager.instance.get("SONARQUBE_EXTERNAL_PROJECT_URL")
        )
      } else {
        updateGitlabCommitStatus(name: "build", state: "failed")

        if (env.gitlabActionType && (env.gitlabActionType.toString() == "MERGE" || env.gitlabActionType.toString() == "NOTE")) {
          addGitLabMRComment(comment: "The Jenkins job ${JOB_NAME} with build ${BUILD_NUMBER} failed. Please visit " +
            "${BUILD_URL} for more details.")

          if (env.gitlabSourceBranch && env.gitlabTargetBranch
              && GitManager.isValidRef(env.gitlabSourceBranch.toString(), GlobalsManager.instance.get("DEVELOPMENT_BRANCH_REGEX"))
              && GitManager.isValidRef(env.gitlabTargetBranch.toString(), GlobalsManager.instance.get("PRODUCTION_BRANCH_REGEX"))) {
            echo "Unlocking merge request target branch for production since the job wasn't successful and the source branch is " +
              "production and the target branch is development..."
            merReqAuthorizer.unlockTargetBranches(GlobalsManager.instance.get("MR_LOCKED_BRANCHES_JENKINS_CONTAINER_PATH"),
              GlobalsManager.instance.get("MR_LOCKED_BRANCHES_FILE_NAME"), [ env.gitlabTargetBranch.toString() ])
          } else {
            echo "Skipped unlocking merge request target branch for production since the source branch isn't development and " +
              "the target branch isn't production."
          }
        }

        new NotificationSystem(this).send(
          NotificationSystem.NotificationType.FAILURE,
          GlobalsManager.instance.get("EMAIL_NOTIFICATION_RECIPIENTS"),
          GlobalsManager.instance.get("EMAIL_NOTIFICATION_REPLY_TO_ADDRESSES"),
          emailFileAttachmentPattern,
          GlobalsManager.instance.get("SONARQUBE_EXTERNAL_PROJECT_URL")
        )

        throw new Exception("Jenkins job failure has been detected.") as Throwable
      }
    } else {
      throw new JobDataException("Unable to execute the finalizer.invoke step due to missing globals. This step requires " +
        "the following globals: EMAIL_NOTIFICATION_RECIPIENTS, EMAIL_NOTIFICATION_REPLY_TO_ADDRESSES, " +
        "SONARQUBE_EXTERNAL_PROJECT_URL, JENKINS_PARENT_WORKSPACE_CONTAINER_PATH, " +
        "JENKINS_CHILD_WORKSPACE_CONTAINER_PATH, and JENKINS_JOB_INSTANCE_COLLECTION_FOLDER.") as Throwable
    }
  } catch (Exception exception) {
    updateGitlabCommitStatus(name: "build", state: "failed")
    error("An exception was thrown inside the finalizer.invoke step which has caused this job instance to fail.")
  }
}
