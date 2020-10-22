#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components

/**
 * Provides functionality for mandating the use of Eagle Versioner (see https://github.com/GetchaDEAGLE/eagle-versioner).
 */
class EagleVersionerMandator implements Serializable {
  /**
   * The type of change.
   */
  static enum ChangeType {
    /**
     * The WIP change type.
     */
    WIP,

    /**
     * The bug fix change type.
     */
    BUG_FIX,

    /**
     * The changelog change type.
     */
    CHANGELOG,

    /**
     * The chore change type.
     */
    CHORE,

    /**
     * The dependency change type.
     */
    DEPENDENCY,

    /**
     * The documentation change type.
     */
    DOC,

    /**
     * The feature change type.
     */
    FEATURE,

    /**
     * The performance change type.
     */
    PERF,

    /**
     * The refactor change type.
     */
    REFACTOR,

    /**
     * The styling change type.
     */
    STYLING,

    /**
     * The test change type.
     */
    TEST,

    /**
     * The version change type.
     */
    VERSION_CHANGE
  }

  /**
  * Checks if the commit message is compliant (e.g. contains a supported change type created with Eagle Versioner).
  * @param commitMessage The message of the commit to check for compliance.
  * @return True if the commit is compliant and false if not.
  * @throws IllegalArgumentException when passing an empty or null argument.
  */
  Boolean isCommitCompliant(final String commitMessage) throws IllegalArgumentException {
    Boolean isCommitCompliant = false

    if (commitMessage) {
      for (ChangeType changeType : ChangeType.values()) {
        if (commitMessage.contains("[" + changeType.name() + "]")) {
          isCommitCompliant = true
          break
        }
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the EagleVersionerMandator isCommitCompliant method " +
        "is invalid. It could be empty or null.") as Throwable
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
      isVersionChangeCommit = commitMessage.contains("[" + ChangeType.VERSION_CHANGE.name() + "]")
    } else {
      throw new IllegalArgumentException("The argument passed to the EagleVersionerMandator isVersionChangeCommit method " +
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
      isWipCommit = commitMessage.contains("[" + ChangeType.WIP.name() + "]")
    } else {
      throw new IllegalArgumentException("The argument passed to the EagleVersionerMandator isWipCommit method " +
        "is invalid. It could be empty or null.") as Throwable
    }

    return isWipCommit
  }
}
