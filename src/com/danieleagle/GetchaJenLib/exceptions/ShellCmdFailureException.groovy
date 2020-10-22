#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.exceptions

class ShellCmdFailureException extends RuntimeException {
  final String message

  ShellCmdFailureException(final String message) {
    this.message = message
  }
}
