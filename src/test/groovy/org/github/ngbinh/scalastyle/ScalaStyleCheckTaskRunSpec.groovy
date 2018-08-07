package org.github.ngbinh.scalastyle

import org.gradle.testkit.runner.BuildResult

import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class ScalaStyleCheckTaskRunSpec extends ScalaStyleFunSpec {

    def "should run scalaStyleCheck"() {
        setup:
        prepareTest("simple")

        when:
        BuildResult result = executeGradle('scalaStyleCheck')

        then: "compiled successfully"
        result.task(":scalaStyleCheck").outcome == SUCCESS
        result.task(':scalaStyleMainCheck').outcome == SUCCESS
        result.task(':scalaStyleTestCheck').outcome == NO_SOURCE
    }
}
