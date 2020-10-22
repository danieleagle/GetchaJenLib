#!/usr/bin/groovy

package com.danieleagle.GetchaJenLib.components

/**
 * Provides general notification functionality. Requires the Email-ext Plugin.
 */
class NotificationSystem implements Serializable {
  /**
   * The type of notification.
   */
  static enum NotificationType {
    SUCCESS,
    FAILURE
  }

  /**
   * The maximum amount of characters allowed for email recipients CSV string.
   */
  static final int EMAIL_RECIPIENT_CHAR_LIMIT = 512

  /**
   * Steps object for using steps such as echo, sh, etc. and accessing environment variables.
   */
  private final def steps

  /**
   * Sets the steps object to allow for accessing steps such as echo, sh, etc. and accessing environment variables.
   * @param steps The object accessible in the context of the running job instance allowing access to all available steps,
   *              environment variables, etc.
   * @throws IllegalArgumentException when passing an empty or null argument.
   */
  NotificationSystem(final def steps) throws IllegalArgumentException {
    if (steps) {
      this.steps = steps
    } else {
      throw new IllegalArgumentException("The argument passed to the NotificationSystem constructor is invalid. " +
        "It could be empty or null.") as Throwable
    }
  }

  /**
   * Sends either a success or failure notification with job instance information to the specified recipients.
   * @param notificationType The type of notification to send.
   * @param emailRecipientsCsv The email recipients list formatted as coma separated values.
   * @param replyToRecipientsCsv The email recipients list when using reply-to functionality formatted as coma separated
   *                             values.
   * @param fileAttachmentPattern The file attachment pattern for including files in the notification email.
   * @param sonarQubeProjectUrl The SonarQube project URL.
   * @param shouldLogsBeZipped The option to compress the logs.
   * @throws IllegalArgumentException when passing an empty or null argument.
   * @throws IllegalArgumentException when the specified email recipients CSV contains too many characters.
   */
  void send(NotificationType notificationType, final String emailRecipientsCsv, final String replyToRecipientsCsv,
            final String fileAttachmentPattern = "", final String sonarQubeProjectUrl = "",
            final Boolean shouldLogsBeZipped = false) throws IllegalArgumentException {
    if (notificationType && emailRecipientsCsv && emailRecipientsCsv.split(",").size() > 0
        && replyToRecipientsCsv && replyToRecipientsCsv.split(",").size() > 0) {
      // if the string lengths are within their character limits
      if (emailRecipientsCsv.length() <= EMAIL_RECIPIENT_CHAR_LIMIT && replyToRecipientsCsv.length()
          <= EMAIL_RECIPIENT_CHAR_LIMIT) {
        String sonarQubeMessage = (sonarQubeProjectUrl) ? "<p>Finally, to see code quality details from SonarQube, " +
          "go to ${sonarQubeProjectUrl}.</p>" : ""
        String jobStatusFormatted = ""
        String jobStatus = ""

        if (NotificationType.SUCCESS == notificationType) {
          jobStatusFormatted = "<font color=green><b>SUCCESS</b></font>"
          jobStatus = "Success"
        } else if (NotificationType.FAILURE == notificationType) {
          jobStatusFormatted = "<font color=red><b>FAILURE</b></font>"
          jobStatus = "Failure"
        }

        // send email notification about the job along with the attached logs
        steps.emailext(attachLog: true, attachmentsPattern: fileAttachmentPattern, body: "<p>Jenkins has " +
          "reported a ${jobStatusFormatted} for job <b>${steps.env.JOB_NAME}</b>, <b>build ${steps.env.BUILD_NUMBER}</b>." +
          "</p><p>Please visit ${steps.env.BUILD_URL} or inspect the attached files for more details.</p>" +
          "${sonarQubeMessage}", compressLog: shouldLogsBeZipped, mimeType: "text/html", replyTo: "${replyToRecipientsCsv}",
          subject: "${jobStatus} Reported for Jenkins Job ${steps.env.JOB_NAME}, Build ${steps.env.BUILD_NUMBER}",
          to: "${emailRecipientsCsv}")
      } else {
        throw new IllegalArgumentException("The specified email recipients contains too many characters. The current " +
          "character limits are ${EMAIL_RECIPIENT_CHAR_LIMIT} for the email recipients CSV and " +
          "${EMAIL_RECIPIENT_CHAR_LIMIT} for the reply-to recipients CSV.") as Throwable
      }
    } else {
      throw new IllegalArgumentException("The argument passed to the NotificationSystem send method is invalid. It " +
        "could be empty or null.") as Throwable
    }
  }
}
