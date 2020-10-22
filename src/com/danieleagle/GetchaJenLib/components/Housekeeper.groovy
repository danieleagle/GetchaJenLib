#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components

import com.danieleagle.GetchaJenLib.exceptions.InvalidDirOrFileNameException

/**
 * Provides cleanup functionality for Jenkins jobs.
 */
class Housekeeper implements Serializable {
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
  Housekeeper(final def steps) throws IllegalArgumentException {
    if (steps) {
      this.steps = steps
    } else {
      throw new IllegalArgumentException("The argument passed to the Housekeeper constructor is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Cleans up previous job instance data and unnecessary artifacts.
   * @param parentWorkspacePath The parent workspace path where the job instance data is stored.
   * @param childWorkspacePath The child workspace path where the current job instance data is stored.
   * @param jobInstCollectionFolder The name of the folder that stores all the job instance data.
   * @param currentJobInstWorkspacePath The workspace path where the current job instance data is stored.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the specified directory contains invalid characters.
   */
  void cleanup(final String parentWorkspacePath, final String childWorkspacePath, final String jobInstCollectionFolder)
      throws IllegalArgumentException, InvalidDirOrFileNameException {
    if (parentWorkspacePath && childWorkspacePath && jobInstCollectionFolder) {
      FileSystemHandler fileSystemHandler = new FileSystemHandler(steps)

      if (fileSystemHandler.isValidDirectoryName(parentWorkspacePath)
          && fileSystemHandler.isValidDirectoryName(childWorkspacePath)
          && fileSystemHandler.isValidDirectoryName(jobInstCollectionFolder)
          && jobInstCollectionFolder.indexOf("/") == -1) {
        steps.echo "Removing data from previous job instance(s)..."

        try {
          // get list of all data directories (e.g. each data directory is named after the build number) belonging to the job
          List jobDataDirectories = fileSystemHandler.listDirectories(parentWorkspacePath + "/" + jobInstCollectionFolder)

          // loop through the job data directories and delete previous directories
          for (int i = 0; i < jobDataDirectories.size(); i++) {
            // if the directory is for the current job instance, don't delete it
            if ("${parentWorkspacePath}/${jobInstCollectionFolder}/${jobDataDirectories[i]}" == childWorkspacePath) {
              steps.echo "Skipping the deletion of the job instance data folder ${childWorkspacePath} since it is " +
                "used for the current job instance."
            }
            // otherwise, remove the directory
            else {
              fileSystemHandler.deleteDir(parentWorkspacePath + "/" + jobInstCollectionFolder + "/" + jobDataDirectories[i])
            }
          }
        } catch (Exception exception) {
          steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
            "details.\n\n" + exception.getMessage())
        }
      } else {
        throw new InvalidDirOrFileNameException("The parent or child workspace path contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the Housekeeper cleanup method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }
}
