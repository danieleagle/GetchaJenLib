#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components

import java.time.*

/**
 * Provides functionality for working with time.
 */
class DateTimeManager implements Serializable {

  /**
  * Gets the current date and time.
  * @return The current date and time.
  */
  static String getCurrentDateTime() {
    return LocalDateTime.now()
  }
}
