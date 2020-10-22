#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components

/**
 * Provides functionality for working with Docker. Requires the Docker and Docker Pipeline plugins. Also, requires at
 * least one Docker node listening at port 2376.
 */
class DockerCommander implements Serializable {
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
  DockerCommander(final def steps) throws IllegalArgumentException {
    if (steps) {
      this.steps = steps
    } else {
      throw new IllegalArgumentException("The argument passed to the DockerCommander constructor is invalid. It " +
        "could be empty or null.") as Throwable
    }
  }

  /**
   * Gets a list of online nodes based on the specified node map.
   * @param nodeMap The map of Docker nodes to check for connectivity.
   * @return List of Docker nodes which are online.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  List getOnlineNodes(final Map nodeMap) throws IllegalArgumentException {
    List onlineNodes = []

    if (nodeMap) {
      nodeMap.each {
        int statusCode = steps.sh(script: "nc -vz ${it.value} 2376", returnStatus: true)

        // if the node is online, add it to the list, and notify user it is online
        if (statusCode == 0) {
          steps.echo "${it.key} is currently online."
          onlineNodes.add(it.key)
        } else {
          steps.echo "${it.key} is currently offline."
        }
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the DockerCommander getOnlineNodes method " +
        "is invalid. It could be empty or null.") as Throwable
    }

    return onlineNodes
  }

  /**
   * Gets a random worker Docker node that is online and available.
   * @param nodeMap The map of Docker nodes.
   * @param retries Number of times to retry getting list of online Docker nodes.
   * @param sleepBeforeRetrySecs Length of time to wait between retries of getting list of online Docker nodes.
   * @return The name of the random online Docker node.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws IOException if there aren't any Docker nodes online.
   */
  String getRandomWorkerNode(final Map nodeMap, final int retries = 3, final int sleepBeforeRetrySecs = 120)
      throws IllegalArgumentException, IOException {
    String randomWorkerNode = ""

    if (nodeMap && retries && sleepBeforeRetrySecs) {
      List availableNodes = getOnlineNodes(nodeMap)
      int retryCount = retries

      // keep retrying to get online nodes until retry count has been depleted
      while (availableNodes.size() == 0 && retryCount > 0) {
        steps.echo(String.format("There aren't any nodes online to process Jenkins jobs. Waiting %s seconds and then " +
          "retrying.", sleepBeforeRetrySecs))
        retryCount--
        steps.echo "Retries left after this attempt: ${retryCount}"
        steps.sleep(sleepBeforeRetrySecs)
        availableNodes = getOnlineNodes(nodeMap)
      }

      // if there is at least one available node, randomly choose one
      if (availableNodes.size() > 0) {
        steps.echo "${availableNodes.size()} node(s) online and available to process Jenkins jobs."
        int chosenNodeIndex = new Random().nextInt(availableNodes.size())
        randomWorkerNode = availableNodes.get(chosenNodeIndex)
        steps.echo "Using ${randomWorkerNode} to process the Jenkins job. Please wait while the container is created. " +
          "Also, please disregard any \"Jenkins doesn’t have label\" messages as Jenkins Master is waiting for " +
          "the worker container to be provisioned."
      } else {
        throw new IOException("No available Docker nodes are online to process Jenkins jobs.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the DockerCommander getRandomWorkerNode method is " +
        "invalid. It could be empty or null.") as Throwable
    }

    return randomWorkerNode
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
      try {
        // use same Docker node the job is running on for all Docker commands
        steps.docker.withServer("tcp://${nodeHostNameOrIp}:2376", nodeCredId) {
          steps.docker.withRegistry(privateRegistryUrl, privateRegistryCredId) {
            steps.docker.build("${imageName}:${imageTag}")
          }
        }
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the DockerCommander buildImage method is " +
        "invalid. It could be empty or null.") as Throwable
    }
  }

  /**
   * Pushes an image to the private Docker registry.
   * @param nodeHostNameOrIp The Docker node host name or IP used to push the image.
   * @param nodeCredId The credentials ID belonging to the Docker node.
   * @param privateRegistryUrl The private Docker registry URL.
   * @param privateRegistryCredId The credentials ID for the private Docker registry.
   * @param imageName The name of the image to push.
   * @param imageTag The tag of the image to push.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  void pushImage(final String nodeHostNameOrIp, final String nodeCredId, final String privateRegistryUrl,
                 final String privateRegistryCredId, final String imageName, final String imageTag)
      throws IllegalArgumentException {
    if (nodeHostNameOrIp && nodeCredId && privateRegistryUrl && privateRegistryCredId && imageName && imageTag) {
      try {
        def imageToPush = steps.docker.image("${imageName}:${imageTag}")

        // use same Docker node the job is running on for all Docker commands
        steps.docker.withServer("tcp://${nodeHostNameOrIp}:2376", nodeCredId) {
          steps.docker.withRegistry(privateRegistryUrl, privateRegistryCredId) {
            imageToPush.push()
          }
        }
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the DockerCommander pushImage method is " +
        "invalid. It could be empty or null.") as Throwable
    }
  }

  /**
   * Runs a container using the specified command and returns the output.
   * @param nodeHostNameOrIp The Docker node host name or IP used to run the container.
   * @param nodeCredId The credentials ID belonging to the Docker node.
   * @param privateRegistryUrl The private Docker registry URL.
   * @param privateRegistryCredId The credentials ID for the private Docker registry.
   * @param imageName The name of the image used by the container.
   * @param imageTag The tag of the image to used by the container.
   * @param containerOptions The options for running the container.
   * @param runCommand The run command invoked by the container.
   * @return The output of the command executed inside the container.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  String runContainer(final String nodeHostNameOrIp, final String nodeCredId, final String privateRegistryUrl,
                      final String privateRegistryCredId, final String imageName, final String imageTag,
                      final String containerOptions, final String runCommand) throws IllegalArgumentException {
    String commandOutput = ""

    if (nodeHostNameOrIp && nodeCredId && privateRegistryUrl && privateRegistryCredId && imageName && imageTag
      && containerOptions && runCommand) {
      try {
        // use same Docker node the job is running on for all Docker commands
        steps.docker.withServer("tcp://${nodeHostNameOrIp}:2376", nodeCredId) {
          steps.docker.withRegistry(privateRegistryUrl, privateRegistryCredId) {
            def dockerImage = steps.docker.image("${imageName}:${imageTag}")

            // ensure the latest image is used
            dockerImage.pull()

            // run the container with the specified options and then execute the desired command
            dockerImage.inside(containerOptions) {
              commandOutput = steps.sh(script: "${runCommand}", returnStdout: true).trim()
            }
          }
        }
      } catch (Exception exception) {
        steps.error("An exception was thrown which has caused this job instance to fail. Please see below for the " +
          "details.\n\n" + exception.getMessage())
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the DockerCommander runContainer method is " +
        "invalid. It could be empty or null.") as Throwable
    }

    return commandOutput
  }
}
