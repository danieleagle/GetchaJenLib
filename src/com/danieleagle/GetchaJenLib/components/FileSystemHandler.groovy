#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components

import com.danieleagle.GetchaJenLib.exceptions.InvalidDirOrFileNameException

/**
 * Provides functionality for manipulating the file system.
 */
class FileSystemHandler implements Serializable {
  /**
   * Steps object for using steps such as echo, sh, etc. and accessing environment variables.
   */
  private final def steps

  /**
   * Sets the steps object to allow for accessing steps such as echo, sh, etc., and accessing environment variables.
   * @param steps The object accessible in the context of the running job instance allowing access to all available steps,
   *              environment variables, etc.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  FileSystemHandler(final def steps) throws IllegalArgumentException {
    if (steps) {
      this.steps = steps
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler constructor is invalid. It " +
        "could be empty or null.") as Throwable
    }
  }

  /**
   * Determines if the specified file name is valid (e.g. doesn't contain invalid characters).
   * @param fileName The name of the file to check.
   * @return True if the file name is valid and false if not.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  Boolean isValidFileName(final String fileName) throws IllegalArgumentException {
    String cleansedFileName = ""

    if (fileName) {
      // remove invalid characters from file name used for comparison below
      cleansedFileName = fileName.replaceAll(/[`~!@#$%^&*()|+=?;:'",<>{}\[\]\\/]/, "")
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler isValidFileName method is " +
        "invalid. It could be empty or null.") as Throwable
    }

    return fileName == cleansedFileName
  }

  /**
   * Determines if the specified directory name is valid (e.g. doesn't contain invalid characters).
   * @param directory The name of the directory to check.
   * @return True if the directory name is valid and false if not.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  Boolean isValidDirectoryName(final String directory) throws IllegalArgumentException {
    String cleansedDirectoryName = ""

    if (directory) {
      // remove invalid characters from file name used for comparison below
      cleansedDirectoryName = directory.replaceAll(/[`~!#$%^&*()|+=?;:'",<>{}\[\]\\]|\/{2,}/, "")
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler isValidDirectoryName method is " +
        "invalid. It could be empty or null.") as Throwable
    }

    return directory == cleansedDirectoryName
  }

  /**
   * Combines the directory and file name.
   * @param directory The directory to combine.
   * @param fileName The file name to combine.
   * @return The combined directory and file name.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the specified directory or file name contains invalid characters.
   */
  String combineDirAndFileName(final String directory, final String fileName)
      throws IllegalArgumentException, InvalidDirOrFileNameException {
    String dirAndFileName = ""

    if (directory && fileName) {
      if (isValidDirectoryName(directory) && isValidFileName(fileName)) {
        if (directory.charAt(directory.length() - 1).toString() == "/") {
          dirAndFileName = "${directory}${fileName}"
        } else {
          dirAndFileName = "${directory}/${fileName}"
        }
      } else {
        throw new InvalidDirOrFileNameException("The directory or filename contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler combineDirAndFileName method is " +
        "invalid. It could be empty or null.") as Throwable
    }

    return dirAndFileName
  }

  /**
   * Reads the file as a string.
   * @param directory The directory where the file should be read.
   * @param fileName The name of the file to read.
   * @param grepFor The string to grep for.
   * @return The contents of the file as a string.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the specified directory or file name contains invalid characters.
   */
  String readFileAsString(final String directory, final String fileName, final String grepFor = "")
      throws IllegalArgumentException, InvalidDirOrFileNameException {
    String fileContents = ""

    if (directory && fileName) {
      if (isValidDirectoryName(directory) && isValidFileName(fileName)) {
        String dirAndFileName = combineDirAndFileName(directory, fileName)

        if (grepFor) {
          fileContents = steps.sh(script: "cat \"${dirAndFileName}\" | grep \"${grepFor}\" || true", returnStdout: true).trim()
        } else {
          fileContents = steps.sh(script: "cat \"${dirAndFileName}\" || true", returnStdout: true).trim()
        }
      } else {
        throw new InvalidDirOrFileNameException("The directory or filename contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler readFileAsString method is " +
        "invalid. It could be empty or null.") as Throwable
    }

    return fileContents
  }

  /**
   * Creates a file using the contents of a string.
   * @param directory The directory where the file should be created.
   * @param fileName The name of the file to create.
   * @param fileContents The file contents string.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the specified directory or file name contains invalid characters.
   * @throws IOException when problem occurs while creating the file.
   */
  void createFileFromString(final String directory, final String fileName, final String fileContents)
      throws IllegalArgumentException, InvalidDirOrFileNameException, IOException {
    if (directory && fileName && (fileContents == "" || fileContents.length() > 0)) {
      if (isValidDirectoryName(directory) && isValidFileName(fileName)) {
        String dirAndFileName = combineDirAndFileName(directory, fileName)
        int commandExitCode = steps.sh(script: "echo \"${fileContents}\" > \"${dirAndFileName}\"",
          returnStatus: true)

        // if no errors when creating the file, notify the user of successful creation
        if (commandExitCode == 0) {
          steps.echo "Created/updated the file ${dirAndFileName}."
        } else {
          throw new IOException("There was a problem trying to create the file ${dirAndFileName}. Please notify a " +
            "Jenkins administrator.") as Throwable
        }
      } else {
        throw new InvalidDirOrFileNameException("The directory or filename contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler createFileFromString method is " +
        "invalid. It could be empty or null.") as Throwable
    }
  }

  /**
   * Removes the specified file.
   * @param directory The directory containing the file to delete.
   * @param fileName The name of the file to delete.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the specified directory or file name contains invalid characters.
   * @throws IOException when problem occurs while deleting the file.
   */
  void deleteFile(final String directory, final String fileName)
      throws IllegalArgumentException, InvalidDirOrFileNameException, IOException {
    if (directory && fileName) {
      if (isValidDirectoryName(directory) && isValidFileName(fileName)) {
        String dirAndFileName = combineDirAndFileName(directory, fileName)
        int commandExitCode = steps.sh(script: "rm \"${dirAndFileName}\"", returnStatus: true)

        // if no errors when deleting the file, notify the user of successful deletion
        if (commandExitCode == 0) {
          steps.echo "Removed the file ${dirAndFileName}."
        } else {
          throw new IOException("There was a problem trying to remove the file ${dirAndFileName}. Please notify a " +
            "Jenkins administrator.") as Throwable
        }
      } else {
        throw new InvalidDirOrFileNameException("The directory or filename contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler deleteFile method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Creates the specified directory.
   * @param directory The directory to delete.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the directory to create contains invalid characters.
   * @throws IOException when problem occurs while deleting the directory.
   */
  void createDir(final String directory) throws IllegalArgumentException, InvalidDirOrFileNameException, IOException {
    if (directory) {
      if (isValidDirectoryName(directory)) {
        int commandExitCode = steps.sh(script: "mkdir -p \"${directory}\"", returnStatus: true)

        // if no errors when creating the directory, notify the user of successful creation
        if (commandExitCode == 0) {
          steps.echo "Created the directory ${directory}."
        } else {
          throw new IOException("There was a problem trying to create the directory ${directory}. Please notify a Jenkins " +
            "administrator.") as Throwable
        }
      } else {
        throw new InvalidDirOrFileNameException("The directory to create contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler createDir method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Removes the specified directory.
   * @param directory The directory to delete.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the directory to delete contains invalid characters.
   * @throws IOException when problem occurs while deleting the directory.
   */
  void deleteDir(final String directory) throws IllegalArgumentException, InvalidDirOrFileNameException, IOException {
    if (directory) {
      if (isValidDirectoryName(directory)) {
        int commandExitCode = steps.sh(script: "rm -rf \"${directory}\"", returnStatus: true)

        // if no errors when deleting the file, notify the user of successful deletion
        if (commandExitCode == 0) {
          steps.echo "Removed the directory ${directory}."
        } else {
          throw new IOException("There was a problem trying to remove the directory ${directory}. Please notify a Jenkins " +
            "administrator.") as Throwable
        }
      } else {
        throw new InvalidDirOrFileNameException("The directory to delete contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler deleteDir method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Checks if the specified file exists.
   * @param directory The directory containing the file.
   * @param fileName The name of the file to check.
   * @return True if the file exists and false if not.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the specified directory or file name contain invalid characters.
   */
  Boolean doesFileExist(final String directory, final String fileName)
      throws IllegalArgumentException, InvalidDirOrFileNameException {
    Boolean doesFileExist = false

    if (directory && fileName) {
      if (isValidDirectoryName(directory) && isValidFileName(fileName)) {
        doesFileExist =
          steps.sh(script: "test -f \"${combineDirAndFileName(directory, fileName)}\" && echo \"true\" || echo \"false\"",
          returnStdout: true).trim().toBoolean()
      } else {
        throw new InvalidDirOrFileNameException("The directory or filename contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler doesFileExist method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return doesFileExist
  }

  /**
   * Checks if the specified directory exists.
   * @param directory The directory to check.
   * @return True if the directory exists and false if not.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the directory to check for existence contains invalid characters.
   */
  Boolean doesDirExist(final String directory) throws IllegalArgumentException, InvalidDirOrFileNameException {
    Boolean doesDirExist = false

    if (directory) {
      if (isValidDirectoryName(directory)) {
        doesDirExist = steps.sh(script: "test -d \"${directory}\" && echo \"true\" || echo \"false\"",
          returnStdout: true).trim().toBoolean()
      } else {
        throw new InvalidDirOrFileNameException("The directory to check for existence contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler doesDirExist method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return doesDirExist
  }

  /**
   * Copies directory contents over to another directory.
   * @param sourceDir The source directory.
   * @param destDir The destination directory.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the source or destination directory contains invalid characters.
   * @throws IOException when problem occurs while copying the directory contents.
   */
  void copyDirContents(final String sourceDir, final String destDir)
      throws IllegalArgumentException, InvalidDirOrFileNameException, IOException {
    if (sourceDir && destDir) {
      if (isValidDirectoryName(sourceDir) && isValidDirectoryName(destDir)) {
        int commandExitCode = steps.sh(script: "cp -R \"${sourceDir}\"/. \"${destDir}\"", returnStatus: true)

        // if no errors were found when copying directory contents, notify the user of successful copy
        if (commandExitCode == 0) {
          steps.echo "Copied the contents in the directory ${sourceDir} to ${destDir}."
        } else {
          throw new IOException("There was a problem trying to copy the contents of the directory ${sourceDir} to " +
            "${destDir}. Please notify a Jenkins administrator.") as Throwable
        }
      } else {
        throw new InvalidDirOrFileNameException("The source or destination directory needed for the copying of directory " +
          "contents contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler copyDirContents method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Deletes the contents of the specified directory.
   * @param directory The directory containing the contents to delete.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws InvalidDirOrFileNameException when the directory used for content deletion contains invalid characters.
   * @throws IOException when problem occurs while deleting the directory contents.
   */
  void deleteDirContents(final String directory) throws IllegalArgumentException, InvalidDirOrFileNameException, IOException {
    if (directory) {
      if (isValidDirectoryName(directory)) {
        int commandExitCode = steps.sh(script: "rm -rf \"${directory}\"/*", returnStatus: true)

        // if no errors were found in deleting the folder, notify the user of successful deletion
        if (commandExitCode == 0) {
          steps.echo "Deleted the contents in the directory ${directory}."
        } else {
          throw new IOException("There was a problem trying to delete the contents of the directory ${directory}. " +
            "Please notify a Jenkins administrator.") as Throwable
        }
      } else {
        throw new InvalidDirOrFileNameException("The directory used for content deletion contains invalid characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler deleteDirContents method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
  * Lists any sub-directories found in the specified path.
  * @param directory The path to list sub-folders from.
  * @throws IllegalArgumentException when passing an empty or null argument.
  * @throws InvalidDirOrFileNameException when the directory used for content deletion contains invalid characters.
  */
  List listDirectories(final String path) throws IllegalArgumentException, InvalidDirOrFileNameException {
    List directories = []

    if (path) {
      if (isValidDirectoryName(path)) {
        String commandOutput = steps.sh(script: "ls -1 \"${path}\"", returnStdout: true).trim()
        directories = (commandOutput) ? commandOutput.split("\n") : []
      } else {
        throw new InvalidDirOrFileNameException("The path used to get sub-directories contains invalid " +
          "characters.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the FileSystemHandler listDirectories method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return directories
  }
}
