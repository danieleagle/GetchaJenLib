#!/usr/bin/groovy

import org.jenkinsci.plugins.pipeline.modeldefinition.Utils

/**
 * When used within a stage, only executes the stage if the expression is true.
 * Otherwise, it skips the stage. This code ensures the Jenkins UI shows the stage
 * was skipped as it would normally using the declarative syntax.
 * @param expression The expression evaluated to true or false.
 * @param body The body used by Jenkins which is executed if the expression is true.
 * @throws IllegalArgumentException when passing an empty or null argument.
 */
void call(final Boolean expression, body) throws IllegalArgumentException {
  if (expression instanceof Boolean && body) {
    def config = [:]
    body.resolveStrategy = Closure.OWNER_FIRST
    body.delegate = config

    if (expression) {
      body()
    } else {
      Utils.markStageSkippedForConditional(env.STAGE_NAME)
    }
  } else {
    throw new IllegalArgumentException("The argument passed to the when step is invalid. It could be empty or " +
      "null.") as Throwable
  }
}
