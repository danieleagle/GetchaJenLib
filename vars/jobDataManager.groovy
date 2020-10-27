#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.FileSystemHandler
import com.danieleagle.GetchaJenLib.exceptions.InvalidConfigurationException

/**
* Checks if the file exists.
* @param currentJobInstWorkspacePath The workspace path where the current job instance data is stored.
* @param fileName The name of the file to check for existence.
* @return True if the file exists and false if not.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
Boolean doesFileExist(final String currentJobInstWorkspacePath, final String fileName) throws IllegalArgumentException {
  Boolean doesFileExist = false

  if (currentJobInstWorkspacePath && fileName) {
    FileSystemHandler fileSystemHandler = new FileSystemHandler(this)

    doesFileExist = (fileSystemHandler.doesDirExist(currentJobInstWorkspacePath)
      && fileSystemHandler.doesFileExist(currentJobInstWorkspacePath, fileName))
  } else {
    throw new IllegalArgumentException("The argument passed to the jobDataManager.doesFileExist step is " +
      "invalid. It could be empty or null.") as Throwable
  }

  return doesFileExist
}

/**
 * Checks if the specified directory exists.
 * @param directory The directory to check.
 * @return True if the directory exists and false if not.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
Boolean doesDirExist(final String directory) throws IllegalArgumentException {
  Boolean doesDirExist = false

  if (directory) {
    doesDirExist = new FileSystemHandler(this).doesDirExist(directory)
  } else {
    throw new IllegalArgumentException("The argument passed to the jobDataManager.doesDirExist step is invalid. " +
      "It could be empty or null.") as Throwable
  }

  return doesDirExist
}

/**
* Reads the file as a string.
* @param currentJobInstWorkspacePath The workspace path where the current job instance data is stored.
* @param fileName The name of the file to read as a string.
* @return The contents of the file as a string.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
String readFileAsString(final String currentJobInstWorkspacePath, final String fileName) throws IllegalArgumentException {
  String fileContents = ""

  if (currentJobInstWorkspacePath && fileName) {
    fileContents = new FileSystemHandler(this).readFileAsString(currentJobInstWorkspacePath, fileName)
  } else {
    throw new IllegalArgumentException("The argument passed to the jobDataManager.readFileAsString step is " +
      "invalid. It could be empty or null.") as Throwable
  }

  return fileContents
}

/**
* Creates the directories used by the current Jenkins job instance.
* @param directories The directories needed by the current job instance.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
void createDirectories(final List directories) throws IllegalArgumentException {
  if (directories) {
    FileSystemHandler fileSystemHandler = new FileSystemHandler(this)

    directories.each {
      if (fileSystemHandler.doesDirExist(it)) {
        echo "Skipped the creation of the directory ${it} since it already exists."
      } else {
        fileSystemHandler.createDir(it)
      }
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the jobDataManager.createDirectories step is " +
      "invalid. It could be empty or null.") as Throwable
  }
}

/**
* Clears the current job instance directory.
* @param currentJobInstWorkspacePath The workspace path where the current job instance data is stored.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
void clearCurrentInstDir(final String currentJobInstWorkspacePath) throws IllegalArgumentException {
  if (currentJobInstWorkspacePath) {
    FileSystemHandler fileSystemHandler = new FileSystemHandler(this)

    if (fileSystemHandler.doesDirExist(currentJobInstWorkspacePath)) {
      fileSystemHandler.deleteDirContents(currentJobInstWorkspacePath)
    } else {
      echo "Skipped clearing the current job instance directory since it doesn't exist."
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the jobDataManager.clearCurrentInstDir step is " +
      "invalid. It could be empty or null.") as Throwable
  }
}

/**
 * Validates the configuration file used by the Haskell Dockerfile Linter.
 * @param currentJobInstWorkspacePath The workspace path where the current job instance data is stored.
 * @param configFileName The name of the Hadolint config file.
 * @param trustedRegistries The list of trusted registries.
 * @param ignoredRules The list of rules to ignore.
 * @throws IllegalArgumentException when passing an empty or null argument.
 * @throws FileNotFoundException when the specified file couldn't be found
 * @throws InvalidConfigurationException when the Hadolint configuration file doesn't contain what was specified in the Jenkins
 *                                       job configuration.
 */
void validateHadolintConfigFile(final String currentJobInstWorkspacePath, final String configFileName,
                                final List trustedRegistries, final List ignoredRules = [])
    throws IllegalArgumentException, FileNotFoundException, InvalidConfigurationException {
  if (currentJobInstWorkspacePath && configFileName && trustedRegistries) {
    FileSystemHandler fileSystemHandler = new FileSystemHandler(this)

    if (fileSystemHandler.doesFileExist(currentJobInstWorkspacePath, configFileName)) {
      String hadolintConfig = fileSystemHandler.readFileAsString(currentJobInstWorkspacePath, configFileName).trim()
      StringBuilder stringBuilder = new StringBuilder()

      // put together trusted registries
      stringBuilder << "trustedRegistries:\n"

      trustedRegistries.each {
        stringBuilder << "  - " << it << "\n"
      }

      String trustedRegistriesContent = stringBuilder.toString().trim()
      stringBuilder.setLength(0)

      // put together ignored rules if they exist
      if (ignoredRules) {
        stringBuilder << "\n" << "ignored:\n"

        ignoredRules.each {
          stringBuilder << "  - " << it << "\n"
        }
      }

      String ignoredRulesContent = (ignoredRules) ? stringBuilder.toString().trim() : ""

      if (hadolintConfig.contains(trustedRegistriesContent)) {
        echo "The Hadolint configuration file ${configFileName} contains the trusted registries defined in the " +
          "current Jenkins job configuration. Please note, trusted registry entries in the current Jenkins job " +
          "configuration must be listed first under the appropriate element (e.g. trustedRegistried:). Additional " +
          "entries not listed in the current Jenkins job configuration can be added to the end of the list."
      } else {
        throw new InvalidConfigurationException("The Hadolint configuration file ${configFileName} doesn't contain " +
          "the trusted registries specified in the current Jenkins job configuration. Please note, trusted registry " +
          "entries in the current Jenkins job configuration must be listed first under the appropriate element (e.g. " +
          "trustedRegistried:). Additional entries not listed in the current Jenkins job configuration can be added " +
          "to the end of the list.") as Throwable
      }

      if (ignoredRules) {
        if (hadolintConfig.contains(ignoredRulesContent)) {
          echo "The Hadolint configuration file ${configFileName} contains the ignored rules defined in the current " +
            "Jenkins job configuration. Please note, ignored rule entries in the current Jenkins job configuration " +
            "must be listed first under the appropriate element (e.g. ignored:). Additional entries not listed in the " +
            "current Jenkins job configuration can be added to the end of the list."
        } else {
          throw new InvalidConfigurationException("The Hadolint configuration file ${configFileName} doesn't contain " +
            "the ignored rules specified in the current Jenkins job configuration. Please note, ignored rule entries " +
            "in the current Jenkins job configuration must be listed first under the appropriate element (e.g. " +
            "ignored:). Additional entries not listed in the current Jenkins job configuration can be added to the " +
            "end of the list.") as Throwable
        }
      } else {
        echo "There weren't any ignored rules for Hadolint specified in the current Jenkins job configuration."
      }
    } else {
      throw new FileNotFoundException("The Hadolint configuration file ${configFileName} could not be found. Be sure " +
        "it is created in the root of the Git repository with exactly the following contents below.\n\n${hadolintConfig}")
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the jobDataManager.validateHadolintConfigFile step is " +
      "invalid. It could be empty or null.") as Throwable
  }
}

/**
* Gets the stages to skip from the specified merge request comment.
* @param skippableStages The list of stages that are allowed to be skipped.
* @param mergeRequestComment The merge request comment that triggered the current job instance.
* @return The list of stages for which skipping has been requested.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
List getStagesToSkip(final List skippableStages, final String mergeRequestComment) throws IllegalArgumentException {
  List stagesToSkip = []

  if (skippableStages && mergeRequestComment) {
    String unfilteredStages = mergeRequestComment.substring(mergeRequestComment.indexOf("-") + 1)
    List discoveredStages = unfilteredStages.split(",")

    discoveredStages.each {
      String stageToSkip = it.trim()

      if (skippableStages.contains(stageToSkip)) {
        echo "The requested stage [${stageToSkip}] to skip is on the skippable stages list. Continuing..."
        stagesToSkip.add(stageToSkip)
      } else {
        echo "The requested stage [${stageToSkip}] to skip isn't on the skippable stages list. Ignoring..."
      }
    }

    echo "It has been requested to skip the following stages: ${stagesToSkip.toString()}"
  } else {
    throw new IllegalArgumentException("The argument passed to the jobDataManager.getStagesToSkip step is invalid. It " +
      "could be empty or null.") as Throwable
  }

  return stagesToSkip
}
