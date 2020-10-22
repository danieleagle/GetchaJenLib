#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.runners

import com.danieleagle.GetchaJenLib.components.DockerCommander

/**
 * Provides functionality to run Docker containers.
 */
class DockerContainerRunner implements Serializable {
  /**
   * The Docker node host name or IP used to run the container.
   */
  final String nodeHostNameOrIp

  /**
   * The credentials ID belonging to the Docker node.
   */
  final String nodeCredId

  /**
   * The private Docker registry URL.
   */
  final String privateRegistryUrl

  /**
   * The credentials ID for the private Docker registry.
   */
  final String privateRegistryCredId

  /**
   * The name of the Docker image.
   */
  final String imageName

  /**
   * The tag of the Docker image.
   */
  final String imageTag

  /**
   * The options for running the container.
   */
  final String containerOptions

  /**
   * Docker Coordinator object used to perform Docker operations.
   */
  final DockerCommander dockerCommander

  /**
   * Steps object for using steps such as echo, sh, etc. and accessing environment variables.
   */
  final def steps

  /**
   * Sets the steps object to allow for accessing steps such as echo, sh, etc., and accessing environment variables. It
   * also sets additional member variables.
   * @param steps The object accessible in the context of the running job instance allowing access to all available steps,
   *              environment variables, etc.
   * @param nodeHostNameOrIp The Docker node host name or IP used to run the container.
   * @param nodeCredId The credentials ID belonging to the Docker node.
   * @param privateRegistryUrl The private Docker registry URL.
   * @param privateRegistryCredId The credentials ID for the private Docker registry.
   * @param imageName The name of the Docker image.
   * @param containerOptions The options for running the container.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  DockerContainerRunner(final def steps, final String nodeHostNameOrIp, final String nodeCredId,
                        final String privateRegistryUrl, final String privateRegistryCredId,
                        final String imageName, final String imageTag, final String containerOptions)
      throws IllegalArgumentException {
    if (steps && nodeHostNameOrIp && nodeCredId && privateRegistryUrl && privateRegistryCredId && imageName && imageTag
        && containerOptions) {
      this.steps = steps
      this.dockerCommander = new DockerCommander(this.steps)
      this.nodeHostNameOrIp = nodeHostNameOrIp
      this.nodeCredId = nodeCredId
      this.privateRegistryUrl = privateRegistryUrl
      this.privateRegistryCredId = privateRegistryCredId
      this.imageName = imageName
      this.imageTag = imageTag
      this.containerOptions = containerOptions
    } else {
      throw new IllegalArgumentException("The argument passed to the DockerContainerRunner constructor is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Invokes the action.
   * @param actionCmd The action command.
   * @return The command output.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  String invokeAction(final String actionCmd) throws IllegalArgumentException {
    String commandOutput = ""

    if (actionCmd) {
      // run Docker container to perform the action
      commandOutput = dockerCommander.runContainer(nodeHostNameOrIp, nodeCredId, privateRegistryUrl, privateRegistryCredId,
        imageName, imageTag, containerOptions, actionCmd)
    } else {
      throw new IllegalArgumentException("The argument passed to the DockerContainerRunner invokeAction method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return commandOutput
  }
}
