#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.agents

/**
 * Provides Veracode scanning functionality. Requires the Veracode Scan Plugin.
 */
class VeracodeAgent implements Serializable {
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
  VeracodeAgent(final def steps) throws IllegalArgumentException {
    if (steps) {
      this.steps = steps
    } else {
      throw new IllegalArgumentException("The argument passed to the OwaspAgent constructor is invalid. It could be " +
        "empty or null.") as Throwable
    }
  }

  /**
   * Scans the workspace using the Veracode scanner.
   * @param veracodeCredId The credential ID for Veracode.
   * @param scanOptions The scan options. Keys and values should be Strings. Required keys are appName, canFailJob,
   *                    criticality, debug, sandboxName, scanName, timeout, uploadIncludesPattern, and waitForScan.
   *                    Optional keys are fileNamePattern, replacementPattern, scanExcludesPattern, scanIncludesPattern,
   *                    and uploadExcludesPattern.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  void scan(final String veracodeCredId, final Map scanOptions) throws IllegalArgumentException {
    if (veracodeCredId && scanOptions && scanOptions.get("applicationName")
        && scanOptions.get("applicationName") instanceof String && scanOptions.get("canFailJob")
        && scanOptions.get("canFailJob") instanceof String
        && (scanOptions.get("canFailJob") == "true" || scanOptions.get("canFailJob") == "false")
        && scanOptions.get("criticality") && scanOptions.get("criticality") instanceof String
        && scanOptions.get("debug") && scanOptions.get("debug") instanceof String
        && (scanOptions.get("debug") == "true" || scanOptions.get("debug") == "false")
        && scanOptions.get("sandboxName") instanceof String && scanOptions.get("scanName")
        && scanOptions.get("scanName") instanceof String && scanOptions.get("teams")
        && scanOptions.get("teams") instanceof String && scanOptions.get("timeout")
        && scanOptions.get("timeout") instanceof String && scanOptions.get("timeout").isNumber()
        && scanOptions.get("uploadIncludesPattern") && scanOptions.get("uploadIncludesPattern") instanceof String
        && scanOptions.get("waitForScan") && scanOptions.get("waitForScan") instanceof String
        && (scanOptions.get("waitForScan") == "true" || scanOptions.get("waitForScan") == "false")) {
      String fileNamePattern = (scanOptions.get("fileNamePattern") && scanOptions.get("fileNamePattern") instanceof String)
        ? scanOptions.get("fileNamePattern") : ""
      String replacementPattern = (scanOptions.get("replacementPattern")
        && scanOptions.get("replacementPattern") instanceof String) ? scanOptions.get("replacementPattern") : ""
      String scanExcludesPattern = (scanOptions.get("scanExcludesPattern")
        && scanOptions.get("scanExcludesPattern") instanceof String) ? scanOptions.get("scanExcludesPattern") : ""
      String scanIncludesPattern = (scanOptions.get("scanIncludesPattern")
        && scanOptions.get("scanIncludesPattern") instanceof String) ? scanOptions.get("scanIncludesPattern") : ""
      String uploadExcludesPattern = (scanOptions.get("uploadExcludesPattern")
        && scanOptions.get("uploadExcludesPattern") instanceof String) ? scanOptions.get("uploadExcludesPattern") : ""

      steps.withCredentials([steps.usernamePassword(credentialsId: veracodeCredId,
            usernameVariable: "veracodeUserName", passwordVariable: "veracodePassword")]) {
          steps.veracode(applicationName: scanOptions.get("applicationName"),
            canFailJob: scanOptions.get("canFailJob").toBoolean(), criticality: scanOptions.get("criticality"),
            debug: scanOptions.get("debug").toBoolean(), fileNamePattern: fileNamePattern,
            replacementPattern: replacementPattern, sandboxName: scanOptions.get("sandboxName"),
            scanExcludesPattern: scanExcludesPattern, scanIncludesPattern: scanIncludesPattern,
            scanName: scanOptions.get("scanName"), teams: scanOptions.get("teams"),
            timeout: scanOptions.get("timeout").toInteger(), uploadExcludesPattern: uploadExcludesPattern,
            uploadIncludesPattern: scanOptions.get("uploadIncludesPattern"), vid: steps.veracodeUserName,
            vkey: steps.veracodePassword, waitForScan: scanOptions.get("waitForScan").toBoolean())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the VeracodeAgent scan method is invalid. It could be " +
        "empty or null.") as Throwable
    }
  }
}
