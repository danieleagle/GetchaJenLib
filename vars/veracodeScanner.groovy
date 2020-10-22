#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.agents.VeracodeAgent
import com.danieleagle.GetchaJenLib.exceptions.GenericToolsException

/**
 * Invokes the Veracode scanner to scan the code.
 * @param veracodeCredId The credential ID for Veracode.
 * @param scanOptions The scan options. Keys and values should be Strings. Required keys are appName, canFailJob,
 *                    criticality, debug, sandboxName, scanName, timeout, uploadIncludesPattern, and waitForScan.
 *                    Optional keys are fileNamePattern, replacementPattern, scanExcludesPattern, scanIncludesPattern,
 *                    and uploadExcludesPattern.
 * @throws IllegalArgumentException when passing an empty or null argument.
 * @throws GenericToolsException when the Veracode plugin returns an error.
 */
void invoke(final String veracodeCredId, final Map scanOptions) throws IllegalArgumentException, GenericToolsException {
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
    new VeracodeAgent(this).scan(veracodeCredId, scanOptions)

    // check the build log immediately after the Veracode plugin is invoked since when it fails, it won't throw an exception
    if (currentBuild.rawBuild.log.contains("Error- Returned code from wrapper:")) {
      throw new GenericToolsException("The Veracode plugin failed.") as Throwable
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the veracodeScanner.invoke step is invalid. It could be " +
      "empty or null.") as Throwable
  }
}
