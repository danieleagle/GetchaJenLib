#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.DateTimeManager

/**
* Gets the current date and time.
* @return The current date and time.
*/
String getCurrent() {
  return DateTimeManager.getCurrentDateTime()
}
