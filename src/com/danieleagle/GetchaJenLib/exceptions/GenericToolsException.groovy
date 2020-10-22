#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.exceptions

class GenericToolsException extends RuntimeException {
  final String message

  GenericToolsException(final String message) {
    this.message = message
  }
}
