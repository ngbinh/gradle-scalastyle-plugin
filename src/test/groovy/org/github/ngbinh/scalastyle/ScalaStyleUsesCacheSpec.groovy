package org.github.ngbinh.scalastyle

import org.gradle.testkit.runner.BuildResult

import static org.gradle.testkit.runner.TaskOutcome.*

class ScalaStyleUsesCacheSpec extends ScalaStyleFunSpec {

    def "should run scalaStyleCheck"() {
        setup:
        prepareTest("simple")

        when:
        BuildResult result = executeGradle('scalaStyleCheck')

        then: "compiled successfully"
        result.task(":scalaStyleCheck").outcome == SUCCESS
        result.task(':scalaStyleMainCheck').outcome == SUCCESS
        result.task(':scalaStyleTestCheck').outcome == NO_SOURCE

        when: "run again without clean"
        result = executeGradle('scalaStyleCheck')

        then: "scalaStyle should not be run"
        result.task(":scalaStyleCheck").outcome == UP_TO_DATE
        result.task(':scalaStyleMainCheck').outcome == UP_TO_DATE
        result.task(':scalaStyleTestCheck').outcome == NO_SOURCE
    }
}
