#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.exceptions

class JobDataException extends RuntimeException {
  final String message

  JobDataException(final String message) {
    this.message = message
  }
}
