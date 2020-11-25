#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.agents.VeracodeAgent
import com.danieleagle.GetchaJenLib.exceptions.GenericToolsException

/**
 * Scans the workspace using the Veracode Policy Scanner. A policy scan is a total analysis of your compiled
 * binary/zipped code as it pertains to a security policy within the Veracode platform, matched against existing flaw
 * data and utilized for platform analytics across your application program.
 * @param veracodeCredId The credential ID for Veracode.
 * @param scanOptions The scan options. Keys and values should be Strings. Required keys are appName, canFailJob,
 *                    criticality, debug, sandboxName, scanName, timeout, uploadIncludesPattern, and waitForScan.
 *                    Optional keys are fileNamePattern, replacementPattern, scanExcludesPattern, scanIncludesPattern,
 *                    and uploadExcludesPattern.
 * @throws IllegalArgumentException when passing an empty or null argument.
 * @throws GenericToolsException when the Veracode plugin returns an error.
 */
void runPolicy(final String veracodeCredId, final Map scanOptions) throws IllegalArgumentException, GenericToolsException {
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
    new VeracodeAgent(this).runPolicyScan(veracodeCredId, scanOptions)

    // check the build log immediately after the Veracode plugin is invoked since when it fails, it won't throw an exception
    if (currentBuild.rawBuild.log.contains("Error- Returned code from wrapper:")) {
      throw new GenericToolsException("The Veracode plugin failed.") as Throwable
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the veracodeScanner.runPolicy step is invalid. It could be " +
      "empty or null.") as Throwable
  }
}

/**
* Scans the workspace using the Veracode Pipeline Scanner. A pipeline scan uses a lightweight jar file that uploads
* your compiled/zipped code to the Veracode platform where it will fully scan your application. As part of this upload
* you can tell the scan to fail based on CWEs or specific severities such as Medium flaws and above. It’s meant for
* a quick YES/NO answer to whether you should push your code through whether it be to commit to an initial branch
* or with deployment.
* @param veracodeCredId The credential ID for Veracode.
* @param scanOptions The scan options. Keys and values should be Strings. Required keys are filePath, failOnSeverity,
*                    failOnCwe, timeout, and applicationName.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
void runPipeline(final String veracodeCredId, final Map scanOptions) throws IllegalArgumentException {
  if (veracodeCredId && scanOptions && scanOptions.get("filePath") && scanOptions.get("filePath") instanceof String
      && scanOptions.get("failOnSeverity") && scanOptions.get("failOnSeverity") instanceof String
      && scanOptions.get("failOnCwe") && scanOptions.get("failOnCwe") instanceof String && scanOptions.get("timeout")
      && scanOptions.get("timeout") instanceof String && scanOptions.get("applicationName")
      && scanOptions.get("applicationName") instanceof String) {
    new VeracodeAgent(this).runPipelineScan(veracodeCredId, scanOptions)
  } else {
    throw new IllegalArgumentException("The argument passed to the veracodeScanner.runPipeline step is invalid. It " +
      "could be empty or null.") as Throwable
  }
}
