#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.MergeReqAuthorizer

/**
* Checks if the merge request is allowed based on whether the target branch is in the merge request locked branches file.
* @param merReqLockedBranchesDirectory The directory containing the merge request locked branches file.
* @param merReqLockedBranchesFile The name of the merge request locked branches file.
* @param gitlabTargetBranch The target branch for the merge request.
* @return True if the merge request is allowed and false if not.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
Boolean isAllowed(final String merReqLockedBranchesDirectory, final String merReqLockedBranchesFile,
                  final String gitlabTargetBranch) throws IllegalArgumentException {
  Boolean isMergeReqAllowed = false

  if (merReqLockedBranchesDirectory && merReqLockedBranchesFile && gitlabTargetBranch) {
    lock("merReqLockedBranches") {
      isMergeReqAllowed =
        new MergeReqAuthorizer(this).isAllowed(merReqLockedBranchesDirectory, merReqLockedBranchesFile, gitlabTargetBranch)
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the merReqAuthorizer.isAllowed step is " +
      "invalid. It could be empty or null.") as Throwable
  }

  return isMergeReqAllowed
}

/**
* Adds the specified branches to the merge request locked branches file.
* @param merReqLockedBranchesDirectory The directory containing the merge request locked branches file.
* @param merReqLockedBranchesFile The name of the merge request locked branches file.
* @param targetBranches The branches to add to the merge request locked branches file.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
void lockTargetBranches(final String currentJobInstWorkspacePath, final String merReqLockedBranchesFile,
                        final List targetBranches) throws IllegalArgumentException {
  if (currentJobInstWorkspacePath && merReqLockedBranchesFile && targetBranches) {
    lock("merReqLockedBranches") {
      new MergeReqAuthorizer(this).lockTargetBranches(currentJobInstWorkspacePath, merReqLockedBranchesFile, targetBranches)
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the merReqAuthorizer.lockTargetBranches step is " +
      "invalid. It could be empty or null.") as Throwable
  }
}

/**
* Removes the specified branches from the merge request locked branches file.
* @param merReqLockedBranchesDirectory The directory containing the merge request locked branches file.
* @param merReqLockedBranchesFile The name of the merge request locked branches file.
* @param targetBranches The branches to remove from the merge request locked branches file.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
void unlockTargetBranches(final String currentJobInstWorkspacePath, final String merReqLockedBranchesFile,
                          final List targetBranches) throws IllegalArgumentException {
  if (currentJobInstWorkspacePath && merReqLockedBranchesFile && targetBranches) {
    lock("merReqLockedBranches") {
      new MergeReqAuthorizer(this).unlockTargetBranches(currentJobInstWorkspacePath, merReqLockedBranchesFile, targetBranches)
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the merReqAuthorizer.unlockTargetBranches step is " +
      "invalid. It could be empty or null.") as Throwable
  }
}
