#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.exceptions

class InvalidGitBranchException extends RuntimeException {
  final String message

  InvalidGitBranchException(final String message) {
    this.message = message
  }
}
