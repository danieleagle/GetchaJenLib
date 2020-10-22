#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.exceptions

class InvalidDirOrFileNameException extends RuntimeException {
  final String message

  InvalidDirOrFileNameException(final String message) {
    this.message = message
  }
}
