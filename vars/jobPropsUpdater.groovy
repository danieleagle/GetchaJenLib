#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.GitManager
import java.util.regex.Pattern

/**
* Sets up the properties for the job. Overwrites anything configured in the GUI.
* @param webhookTokenCredId The webhook credentials ID containing the token sent to Jenkins. Valid tokens should trigger
*                           the job successfully.
* @param productionBranchRegex The production branch regex.
* @param noteRegexString The note regex string used to trigger Jenkins from the merge request comment.
* @param maxBuildsToKeep The maximum number of builds to keep in history.
* @param skippableStages Stages that are allowed to be skipped if authorized.
* @param gitLabConnectionProps The GitLab connection properties. Keys and values should be Strings. Valid keys are
*                              gitlabConnectionName and gitlabConnectionCredId.
* @param gitlabActionDetails The GitLab action details. Keys and values should be Strings. Valid keys are gitlabActionType
*                            and gitlabTargetBranch. This param can be empty.
* @param validBranches List of valid branches the job can work with.
* @param buildTriggerBranchesCsv The branches (comma separated values) that can trigger Jenkins.
* @param ciSkipExclBranches The map of CI Skip excluded branches. Keys and values should be Strings. Valid keys are
*                           exclCiSkipPushBranchesCsv, exclCiSkipMergeBranchesCsv, exclCiSkipNoteBranchesCsv, and
*                           exclCiSkipPipelineBranchesCsv. Entries in each value should be separated by a comma. This
*                           param can be empty.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
void invoke(final String webhookTokenCredId, final Pattern productionBranchRegex, final String noteRegexString,
            final String maxBuildsToKeep, final List skippableStages, final Map gitLabConnectionProps,
            final Map gitlabActionDetails, final List validBranches, final String buildTriggerBranchesCsv,
            final Map ciSkipExclBranches) throws IllegalArgumentException {
  if (webhookTokenCredId && productionBranchRegex && noteRegexString && maxBuildsToKeep.isNumber() && skippableStages
      && gitLabConnectionProps && gitLabConnectionProps.get("gitlabConnectionName")
      && gitLabConnectionProps.get("gitlabConnectionCredId") && validBranches && buildTriggerBranchesCsv) {
    String exclCiSkipPushBranchesCsv =
      (gitlabActionDetails && gitlabActionDetails.get("gitlabActionType")
      && (gitlabActionDetails.get("gitlabActionType") == "MERGE" || gitlabActionDetails.get("gitlabActionType") == "NOTE")
      && gitlabActionDetails.get("gitlabTargetBranch")
      && GitManager.isValidRef(gitlabActionDetails.get("gitlabTargetBranch"), productionBranchRegex)
      && ciSkipExclBranches && ciSkipExclBranches.get("exclCiSkipPushBranchesCsv"))
      ? ciSkipExclBranches.get("exclCiSkipPushBranchesCsv") : ""
    String exclCiSkipMergeBranchesCsv = (ciSkipExclBranches && ciSkipExclBranches.get("exclCiSkipMergeBranchesCsv"))
      ? ciSkipExclBranches.get("exclCiSkipMergeBranchesCsv") : ""
    String exclCiSkipNoteBranchesCsv = (ciSkipExclBranches && ciSkipExclBranches.get("exclCiSkipNoteBranchesCsv"))
      ? ciSkipExclBranches.get("exclCiSkipNoteBranchesCsv") : ""
    String exclCiSkipPipelineBranchesCsv = (ciSkipExclBranches && ciSkipExclBranches.get("exclCiSkipPipelineBranchesCsv"))
      ? ciSkipExclBranches.get("exclCiSkipPipelineBranchesCsv") : ""
    StringBuilder stringBuilder = new StringBuilder()

    for (int i = 0; i < skippableStages.size(); i++) {
      if (i == skippableStages.size() - 1) {
        stringBuilder << skippableStages[i]
      } else {
        stringBuilder << skippableStages[i] << ","
      }
    }

    String skippableStagesCsv = stringBuilder.toString()

    withCredentials([string(credentialsId: webhookTokenCredId, variable: "webhookToken")]) {
      properties(
        [
          disableConcurrentBuilds(),
          buildDiscarder(logRotator(numToKeepStr: maxBuildsToKeep)),
          parameters(
            [
              choice(choices: validBranches, description: "", name: "chosenBranch"),
              extendedChoice(description: "Stages that are to be skipped if authorized.", multiSelectDelimiter: ",",
                name: "stagesToSkip", quoteValue: false, saveJSONParameterToFile: false, type: "PT_MULTI_SELECT",
                value: skippableStagesCsv, visibleItemCount: 20),
              booleanParam(defaultValue: false, description: "Perform only the deployment. Assumes all other stages " +
                "previously ran and all deployment artifacts are staged.", name: "deployOnly")
            ]
          ),
          [
            $class: "GitLabConnectionProperty",
            gitLabConnection: gitLabConnectionProps.get("gitlabConnectionName"),
            jobCredentialId: gitLabConnectionProps.get("gitlabConnectionCredId"),
            useAlternativeCredential: true
          ],
          pipelineTriggers([
            [
              $class                        : "GitLabPushTrigger",
              triggerOnPush                 : true,
              triggerOnMergeRequest         : true,
              triggerOpenMergeRequestOnPush : "both",
              triggerOnNoteRequest          : true,
              noteRegex                     : noteRegexString,
              skipWorkInProgressMergeRequest: true,
              ciSkipOnPush                  : true,
              exclCiSkipPushBranchesCsv     : exclCiSkipPushBranchesCsv,
              ciSkipOnMerge                 : false,
              exclCiSkipMergeBranchesCsv    : exclCiSkipMergeBranchesCsv,
              ciSkipOnNote                  : false,
              exclCiSkipNoteBranchesCsv     : exclCiSkipNoteBranchesCsv,
              ciSkipOnPipeline              : true,
              exclCiSkipPipelineBranchesCsv : exclCiSkipPipelineBranchesCsv,
              setBuildDescription           : true,
              addNoteOnMergeRequest         : true,
              addCiMessage                  : true,
              addVoteOnMergeRequest         : false,
              acceptMergeRequestOnSuccess   : false,
              branchFilterType              : "NameBasedFilter",
              includeBranchesSpec           : buildTriggerBranchesCsv,
              excludeBranchesSpec           : "",
              pendingBuildName              : "",
              cancelPendingBuildsOnUpdate   : true,
              secretToken                   : webhookToken
            ]
          ])
        ]
      )
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the jobPropsUpdater.invoke step is invalid. It could be " +
      "empty or null.") as Throwable
  }
}
