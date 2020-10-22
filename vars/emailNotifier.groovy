#!/usr/bin/groovy

import com.danieleagle.GetchaJenLib.components.NotificationSystem

/**
 * Sends a success notification with job instance information to the specified recipients.
 * @param emailRecipientsCsv The email recipients list formatted as coma separated values.
 * @param replyToRecipientsCsv The email recipients list when using reply-to functionality formatted as coma separated
 *                             values.
 * @param fileAttachmentPattern The file attachment pattern for including files in the notification email.
 * @param sonarQubeProjectUrl The SonarQube project URL.
 * @param shouldLogsBeZipped The option to compress the logs.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void sendSuccess(final String emailRecipientsCsv, final String replyToRecipientsCsv,
                 final String fileAttachmentPattern = "", final String sonarQubeProjectUrl = "",
                 final Boolean shouldLogsBeZipped = false) throws IllegalArgumentException {
  if (emailRecipientsCsv && emailRecipientsCsv.split(",").size() > 0
      && replyToRecipientsCsv && replyToRecipientsCsv.split(",").size() > 0) {
    new NotificationSystem(this).send(NotificationSystem.NotificationType.SUCCESS, emailRecipientsCsv,
      replyToRecipientsCsv, fileAttachmentPattern, sonarQubeProjectUrl, shouldLogsBeZipped)
  } else {
    throw new IllegalArgumentException("The argument passed to the emailNotifier.sendSuccess step is invalid. " +
      "It could be empty or null.") as Throwable
  }
}

/**
 * Sends a failure notification with job instance information to the specified recipients.
 * @param emailRecipientsCsv The email recipients list formatted as coma separated values.
 * @param replyToRecipientsCsv The email recipients list when using reply-to functionality formatted as coma separated
 *                             values.
 * @param fileAttachmentPattern The file attachment pattern for including files in the notification email.
 * @param sonarQubeProjectUrl The SonarQube project URL.
 * @param shouldLogsBeZipped The option to compress the logs.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void sendFailure(final String emailRecipientsCsv, final String replyToRecipientsCsv,
                 final String fileAttachmentPattern = "", final String sonarQubeProjectUrl = "",
                 final Boolean shouldLogsBeZipped = false) throws IllegalArgumentException {
  if (emailRecipientsCsv && emailRecipientsCsv.split(",").size() > 0
      && replyToRecipientsCsv && replyToRecipientsCsv.split(",").size() > 0) {
    new NotificationSystem(this).send(NotificationSystem.NotificationType.FAILURE, emailRecipientsCsv,
      replyToRecipientsCsv, fileAttachmentPattern, sonarQubeProjectUrl, shouldLogsBeZipped)
  } else {
    throw new IllegalArgumentException("The argument passed to the emailNotifier.sendFailure step is invalid. " +
      "It could be empty or null.") as Throwable
  }
}
