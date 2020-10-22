#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.exceptions

class InvalidConfigurationException extends RuntimeException {
  final String message

  InvalidConfigurationException(final String message) {
    this.message = message
  }
}
