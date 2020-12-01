#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components
import com.danieleagle.GetchaJenLib.exceptions.JobDataException
import com.danieleagle.GetchaJenLib.exceptions.ShellCmdFailureException
import groovy.json.JsonSlurperClassic
import java.util.regex.Pattern

/**
 * Provides common Git/GitLab functionality across the pipeline. Requires the Git and GitLab plugins in addition to the
 * Git binaries installed on the Jenkins node running the job.
 */
class GitManager implements Serializable {
  /**
   * The main URL (e.g. https://git-server.example.com) used to access the Git server.
   */
  final String gitServerUrl

  /**
   * The Git server credentials ID to allow for successful authentication across repositories.
   */
  private final String gitServerCredId

  /**
   * The Git server API token credentials ID used to access the REST API.
   */
  private final String gitServerApiTokenCredId

  /**
   * The email address of the Git user used when running certain Git commands.
   */
  private final String userEmailAddress

  /**
   * The full name of the Git user used when running certain Git commands.
   */
  private final String userFullName

  /**
  * Keeps track of the setting of Git user info.
  */
  private Boolean isUserInfoPresent

  /**
   * Steps object for using steps such as echo, sh, etc. and accessing environment variables.
   */
  private final def steps

  /**
   * Sets the steps object to allow for accessing steps such as echo, sh, etc. and accessing environment variables. It
   * also sets additional member variables.
   * @param steps The object accessible in the context of the running job instance allowing access to all available steps,
   *              environment variables, etc.
   * @param gitServerUrl The URL of the Git server.
   * @param gitServerCredentials The Git server credentials. Keys and values should be Strings. Valid keys are
   *                             gitServerCredentialsId and gitServerApiTokenCredId.
   * @param userInfo The Git user information. Keys and values should be Strings. Valid keys are userFullName and
   *                 userEmailAddress.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  GitManager(final def steps, final String gitServerUrl, final Map gitServerCredentials, final Map userInfo)
      throws IllegalArgumentException {
    if (steps && gitServerUrl && gitServerCredentials && gitServerCredentials.get("gitServerCredentialsId")
        && gitServerCredentials.get("gitServerCredentialsId") instanceof String
        && gitServerCredentials.get("gitServerApiTokenCredId")
        && gitServerCredentials.get("gitServerApiTokenCredId") instanceof String
        && userInfo && userInfo.get("userFullName") && userInfo.get("userFullName") instanceof String
        && userInfo.get("userEmailAddress") && userInfo.get("userEmailAddress") instanceof String) {
      this.steps = steps
      this.gitServerUrl = gitServerUrl
      this.gitServerCredId = gitServerCredentials.get("gitServerCredentialsId")
      this.gitServerApiTokenCredId = gitServerCredentials.get("gitServerApiTokenCredId")
      this.userFullName = userInfo.get("userFullName")
      this.userEmailAddress = userInfo.get("userEmailAddress")
      this.isUserInfoPresent = false
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager constructor is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Sets the Git user name and email address as needed by various Git commands.
   * @throws ShellCmdFailureException when the command to set the Git user name or email address fails.
   */
  void setUserInfo() throws ShellCmdFailureException {
    if (this.isUserInfoPresent) {
      steps.echo "The Git user info has already been set."
    } else {
      // run commands to set the email address and name in order for applicable Git commands to succeed
      int commandOneExitCode = steps.sh(script: "git config --local user.name  \"${this.userFullName}\"",
        returnStatus: true)
      int commandTwoExitCode = steps.sh(script: "git config --local user.email ${this.userEmailAddress}",
        returnStatus: true)

      if (commandOneExitCode > 0 && commandTwoExitCode > 0) {
        throw new ShellCmdFailureException("The command to set the Git user name or email failed.")
      } else {
        this.steps.echo "The command to set the Git user name and email succeeded."
        this.isUserInfoPresent = true
      }
    }
  }

  /**
  * Checks if the Git reference is valid based on the specified regex.
  * @param reference The Git reference to validate.
  * @param referenceRegex The Git reference regex.
  * @return True if the reference is valid and false if not.
  * @throws IllegalArgumentException when passing an empty or null argument.
  */
  static Boolean isValidRef(final String reference, final Pattern referenceRegex) throws IllegalArgumentException {
    Boolean isValidReference = false

    if (reference && referenceRegex) {
      isValidReference = (reference ==~ referenceRegex)
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager isValidRef method is invalid. It " +
        "could be empty or null.") as Throwable
    }

    return isValidReference
  }

  /**
   * Gets the Git remote server HEAD SHA.
   * @param upstreamRepoUrl The upstream repository URL.
   * @return The Git remote server HEAD SHA.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  String retrieveRemoteHeadSha(final String upstreamRepoUrl) throws IllegalArgumentException {
    String commandOutput = ""

    if (upstreamRepoUrl) {
      try {
        String httpPrefix = (upstreamRepoUrl.contains("https")) ? "https://" : "http://"
        String upstreamRepoFqdn = (upstreamRepoUrl.contains("https"))
          ? upstreamRepoUrl.replaceAll("https://", "") : upstreamRepoUrl.replaceAll("http://", "")

        // use the withCredentials step so sensitive account data is protected
        steps.withCredentials(
            [steps.usernameColonPassword(credentialsId: gitServerCredId, variable: "gitUserAndPassword")]) {
          // fail the job instance if no output is returned after 5 minutes
          steps.timeout(5) {
            setUserInfo()

            // run the Git command to list the remote SHAs and filter the output to retrieve HEAD only and return
            // only the 40 character SHA and remove the rest (e.g. remove whitespace and HEAD after the SHA)
            commandOutput = steps.sh(script: "git ls-remote ${httpPrefix}" + steps.gitUserAndPassword + "@${upstreamRepoFqdn} | grep HEAD",
              returnStdout: true).substring(0, 41).trim()
          }
        }
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager retrieveRemoteHeadSha method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return commandOutput
  }

  /**
   * Gets the Git local HEAD SHA.
   * @return The Git local server HEAD SHA.
   */
  String retrieveLocalHeadSha() {
    String localHEADsha = ""

    try {
      setUserInfo()
      localHEADsha = steps.sh(script: "git log -n 1 --pretty=format:\"%H\"", returnStdout: true).trim()
    } catch (Exception exception) {
      steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
        "details.\n\n" + exception.getMessage())
    }

    return localHEADsha
  }

  /**
   * Checks out a branch.
   * @param upstreamRepoUrl The upstream repository URL.
   * @param branchName The name of the Git branch.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  void checkoutBranch(final String upstreamRepoUrl, final String branchName) throws IllegalArgumentException {
    if (upstreamRepoUrl && branchName) {
      try {
        steps.checkout changelog: false, poll: false,
          scm: [$class: "GitSCM", branches: [[name: "*/${branchName}"]], doGenerateSubmoduleConfigurations: false,
                extensions: [[$class: "UserIdentity", email: userEmailAddress, name: userFullName]], submoduleCfg: [],
                userRemoteConfigs: [[credentialsId: gitServerCredId, url: upstreamRepoUrl]]]
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager checkoutBranch method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Checks out a tag.
   * @param upstreamRepoUrl The upstream repository URL.
   * @param tag The Git tag to checkout.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  void checkoutTag(final String upstreamRepoUrl, final String tag) throws IllegalArgumentException {
    if (upstreamRepoUrl && tag) {
      try {
        steps.checkout changelog: false, poll: false,
          scm: [$class: "GitSCM", branches: [[name: "refs/tags/${tag}"]], doGenerateSubmoduleConfigurations: false,
                extensions: [[$class: "UserIdentity", email: userEmailAddress, name: userFullName]], submoduleCfg: [],
                userRemoteConfigs: [[credentialsId: gitServerCredId, url: upstreamRepoUrl]]]
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager checkoutTag method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Checks out the merge request using its ID and merges with its target branch.
   * @param mergeRequestId The ID of the merge request.
   * @param sourceRepoName The source repository name.
   * @param upstreamRepoUrl The upstream repository URL.
   * @param forkedRepoUrl The forked repository URL.
   * @param targetBranch The target branch to merge into.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  void checkoutMergeRequest(final int mergeRequestId, final String sourceRepoName, final String upstreamRepoUrl,
                            final String forkedRepoUrl, final String targetBranch) throws IllegalArgumentException {
    if (mergeRequestId instanceof Integer && sourceRepoName && upstreamRepoUrl && forkedRepoUrl && targetBranch) {
      try {
        steps.checkout changelog: false, poll: false,
          scm: [$class: "GitSCM", branches: [[name: "merge-requests/${mergeRequestId}"]],
                doGenerateSubmoduleConfigurations: false,
                extensions: [[$class: "UserIdentity", email: userEmailAddress, name: userFullName],
                             [$class: "PreBuildMerge", options: [fastForwardMode: "FF", mergeRemote: "origin",
                                                                 mergeStrategy: "default", mergeTarget: targetBranch]]],
                submoduleCfg: [], userRemoteConfigs: [[credentialsId: gitServerCredId, name: "origin",
                                                       refspec      : "+refs/heads/*:refs/remotes/origin/* +refs/merge-" +
                                                         "requests/*/head:refs/remotes/origin/merge-requests/*",
                                                       url          : upstreamRepoUrl],
                                                      [credentialsId: gitServerCredId, name: sourceRepoName,
                                                       url          : forkedRepoUrl]]]
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager checkoutMergeRequest method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Gets the contents of a file using the GitLab REST API.
   * @param gitRootApiUrl The root API URL specific to the current repository
   *                      (e.g. https://gitlab-server.example.com/api/v4/projects/45/repository/files). In this example,
   *                      45 is the repo ID.
   * @param subPathToFile The sub path to the file on the repository (e.g. /config/hello-world/Docker). If no sub path
   *                      exists, use empty string.
   * @param fileName The name of the file to get the contents for.
   * @param reference The Git reference used to retrieve the appropriate file version (e.g. branch name).
   * @return The contents of the file as a string.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws FileNotFoundException when the specified file couldn't be found.
   */
  String getFileContents(final String gitRootApiUrl, final String subPathToFile, final String fileName,
                         final String reference) throws IllegalArgumentException, FileNotFoundException {
    String fileContents = ""

    if (gitRootApiUrl && subPathToFile instanceof String && fileName && reference) {
      // remove the leading forward slash if it exists
      String updatedSubPathToFile = (subPathToFile && subPathToFile.indexOf("/") == 0) ? subPathToFile.substring(1) : ""

      // the URI subpath and filename are percent encoded to properly work with the GitLab v4 API
      String encodedSubpathAndFileName = URLEncoder.encode("${updatedSubPathToFile}/${fileName}", "UTF-8")

      steps.withCredentials([steps.string(credentialsId: gitServerApiTokenCredId, variable: "apiToken")]) {
        try {
          fileContents = steps.sh(script: "curl --output /dev/stdout --request GET --header \"PRIVATE-TOKEN: " +
            steps.apiToken + "\" '${gitRootApiUrl}/${encodedSubpathAndFileName}/raw?ref=${reference}'",returnStdout: true).trim()
        } catch (Exception exception) {
          steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
            "details.\n\n" + exception.getMessage())
        }

        if (fileContents.contains("{\"message\":\"404 File Not Found\"}")) {
          throw new FileNotFoundException("Unable to retrieve ${fileName} as it doesn't exist.") as Throwable
        } else {
          steps.echo "Successfully retrieved ${fileContents} from the specified location using the GitLab v4 API."
        }
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager getFileContents method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return fileContents
  }

  /**
   * Gets the merge request author information using the GitLab REST API.
   * @param gitMergeReqApiUrl The merge request API URL specific to the current repository
   *                          (e.g. https://gitlab-server.example.com/api/v4/projects/45/merge_requests). In this example,
   *                          45 is the repo ID.
   * @param mergeReqId The merge request ID.
   * @return The merge request author information.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws JobDataException when the specified merge request couldn't be found.
   */
  Map getMergeReqAuthorInfo(final String gitMergeReqApiUrl, final String mergeReqId)
      throws IllegalArgumentException, JobDataException {
    Map mergeReqAuthorInfo = [:]

    if (gitMergeReqApiUrl && mergeReqId && mergeReqId.isNumber()) {
      steps.withCredentials([steps.string(credentialsId: gitServerApiTokenCredId, variable: "apiToken")]) {
        String jsonResult = ""

        try {
          jsonResult = steps.sh(script: "curl --output /dev/stdout --request GET --header \"PRIVATE-TOKEN: " +
            steps.apiToken + "\" ${gitMergeReqApiUrl}/${mergeReqId}", returnStdout: true).trim()
          Map mergeRequest = (jsonResult) ? new JsonSlurperClassic().parseText(jsonResult) : null

          if (mergeRequest.get("author") && mergeRequest.get("author").get("name") && mergeRequest.get("author").get("username")
              && mergeRequest.get("author").get("state")) {
            mergeReqAuthorInfo.put("name", mergeRequest.get("author").get("name"))
            mergeReqAuthorInfo.put("username", mergeRequest.get("author").get("username"))
            mergeReqAuthorInfo.put("state", mergeRequest.get("author").get("state"))
          }
        } catch (Exception exception) {
          steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
            "details.\n\n" + exception.getMessage())
        }

        if (jsonResult.contains("{\"message\":\"404 Project Not Found\"}")) {
          throw new JobDataException("Unable to retrieve merge request details for ID ${mergeReqId} as it " +
            "doesn't exist.") as Throwable
        } else {
          steps.echo "Successfully retrieved merge request details for ID ${mergeReqId} using the GitLab v4 API."
        }
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager getMergeReqAuthorInfo method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return mergeReqAuthorInfo
  }

  /**
   * Gets the commit messages belonging to the merge request using the GitLab REST API.
   * @param gitMergeReqApiUrl The merge request API URL specific to the current repository
   *                          (e.g. https://gitlab-server.example.com/api/v4/projects/45/merge_requests). In this example,
   *                          45 is the repo ID.
   * @param mergeReqId The merge request ID.
   * @return The merge request commit messages as a list.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws JobDataException when the specified merge request couldn't be found.
   */
  String getMergeReqCommitMsgs(final String gitMergeReqApiUrl, final String mergeReqId)
      throws IllegalArgumentException, JobDataException {
    List mergeReqCommitMsgs = []

    if (gitMergeReqApiUrl && mergeReqId && mergeReqId.isNumber()) {
      steps.withCredentials([steps.string(credentialsId: gitServerApiTokenCredId, variable: "apiToken")]) {
        String jsonResult = ""

        try {
          jsonResult = steps.sh(script: "curl --output /dev/stdout --request GET --header \"PRIVATE-TOKEN: " +
            steps.apiToken + "\" ${gitMergeReqApiUrl}/${mergeReqId}/commits", returnStdout: true).trim()
          List mergeRequestObjects = (jsonResult) ? new JsonSlurperClassic().parseText(jsonResult) : []

          mergeRequestObjects.each {
            mergeReqCommitMsgs.add(it.message)
          }
        } catch (Exception exception) {
          steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
            "details.\n\n" + exception.getMessage())
        }

        if (jsonResult.contains("{\"message\":\"404 Project Not Found\"}")) {
          throw new JobDataException("Unable to retrieve merge request commit messages for ID ${mergeReqId} as it " +
            "doesn't exist.") as Throwable
        } else {
          steps.echo "Successfully retrieved merge request commit messages for ID ${mergeReqId} using the GitLab v4 API."
        }
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager getMergeReqCommitMsgs method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return mergeReqCommitMsgs
  }

  /**
  * Gets the commit message of the specified reference.
  * @param reference The Git reference (branch, SHA, etc.).
  * @return The commit message.
  * @throws IllegalArgumentException when passing an empty or null argument.
  */
  String getCommitMsg(final String reference) throws IllegalArgumentException {
    String commitMsg = ""

    if (reference) {
      try {
        setUserInfo()
        commitMsg = steps.sh(script: "git log --format=%B -n 1 ${reference}", returnStdout: true).trim()
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager getCommitMsg method is invalid. It " +
        "could be empty or null.") as Throwable
    }

    return commitMsg
  }

  /**
  * Gets the tags belonging to the specified reference.
  * @param reference The Git reference (branch, SHA, etc.).
  * @return The list of Git tags.
  * @throws IllegalArgumentException when passing an empty or null argument.
  */
  List getTags(final String reference) throws IllegalArgumentException {
    List gitTags = []

    if (reference) {
      try {
        setUserInfo()
        String commandOutput = steps.sh(script: "git tag --points-at ${reference}", returnStdout: true)
        gitTags = (commandOutput) ? commandOutput.split("\n") : []
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager getTags method is invalid. It " +
        "could be empty or null.") as Throwable
    }

    return gitTags
  }

  /**
  * Creates a Git tag.
  * @param tagName The name of the Git tag to create.
  * @throws IllegalArgumentException when passing an empty or null argument.
  */
  void createTag(final String tagName) throws IllegalArgumentException {
    if (tagName) {
      setUserInfo()
      steps.sh "git tag \"${tagName}\""
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager createTag method is invalid. It " +
        "could be empty or null.") as Throwable
    }
  }

  /**
   * Pushes commits to the upstream repository.
   * @param upstreamRepoUrl The upstream repository URL.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  void pushCommits(final String upstreamRepoUrl) throws IllegalArgumentException {
    if (upstreamRepoUrl) {
      try {
        String httpPrefix = (upstreamRepoUrl.contains("https")) ? "https://" : "http://"
        String upstreamRepoFqdn = (upstreamRepoUrl.contains("https"))
          ? upstreamRepoUrl.replaceAll("https://", "") : upstreamRepoUrl.replaceAll("http://", "")

        steps.withCredentials([steps.usernameColonPassword(credentialsId: gitServerCredId,
            variable: "gitUserAndPassword")]) {
          setUserInfo()
          steps.sh "git push ${httpPrefix}" + steps.gitUserAndPassword + "@${upstreamRepoFqdn} HEAD"
        }
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager pushCommits method is invalid. It could be " +
        "empty or null.") as Throwable
    }
  }

  /**
   * Pushes tag to the upstream repository.
   * @param upstreamRepoUrl The upstream repository URL.
   * @param tag The Git tag to push.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  void pushTag(final String upstreamRepoUrl, final String tag) throws IllegalArgumentException {
    if (upstreamRepoUrl && tag) {
      try {
        String httpPrefix = (upstreamRepoUrl.contains("https")) ? "https://" : "http://"
        String upstreamRepoFqdn = (upstreamRepoUrl.contains("https"))
          ? upstreamRepoUrl.replaceAll("https://", "") : upstreamRepoUrl.replaceAll("http://", "")

        steps.withCredentials([steps.usernameColonPassword(credentialsId: gitServerCredId,
            variable: "gitUserAndPassword")]) {
          setUserInfo()
          steps.sh "git push ${httpPrefix}" + steps.gitUserAndPassword + "@${upstreamRepoFqdn} ${tag}"
        }
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GitManager pushTag method is invalid. It could be " +
        "empty or null.") as Throwable
    }
  }
}
