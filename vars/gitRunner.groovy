#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.GlobalsManager
import com.danieleagle.GetchaJenLib.components.GitManager
import com.danieleagle.GetchaJenLib.components.FileSystemHandler
import com.danieleagle.GetchaJenLib.exceptions.JobDataException
import com.danieleagle.GetchaJenLib.exceptions.InvalidGitBranchException
import java.util.regex.Pattern

/**
 * Checks if the Git reference is valid based on the specified regex.
 * @param reference The Git reference to validate.
 * @param referenceRegex The Git reference regex.
 * @return True if the reference is valid and false if not.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
Boolean isValidRef(final String reference, final Pattern referenceRegex) throws IllegalArgumentException {
  Boolean isValidReference = false

  if (reference && referenceRegex) {
    isValidReference = GitManager.isValidRef(reference, referenceRegex)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.isValidRef step is invalid. It " +
      "could be empty or null.") as Throwable
  }

  return isValidReference
}

/**
 * Checks out a branch.
 * @param gitServerUrl The URL of the Git server.
 * @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
 *                             gitServerCredentialsId and gitServerApiTokenCredId.
 * @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
 *                 userEmailAddress.
 * @param upstreamRepoUrl The upstream repository URL.
 * @param branchName The name of the Git branch.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void checkoutBranch(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo,
                    final String upstreamRepoUrl, final String branchName) throws IllegalArgumentException {
  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String && upstreamRepoUrl
      && branchName) {
    new GitManager(this, gitServerUrl, gitServerCredentials, userInfo).checkoutBranch(upstreamRepoUrl, branchName)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.checkoutBranch step is invalid. It could be " +
      "empty or null.") as Throwable
  }
}

/**
 * Checks out a tag.
 * @param gitServerUrl The URL of the Git server.
 * @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
 *                             gitServerCredentialsId and gitServerApiTokenCredId.
 * @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
 *                 userEmailAddress.
 * @param upstreamRepoUrl The upstream repository URL.
 * @param tag The Git tag to checkout.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void checkoutTag(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo,
                 final String upstreamRepoUrl, final String tag) throws IllegalArgumentException {
  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String && upstreamRepoUrl
      && tag) {
    new GitManager(this, gitServerUrl, gitServerCredentials, userInfo).checkoutTag(upstreamRepoUrl, tag)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.checkoutTag step is invalid. It could be " +
      "empty or null.") as Throwable
  }
}

/**
 * Checks out the merge request using its ID and merges with its target branch.
 * @param gitServerUrl The URL of the Git server.
 * @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
 *                             gitServerCredentialsId and gitServerApiTokenCredId.
 * @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
 *                 userEmailAddress.
 * @param mergeRequestId The ID of the merge request.
 * @param sourceRepoName The source repository name.
 * @param upstreamRepoUrl The upstream repository URL.
 * @param forkedRepoUrl The forked repository URL.
 * @param targetBranch The target branch to merge into.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void checkoutMergeRequest(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo,
                          final int mergeRequestId, final String sourceRepoName, final String upstreamRepoUrl,
                          final String forkedRepoUrl, final String targetBranch) throws IllegalArgumentException {
  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String
      && mergeRequestId instanceof Integer && sourceRepoName && upstreamRepoUrl && forkedRepoUrl && targetBranch) {
    new GitManager(this, gitServerUrl, gitServerCredentials, userInfo)
      .checkoutMergeRequest(mergeRequestId, sourceRepoName, upstreamRepoUrl, forkedRepoUrl, targetBranch)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.checkoutMergeRequest step is invalid. It could " +
      "be empty or null.") as Throwable
  }
}

/**
 * Pushes commits to the upstream repository.
 * @param gitServerUrl The URL of the Git server.
 * @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
 *                             gitServerCredentialsId and gitServerApiTokenCredId.
 * @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
 *                 userEmailAddress.
 * @param upstreamRepoUrl The upstream repository URL.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void pushCommits(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo, final String upstreamRepoUrl)
    throws IllegalArgumentException {
  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String && upstreamRepoUrl) {
    new GitManager(this, gitServerUrl, gitServerCredentials, userInfo).pushCommits(upstreamRepoUrl)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.pushCommits step is invalid. It could be empty or " +
      "null.") as Throwable
  }
}

/**
 * Pushes tag to the upstream repository.
 * @param gitServerUrl The URL of the Git server.
 * @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
 *                             gitServerCredentialsId and gitServerApiTokenCredId.
 * @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
 *                 userEmailAddress.
 * @param upstreamRepoUrl The upstream repository URL.
 * @param tag The Git tag to push.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void pushTag(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo,
             final String upstreamRepoUrl, final String tag) throws IllegalArgumentException {
  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String && upstreamRepoUrl
      && tag) {
    new GitManager(this, gitServerUrl, gitServerCredentials, userInfo).pushTag(upstreamRepoUrl, tag)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.pushTag step is invalid. It " +
      "could be empty or null.") as Throwable
  }
}

/**
 * Gets the contents of a file using the GitLab REST API.
 * @param gitServerUrl The URL of the Git server.
 * @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
 *                             gitServerCredentialsId and gitServerApiTokenCredId.
 * @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
 *                 userEmailAddress.
 * @param gitRootApiUrl The root API URL specific to the current repository
 *                      (e.g. https://gitlab-server.example.com/api/v4/projects/45/repository/files). In this example,
 *                      45 is the repo ID.
 * @param subPathToFile The sub path to the file on the repository (e.g. /config/hello-world/Docker). If no sub path
 *                      exists, use empty string.
 * @param fileName The name of the file to get the contents for.
 * @param reference The Git reference used to retrieve the appropriate file version (e.g. branch name).
 * @return The contents of the file as a string.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
String getFileContents(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo,
                       final String gitRootApiUrl, final String subPathToFile, final String fileName,
                       final String reference) throws IllegalArgumentException {
  String fileContents = ""

  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String
      && gitRootApiUrl && subPathToFile instanceof String && fileName && reference) {
    fileContents = new GitManager(this, gitServerUrl, gitServerCredentials, userInfo)
      .getFileContents(gitRootApiUrl, subPathToFile, fileName, reference)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.getFileContents step is invalid. It could be " +
      "empty or null.") as Throwable
  }

  return fileContents
}

/**
 * Gets the commit messages belonging to the merge request using the GitLab REST API.
 * @param gitServerUrl The URL of the Git server.
 * @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
 *                             gitServerCredentialsId and gitServerApiTokenCredId.
 * @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
 *                 userEmailAddress.
 * @param gitMergeReqApiUrl The merge request API URL specific to the current repository
 *                          (e.g. https://gitlab-server.example.com/api/v4/projects/45/merge_requests). In this example,
 *                          45 is the repo ID.
 * @param mergeReqId The merge request ID.
 * @return The merge request commit messages as a list.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
String getMergeReqCommitMsgs(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo,
                             final String gitMergeReqApiUrl, final String mergeReqId) throws IllegalArgumentException {
  List mergeReqCommitMsgs = []

  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String
      && gitMergeReqApiUrl && mergeReqId && mergeReqId.isNumber()) {
    mergeReqCommitMsgs = new GitManager(this, gitServerUrl, gitServerCredentials, userInfo)
      .getMergeReqCommitMsgs(gitMergeReqApiUrl, mergeReqId)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.getMergeReqCommitMsgs step is invalid. It " +
      "could be empty or null.") as Throwable
  }

  return mergeReqCommitMsgs
}

/**
* Gets the commit message of the specified reference.
* @param gitServerUrl The URL of the Git server.
* @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
*                             gitServerCredentialsId and gitServerApiTokenCredId.
* @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
*                 userEmailAddress.
* @param reference The Git reference (branch, SHA, etc.).
* @return The commit message.
* @throws IllegalArgumentException when passing an empty or null argument.
* @throws JobDataException when missing the required Git server environment variables.
*/
String getCommitMsg(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo, final String reference)
    throws IllegalArgumentException {
  String commitMsg = ""

  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String && reference) {
    commitMsg = new GitManager(this, gitServerUrl, gitServerCredentials, userInfo).getCommitMsg(reference)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.getCommitMsg step is invalid. It could be " +
      "empty or null.") as Throwable
  }

  return commitMsg
}

/**
* Gets the tags belonging to the specified reference.
* @param gitServerUrl The URL of the Git server.
* @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
*                             gitServerCredentialsId and gitServerApiTokenCredId.
* @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
*                 userEmailAddress.
* @param reference The Git reference (branch, SHA, etc.).
* @return The list of Git tags.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
List getTags(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo, final String reference)
    throws IllegalArgumentException {
  List gitTags = []

  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String && reference) {
    gitTags = new GitManager(this, gitServerUrl, gitServerCredentials, userInfo).getTags(reference)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.getTags step is invalid. It " +
      "could be empty or null.") as Throwable
  }

  return gitTags
}

/**
* Creates a Git tag.
* @param gitServerUrl The URL of the Git server.
* @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
*                             gitServerCredentialsId and gitServerApiTokenCredId.
* @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
*                 userEmailAddress.
* @param tagName The name of the Git tag to create.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
void createTag(final String gitServerUrl, final Map gitServerCredentials, final Map userInfo, final String tagName)
    throws IllegalArgumentException {
  if (gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
      && gitServerCredentials.get("gitServerCredentialsId") instanceof String
      && gitServerCredentials.get("gitServerApiTokenCredId")
      && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
      && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
      && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String && tagName) {
    new GitManager(this, gitServerUrl, gitServerCredentials, userInfo).createTag(tagName)
  } else {
    throw new IllegalArgumentException("The argument passed to the gitRunner.createTag step is invalid. It " +
      "could be empty or null.") as Throwable
  }
}

/**
* Automatically checks out the code based on the specified global variables. Requires the following globals:
* JENKINS_CHILD_WORKSPACE_CONTAINER_PATH, DEVELOPMENT_BRANCH_REGEX, TEST_BRANCH_REGEX, PRODUCTION_BRANCH_REGEX,
* GITLAB_INTERNAL_ROOT_URL, GITLAB_USER_CRED_ID, GITLAB_API_CURL_CRED_ID, CICD_ADMIN_NAME, CICD_ADMIN_EMAIL,
* GIT_UPSTREAM_INTERNAL_REPO_URL, GIT_FORKED_INTERNAL_REPO_URL, and MANUAL_JOB_INVOCATION_BRANCH.
* @throws JobDataException when missing the required global variables.
* @throws InvalidGitBranchException when the current job instance isn't valid or does not meet the defined GitOps
*                                   workflow standards.
*/
void autoCheckout() throws JobDataException, InvalidGitBranchException {
  if (GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH")
      && GlobalsManager.instance.get("DEVELOPMENT_BRANCH_REGEX") instanceof Pattern
      && GlobalsManager.instance.get("TEST_BRANCH_REGEX") instanceof Pattern
      && GlobalsManager.instance.get("PRODUCTION_BRANCH_REGEX") instanceof Pattern
      && GlobalsManager.instance.get("GITLAB_INTERNAL_ROOT_URL")
      && GlobalsManager.instance.get("GITLAB_USER_CRED_ID") && GlobalsManager.instance.get("GITLAB_API_CURL_CRED_ID")
      && GlobalsManager.instance.get("CICD_ADMIN_NAME") && GlobalsManager.instance.get("CICD_ADMIN_EMAIL")
      && GlobalsManager.instance.get("GIT_UPSTREAM_INTERNAL_REPO_URL")
      && GlobalsManager.instance.get("GIT_FORKED_INTERNAL_REPO_URL")
      && GlobalsManager.instance.get("MANUAL_JOB_INVOCATION_BRANCH") instanceof String) {
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

    // clear the workspace in preparation for any stage retries
    new FileSystemHandler(this).deleteDirContents(GlobalsManager.instance.get("JENKINS_CHILD_WORKSPACE_CONTAINER_PATH"))

    if ((env.gitlabActionType.toString() == "MERGE" || env.gitlabActionType.toString() == "NOTE")
        && (GitManager.isValidRef(env.gitlabTargetBranch.toString(), GlobalsManager.instance.get("DEVELOPMENT_BRANCH_REGEX"))
        || GitManager.isValidRef(env.gitlabTargetBranch.toString(), GlobalsManager.instance.get("TEST_BRANCH_REGEX"))
        || GitManager.isValidRef(env.gitlabTargetBranch.toString(), GlobalsManager.instance.get("PRODUCTION_BRANCH_REGEX")))) {
      echo "Checking out the upstream ${gitlabTargetBranch} branch and merging with forked source branch..."

      gitManager.checkoutMergeRequest(
        env.gitlabMergeRequestIid.toInteger(),
        env.gitlabSourceRepoName.toString(),
        GlobalsManager.instance.get("GIT_UPSTREAM_INTERNAL_REPO_URL"),
        GlobalsManager.instance.get("GIT_FORKED_INTERNAL_REPO_URL"),
        env.gitlabTargetBranch.toString()
      )
    } else if (env.gitlabActionType == "PUSH"
        && (GitManager.isValidRef(env.gitlabBranch.toString(), GlobalsManager.instance.get("DEVELOPMENT_BRANCH_REGEX"))
        || GitManager.isValidRef(env.gitlabBranch.toString(), GlobalsManager.instance.get("TEST_BRANCH_REGEX"))
        || GitManager.isValidRef(env.gitlabBranch.toString(), GlobalsManager.instance.get("PRODUCTION_BRANCH_REGEX")))) {
      echo "Checking out the upstream ${gitlabBranch} branch..."

      gitManager.checkoutBranch(GlobalsManager.instance.get("GIT_UPSTREAM_INTERNAL_REPO_URL"),
        env.gitlabBranch.toString())
    } else if (GlobalsManager.instance.get("MANUAL_JOB_INVOCATION_BRANCH")
        && (GitManager.isValidRef(GlobalsManager.instance.get("MANUAL_JOB_INVOCATION_BRANCH"),
        GlobalsManager.instance.get("DEVELOPMENT_BRANCH_REGEX"))
        || GitManager.isValidRef(GlobalsManager.instance.get("MANUAL_JOB_INVOCATION_BRANCH"),
        GlobalsManager.instance.get("TEST_BRANCH_REGEX"))
        || GitManager.isValidRef(GlobalsManager.instance.get("MANUAL_JOB_INVOCATION_BRANCH"),
        GlobalsManager.instance.get("PRODUCTION_BRANCH_REGEX")))) {
      echo "Checking out the upstream ${GlobalsManager.instance.get('MANUAL_JOB_INVOCATION_BRANCH')} branch, specified " +
        "for manual job invocation (i.e. triggered directly from Jenkins and not the Git server)..."

      gitManager.checkoutBranch(GlobalsManager.instance.get("GIT_UPSTREAM_INTERNAL_REPO_URL"),
        GlobalsManager.instance.get("MANUAL_JOB_INVOCATION_BRANCH"))
    } else {
      throw new InvalidGitBranchException("The branch which triggered the current job instance isn't valid or does not " +
        "meet the defined GitOps workflow standards. If this job instance was triggered manually from Jenkins, the " +
        "manual job invocation branch doesn't match the development branch naming standards.") as Throwable
    }
  } else {
    throw new JobDataException("Problem found in the gitRunner.autoCheckout step. One or more of the following " +
      "globals weren't defined: JENKINS_CHILD_WORKSPACE_CONTAINER_PATH, DEVELOPMENT_BRANCH_REGEX, " +
      "TEST_BRANCH_REGEX, PRODUCTION_BRANCH_REGEX, GITLAB_INTERNAL_ROOT_URL, GITLAB_USER_CRED_ID, " +
      "GITLAB_API_CURL_CRED_ID, CICD_ADMIN_NAME, CICD_ADMIN_EMAIL, GIT_UPSTREAM_INTERNAL_REPO_URL, " +
      "GIT_FORKED_INTERNAL_REPO_URL, and MANUAL_JOB_INVOCATION_BRANCH. Be sure " +
      "these are all set using the globals.set step.") as Throwable
  }
}
