#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.DockerCommander
import com.danieleagle.GetchaJenLib.runners.DockerContainerRunner

/**
 * Gets a random worker Docker node that is online and available.
 * @param nodeMap The map of Docker nodes.
 * @param retries Number of times to retry getting list of online Docker nodes.
 * @param sleepBeforeRetrySecs Length of time to wait between retries of getting list of online Docker nodes.
 * @return The name of the random online Docker node.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
String getRandomWorkerNode(final Map nodeMap, final int retries = 3, final int sleepBeforeRetrySecs = 120)
    throws IllegalArgumentException {
  String randomWorkerNode = ""

  if (nodeMap && retries && sleepBeforeRetrySecs) {
    randomWorkerNode = new DockerCommander(this).getRandomWorkerNode(nodeMap, retries, sleepBeforeRetrySecs)
  } else {
    throw new IllegalArgumentException("The argument passed to the docker_.getRandomWorkerNode step is invalid. " +
      "It could be empty or null.") as Throwable
  }

  return randomWorkerNode
}

/**
 * Invokes the desired container to perform the specified action.
 * @param nodeHostNameOrIp The Docker node host name or IP used to run the container.
 * @param nodeCredId The credentials ID belonging to the Docker node.
 * @param privateRegistryUrl The private Docker registry URL.
 * @param privateRegistryCredId The credentials ID for the private Docker registry.
 * @param imageName The name of the Docker image.
 * @param imageTag The tag of the Docker image..
 * @param containerOptions The options for running the container.
 * @param actionCmd The action command.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void invokeAction(final String nodeHostNameOrIp, final String nodeCredId, final String privateRegistryUrl,
                  final String privateRegistryCredId, final String imageName, final String imageTag,
                  final String containerOptions, final String actionCmd) throws IllegalArgumentException {
  if (nodeHostNameOrIp && nodeCredId && privateRegistryUrl && privateRegistryCredId && imageName && imageTag
      && containerOptions && actionCmd) {
    new DockerContainerRunner(this, nodeHostNameOrIp, nodeCredId, privateRegistryUrl, privateRegistryCredId, imageName,
      imageTag, containerOptions).invokeAction(actionCmd)
  } else {
    throw new IllegalArgumentException("The argument passed to the dockerBroker.invokeAction step is invalid. " +
      "It could be empty or null.") as Throwable
  }
}

/**
* Builds the Docker image.
* @param nodeHostNameOrIp The Docker node host name or IP used to build the image.
* @param nodeCredId The credentials ID belonging to the Docker node.
* @param privateRegistryUrl The private Docker registry URL.
* @param privateRegistryCredId The credentials ID for the private Docker registry.
* @param imageName The name of the image to build.
* @param imageTag The tag of the image to build.
* @throws IllegalArgumentException when passing an empty or null argument.
*/
void buildImage(final String nodeHostNameOrIp, final String nodeCredId, final String privateRegistryUrl,
                final String privateRegistryCredId, final String imageName, final String imageTag)
    throws IllegalArgumentException {
  if (nodeHostNameOrIp && nodeCredId && privateRegistryUrl && privateRegistryCredId && imageName && imageTag) {
    new DockerCommander(this).buildImage(nodeHostNameOrIp, nodeCredId, privateRegistryUrl, privateRegistryCredId,
      imageName, imageTag)
  } else {
    throw new IllegalArgumentException("The argument passed to the dockerBroker.buildImage step is invalid. It could be " +
      "empty or null.") as Throwable
  }
}

/**
 * Pushes an image to the private Docker registry.
 * @param nodeHostNameOrIp The Docker node host name or IP used to push the image.
 * @param nodeCredId The credentials ID belonging to the Docker node.
 * @param privateRegistryUrl The private Docker registry URL.
 * @param privateRegistryCredId The credentials ID for the private Docker registry.
 * @param imageName The name and tag of the image to push.
 * @param imageTag The tag of the image.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void pushImage(final String nodeHostNameOrIp, final String nodeCredId, final String privateRegistryUrl,
               final String privateRegistryCredId, final String imageName, final String imageTag)
    throws IllegalArgumentException {
  if (nodeHostNameOrIp && nodeCredId && privateRegistryUrl && privateRegistryCredId && imageName && imageTag) {
    new DockerCommander(this).pushImage(nodeHostNameOrIp, nodeCredId, privateRegistryUrl, privateRegistryCredId,
      imageName, imageTag)
  } else {
    throw new IllegalArgumentException("The argument passed to the dockerBroker.pushImage step is invalid. It could be " +
      "empty or null.") as Throwable
  }
}
