#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.EagleVersionerMandator

/**
* Checks if the commit message is compliant (e.g. contains a supported change type created with Eagle Versioner).
* @param commitMessage The message of the commit to check for compliance.
* @return True if the commit is compliant and false if not.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
Boolean isCommitCompliant(final String commitMessage) throws IllegalArgumentException {
  Boolean isCommitCompliant = false

  if (commitMessage) {
    isCommitCompliant = new EagleVersionerMandator().isCommitCompliant(commitMessage)
  } else {
    throw new IllegalArgumentException("The argument passed to the evMandator.isCommitCompliant step is invalid. It " +
      "could be empty or null.") as Throwable
  }

  return isCommitCompliant
}

/**
* Checks if the commit message is a version change commit.
* @param commitMessage The message of the commit to check for a version change commit.
* @return True if the commit is a version change commit and false if not.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
Boolean isVersionChangeCommit(final String commitMessage) throws IllegalArgumentException {
  Boolean isVersionChangeCommit = false

  if (commitMessage) {
    isVersionChangeCommit = new EagleVersionerMandator().isVersionChangeCommit(commitMessage)
  } else {
    throw new IllegalArgumentException("The argument passed to the evMandator.isVersionChangeCommit step " +
      "is invalid. It could be empty or null.") as Throwable
  }

  return isVersionChangeCommit
}

/**
* Checks if the commit message is a WIP commit.
* @param commitMessage The message of the commit to check for a WIP commit.
* @return True if the commit is a WIP commit and false if not.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
Boolean isWipCommit(final String commitMessage) throws IllegalArgumentException {
  Boolean isWipCommit = false

  if (commitMessage) {
    isWipCommit = new EagleVersionerMandator().isWipCommit(commitMessage)
  } else {
    throw new IllegalArgumentException("The argument passed to the evMandator.isWipCommit step " +
      "is invalid. It could be empty or null.") as Throwable
  }

  return isWipCommit
}
