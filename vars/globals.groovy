#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.GlobalsManager

/**
 * Sets a global variable.
 * @param key The name of the key.
 * @param value The value to assign.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void set(final String key, final def value) throws IllegalArgumentException {
  if (key && value != null) {
    GlobalsManager.instance.set(key, value)
  } else {
    throw new IllegalArgumentException("The argument passed to the globals.set step is invalid. " +
      "It could be empty or null.") as Throwable
  }
}

/**
 * Gets a global variable.
 * @param key The name of the key used to retrieve its value.
 * @return The specified global variable.
 */
def get(final String key) throws IllegalArgumentException {
  def retrievedVar

  if (key) {
    retrievedVar = GlobalsManager.instance.get(key)
  } else {
    throw new IllegalArgumentException("The argument passed to the globals.get step is invalid. " +
      "It could be empty or null.") as Throwable
  }

  return retrievedVar
}
