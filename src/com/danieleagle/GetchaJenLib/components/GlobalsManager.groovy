#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components

/**
 * Provides functionality to define global variables.
 */
@Singleton
final class GlobalsManager implements Serializable {
  /**
   * The map to store the variables.
   */
  private Map variableMap = [:]

  /**
   * Sets a global variable.
   * @param key The name of the key.
   * @param value The value to assign.
   * @throws IllegalArgumentException when the key contains invalid characters.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  void set(final String key, final def value) throws IllegalArgumentException {
    if (key && value != null) {
      // if the key doesn't contain any invalid characters
      if (key && key.indexOf(" ") == -1 && (key[0].isNumber() == false)) {
        if (value instanceof GString) {
          variableMap.put(key, value.toString())
        } else {
          variableMap.put(key, value)
        }
      } else {
        throw new IllegalArgumentException("The key passed to the Globals set method is empty or contains an " +
          "invalid character. No spaces in any position or numbers as the first character are allowed.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the GlobalsManager set method is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Gets a global variable.
   * @param key The name of the key used to retrieve its value.
   * @return The specified global variable.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  def get(final String key) throws IllegalArgumentException {
    def retrievedVar

    if (key) {
      retrievedVar = variableMap.get(key)
    } else {
      throw new IllegalArgumentException("The argument passed to the GlobalsManager get method is invalid. " +
        "It could be empty or null.") as Throwable
    }

    return retrievedVar
  }
}
