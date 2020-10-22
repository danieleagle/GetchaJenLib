#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components

import com.danieleagle.GetchaJenLib.exceptions.InvalidDirOrFileNameException

/**
 * Provides authorization functionality to control merge requests.
 */
class MergeReqAuthorizer implements Serializable {
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
  MergeReqAuthorizer(final def steps) throws IllegalArgumentException {
    if (steps) {
      this.steps = steps
    } else {
      throw new IllegalArgumentException("The argument passed to the MergeReqAuthorizer constructor is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
  * Checks if the merge request is allowed based on whether the target branch is in the merge request locked branches file.
  * @param merReqLockedBranchesDirectory The directory containing the merge request locked branches file.
  * @param merReqLockedBranchesFile The name of the merge request locked branches file.
  * @param gitlabTargetBranch The target branch for the merge request.
  * @return True if the merge request is allowed and false if not.
  * @throws IllegalArgumentException when passing an empty or null argument.
  * @throws InvalidDirOrFileNameException when the specified directory or file name contains invalid characters.
  */
  Boolean isAllowed(final String merReqLockedBranchesDirectory, final String merReqLockedBranchesFile,
                    final String gitlabTargetBranch) throws IllegalArgumentException, InvalidDirOrFileNameException {
    Boolean isMergeReqAllowed = true

    if (merReqLockedBranchesDirectory && merReqLockedBranchesFile && gitlabTargetBranch) {
      FileSystemHandler fileSystemHandler = new FileSystemHandler(steps)

      if (fileSystemHandler.isValidDirectoryName(merReqLockedBranchesDirectory)
          && fileSystemHandler.isValidFileName(merReqLockedBranchesFile)) {
        try {
          String fileContents =
            fileSystemHandler.readFileAsString(merReqLockedBranchesDirectory, merReqLockedBranchesFile)
          List existingLockedTargetBranches = (fileContents) ? fileContents.split("\n") : []

          for (int i = 0; i < existingLockedTargetBranches.size(); i++) {
            if (existingLockedTargetBranches[i] == gitlabTargetBranch) {
              isMergeReqAllowed = false
              break
            }
          }
        } catch (Exception exception) {
          steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
            "details.\n\n" + exception.getMessage())
        }
      } else {
        throw new InvalidDirOrFileNameException("The merge request locked branches directory or file name contains " +
          "invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the MergeReqAuthorizer isAllowed method is " +
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
  * @throws InvalidDirOrFileNameException when the specified directory or file name contains invalid characters.
  */
  void lockTargetBranches(final String merReqLockedBranchesDirectory, final String merReqLockedBranchesFile,
                          final List targetBranches) throws IllegalArgumentException, InvalidDirOrFileNameException {
    if (merReqLockedBranchesDirectory && merReqLockedBranchesFile && targetBranches) {
      FileSystemHandler fileSystemHandler = new FileSystemHandler(steps)

      if (fileSystemHandler.isValidDirectoryName(merReqLockedBranchesDirectory)
          && fileSystemHandler.isValidFileName(merReqLockedBranchesFile)) {
        try {
          String fileContents =
            fileSystemHandler.readFileAsString(merReqLockedBranchesDirectory, merReqLockedBranchesFile)
          List existingLockedTargetBranches = (fileContents) ? fileContents.split("\n") : []

          targetBranches.each {
            if (existingLockedTargetBranches.contains(it)) {
              steps.echo "Skipped adding the target branch ${it} to the locked target branches file since it already exists."
            } else {
              existingLockedTargetBranches.add(it)
              steps.echo "Added the target branch ${it} to the locked target branches file."
            }
          }

          StringBuilder stringBuilder = new StringBuilder()
          stringBuilder << ""

          for (int i = 0; i < existingLockedTargetBranches.size(); i++) {
            if (i == existingLockedTargetBranches.size() - 1) {
              stringBuilder << existingLockedTargetBranches[i]
            } else {
              stringBuilder << existingLockedTargetBranches[i] << "\n"
            }
          }

          fileSystemHandler.createFileFromString(merReqLockedBranchesDirectory, merReqLockedBranchesFile, stringBuilder.toString())
        } catch (Exception exception) {
          steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
            "details.\n\n" + exception.getMessage())
        }
      } else {
        throw new InvalidDirOrFileNameException("The merge request locked branches directory or file name contains " +
          "invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the MergeReqAuthorizer lockTargetBranches method is " +
        "invalid. It could be empty or null.") as Throwable
    }
  }

  /**
  * Removes the specified branches from the merge request locked branches file.
  * @param merReqLockedBranchesDirectory The directory containing the merge request locked branches file.
  * @param merReqLockedBranchesFile The name of the merge request locked branches file.
  * @param targetBranches The branches to remove from the merge request locked branches file.
  * @throws IllegalArgumentException when passing an empty or null argument.
  * @throws InvalidDirOrFileNameException when the specified directory or file name contains invalid characters.
  */
  void unlockTargetBranches(final String merReqLockedBranchesDirectory, final String merReqLockedBranchesFile,
                            final List targetBranches) throws IllegalArgumentException, InvalidDirOrFileNameException {
    if (merReqLockedBranchesDirectory && merReqLockedBranchesFile && targetBranches) {
      FileSystemHandler fileSystemHandler = new FileSystemHandler(steps)

      if (fileSystemHandler.isValidDirectoryName(merReqLockedBranchesDirectory)
          && fileSystemHandler.isValidFileName(merReqLockedBranchesFile)) {
        try {
          String fileContents =
            fileSystemHandler.readFileAsString(merReqLockedBranchesDirectory, merReqLockedBranchesFile)
          List existingLockedTargetBranches = (fileContents) ? fileContents.split("\n") : []
          List updatedLockedTargetBranches = []

          existingLockedTargetBranches.each {
            if (targetBranches.contains(it)) {
              steps.echo "Removed the target branch ${it} from the locked target branches file."
            } else {
              updatedLockedTargetBranches.add(it)
            }
          }

          StringBuilder stringBuilder = new StringBuilder()
          stringBuilder << ""

          for (int i = 0; i < updatedLockedTargetBranches.size(); i++) {
            if (i == updatedLockedTargetBranches.size() - 1) {
              stringBuilder << updatedLockedTargetBranches[i]
            } else {
              stringBuilder << updatedLockedTargetBranches[i] << "\n"
            }
          }

          fileSystemHandler.createFileFromString(merReqLockedBranchesDirectory, merReqLockedBranchesFile, stringBuilder.toString())
        } catch (Exception exception) {
          steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
            "details.\n\n" + exception.getMessage())
        }
      } else {
        throw new InvalidDirOrFileNameException("The merge request locked branches directory or file name contains " +
          "invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the MergeReqAuthorizer unlockTargetBranches method is " +
        "invalid. It could be empty or null.") as Throwable
    }
  }
}
